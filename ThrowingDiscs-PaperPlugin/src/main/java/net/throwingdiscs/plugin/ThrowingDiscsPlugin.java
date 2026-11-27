package net.throwingdiscs.plugin;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ThrowingDiscsPlugin extends JavaPlugin {

    private final Set<ItemDisplay> activeDisplays = new HashSet<>();

    @Override
    public void onEnable() {
        DiscItemFactory factory = new DiscItemFactory(this);
        factory.registerRecipes();

        getServer().getPluginManager().registerEvents(new DiscListener(this, factory), this);

        DiscCommand cmd = new DiscCommand(factory);
        PluginCommand pluginCommand = getCommand("throwingdiscs");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(cmd);
            pluginCommand.setTabCompleter(cmd);
        }

        getLogger().info("ThrowingDiscs Paper plugin enabled!");
    }

    @Override
    public void onDisable() {
        for (ItemDisplay display : activeDisplays) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        activeDisplays.clear();
    }

    public void trackDisplay(ItemDisplay display) {
        activeDisplays.add(display);
    }

    public void untrackDisplay(ItemDisplay display) {
        activeDisplays.remove(display);
    }
}
