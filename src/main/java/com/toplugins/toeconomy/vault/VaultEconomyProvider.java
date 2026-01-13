package com.toplugins.toeconomy.vault;

import com.toplugins.toeconomy.Economy;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VaultEconomyProvider extends AbstractEconomy {

    private final Plugin plugin;
    private final Server server;
    private final Economy economy;

    public VaultEconomyProvider(Plugin plugin, Economy economy) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.economy = economy;
    }

    @Override public boolean isEnabled() { return plugin.isEnabled(); }
    @Override public String getName() { return "ToEconomy"; }
    @Override public boolean hasBankSupport() { return false; }

    @Override public int fractionalDigits() { return 2; }
    @Override public String currencyNameSingular() { return "coin"; }
    @Override public String currencyNamePlural() { return "coins"; }

    @Override
    public String format(double amount) {
        return String.format("%.2f", amount);
    }

    @Override public boolean hasAccount(OfflinePlayer player) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer player) { return true; }

    @Override public boolean hasAccount(String playerName) { return true; }
    @Override public boolean createPlayerAccount(String playerName) { return true; }

    @Override
    public double getBalance(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        economy.warmup(uuid);
        return economy.getCachedBalance(uuid);
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(server.getOfflinePlayer(playerName));
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(server.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Negative amount");
        }

        UUID uuid = player.getUniqueId();
        economy.addBalanceBuffered(uuid, amount);
        double estimated = economy.getCachedBalance(uuid);
        return new EconomyResponse(amount, estimated, EconomyResponse.ResponseType.SUCCESS, null);

    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Negative amount");
        }

        UUID uuid = player.getUniqueId();
        try {
            boolean ok = economy.takeBalance(uuid, amount);
            if (!ok) {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
            double estimated = economy.getCachedBalance(uuid);
            return new EconomyResponse(amount, estimated, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (SQLException e) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "SQL error: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(server.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(server.getOfflinePlayer(playerName), amount);
    }

    @Override public double getBalance(OfflinePlayer player, String worldName) { return getBalance(player); }
    @Override public double getBalance(String playerName, String worldName) { return getBalance(playerName); }

    @Override public boolean has(OfflinePlayer player, String worldName, double amount) { return has(player, amount); }
    @Override public boolean has(String playerName, String worldName, double amount) { return has(playerName, amount); }

    @Override public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) { return depositPlayer(player, amount); }

    // Avisar que não tem suporte, evita quebrar o funcionamento :/

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts not supported");
    }

    @Override
    public EconomyResponse createBank(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override public EconomyResponse depositPlayer(String playerName, String worldName, double amount) { return depositPlayer(playerName, amount); }

    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) { return withdrawPlayer(player, amount); }
    @Override public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) { return withdrawPlayer(playerName, amount); }

    @Override public boolean hasAccount(OfflinePlayer player, String worldName) { return true; }
    @Override public boolean hasAccount(String playerName, String worldName) { return true; }

    @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return true; }
    @Override public boolean createPlayerAccount(String playerName, String worldName) { return true; }
}
