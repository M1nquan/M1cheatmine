package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoEatModule extends Module {
    public final NumberSetting hunger = new NumberSetting("Hunger", 16, 1, 20, 1);
    public final BooleanSetting gapples = new BooleanSetting("AllowGapples", false);
    private int oldSlot = -1;
    private boolean eating = false;

    public AutoEatModule() {
        super("AutoEat", Category.PLAYER, false);
        addSetting(hunger);
        addSetting(gapples);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;

        if (mc.player.getAbilities().instabuild || mc.player.getFoodData().getFoodLevel() > hunger.value) {
            stopEating();
            return;
        }

        int foodSlot = findBestFoodSlot();
        if (foodSlot == -1) {
            stopEating();
            return;
        }

        if (!eating) {
            oldSlot = mc.player.getInventory().getSelectedSlot();
            eating = true;
        }

        mc.player.getInventory().setSelectedSlot(foodSlot);
        mc.options.keyUse.setDown(true);
    }

    @Override
    public void onDisable() {
        stopEating();
    }

    private void stopEating() {
        if (eating) {
            mc.options.keyUse.setDown(false);
            if (mc.player != null && oldSlot != -1) {
                mc.player.getInventory().setSelectedSlot(oldSlot);
            }
            eating = false;
            oldSlot = -1;
        }
    }

    private int findBestFoodSlot() {
        int bestSlot = -1;
        int bestNutrition = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (!gapples.value && (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE))) continue;

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null && food.nutrition() > bestNutrition) {
                bestNutrition = food.nutrition();
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}