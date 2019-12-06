/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import org.bukkit.Bukkit;

public class PluginSupport {

    private boolean factionsFound,regionsFound;

    public PluginSupport() {
        reg();
    }

    public void reg() {
        factionsFound = Bukkit.getPluginManager().isPluginEnabled("Factions");
        regionsFound = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
    }

    public boolean isFactionsFound() {
        return factionsFound;
    }

    public boolean isRegionsFound() {
        return regionsFound;
    }

    public FactionSupport getFactions() {
        reg();
        if (factionsFound) {
            return new FactionSupport();
        } else {
            return null;
        }
    }

    public RegionSupport getRegions() {
        reg();
        if (regionsFound) {
            return new RegionSupport();
        } else {
            return null;
        }
    }
}
