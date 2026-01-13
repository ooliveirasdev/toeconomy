package com.toplugins.toeconomy.papi;

import com.toplugins.toeconomy.Economy;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Placeholders extends PlaceholderExpansion {
    private final JavaPlugin plugin;
    private final Economy es;

    public Placeholders(JavaPlugin plugin, Economy es) {
        this.plugin = plugin;
        this.es = es;
    }

    @Override public String getIdentifier() { return "toeconomy"; }
    @Override public String getAuthor() { return String.join(", ", plugin.getDescription().getAuthors()); }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("balance")) {
            return String.valueOf(es.getBalance(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("formatted_balance")) {
            String balance = es.getFormattedCachedBalance(player.getUniqueId());
            return balance;
        }

        return null;
    }
}
