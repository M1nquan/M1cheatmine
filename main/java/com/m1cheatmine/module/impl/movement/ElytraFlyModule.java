package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class ElytraFlyModule extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", 1.8, 0.1, 5.0, 0.1);
    public final NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", 1.0, 0.1, 3.0, 0.1);

    public ElytraFlyModule() {
        super("ElytraFly", Category.MOVEMENT, false);
        addSetting(speed);
        addSetting(verticalSpeed);
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.isFallFlying()) return;

        double forward = mc.player.input.getMoveVector().y;
        double strafe = mc.player.input.getMoveVector().x;
        double yaw = Math.toRadians(mc.player.getYRot());

        double vx = 0;
        double vz = 0;

        if (forward != 0 || strafe != 0) {
            vx = (forward * -Math.sin(yaw) + strafe * Math.cos(yaw)) * speed.value;
            vz = (forward * Math.cos(yaw) + strafe * Math.sin(yaw)) * speed.value;
        }

        double vy = 0;
        if (mc.options.keyJump.isDown()) {
            vy = verticalSpeed.value;
        } else if (mc.options.keyShift.isDown()) {
            vy = -verticalSpeed.value;
        }

        mc.player.setDeltaMovement(new Vec3(vx, vy, vz));
    }
}