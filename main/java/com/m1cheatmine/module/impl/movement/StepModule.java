package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StepModule extends Module {
    public final NumberSetting height = new NumberSetting("Height", 1.5, 1.0, 3.0, 0.1);

    public StepModule() {
        super("Step", Category.MOVEMENT, false);
        addSetting(height);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        AttributeInstance stepAttr = mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepAttr != null) {
            stepAttr.setBaseValue(mc.player.isShiftKeyDown() ? 0.6 : height.value);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        AttributeInstance stepAttr = mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepAttr != null) {
            stepAttr.setBaseValue(0.6);
        }
    }
}