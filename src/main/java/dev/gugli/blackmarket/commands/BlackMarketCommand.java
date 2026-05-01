package dev.gugli.blackmarket.commands;

import dev.gugli.blackmarket.BlackMarket;
import dev.gugli.blackmarket.gui.MarketGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BlackMarketCommand implements CommandExecutor, TabCompleter {

    private final BlackMarket plugin;
    private final MarketGUI marketGUI;

    public BlackMarketCommand(BlackMarket plugin) {
        this.plugin = plugin;
        this.marketGUI = new MarketGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("blackmarket.use")) {
            player.sendMessage(plugin.getMarketManager().colorize(
                    plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.no-permission", "")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("setlocation")) {
            if (!player.hasPermission("blackmarket.admin")) {
                player.sendMessage(plugin.getMarketManager().colorize(
                        plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.no-permission", "")));
                return true;
            }
            plugin.getMarketManager().setLocation(player.getLocation());
            player.sendMessage(plugin.getMarketManager().colorize(
                    plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.location-set", "")));
            return true;
        }

        if (!plugin.getMarketManager().isOpen()) {
            player.sendMessage(plugin.getMarketManager().colorize(
                    plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.market-closed", "")));
            return true;
        }

        marketGUI.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("blackmarket.admin")) {
            return List.of("setlocation");
        }
        return Collections.emptyList();
    }
}
