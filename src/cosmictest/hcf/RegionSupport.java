/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerInteractEvent;

public class RegionSupport {

    public boolean run(PlayerInteractEvent event, endervaults pl) {
        com.sk89q.worldguard.bukkit.RegionContainer regionContainer = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().getRegionContainer();

        for (String key : pl.getConfig().getConfigurationSection("region-support.worldguard.disabled-regions").getKeys(false)) {
            World world = Bukkit.getWorld(key);
            if (world == null) {
                continue;
            }
            com.sk89q.worldguard.protection.managers.RegionManager regions = regionContainer.get(world);
            for (String r : pl.getConfig().getStringList("region-support.worldguard.disabled-regions." + key)) {

                Location vaultLocation = event.getClickedBlock().getLocation().clone().add(0,1,0);
                Location playerLocation = event.getPlayer().getLocation();

                com.sk89q.worldguard.protection.ApplicableRegionSet set = regions.getApplicableRegions(playerLocation);
                for (com.sk89q.worldguard.protection.regions.ProtectedRegion re : set.getRegions()) {
                    if (re.getId().equalsIgnoreCase(r)) {
                        event.getPlayer().sendMessage(pl.a(pl.getConfig().getString("region-support.worldguard.player-disallow")));
                        return true;
                    }
                }

                set = regions.getApplicableRegions(vaultLocation);
                for (com.sk89q.worldguard.protection.regions.ProtectedRegion re : set.getRegions()) {
                    if (re.getId().equalsIgnoreCase(r)) {
                        event.getPlayer().sendMessage(pl.a(pl.getConfig().getString("region-support.worldguard.vault-disallow")));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
