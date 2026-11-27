package net.throwingdiscs.plugin;

import net.kyori.adventure.text.format.NamedTextColor;

public enum DiscTier {
    COPPER("copper_throwing_disc", "Copper Throwing Disc", NamedTextColor.GOLD, 11.0, 18.0, 22.0, 2, 1001, false, false),
    IRON("iron_throwing_disc", "Iron Throwing Disc", NamedTextColor.WHITE, 14.0, 22.0, 24.0, 3, 1002, false, false),
    GOLD("golden_throwing_disc", "Golden Throwing Disc", NamedTextColor.YELLOW, 11.0, 24.0, 28.0, 3, 1003, false, false),
    DIAMOND("diamond_throwing_disc", "Diamond Throwing Disc", NamedTextColor.AQUA, 17.0, 26.0, 26.0, 4, 1004, false, false),
    NETHERITE("netherite_throwing_disc", "Netherite Throwing Disc", NamedTextColor.DARK_PURPLE, 20.0, 30.0, 28.0, 5, 1005, false, false),
    REDSTONE("redstone_throwing_disc", "Redstone Throwing Disc", NamedTextColor.RED, 12.0, 24.0, 24.0, 4, 1006, true, false),
    GOD("god_throwing_disc", "God Disc", NamedTextColor.LIGHT_PURPLE, 30.0, 48.0, 44.0, 8, 1007, false, false),
    FRISBEE("frisbee", "Frisbee", NamedTextColor.GREEN, 0.0, 28.0, 18.0, 1, 1008, false, true);

    private final String id;
    private final String displayName;
    private final NamedTextColor color;
    private final double damage;
    private final double range;
    private final double speedBlocksPerSec;
    private final int maxBounces;
    private final int customModelData;
    private final boolean redstonePower;
    private final boolean frisbee;

    DiscTier(String id, String displayName, NamedTextColor color, double damage, double range,
             double speedBlocksPerSec, int maxBounces, int customModelData,
             boolean redstonePower, boolean frisbee) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.damage = damage;
        this.range = range;
        this.speedBlocksPerSec = speedBlocksPerSec;
        this.maxBounces = maxBounces;
        this.customModelData = customModelData;
        this.redstonePower = redstonePower;
        this.frisbee = frisbee;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public NamedTextColor getColor() { return color; }
    public double getDamage() { return damage; }
    public double getRange() { return range; }
    public double getSpeedBlocksPerSec() { return speedBlocksPerSec; }
    public int getMaxBounces() { return maxBounces; }
    public int getCustomModelData() { return customModelData; }
    public boolean isRedstonePower() { return redstonePower; }
    public boolean isFrisbee() { return frisbee; }

    public static DiscTier fromId(String input) {
        if (input == null) return null;
        String lower = input.toLowerCase();
        for (DiscTier tier : values()) {
            if (tier.id.equalsIgnoreCase(lower) || tier.name().equalsIgnoreCase(lower)) {
                return tier;
            }
        }
        return null;
    }
}
