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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        //Send user message that reports the argumented users knocked status
        if (cmd.getName().equalsIgnoreCase("checkknocked")) {
            if (args.length == 0) {
                sender.sendMessage("Please specify a player to check.");
                return true;
            }
            sender.sendMessage(args[0] + " is " + (ReviveMe.getInstance().hasKnockedTag(player) ? "knocked" : "not knocked"));
            return true;
        }
        return false;
    }
}
