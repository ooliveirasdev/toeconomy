package com.toplugins.toeconomy;

import com.toplugins.toeconomy.commands.Money;
import com.toplugins.toeconomy.commands.Pay;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private SQLiteDb db;
    private DatabaseWorker dw;
    private EconomyService es;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        db = new SQLiteDb(this);
        dw = new DatabaseWorker();
        es = new EconomyService(db, dw);
        try {
            db.open();           // abre (ou cria) economy.db
            db.createTables();   // cria a tabela se não existir
        } catch (Exception e) {
            getLogger().severe("Falha ao iniciar SQLite: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Exemplo: registrar comando /money
        getCommand("money").setExecutor(new Money(this, es));
        getCommand("pay").setExecutor(new Pay(this, es));

        getLogger().info("SQLite pronto!");
    }

    @Override
    public void onDisable() {
        if (dw != null) dw.shutdown();
        if (db != null) db.close();
    }

    public EconomyService getES() {
        return es;
    }
}
