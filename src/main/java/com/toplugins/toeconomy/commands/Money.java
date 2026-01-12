package com.toplugins.toeconomy.commands;

import com.toplugins.toeconomy.services.EconomyService;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Money implements CommandExecutor {
    private final JavaPlugin plugin;
    private final EconomyService es;

    public Money(JavaPlugin plugin, EconomyService es) {
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

        if(args.length < 1) {
                try {
                    double saldo = es.getCachedBalance(player.getUniqueId());

                    player.sendMessage("§eEconomia§f§l>§r §aSeu saldo é de §fR$" + saldo + "§r§f.");

                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage("§eEconomia§f§l>§r §cAlgo deu errado ao ver seu saldo...");
                }

            return true;
        }

        if(args.length >= 1) {
            if(!player.hasPermission("toeconomy.admin")) {
                player.sendMessage("§eEconomia§f§l>§r §cVocê precisa ser um admin para usar essas funções!");
                return true;
            }

            // DEFINIR ACTION

            if(args[0].equalsIgnoreCase("definir")) {
                if(args.length < 3) {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê precisa preencher completamente todos os campos! §fEx: /money definir Junin 500");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);

                if(target == null) {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê precisa informar um jogador online!");
                    return true;
                }

                double amount;

                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§eEconomia§f§l>§r §cValor inválido. Use apenas números.");
                    return true;
                }

                    try {
                        es.setBalance(target.getUniqueId(), amount);

                        player.sendMessage("§eEconomia§f§l>§r §aVocê definiu com sucesso o saldo de §f" + args[1] + " §apara §fR$" + args[2] + "§r§a!");

                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage("§eEconomia§f§l>§r §cAlgo deu errado ao definir esse saldo...");
                    }
            }

            // ADICIONAR ACTION

            if(args[0].equalsIgnoreCase("adicionar")) {
                if(args.length < 3) {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê precisa preencher completamente todos os campos! §fEx: /money adicionar Junin 500");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);

                if(target == null) {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê precisa informar um jogador online!");
                    return true;
                }

                double amount;

                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§eEconomia§f§l>§r §cValor inválido. Use apenas números.");
                    return true;
                }

                    try {
                        es.addBalance(target.getUniqueId(), amount);

                        player.sendMessage("§eEconomia§f§l>§r §aVocê adicionou com sucesso o saldo de §f" + args[1] + " §apara §f" + args[2] + "§r§a!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage("§eEconomia§f§l>§r §cAlgo deu errado ao definir esse saldo...");
                    }
            }

            // DIMINUIR ACTION

            if(args[0].equalsIgnoreCase("diminuir")) {
                if(args.length < 3) {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê precisa preencher completamente todos os campos! §fEx: /money diminuir Junin 500");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);

                if(target == null) {
                    player.sendMessage("§eEconomia§f§l>§r §cVocê precisa informar um jogador online!");
                    return true;
                }

                double amount;

                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§eEconomia§f§l>§r §cValor inválido. Use apenas números.");
                    return true;
                }

                    try {
                        boolean res = es.takeBalance(target.getUniqueId(), amount);

                        if(res) {
                            player.sendMessage("§eEconomia§f§l>§r §aVocê diminuiu com sucesso §fR$" + args[2] + "§r§ao saldo de §f" + args[1] + "§r§a!");
                        } else {
                            player.sendMessage("§eEconomia§f§l>§r §cO alvo desejado não possui essa quantidade a ser diminuida...");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage("§eEconomia§f§l>§r §cAlgo deu errado ao diminuir esse saldo...");
                    }
            }
            return true;
        }
        return true;
    }
}
