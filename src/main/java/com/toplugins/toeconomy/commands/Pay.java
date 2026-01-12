package com.toplugins.toeconomy.commands;

import com.toplugins.toeconomy.DatabaseWorker;
import com.toplugins.toeconomy.EconomyService;
import com.toplugins.toeconomy.SQLiteDb;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Pay implements CommandExecutor {
    private final JavaPlugin plugin;
    private final EconomyService es;

    public Pay(JavaPlugin plugin, EconomyService es) {
        this.plugin = plugin;
        this.es = es;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if(args.length < 2) {
            player.sendMessage("§eEconomia§f§l>§r §cVocê deve preencher completamente os campos. §fEx: /pay Junin 250");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if(target == null) {
            player.sendMessage("§eEconomia§f§l>§r §cVocê deve informar um jogador online para fazer a transação...");
            return true;
        }

        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§eEconomia§f§l>§r §cValor inválido. Use apenas números.");
            return true;
        }

            try {
                boolean res = es.takeBalance(player.getUniqueId(), amount);
                if(res) {
                    es.addBalance(target.getUniqueId(), amount);
                    player.sendMessage("§eEconomia§f§l>§r §aTransferencia efetuada com sucesso para §f" + args[0] + " §r§ano valor de §fR$" + args[1]);
                    target.sendMessage("§eEconomia§f§l>§r §aVocê acaba de receber uma transferencia no valor de §fR$" + args[1] + " §r§ade §f" + player.getName() + "§r§a!");
                    target.playSound(target.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                } else {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê não tem o saldo necessario para fazer essa transação...");
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage("§eEconomia§f§l>§r §cAlgo deu errado ao fazer esse pagamento...");
            }

        return true;
    }
}
