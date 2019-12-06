/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class endervaults extends JavaPlugin implements Listener {

    private Map<UUID, EnderVault> enderMap;
    private List<EnderVault> enderVaults;

    private Utils utils;

    public int getEnderVaultCount() {
        return enderVaults.size();
    }

    public synchronized String a(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public synchronized String b(String text, String[] s1, String... s2) {
        String c = text;
        if (s1 == null) return a(c);
        for (int x = 0; x < s2.length; x++) {
            c=c.replaceAll("%" + s1[x] + "%", s2[x]);
        }
        return a(c);
    }

    public Utils getUtils() {
        Validate.notNull(utils);
        return utils;
    }

    private ChatColor logColour = ChatColor.GREEN;
    private String header = "=================================";

    @Override
    public void onEnable() {

        Bukkit.getConsoleSender().sendMessage(logColour + header);
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Saving default config...");
        saveDefaultConfig();
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Saved default config");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Registered listeners");
        enderMap = Maps.newHashMap();
        enderVaults = new ArrayList<>();
        utils = new Utils();
        if (!utils.getServerVersion().contains("v1_8_R") && !utils.getServerVersion().contains("v1_9_R")
        && !utils.getServerVersion().contains("v1_10_R") && !utils.getServerVersion().contains("v1_11_R") &&
                !utils.getServerVersion().contains("v1_12_R") &&
                !utils.getServerVersion().contains("v1_13_R") && !utils.getServerVersion().contains("v1_14_R")) {
            Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Version:"+utils.getServerVersion() + " is not supported");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Found worldguard successfully");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Found factions successfully");
        }
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Registered server version to plugin:" + utils.getServerVersion());
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Registering commands");
        getCommand("endervaults").setExecutor(this);
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Registered commands: " + getDescription().getCommands().keySet());
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVaults] Registering vaults");
        loadEnderVaults();
        Bukkit.getConsoleSender().sendMessage(logColour + "[EnderVault] Loaded vaults");

        Metrics metrics = new Metrics(this);
        System.out.println("[EnderVaults] Metrics enabled: " + metrics.isEnabled());
    }

    @Override
    public void onDisable() {
        if (enderVaults == null) return;
        for (EnderVault enderVault : enderVaults) {
            enderVault.unload();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // basic command to give jan crate

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                getConfig().save(new File("plugins" + File.separator + "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadEnderVaults();
            sender.sendMessage(a(getConfig().getString("messages.commands.reload")));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(a(getConfig().getString("messages.commands.give.not-found").replaceAll("%player%", args[1])));
                return true;
            } else {
                String vault = args[2];
                if (vault.equalsIgnoreCase("all")) {
                    int amount = 1;
                    try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ex) {}
                    for (EnderVault enderVault : enderVaults) {
                        for (int x = 0; x < amount; x++) {
                            if (target.getInventory().firstEmpty() == -1) {
                                target.getWorld().dropItemNaturally(target.getLocation(), enderVault.getVaultItem());
                            } else {
                                target.getInventory().addItem(enderVault.getVaultItem());
                            }
                        }
                    }
                    sender.sendMessage(a(getConfig().getString("messages.commands.give.done-all").replaceAll("%player%",
                            target.getName())));
                    target.sendMessage(a(getConfig().getString("messages.commands.give.received-all")));
                } else {
                    int amount = 1;
                    try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ex) {}
                    boolean found = false;
                    for (EnderVault enderVault : enderVaults) {
                        if (enderVault.getName().equalsIgnoreCase(vault)) {
                            found = true;
                            for (int x = 0; x < amount; x++) {
                                if (target.getInventory().firstEmpty() == -1) {
                                    target.getWorld().dropItemNaturally(target.getLocation(), enderVault.getVaultItem());
                                } else {
                                    target.getInventory().addItem(enderVault.getVaultItem());
                                }
                            }
                            sender.sendMessage(a(getConfig().getString("messages.commands.give.done")
                                    .replaceAll("%player%", target.getName()).replaceAll("%endervault%",enderVault.getName())));
                            target.sendMessage(a(getConfig().getString("messages.commands.give.received")
                            .replaceAll("%endervault%",enderVault.getName())));
                        }
                    }
                    if (!found) { sender.sendMessage(a(getConfig().getString("messages.commands.give.invalid-vault"))); }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("giveall")) {

            String vault = args[1];
            if (vault.equalsIgnoreCase("all")) {
                int amount = 1;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                }
                for (EnderVault enderVault : enderVaults) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        for (int x = 0; x < amount; x++) {
                            if (target.getInventory().firstEmpty() == -1) {
                                target.getWorld().dropItemNaturally(target.getLocation(), enderVault.getVaultItem());
                            } else {
                                target.getInventory().addItem(enderVault.getVaultItem());
                            }
                        }
                        target.sendMessage(a(getConfig().getString("messages.commands.giveall.received-all")));
                    }
                }
                sender.sendMessage(a(getConfig().getString("messages.commands.giveall.done-all")));
            } else {
                int amount = 1;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                }
                boolean found = false;
                for (EnderVault enderVault : enderVaults) {
                    if (enderVault.getName().equalsIgnoreCase(vault)) {
                        found = true;
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            for (int x = 0; x < amount; x++) {
                                if (target.getInventory().firstEmpty() == -1) {
                                    target.getWorld().dropItemNaturally(target.getLocation(), enderVault.getVaultItem());
                                } else {
                                    target.getInventory().addItem(enderVault.getVaultItem());
                                }
                            }
                            target.sendMessage(a(getConfig().getString("messages.commands.giveall.received")
                                    .replaceAll("%endervault%", enderVault.getName())));
                        }
                        sender.sendMessage(a(getConfig().getString("messages.commands.giveall.done").replaceAll("%endervault%", enderVault.getName())));
                    }
                }
                if (!found) {
                    sender.sendMessage(a(getConfig().getString("messages.commands.giveall.invalid-vault")));
                }
            }
        } else {
            for (String help : getConfig().getStringList("messages.help")) {
                sender.sendMessage(a(help));
            }
        }

        return false;
    }

    private void loadEnderVaults() {

        if (!enderVaults.isEmpty()) {enderVaults.forEach(ev -> ev.unload());}
        enderVaults.clear();

        ConfigurationSection vaults = getConfig().getConfigurationSection("vaults");
        for (String key : vaults.getKeys(false)) {
            ConfigurationSection s = vaults.getConfigurationSection(key);
            EnderVault enderVault;

            String vaultName = key;
            int items = s.getInt("items");
            ItemStack vi;
            List<EnderVaultItem> enderVaultItems = new ArrayList<>();

            ItemMeta meta;

            // Vault Item
            {
                ConfigurationSection vaultSection = s.getConfigurationSection("vault-item");
                String name = a(vaultSection.getString("name"));
                Material type = Material.getMaterial(vaultSection.getString("material"));
                int amount = vaultSection.getInt("amount");
                int durability = vaultSection.getInt("durability");
                int data = vaultSection.getInt("data");
                List<String> lore = vaultSection.getStringList("lore");

                ItemStack vaultItem = new ItemStack(type, amount, (byte) durability);
                vaultItem.getData().setData((byte) data);
                meta = Bukkit.getItemFactory().getItemMeta(type);
                if (!name.equals("none")) meta.setDisplayName(name);
                List<String> tLore = new ArrayList<>();
                for (String l : lore) {
                    tLore.add(a(l));
                }
                meta.setLore(tLore);
                vaultItem.setItemMeta(meta);
                vi = vaultItem;
            }
            // End

            // Rewards
            ConfigurationSection rewardSection = s.getConfigurationSection("rewards");
            // Guaranteed
            {
                ConfigurationSection ggSection = rewardSection.getConfigurationSection("guaranteed");
                for (int x = 0; x < ggSection.getKeys(false).size(); x++) {
                    ConfigurationSection gSection = ggSection.getConfigurationSection(String.valueOf(x));
                    String name = a(gSection.getString("name"));
                    Material type = Material.getMaterial(gSection.getString("material"));
                    int amount = gSection.getInt("amount");
                    int durability = gSection.getInt("durability");
                    int data = gSection.getInt("data");
                    List<String> lore = gSection.getStringList("lore");

                    ItemStack g = new ItemStack(type, amount, (byte) durability);
                    g.getData().setData((byte) data);
                    meta = Bukkit.getItemFactory().getItemMeta(type);
                    if (!name.equals("none")) meta.setDisplayName(name);
                    List<String> tLore = new ArrayList<>();
                    for (String l : lore) {
                        tLore.add(a(l));
                    }
                    for (String enchant : gSection.getStringList("enchants")) {
                        int id = 0;
                        int lvl = 0;
                        try {
                            id = Integer.parseInt(enchant.split(":")[0]);
                            lvl = Integer.parseInt(enchant.split(":")[1]);
                        } catch (Exception ex) {
                        }
                        meta.addEnchant(Enchantment.getById(id), lvl, true);
                    }
                    meta.setLore(tLore);
                    g.setItemMeta(meta);

                    EnderVaultItem enderVaultItem = new EnderVaultItem(g, false, true, gSection.getStringList("commands"), gSection.getInt("chance"));

                    if (!gSection.getStringList("commands").isEmpty()) {
                        enderVaultItem.setCommanding(true);
                    }
                    enderVaultItems.add(enderVaultItem);
                }

            }
            // every other reward
            {
                ConfigurationSection sSection = rewardSection.getConfigurationSection("stash");
                for (int x = 0; x < sSection.getKeys(false).size(); x++) {
                    // 0,1,2,3,4...
                    ConfigurationSection gSection = sSection.getConfigurationSection(String.valueOf(x));
                    String name = a(gSection.getString("name"));
                    Material type = Material.getMaterial(gSection.getString("material"));
                    int amount = gSection.getInt("amount");
                    int durability = gSection.getInt("durability");
                    int data = gSection.getInt("data");
                    List<String> lore = gSection.getStringList("lore");

                    ItemStack g = new ItemStack(type, amount, (byte) durability);
                    g.getData().setData((byte) data);
                    meta = Bukkit.getItemFactory().getItemMeta(type);
                    if (!name.equals("none")) meta.setDisplayName(name);
                    List<String> tLore = new ArrayList<>();
                    for (String l : lore) {
                        tLore.add(a(l));
                    }
                    for (String enchant : gSection.getStringList("enchants")) {
                        int id = 0;
                        int lvl = 0;
                        try {
                            id = Integer.parseInt(enchant.split(":")[0]);
                            lvl = Integer.parseInt(enchant.split(":")[1]);
                        } catch (Exception ex) {
                        }
                        meta.addEnchant(Enchantment.getById(id), lvl, true);
                    }
                    meta.setLore(tLore);
                    g.setItemMeta(meta);

                    EnderVaultItem enderVaultItem = new EnderVaultItem(g, false, false, gSection.getStringList("commands"),
                            gSection.getInt("chance"));

                    if (!gSection.getStringList("commands").isEmpty()) {
                        enderVaultItem.setCommanding(true);
                    }

                    enderVaultItems.add(enderVaultItem);
                }
            }
            enderVault = new EnderVault(this, vaultName, items, enderVaultItems, vi);
            enderVaults.add(enderVault);
            Bukkit.getPluginManager().registerEvents(enderVault, this);
        }
    }

    public Map<UUID, EnderVault> getEnderMap() {
        return enderMap;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack hand = event.getPlayer().getItemInHand();
        for (EnderVault enderVault : enderVaults) {
            if (enderVault.getVaultItem().isSimilar(hand)) {
                event.setCancelled(true);
                if (enderMap.containsKey(event.getPlayer().getUniqueId())) {
                    return;
                }

                boolean cancel = false;

                PluginSupport pluginSupport = new PluginSupport();
                if (pluginSupport.getRegions() != null) {
                    cancel = pluginSupport.getRegions().run(event,this);
                }

                if (pluginSupport.getFactions() != null) {
                    cancel = pluginSupport.getFactions().run(event,this);
                }

                if (cancel) {
                    return;
                }

                enderMap.put(event.getPlayer().getUniqueId(), enderVault);

                if (hand.getAmount() > 1) {
                    hand.setAmount(hand.getAmount() - 1);
                } else {
                    event.getPlayer().getInventory().removeItem(new ItemStack[]{hand});
                }
                enderMap.get(event.getPlayer().getUniqueId()).run(event.getPlayer(), event.getClickedBlock().getLocation().add(0,1,0).getBlock());
                break;
            }
        }
    }

}
