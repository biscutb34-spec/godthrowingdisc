package net.throwingdiscs.plugin;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DiscProjectileTask extends BukkitRunnable {

    private final ThrowingDiscsPlugin plugin;
    private final Player thrower;
    private final DiscTier tier;
    private final ItemStack originalItem;
    private final boolean shouldReturnItem;
    private final ItemDisplay display;

    private Location currentLoc;
    private Vector direction;
    private double distanceTraveled = 0.0;
    private int bounces = 0;
    private int ticksAlive = 0;
    private boolean returning = false;
    private float spinAngle = 0f;
    private final Set<UUID> hitThisSegment = new HashSet<>();

    public DiscProjectileTask(ThrowingDiscsPlugin plugin, Player thrower, DiscTier tier,
                              ItemStack originalItem, boolean shouldReturnItem) {
        this.plugin = plugin;
        this.thrower = thrower;
        this.tier = tier;
        this.originalItem = originalItem.clone();
        this.shouldReturnItem = shouldReturnItem;

        Location eye = thrower.getEyeLocation();
        this.direction = eye.getDirection().normalize();
        this.currentLoc = eye.clone().add(direction.clone().multiply(0.6));

        World world = thrower.getWorld();
        this.display = world.spawn(currentLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(originalItem.clone());
            entity.setPersistent(false);
            entity.setTeleportDuration(1);
            entity.setTransformation(createFlatSpinTransform(0f));
        });

        plugin.trackDisplay(display);
        world.playSound(currentLoc, Sound.ENTITY_SNOWBALL_THROW, 1.0f, 0.85f);
    }

    private Transformation createFlatSpinTransform(float spinRadians) {
        return new Transformation(
                new Vector3f(0f, 0f, 0f),
                new AxisAngle4f((float) Math.toRadians(90), 1f, 0f, 0f),
                new Vector3f(0.85f, 0.85f, 0.85f),
                new AxisAngle4f(spinRadians, 0f, 0f, 1f)
        );
    }

    @Override
    public void run() {
        ticksAlive++;
        if (display.isDead() || !display.isValid() || ticksAlive > 240) {
            finish();
            return;
        }

        World world = currentLoc.getWorld();
        if (world == null) {
            finish();
            return;
        }

        double step = tier.getSpeedBlocksPerSec() / 20.0;
        if (returning) {
            if (!thrower.isOnline() || thrower.isDead() || !thrower.getWorld().equals(world)) {
                finish();
                return;
            }
            Location target = thrower.getLocation().add(0, 1.0, 0);
            Vector toOwner = target.toVector().subtract(currentLoc.toVector());
            double distToOwner = toOwner.length();
            if (distToOwner <= Math.max(1.4, step * 1.25)) {
                world.playSound(target, Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
                finish();
                return;
            }
            direction = toOwner.normalize();
            step *= 1.25;
        }

        // Block collision / ricochet check (only while flying outward)
        if (!returning) {
            RayTraceResult blockHit = world.rayTraceBlocks(currentLoc, direction, step, FluidCollisionMode.NEVER, true);
            if (blockHit != null && blockHit.getHitBlock() != null) {
                Block hitBlock = blockHit.getHitBlock();
                BlockFace face = blockHit.getHitBlockFace();

                if (tier.isRedstonePower()) {
                    triggerRedstonePulse(hitBlock);
                }

                if (bounces < tier.getMaxBounces() && face != null) {
                    Vector normal = face.getDirection();
                    // Reflect: v' = v - 2 * (v . n) * n
                    double dot = direction.dot(normal);
                    direction = direction.clone().subtract(normal.multiply(2.0 * dot)).normalize();
                    currentLoc = blockHit.getHitPosition().toLocation(world).add(direction.clone().multiply(0.15));
                    bounces++;
                    hitThisSegment.clear();

                    world.playSound(currentLoc, Sound.BLOCK_ANVIL_PLACE, 0.4f, 1.8f);
                    world.spawnParticle(Particle.CRIT, currentLoc, 8, 0.1, 0.1, 0.1, 0.15);
                } else {
                    returning = true;
                    hitThisSegment.clear();
                    world.playSound(currentLoc, Sound.BLOCK_METAL_HIT, 0.7f, 1.4f);
                }
            } else {
                currentLoc.add(direction.clone().multiply(step));
            }

            distanceTraveled += step;
            if (distanceTraveled >= tier.getRange()) {
                returning = true;
                hitThisSegment.clear();
            }
        } else {
            currentLoc.add(direction.clone().multiply(step));
        }

        // Entity pierce / damage check
        if (!tier.isFrisbee()) {
            double finalDamage = tier.getDamage() * (1.0 + (0.15 * bounces));
            for (Entity entity : world.getNearbyEntities(currentLoc, 0.85, 0.85, 0.85)) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (living.getUniqueId().equals(thrower.getUniqueId())) continue;
                if (hitThisSegment.contains(living.getUniqueId())) continue;

                hitThisSegment.add(living.getUniqueId());
                living.damage(finalDamage, thrower);
                world.playSound(currentLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.4f);
                world.spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 1, 0, 0, 0, 0);
            }
        }

        // Update visual spin & trail
        spinAngle += 0.9f;
        display.teleport(currentLoc);
        display.setTransformation(createFlatSpinTransform(spinAngle));
        if (tier == DiscTier.GOD) {
            world.spawnParticle(Particle.END_ROD, currentLoc, 1, 0.05, 0.05, 0.05, 0.01);
        } else if (tier.isRedstonePower()) {
            world.spawnParticle(Particle.FLAME, currentLoc, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    private void triggerRedstonePulse(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Lightable lightable && !lightable.isLit()) {
            lightable.setLit(true);
            block.setBlockData(lightable);
            BukkitRunnable revert = new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getBlockData() instanceof Lightable current) {
                        current.setLit(false);
                        block.setBlockData(current);
                    }
                }
            };
            revert.runTaskLater(plugin, 20L);
        } else if (data instanceof Openable openable) {
            openable.setOpen(!openable.isOpen());
            block.setBlockData(openable);
        } else if (data instanceof Powerable powerable && !powerable.isPowered()) {
            powerable.setPowered(true);
            block.setBlockData(powerable);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getBlockData() instanceof Powerable current) {
                        current.setPowered(false);
                        block.setBlockData(current);
                    }
                }
            }.runTaskLater(plugin, 20L);
        }
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.8f, 1.3f);
    }

    private void finish() {
        cancel();
        plugin.untrackDisplay(display);
        if (display.isValid()) {
            display.remove();
        }
        if (shouldReturnItem) {
            if (thrower.isOnline() && !thrower.isDead()) {
                var leftover = thrower.getInventory().addItem(originalItem);
                for (ItemStack drop : leftover.values()) {
                    thrower.getWorld().dropItemNaturally(thrower.getLocation(), drop);
                }
            } else if (currentLoc.getWorld() != null) {
                currentLoc.getWorld().dropItemNaturally(currentLoc, originalItem);
            }
        }
    }
}
