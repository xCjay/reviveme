package xyz.hexium.reviveme.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.hexium.reviveme.ReviveMe;

public class Revive implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("revive")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Please specify a player to revive.");
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            ReviveMe.getInstance().revivePlayer(player, true);
            sender.sendMessage(ChatColor.GREEN + "Revived " + player.getName() + ".");
            return true;
        }
        return false;
    }

}
