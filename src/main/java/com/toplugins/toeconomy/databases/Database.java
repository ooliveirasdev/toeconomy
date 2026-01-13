package com.toplugins.toeconomy.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

public final class Database {
    private final JavaPlugin plugin;

    // SQL Comum kkkj
    private Connection sqliteConnection;

    // MySQL Usa pool do hiraki
    private HikariDataSource mysqlDataSource;

    public Database(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open() throws SQLException {
        String type = plugin.getConfig().getString("database.types", "sqlite");

        if (type.equalsIgnoreCase("sqlite")) {
            openSqlite();
            return;
        }

        if (type.equalsIgnoreCase("mysql")) {
            openMysqlHikari();
            return;
        }

        throw new SQLException("Tipo de banco não suportado: " + type);
    }

    private void openSqlite() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado no classpath (org.sqlite.JDBC).", e.getMessage());
        }

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File databaseFolder = new File(dataFolder, "database");
        if (!databaseFolder.exists()) databaseFolder.mkdirs();

        String fileName = plugin.getConfig().getString("database.sqlite.file", "economy.db");
        File dbFile = new File(databaseFolder, fileName);

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        sqliteConnection = DriverManager.getConnection(url);
    }

    private void openMysqlHikari() throws SQLException {

        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "minecraft");
        String user = plugin.getConfig().getString("database.mysql.user", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "");
        boolean useSSL = plugin.getConfig().getBoolean("database.mysql.useSSL", false);

        String jdbcUrl =
                "jdbc:mysql://" + host + ":" + port + "/" + database +
                        "?useSSL=" + useSSL +
                        "&characterEncoding=utf8" +
                        "&useUnicode=true" +
                        "&serverTimezone=UTC" +
                        "&allowPublicKeyRetrieval=true";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);

        // Config para us pool
        config.setMaximumPoolSize(plugin.getConfig().getInt("database.mysql.pool.maxPoolSize", 10));
        config.setMinimumIdle(plugin.getConfig().getInt("database.mysql.pool.minIdle", 2));
        config.setConnectionTimeout(plugin.getConfig().getLong("database.mysql.pool.connectionTimeoutMs", 10000));
        config.setIdleTimeout(plugin.getConfig().getLong("database.mysql.pool.idleTimeoutMs", 600000));
        config.setMaxLifetime(plugin.getConfig().getLong("database.mysql.pool.maxLifetimeMs", 1800000));

        // Ajuda em ambientes com classloader de plugin :/
        config.setPoolName("ToEconomy-MySQL");
        config.setInitializationFailTimeout(-1); // não trava startup se o MySQL estiver off

        mysqlDataSource = new HikariDataSource(config);


        try (Connection c = mysqlDataSource.getConnection()) {
            // Faz nada não doidu kkkkkkkkkkkkkk é só teste
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro MySQL/Hikari (SQLException): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Connection getConnection() throws SQLException {
        if (mysqlDataSource != null) {
            return mysqlDataSource.getConnection();
        }
        if (sqliteConnection != null) {
            return sqliteConnection;
        }
        throw new SQLException("Database não foi aberta... não foi chamado ou falhou).");
    }

    public void createTables() throws SQLException {
        String type = plugin.getConfig().getString("database.types", "sqlite");

        // UUID como VARCHAR em mysql, TEXT em sqlite, poderia ser igual mas fzr oq :/
        String sql;
        if (type.equalsIgnoreCase("mysql")) {
            sql = "CREATE TABLE IF NOT EXISTS balances (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "balance DOUBLE NOT NULL DEFAULT 0" +
                    ")";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS balances (" +
                    "uuid TEXT PRIMARY KEY," +
                    "balance REAL NOT NULL DEFAULT 0" +
                    ")";
        }

        try (Connection c = getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    public double getBalance(String uuid) throws SQLException {
        String sql = "SELECT balance FROM balances WHERE uuid = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("balance") : 0.0;
            }
        }
    }

    public void setBalance(String uuid, double value) throws SQLException {
        String type = plugin.getConfig().getString("database.types", "sqlite");

        if (type.equalsIgnoreCase("mysql")) {
            String sql = "INSERT INTO balances (uuid, balance) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE balance = VALUES(balance)";

            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, uuid);
                ps.setDouble(2, value);
                ps.executeUpdate();
            }
            return;
        }

        String updateSql = "UPDATE balances SET balance = ? WHERE uuid = ?";
        try (PreparedStatement ps = sqliteConnection.prepareStatement(updateSql)) {
            ps.setDouble(1, value);
            ps.setString(2, uuid);

            int rows = ps.executeUpdate();
            if (rows > 0) return;
        }

        String insertSql = "INSERT INTO balances(uuid, balance) VALUES(?, ?)";
        try (PreparedStatement ps = sqliteConnection.prepareStatement(insertSql)) {
            ps.setString(1, uuid);
            ps.setDouble(2, value);
            ps.executeUpdate();
        }
    }

    public void addBalance(String uuid, double delta) throws SQLException {
        String sql = "UPDATE balances SET balance = balance + ? WHERE uuid = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setString(2, uuid);
            ps.executeUpdate();
        }
    }

    public boolean takeBalance(String uuid, double delta) throws SQLException {
        String sql = "UPDATE balances " +
                "SET balance = balance - ? " +
                "WHERE uuid = ? AND balance >= ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setString(2, uuid);
            ps.setDouble(3, delta);
            return ps.executeUpdate() > 0;
        }
    }

    public void close() {
        try {
            if (sqliteConnection != null) sqliteConnection.close();
        } catch (SQLException ignored) {}

        if (mysqlDataSource != null) {
            mysqlDataSource.close();
        }
    }
}
