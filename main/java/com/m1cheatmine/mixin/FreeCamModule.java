package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class FreeCamModule extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.2, 5.0, 0.1);
    public double camX, camY, camZ;
    public float cameraYaw, cameraPitch;

    public FreeCamModule() {
        super("FreeCam", Category.RENDER, false);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        Vec3 eye = mc.player.getEyePosition();
        camX = eye.x;
        camY = eye.y;
        camZ = eye.z;
        cameraYaw = mc.player.getYRot();
        cameraPitch = mc.player.getXRot();
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        double forward = 0, strafe = 0, vertical = 0;
        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;
        if (mc.options.keyJump.isDown()) vertical += 1;
        if (mc.options.keyShift.isDown()) vertical -= 1;

        double rad = Math.toRadians(cameraYaw);
        double spd = speed.value * 0.5;
        camX += (forward * -Math.sin(rad) + strafe * Math.cos(rad)) * spd;
        camZ += (forward * Math.cos(rad) + strafe * Math.sin(rad)) * spd;
        camY += vertical * spd;

        // Giá»¯ nguyÃªn vá»‹ trÃ­ nhÃ¢n váº­t tháº­t Ä‘á»©ng yÃªn
        mc.player.setDeltaMovement(Vec3.ZERO);
    }
}