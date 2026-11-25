package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class SpeedModule extends Module {
    public final NumberSetting mult = new NumberSetting("Multiplier", 1.5, 1.0, 3.0, 0.1);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", true);

    public SpeedModule() {
        super("Speed", Category.MOVEMENT, false);
        addSetting(mult);
        addSetting(autoJump);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.isInWater() || mc.player.isInLava()) return;

        double forward = mc.player.input.getMoveVector().y;
        double strafe = mc.player.input.getMoveVector().x;

        if (forward == 0 && strafe == 0) return;

        mc.player.setSprinting(true);

        if (autoJump.value && mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        double baseSpeed = 0.2873 * mult.value;
        double yaw = Math.toRadians(mc.player.getYRot());

        // ChuÃ¡ÂºÂ©n hÃƒÂ³a vector hÃ†Â°Ã¡Â»â€ºng di chuyÃ¡Â»Æ’n chÃƒÂ©o
        double mag = Math.hypot(forward, strafe);
        forward /= mag;
        strafe /= mag;

        double vx = (forward * -Math.sin(yaw) + strafe * Math.cos(yaw)) * baseSpeed;
        double vz = (forward * Math.cos(yaw) + strafe * Math.sin(yaw)) * baseSpeed;

        Vec3 currentVel = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(new Vec3(vx, currentVel.y, vz));
    }
}