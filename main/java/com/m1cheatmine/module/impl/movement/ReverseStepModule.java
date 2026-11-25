package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ReverseStepModule extends Module {
    public final NumberSetting height = new NumberSetting("Height", 3.0, 1.0, 5.0, 0.1);

    public ReverseStepModule() {
        super("ReverseStep", Category.MOVEMENT, false);
        addSetting(height);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava() || mc.player.getAbilities().flying) return;

        Vec3 vel = mc.player.getDeltaMovement();
        if (vel.y >= 0 || mc.player.fallDistance > height.value) return;

        AABB box = mc.player.getBoundingBox();
        // KiÃ¡Â»Æ’m tra xem cÃƒÂ³ mÃ¡ÂºÂ·t Ã„â€˜Ã¡ÂºÂ¥t trong phÃ¡ÂºÂ¡m vi height.value bÃƒÂªn dÃ†Â°Ã¡Â»â€ºi khÃƒÂ´ng
        if (!mc.level.noCollision(box.move(0, -height.value, 0))) {
            mc.player.setDeltaMovement(vel.x, -height.value, vel.z);
        }
    }
}