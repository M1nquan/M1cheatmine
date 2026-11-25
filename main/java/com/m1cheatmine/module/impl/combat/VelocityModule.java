package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class VelocityModule extends Module {
    public final NumberSetting horizontal = new NumberSetting("Horizontal", 0.0, 0.0, 100.0, 5.0);
    public final NumberSetting vertical = new NumberSetting("Vertical", 0.0, 0.0, 100.0, 5.0);

    public VelocityModule() {
        super("Velocity", Category.COMBAT, false);
        addSetting(horizontal);
        addSetting(vertical);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.hurtTime <= 0) return;

        double hFactor = horizontal.value / 100.0;
        double vFactor = vertical.value / 100.0;
        Vec3 motion = mc.player.getDeltaMovement();

        mc.player.setDeltaMovement(
            motion.x * hFactor,
            motion.y > 0 ? motion.y * vFactor : motion.y,
            motion.z * hFactor
        );
    }
}