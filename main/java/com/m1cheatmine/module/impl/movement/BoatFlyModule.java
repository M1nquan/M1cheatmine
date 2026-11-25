package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public class BoatFlyModule extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", 1.5, 0.1, 5.0, 0.1);
    public final NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", 0.8, 0.1, 3.0, 0.1);
    public final BooleanSetting antiGravity = new BooleanSetting("AntiGravity", true);

    public BoatFlyModule() {
        super("BoatFly", Category.MOVEMENT, false);
        addSetting(speed);
        addSetting(verticalSpeed);
        addSetting(antiGravity);
    }

    @Override
    public void onTick() {
        if (mc.player == null || !(mc.player.getVehicle() instanceof Boat boat)) return;

        boat.setYRot(mc.player.getYRot());
        double forward = mc.player.input.getMoveVector().y;
        double strafe = mc.player.input.getMoveVector().x;
        float yaw = mc.player.getYRot();

        double motionX = 0;
        double motionZ = 0;

        if (forward != 0 || strafe != 0) {
            double rad = Math.toRadians(yaw);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            motionX = (forward * -sin + strafe * cos) * speed.value * 0.5;
            motionZ = (forward * cos + strafe * sin) * speed.value * 0.5;
        }

        double motionY = antiGravity.value ? 0.0 : boat.getDeltaMovement().y;
        if (mc.options.keyJump.isDown()) {
            motionY = verticalSpeed.value * 0.5;
        } else if (mc.options.keySprint.isDown()) {
            motionY = -verticalSpeed.value * 0.5;
        }

        boat.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
    }
}