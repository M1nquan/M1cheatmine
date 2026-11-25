package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventoryManagerModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 100, 0, 500, 10);
    public final BooleanSetting cleanTrash = new BooleanSetting("CleanTrash", true);
    public final BooleanSetting bestSword = new BooleanSetting("BestSword", true);
    private long lastAction = 0;

    public InventoryManagerModule() {
        super("InvManager", Category.PLAYER, false);
        addSetting(delay);
        addSetting(cleanTrash);
        addSetting(bestSword);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || mc.screen != null) return;
        long now = System.currentTimeMillis();
        if (now - lastAction < delay.value) return;

        if (bestSword.value) {
            int bestSlot = -1;
            int bestRank = -1;
            for (int i = 0; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.is(ItemTags.SWORDS)) {
                    int rank = getSwordRank(stack.getItem());
                    if (rank > bestRank) {
                        bestRank = rank;
                        bestSlot = i;
                    }
                }
            }
            if (bestSlot != -1 && bestSlot != 0) {
                int containerSlot = bestSlot < 9 ? bestSlot + 36 : bestSlot;
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, containerSlot, 0, ClickType.SWAP, mc.player);
                lastAction = now;
                return;
            }
        }

        if (cleanTrash.value) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (isTrash(stack)) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, i, 1, ClickType.THROW, mc.player);
                    lastAction = now;
                    return;
                }
            }
        }
    }

    private int getSwordRank(Item item) {
        if (item == Items.NETHERITE_SWORD) return 6;
        if (item == Items.DIAMOND_SWORD) return 5;
        if (item == Items.IRON_SWORD) return 4;
        if (item == Items.STONE_SWORD) return 3;
        if (item == Items.GOLDEN_SWORD) return 2;
        return 1;
    }

    private boolean isTrash(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.DIRT || item == Items.COBBLESTONE || item == Items.GRAVEL
                || item == Items.SAND || item == Items.ROTTEN_FLESH || item == Items.WHEAT_SEEDS
                || item == Items.STRING || item == Items.SPIDER_EYE;
    }
}