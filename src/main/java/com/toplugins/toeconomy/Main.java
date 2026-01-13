package com.toplugins.toeconomy;

import com.toplugins.toeconomy.commands.Money;
import com.toplugins.toeconomy.commands.Pay;
import com.toplugins.toeconomy.databases.DatabaseWorker;
import com.toplugins.toeconomy.databases.Database;
import com.toplugins.toeconomy.papi.Placeholders;
import com.toplugins.toeconomy.listeners.PlayerJoin;
import com.toplugins.toeconomy.vault.VaultEconomyProvider;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;

public final class Main extends JavaPlugin {

    private Database db;
    private DatabaseWorker dw;
    private Economy es;
    private VaultEconomyProvider vaultEconomyProvider;

    private boolean papi;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        //////////////////////////////////////////////////////////////

        db = new Database(this);
        dw = new DatabaseWorker();
        es = new Economy(db, dw);

        try {
            db.open();
            db.createTables();
        } catch (Exception e) {
            getLogger().severe("Falha ao iniciar o database: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("money").setExecutor(new Money(this, es));
        getCommand("pay").setExecutor(new Pay(this, es));

        getLogger().info("Database carregado e configurado!");

        //////////////////////////////////////////////////////////////

        registerVault();

        getServer().getServicesManager().register(
                Economy.class,
                es,
                this,
                ServicePriority.Normal
        );

        //////////////////////////////////////////////////////////////
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this,
                es::flushPending,
                20L,
                20L
        );

        getServer().getPluginManager().registerEvents(
                new PlayerJoin(es),
                this
        );
        //////////////////////////////////////////////////////////////

        papi = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        if (papi) {

            new Placeholders(this, es).register();

            getLogger().info("PlaceholderAPI detectado, hook ativo.");
        } else {
            getLogger().info("PlaceholderAPI NÃO detectado, rodando sem placeholders externos.");
        }

        getLogger().info("Sistemas carregados com sucesso! Agradecemos por usar nossos plugins <3");
    }

    public Economy getEconomy() {
        return es;
    }

    @Override
    public void onDisable() {
        es.flushPending();

        getServer().getServicesManager().unregister(Economy.class, es);
        
        if (dw != null) dw.shutdown();
        if (db != null) db.close();
        if (vaultEconomyProvider != null) {
            getServer().getServicesManager().unregister(net.milkbowl.vault.economy.Economy.class, vaultEconomyProvider);
        }
    }

    private void registerVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault não encontrado. Não registrando Economy provider.");
            return;
        }

        vaultEconomyProvider = new VaultEconomyProvider(this, es);

        getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                vaultEconomyProvider,
                this,
                ServicePriority.Highest
        );

        getLogger().info("ToEconomy registrado no Vault como provedor de economia!");
    }
}
