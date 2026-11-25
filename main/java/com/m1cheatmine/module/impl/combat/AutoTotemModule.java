package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotemModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 100, 0, 500, 10);
    public final NumberSetting health = new NumberSetting("Health", 14.0, 1.0, 20.0, 0.5);
    public final BooleanSetting lowHealthOnly = new BooleanSetting("Low Health Only", false);
    private long lastSwap = 0;

    public AutoTotemModule() {
        super("AutoTotem", Category.COMBAT, true);
        addSetting(delay);
        addSetting(health);
        addSetting(lowHealthOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        if (lowHealthOnly.value && mc.player.getHealth() > health.value) return;

        long now = System.currentTimeMillis();
        if (now - lastSwap < delay.value) return;

        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                int slotId = i < 9 ? i + 36 : i;
                mc.gameMode.handleInventoryMouseClick(
                    mc.player.inventoryMenu.containerId,
                    slotId,
                    40,
                    ClickType.SWAP,
                    mc.player
                );
                lastSwap = now;
                return;
            }
        }
    }
}