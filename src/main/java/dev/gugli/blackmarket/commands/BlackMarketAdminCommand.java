package dev.gugli.blackmarket.commands;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.gui.AdminGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BlackMarketAdminCommand implements CommandExecutor, TabCompleter {

    private final BlackMarket plugin;
    private final AdminGUI adminGUI;

    public BlackMarketAdminCommand(BlackMarket plugin) {
        this.plugin = plugin;
        this.adminGUI = new AdminGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("blackmarket.admin")) {
            player.sendMessage(plugin.getMarketManager().colorize(
                    plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.no-permission", "")));
            return true;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "open" -> {
                    plugin.getSchedulerManager().forceOpen();
                    player.sendMessage(plugin.getMarketManager().colorize("&aMarket opened!"));
                    return true;
                }
                case "close" -> {
                    plugin.getSchedulerManager().forceClose();
                    player.sendMessage(plugin.getMarketManager().colorize("&cMarket closed!"));
                    return true;
                }
                case "reload" -> {
                    plugin.reloadConfig();
                    plugin.getMarketManager().reload();
                    player.sendMessage(plugin.getMarketManager().colorize("&aBlackMarket reloaded!"));
                    return true;
                }
            }
        }

        adminGUI.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("open", "close", "reload");
        return Collections.emptyList();
    }
}
