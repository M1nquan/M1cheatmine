package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class AutoArmorModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 100, 0, 500, 10);
    private long lastSwap = 0;

    public AutoArmorModule() {
        super("AutoArmor", Category.COMBAT, true);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        long now = System.currentTimeMillis();
        if (now - lastSwap < delay.value) return;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Equippable eq = stack.get(DataComponents.EQUIPPABLE);
            if (eq == null || eq.slot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            if (mc.player.getItemBySlot(eq.slot()).isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                lastSwap = now;
                return;
            }
        }
    }
}