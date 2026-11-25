package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;

public class ToggleSprintModule extends Module {
    public final BooleanSetting omniSprint = new BooleanSetting("OmniSprint", false);

    public ToggleSprintModule() {
        super("ToggleSprint", Category.MOVEMENT, true);
        addSetting(omniSprint);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        boolean hasInput = omniSprint.value
                ? (mc.player.input.getMoveVector().y != 0 || mc.player.input.getMoveVector().x != 0)
                : (mc.player.input.getMoveVector().y > 0);

        boolean canSprint = hasInput
                && !mc.player.isShiftKeyDown()
                && !mc.player.isUsingItem()
                && !mc.player.horizontalCollision
                && (mc.player.getFoodData().getFoodLevel() > 6 || mc.player.getAbilities().mayfly);

        if (canSprint) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && !mc.options.keySprint.isDown()) {
            mc.player.setSprinting(false);
        }
    }
}