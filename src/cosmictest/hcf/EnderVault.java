/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;


import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EnderVault implements Listener {

    private String name;
    private int size;
    private List<EnderVaultItem> items;
    private ItemStack vaultItem;
    private ConfigurationSection config;
    private endervaults pl;


    public EnderVault(endervaults pl, String name, int size, List<EnderVaultItem> items, ItemStack vaultItem) {
        this.name = name;
        this.size = size;
        this.items = items;
        this.vaultItem = vaultItem;
        this.pl = pl;
        this.config = pl.getConfig().getConfigurationSection("vaults." + this.name);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public List<EnderVaultItem> getItems() {
        return items;
    }

    public ItemStack getVaultItem() {
        return vaultItem;
    }

    public void run(Player player, Block block) {
        Map<UUID, EnderVault> enderMap = pl.getEnderMap();

        Location ahead = player.getLocation().add(player.getEyeLocation().getDirection().multiply(config.getInt("distance")));
        ahead.setY(block.getY() - 1.25);
        ArmorStand armorStand = ahead.getWorld().spawn(ahead, ArmorStand.class);
        pl.getUtils().new Visible().setInvisible(armorStand, true);
        pl.getUtils().new Slots().setEnableSlots(armorStand, false);
        pl.getUtils().new Invulnerable().setInvulnerable(armorStand, true);
        armorStand.setArms(false);
        armorStand.setHelmet(new ItemStack(Material.getMaterial(config.getString("base"))));
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);

        if (config.getBoolean("show-name")) {
            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("vault-name")));
            armorStand.setCustomNameVisible(true);
        }

        List<EnderVaultItem> rewards = new ArrayList<>();
        List<EnderVaultItem> defos = new ArrayList<>();

        // Calculating rewards
        {
            List<EnderVaultItem> clone = new ArrayList<>();
            items.forEach(ev -> {
                if (!ev.isGuaranteed()) {
                    clone.add(ev);//stash
                } else {
                    defos.add(ev);
                }
            });

            do {
                Collections.shuffle(defos);
                EnderVaultItem r = defos.get(0);
                if (ThreadLocalRandom.current().nextInt(0, 101) <= r.getChance()) {
                    rewards.add(r);
                    defos.clear();
                    break;
                }
            } while (rewards.isEmpty());

            // guaranteed is done

            // Calculation of real rewards from stash
            int rewardsSize = config.getInt("stash-size");
            do {
                Collections.shuffle(clone);
                EnderVaultItem r = clone.get(0);
                if (ThreadLocalRandom.current().nextInt(0, 101) <= r.getChance()) {
                    rewards.add(r);// if the chance is right then add to rewards, else do again
                    clone.remove(0);
                }
            } while (rewards.size() != rewardsSize + 1);
        }

        Long show = config.getInt("show-time") * 20L;

        Collections.reverse(rewards);// so guaranteed goes last

        final float radius = Float.parseFloat(Double.valueOf(config.getDouble("options.spin.radius")).toString());

        ArmorStand label = armorStand.getWorld().spawn(armorStand.getLocation().clone().add(0,config.getDouble("label-height"),0),
                ArmorStand.class);
        ArmorStand witherSkull = armorStand.getWorld().spawn(armorStand.getLocation().clone().add(0, config.getDouble("item-height"), 0),
                ArmorStand.class);

        pl.getUtils().new Slots().setEnableSlots(witherSkull, false);
        pl.getUtils().new Invulnerable().setInvulnerable(witherSkull, true);
        pl.getUtils().new Visible().setInvisible(witherSkull, true);
        pl.getUtils().new Slots().setEnableSlots(label, false);
        pl.getUtils().new Invulnerable().setInvulnerable(label, true);
        pl.getUtils().new Visible().setInvisible(label, true);

        witherSkull.setGravity(false);
        label.setGravity(false);

        label.setCustomName(getItemName(rewards.get(0).getReward()));
        label.setCustomNameVisible(true);

        witherSkull.setCustomNameVisible(false);
        witherSkull.setSmall(false);
        witherSkull.setItemInHand(rewards.get(0).getReward());

        final Location center = label.getLocation();
        final float radPerSec = Float.parseFloat(Double.valueOf(config.getDouble("options.spin.speed")).toString());
        final float radPerTick = radPerSec / 20f;

        BukkitTask test = new BukkitRunnable() {
            int tick = 0;
            public void run() {
                ++tick;

                Location loc = getLocationAroundCircle(center, radius, radPerTick * tick);
                witherSkull.setVelocity(new Vector(1, 0, 0));
                witherSkull.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }.runTaskTimer(pl, 0L, 1);

        BukkitTask display = Bukkit.getScheduler().runTaskTimer(pl, new BukkitRunnable() {
            @Override
            public void run() {

                // Checking for errors basically

                if (rewards.isEmpty()) {
                    return;
                }

                try {
                    player.playSound(player.getLocation(), Sound.valueOf(pl.getConfig().getString("sounds.item-show")), 5, 10);
                } catch (Exception ex) {
                    System.err.println("[EnderVaults] Could not find sound:" + pl.getConfig().getString("sounds.item-show"));
                }
                for (double t = 0; t < 360; t++) {
                    double x = Math.toRadians(t);
                    double z = Math.toRadians(t);
                    armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(
                            Math.cos(x), 1.5, Math.sin(z)
                    ), Effect.INSTANT_SPELL, 1);
                    armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(
                            Math.cos(x), 2, Math.sin(z)
                    ), Effect.FIREWORKS_SPARK, 1);
                    armorStand.getWorld().playEffect(armorStand.getLocation().clone().add(
                            Math.cos(x), 1, Math.sin(z)
                      ), Effect.PARTICLE_SMOKE, 1);
                }

                label.setCustomName(getItemName(rewards.get(0).getReward()));

                witherSkull.setItemInHand(rewards.get(0).getReward());

                //test

                // rewards
                if (rewards.get(0) != null) {
                    if (rewards.get(0).isCommanding()) {
                        List<String> commands = rewards.get(0).getCommands();
                        commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%",
                                player.getName()).replace("%item%", getItemName(rewards.get(0).getReward()))));
                    } else {
                        if (player.getInventory().firstEmpty() == -1) {
                            armorStand.getWorld().dropItemNaturally(player.getLocation(),rewards.get(0).getReward());
                        } else {
                            player.getInventory().addItem(rewards.get(0).getReward());
                        }
                    }
                }

                Bukkit.getScheduler().runTaskLater(pl, () -> {
                    rewards.remove(0);
                }, show - 5L);

            }

        }, 0L, show);

        Bukkit.getScheduler().runTaskLater(pl, () -> {
            test.cancel();
            label.remove();
            witherSkull.remove();
            display.cancel();
            armorStand.remove();
            enderMap.remove(player.getUniqueId());
        }, (show) * (config.getInt("stash-size") + 1));
    }

    private Location getLocationAroundCircle(Location center, double radius, double angleInRadian) {
        double x = center.getX() + radius * Math.cos(angleInRadian);
        double z = center.getZ() + radius * Math.sin(angleInRadian);
        double y = center.getY();

        Location loc = new Location(center.getWorld(), x, y, z);
        Vector difference = center.toVector().clone().subtract(loc.toVector()); // this sets the returned location's direction toward the center of the circle
        loc.setDirection(difference);

        return loc;
    }

    private String getItemName(ItemStack currentItem) {
        if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
            return currentItem.getItemMeta().getDisplayName();
        } else {
            String str = currentItem.getType().name()
                    .replace("_", " ").toLowerCase();
            StringBuilder b = new StringBuilder(str);
            int i = 0;
            do {
                b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
                i = b.indexOf(" ", i) + 1;
            } while (i > 0 && i < b.length());
            return b.toString();
        }
    }

}