package com.m1cheatmine.module.impl.misc;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class PacketFlyModule extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
    public final NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", 0.6, 0.1, 3.0, 0.1);
    public final BooleanSetting antiKick = new BooleanSetting("AntiKick", true);

    private int ticks = 0;

    public PacketFlyModule() {
        super("PacketFly", Category.MISC, false);
        addSetting(speed);
        addSetting(verticalSpeed);
        addSetting(antiKick);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) return;

        mc.player.setNoGravity(true);
        ticks++;

        float forward = mc.player.input.getMoveVector().y;
        float strafe = mc.player.input.getMoveVector().x;
        double yaw = Math.toRadians(mc.player.getYRot());

        double vx = 0, vz = 0;
        if (forward != 0 || strafe != 0) {
            double mag = Math.hypot(forward, strafe);
            forward /= (float) mag;
            strafe /= (float) mag;
            vx = (forward * -Math.sin(yaw) + strafe * Math.cos(yaw)) * speed.value * 0.28;
            vz = (forward * Math.cos(yaw) + strafe * Math.sin(yaw)) * speed.value * 0.28;
        }

        double vy = 0;
        if (mc.options.keyJump.isDown()) {
            vy = verticalSpeed.value * 0.25;
        } else if (mc.options.keyShift.isDown()) {
            vy = -verticalSpeed.value * 0.25;
        } else if (antiKick.value && ticks % 20 == 0) {
            vy = -0.04;
        }

        mc.player.setDeltaMovement(new Vec3(vx, vy, vz));

        double nextX = mc.player.getX() + vx;
        double nextY = mc.player.getY() + vy;
        double nextZ = mc.player.getZ() + vz;

        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(nextX, nextY, nextZ, false, mc.player.horizontalCollision));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(nextX, nextY + 1337.0, nextZ, false, false));
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setNoGravity(false);
            mc.player.setDeltaMovement(Vec3.ZERO);
        }
    }
}