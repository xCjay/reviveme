package xyz.hexium.reviveme.commands;

import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import xyz.hexium.reviveme.ReviveMe;

import java.util.ArrayList;
import java.util.List;

public class Configs implements CommandExecutor, TabCompleter {
    private final ReviveMe plugin;

    public Configs(ReviveMe plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("banOnDeath");
            options.add("reviveTime");
            options.add("reviveTimeLimit");
            options.add("reviveRadius");
            return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
        } else if (args.length == 2) {
            // For the second argument, if the first argument is one of the options, provide "get" and "set" as choices
            if (args[0].equalsIgnoreCase("banOnDeath") || args[0].equalsIgnoreCase("reviveTime")
                    || args[0].equalsIgnoreCase("reviveTimeLimit") || args[0].equalsIgnoreCase("reviveRadius")) {
                List<String> actions = new ArrayList<>();
                actions.add("get");
                actions.add("set");
                return StringUtil.copyPartialMatches(args[1], actions, new ArrayList<>());
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /rmconfig <setting> <action> [value]");
            return false;
        }

        String setting = args[0];
        String action = args[1];

        if (setting.equalsIgnoreCase("banOnDeath")) {
            if (action.equalsIgnoreCase("set") && args.length > 2) {
                // Set the value
                boolean value = Boolean.parseBoolean(args[2]);
                // Save the value to your config or data storage
                plugin.getConfig().set("banOnDeath", value);
                plugin.saveConfig();
                sender.sendMessage("banOnDeath set to " + value);
            } else if (action.equalsIgnoreCase("get")) {
                // Get the value
                boolean value = plugin.getConfig().getBoolean("banOnDeath");
                sender.sendMessage("banOnDeath is set to " + value);
            } else {
                sender.sendMessage("Usage: /rmconfig banOnDeath <set/get> [value]");
            }
        } else if (setting.equalsIgnoreCase("reviveTime")) {
            if (action.equalsIgnoreCase("set") && args.length > 2) {
                // Set the value
                int value = Integer.parseInt(args[2]);
                // Save the value to your config or data storage
                plugin.getConfig().set("reviveTime", value);
                plugin.saveConfig();
                sender.sendMessage("reviveTime set to " + value);
            } else if (action.equalsIgnoreCase("get")) {
                // Get the value
                int value = plugin.getConfig().getInt("reviveTime");
                sender.sendMessage("reviveTime is set to " + value);
            } else {
                sender.sendMessage("Usage: /rmconfig reviveTime <set/get> [value]");
            }
        } else if (setting.equalsIgnoreCase("reviveTimeLimit")) {
            if (action.equalsIgnoreCase("set") && args.length > 2) {
                // Set the value
                long value = Long.parseLong(args[2]);
                // Save the value to your config or data storage
                plugin.getConfig().set("reviveTimeLimit", value);
                plugin.saveConfig();
                sender.sendMessage("reviveTimeLimit set to " + value);
            } else if (action.equalsIgnoreCase("get")) {
                // Get the value
                long value = plugin.getConfig().getLong("reviveTimeLimit");
                sender.sendMessage("reviveTimeLimit is set to " + value);
            } else {
                sender.sendMessage("Usage: /rmconfig reviveTimeLimit <set/get> [value]");
            }
            plugin.reloadConfigs();
        }

        return false;
    }
}
