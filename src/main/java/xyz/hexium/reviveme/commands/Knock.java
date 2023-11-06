package xyz.hexium.reviveme.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.hexium.reviveme.ReviveMe;

public class Knock implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("knock")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Please specify a player to knock.");
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            ReviveMe.getInstance().knockPlayer(player, player.getLocation());
            sender.sendMessage(ChatColor.GREEN + "Knocked " + player.getName() + ".");
            return true;
        }
        return false;
    }

}
