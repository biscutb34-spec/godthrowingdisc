package net.throwingdiscs.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class DiscItemFactory {

    private final ThrowingDiscsPlugin plugin;
    private final NamespacedKey tierKey;

    public DiscItemFactory(ThrowingDiscsPlugin plugin) {
        this.plugin = plugin;
        this.tierKey = new NamespacedKey(plugin, "disc_tier");
    }

    public NamespacedKey getTierKey() {
        return tierKey;
    }

    @SuppressWarnings("deprecation")
    public ItemStack createDisc(DiscTier tier) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Component.text(tier.getDisplayName(), tier.getColor())
                .decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(tier.getCustomModelData());
        meta.setMaxStackSize(1);

        List<Component> lore = new ArrayList<>();
        if (tier.isFrisbee()) {
            lore.add(Component.text("Play with tamed dogs or other players", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text(" " + (int) tier.getDamage() + " Thrown Damage", NamedTextColor.DARK_GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" " + (int) tier.getRange() + " Block Range", NamedTextColor.DARK_GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" " + (int) tier.getSpeedBlocksPerSec() + " Blocks/s Speed", NamedTextColor.DARK_GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" " + tier.getMaxBounces() + " Max Bounces", NamedTextColor.DARK_GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(" +15% Damage per Bounce", NamedTextColor.BLUE)
                    .decoration(TextDecoration.ITALIC, false));
        }

        if (tier.isRedstonePower()) {
            lore.add(Component.text("Powers blocks it bounces off", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(tierKey, PersistentDataType.STRING, tier.getId());

        item.setItemMeta(meta);
        return item;
    }

    public DiscTier getDiscTier(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_NUGGET || !item.hasItemMeta()) {
            return null;
        }
        String id = item.getItemMeta().getPersistentDataContainer().get(tierKey, PersistentDataType.STRING);
        return DiscTier.fromId(id);
    }

    public void registerRecipes() {
        registerRingRecipe(DiscTier.COPPER, Material.COPPER_INGOT);
        registerRingRecipe(DiscTier.IRON, Material.IRON_INGOT);
        registerRingRecipe(DiscTier.GOLD, Material.GOLD_INGOT);
        registerRingRecipe(DiscTier.DIAMOND, Material.DIAMOND);

        // Redstone Throwing Disc
        ShapedRecipe redstone = new ShapedRecipe(new NamespacedKey(plugin, "redstone_throwing_disc"),
                createDisc(DiscTier.REDSTONE));
        redstone.shape(" # ", "#X#", " # ");
        redstone.setIngredient('#', Material.IRON_INGOT);
        redstone.setIngredient('X', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(redstone);

        // Frisbee
        ShapedRecipe frisbee = new ShapedRecipe(new NamespacedKey(plugin, "frisbee"),
                createDisc(DiscTier.FRISBEE));
        frisbee.shape(" # ", "#X#", " # ");
        frisbee.setIngredient('#', new RecipeChoice.MaterialChoice(Tag.PLANKS));
        frisbee.setIngredient('X', Material.LEATHER);
        Bukkit.addRecipe(frisbee);
    }

    private void registerRingRecipe(DiscTier tier, Material material) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, tier.getId()), createDisc(tier));
        recipe.shape(" # ", "# #", " # ");
        recipe.setIngredient('#', material);
        Bukkit.addRecipe(recipe);
    }
}
