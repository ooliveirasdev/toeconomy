package com.toplugins.toeconomy.databases;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SQLiteDb {
    private final JavaPlugin plugin;
    private Connection connection;

    private Map<String, Double> cache = new ConcurrentHashMap<>();

    public SQLiteDb(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado no classpath (org.sqlite.JDBC).", e);
        }

        String type = plugin.getConfig().getString(
                "database.types",
                "sqlite"
        );

        if(!type.equalsIgnoreCase("sqlite")) {
            throw new SQLException("Tipo de banco não suportado: " + type);
        }

        File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) dataFolder.mkdirs();

        File databaseFolder = new File(dataFolder, "database");
        if (!databaseFolder.exists()) databaseFolder.mkdirs();

        String fileName = plugin.getConfig().getString(
                "database.sqlite.file",
                "economy.db"
        );

        File dbFile = new File(databaseFolder, fileName);
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        connection = DriverManager.getConnection(url);
    }

    public void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS balances (" +
                "uuid TEXT PRIMARY KEY," +
                "balance REAL NOT NULL DEFAULT 0" +
                ")";

        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    public double getBalance(String uuid) throws SQLException {
        String sql = "SELECT balance FROM balances WHERE uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("balance") : 0.0;
            }
        }
    }

    public void setBalance(String uuid, double value) throws SQLException {
        String updateSql = "UPDATE balances SET balance = ? WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setDouble(1, value);
            ps.setString(2, uuid);

            int rows = ps.executeUpdate();
            if (rows > 0) return; // existia, atualizou, acabou
        }

        String insertSql = "INSERT INTO balances(uuid, balance) VALUES(?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, uuid);
            ps.setDouble(2, value);
            ps.executeUpdate();
        }
    }


    public void addBalance(String uuid, double delta) throws SQLException {
        String sql = "UPDATE balances " +
                "SET balance = balance + ? " +
                "WHERE uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setString(2, uuid);
            ps.executeUpdate();
        }
    }

    public boolean takeBalance(String uuid, double delta) throws SQLException {
        String sql = "UPDATE balances " +
                "SET balance = balance - ? " +
                "WHERE uuid = ? AND balance >= ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setString(2, uuid);
            ps.setDouble(3, delta);
            return ps.executeUpdate() > 0;
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}
