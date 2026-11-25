package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

public class ChestStealerModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 80, 0, 500, 10);
    public final BooleanSetting autoClose = new BooleanSetting("AutoClose", true);
    private long lastClick = 0;

    public ChestStealerModule() {
        super("ChestStealer", Category.PLAYER, false);
        addSetting(delay);
        addSetting(autoClose);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        if (!(mc.player.containerMenu instanceof ChestMenu menu)) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < delay.value) return;

        int containerSlots = menu.getRowCount() * 9;
        for (int i = 0; i < containerSlots; i++) {
            if (!menu.getSlot(i).getItem().isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(menu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                lastClick = now;
                return;
            }
        }

        if (autoClose.value) {
            mc.player.closeContainer();
        }
    }
}