package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;

public class CriticalsModule extends Module {
    public final NumberSetting hopForce = new NumberSetting("Hop Force", 0.14, 0.05, 0.42, 0.01);
    public final BooleanSetting onlyMoving = new BooleanSetting("Only Moving", false);

    public CriticalsModule() {
        super("Criticals", Category.COMBAT, false);
        addSetting(hopForce);
        addSetting(onlyMoving);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava()) return;
        if (onlyMoving.value && mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.001) return;

        if (mc.player.getDeltaMovement().y <= 0.0) {
            mc.player.setDeltaMovement(
                mc.player.getDeltaMovement().x,
                hopForce.value,
                mc.player.getDeltaMovement().z
            );
        }
    }
}