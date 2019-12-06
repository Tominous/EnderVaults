/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import com.massivecraft.factions.FLocation;
import org.bukkit.event.player.PlayerInteractEvent;

public class FactionSupport {

    public boolean run(PlayerInteractEvent event, endervaults pl) {
        com.massivecraft.factions.FPlayer fplayer = com.massivecraft.factions.FPlayers.getInstance().getByPlayer(event.getPlayer());
        boolean cancel = false;
        String placeholder = "";// TODO
        com.massivecraft.factions.Faction faction = null;
        faction = com.massivecraft.factions.Board.getInstance().getFactionAt(fplayer.getLastStoodAt());
        com.massivecraft.factions.Faction factionEv = null;
        factionEv = com.massivecraft.factions.Board.getInstance().getFactionAt(new FLocation(event.getClickedBlock().getLocation()));
        if (faction != null) {
            if (!faction.isWarZone()) {
                if (pl.getConfig().getBoolean("region-support.factions.warzone-only")) {
                    cancel = true;
                    placeholder = faction.getColorTo(fplayer.getFaction()) + faction.getTag();
                }
            }
        }
        if (factionEv != null) {
            if (!factionEv.isWarZone()) {
                if (pl.getConfig().getBoolean("region-support.factions.warzone-only")) {
                    cancel = true;
                    placeholder = factionEv.getColorTo(fplayer.getFaction()) + factionEv.getTag();
                }
            }
        }
        if (cancel) {
            event.getPlayer().sendMessage(pl.b(pl.getConfig().getString("region-support.factions.player-disallow"),new String[] {"faction"}, placeholder));
        }
        return cancel;
    }

}
