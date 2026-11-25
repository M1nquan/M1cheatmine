package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class PhaseModule extends Module {
    public final BooleanSetting noclip = new BooleanSetting("Noclip", true);
    public final NumberSetting speed = new NumberSetting("Speed", 0.3, 0.1, 1.5, 0.1);

    public PhaseModule() {
        super("Phase", Category.MOVEMENT, false);
        addSetting(noclip);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (noclip.value) {
            mc.player.noPhysics = true;
            mc.player.setNoGravity(true);

            double forward = mc.player.input.getMoveVector().y;
            double strafe = mc.player.input.getMoveVector().x;
            double yaw = Math.toRadians(mc.player.getYRot());

            double vx = (forward * -Math.sin(yaw) + strafe * Math.cos(yaw)) * speed.value;
            double vz = (forward * Math.cos(yaw) + strafe * Math.sin(yaw)) * speed.value;
            double vy = 0;

            if (mc.options.keyJump.isDown()) vy = speed.value;
            else if (mc.options.keyShift.isDown()) vy = -speed.value;

            mc.player.setDeltaMovement(new Vec3(vx, vy, vz));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.noPhysics = false;
            mc.player.setNoGravity(false);
        }
    }
}