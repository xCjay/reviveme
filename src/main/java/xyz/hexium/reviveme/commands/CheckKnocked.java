package xyz.hexium.reviveme.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.hexium.reviveme.ReviveMe;

public class CheckKnocked implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // If no arguments are given, show usage
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify a player to check the knock on.");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        // Send user message that reports the argumented user's knocked status
        if (cmd.getName().equalsIgnoreCase("checkknocked")) {
            sender.sendMessage(args[0] + " is " + (ReviveMe.getInstance().hasKnockedTag(player) ? "knocked" : "not knocked"));
            return true;
        }
        return false;
    }

}
