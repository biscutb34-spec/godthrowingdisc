package net.throwingdiscs.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiscCommand implements CommandExecutor, TabCompleter {

    private final DiscItemFactory factory;

    public DiscCommand(DiscItemFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            String list = Arrays.stream(DiscTier.values())
                    .map(t -> t.name().toLowerCase())
                    .collect(Collectors.joining(", "));
            sender.sendMessage(Component.text("Available discs: " + list, NamedTextColor.AQUA));
            return true;
        }

        if (args[0].equalsIgnoreCase("get") && args.length >= 2) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use /td get.", NamedTextColor.RED));
                return true;
            }
            DiscTier tier = DiscTier.fromId(args[1]);
            if (tier == null) {
                sender.sendMessage(Component.text("Unknown disc: " + args[1], NamedTextColor.RED));
                return true;
            }
            player.getInventory().addItem(factory.createDisc(tier));
            player.sendMessage(Component.text("Gave you " + tier.getDisplayName() + "!", NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length >= 3) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                return true;
            }
            DiscTier tier = DiscTier.fromId(args[2]);
            if (tier == null) {
                sender.sendMessage(Component.text("Unknown disc: " + args[2], NamedTextColor.RED));
                return true;
            }
            target.getInventory().addItem(factory.createDisc(tier));
            sender.sendMessage(Component.text("Gave " + tier.getDisplayName() + " to " + target.getName() + ".", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Usage: /td <get <disc> | give <player> <disc> | list>", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("get", "give", "list");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            return Arrays.stream(DiscTier.values()).map(t -> t.name().toLowerCase()).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) players.add(p.getName());
            return players;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.stream(DiscTier.values()).map(t -> t.name().toLowerCase()).toList();
        }
        return List.of();
    }
}
