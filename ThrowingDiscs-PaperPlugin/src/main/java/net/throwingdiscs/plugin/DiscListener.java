package net.throwingdiscs.plugin;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscListener implements Listener {

    private final ThrowingDiscsPlugin plugin;
    private final DiscItemFactory factory;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DiscListener(ThrowingDiscsPlugin plugin, DiscItemFactory factory) {
        this.plugin = plugin;
        this.factory = factory;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        DiscTier tier = factory.getDiscTier(item);
        if (tier == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("throwingdiscs.use")) {
            return;
        }

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 300L) {
            return;
        }
        cooldowns.put(player.getUniqueId(), now);

        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        ItemStack thrownCopy = item.clone();
        thrownCopy.setAmount(1);

        if (!creative) {
            if (event.getHand() == EquipmentSlot.HAND) {
                item.setAmount(item.getAmount() - 1);
            } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
                item.setAmount(item.getAmount() - 1);
            }
        }

        new DiscProjectileTask(plugin, player, tier, thrownCopy, !creative).runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        SmithingInventory inv = event.getInventory();
        ItemStack template = inv.getInputTemplate();
        ItemStack base = inv.getInputEquipment();
        ItemStack addition = inv.getInputMineral();

        if (template == null || base == null || addition == null) return;
        if (template.getType() != Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) return;
        if (addition.getType() != Material.NETHERITE_INGOT) return;

        DiscTier baseTier = factory.getDiscTier(base);
        if (baseTier == DiscTier.DIAMOND) {
            event.setResult(factory.createDisc(DiscTier.NETHERITE));
        }
    }
}
