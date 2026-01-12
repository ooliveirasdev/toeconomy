package com.toplugins.toeconomy;

import com.toplugins.toeconomy.commands.Money;
import com.toplugins.toeconomy.commands.Pay;
import com.toplugins.toeconomy.databases.DatabaseWorker;
import com.toplugins.toeconomy.databases.SQLiteDb;
import com.toplugins.toeconomy.services.EconomyService;
import org.bukkit.plugin.java.JavaPlugin;

import com.toplugins.toeconomy.vault.VaultEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;

public final class Main extends JavaPlugin {

    private SQLiteDb db;
    private DatabaseWorker dw;
    private EconomyService es;
    private Economy vaultEconomyProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        db = new SQLiteDb(this);
        dw = new DatabaseWorker();
        es = new EconomyService(db, dw);

        registerVault();

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
        if (vaultEconomyProvider != null) {
            getServer().getServicesManager().unregister(Economy.class, vaultEconomyProvider);
        }
    }

    public EconomyService getES() {
        return es;
    }

    private void registerVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault não encontrado. Não registrando Economy provider.");
            return;
        }

        vaultEconomyProvider = new VaultEconomyProvider(this, es);

        getServer().getServicesManager().register(
                Economy.class,
                vaultEconomyProvider,
                this,
                ServicePriority.Normal
        );

        getLogger().info("ToEconomy registrado no Vault como Economy provider!");
    }

}
