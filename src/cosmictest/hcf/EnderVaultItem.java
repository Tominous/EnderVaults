/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package cosmictest.hcf;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EnderVaultItem {

    private List<String> commands;
    private boolean commanding, defo;
    private ItemStack reward;
    private int chance;

    public EnderVaultItem(ItemStack reward, boolean commanding, boolean defo, List<String> commands, int chance) {
        this.reward = reward;
        this.commanding = commanding;
        this.defo = defo;
        this.commands = commands;
        this.chance = chance;
    }

    public int getChance() {
        return chance;
    }

    public boolean isCommanding() {
        return commanding;
    }

    public boolean isGuaranteed() {
        return defo;
    }

    public ItemStack getReward() {
        return reward;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommanding(boolean a) {
        commanding = a;
    }

}
