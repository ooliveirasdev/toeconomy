package com.toplugins.toeconomy.listeners;

import com.toplugins.toeconomy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final Economy economy;

    public PlayerJoin(Economy economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        economy.warmup(event.getPlayer().getUniqueId());
    }
}
