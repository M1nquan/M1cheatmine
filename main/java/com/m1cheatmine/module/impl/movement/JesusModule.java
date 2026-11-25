package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class JesusModule extends Module {
    public JesusModule() {
        super("Jesus", Category.MOVEMENT, false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isShiftKeyDown()) return; // Cho phÃƒÂ©p lÃ¡ÂºÂ·n khi nhÃ¡ÂºÂ¥n Shift

        Vec3 vel = mc.player.getDeltaMovement();

        if (mc.player.isInWater() || mc.player.isInLava()) {
            mc.player.setDeltaMovement(vel.x, 0.11, vel.z);
            mc.player.fallDistance = 0f;
            return;
        }

        BlockPos below = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        boolean liquidBelow = !mc.level.getFluidState(below).isEmpty();

        if (liquidBelow && vel.y < 0) {
            double newY = mc.options.keyJump.isDown() ? 0.42 : 0.0;
            mc.player.setDeltaMovement(vel.x, newY, vel.z);
            mc.player.setOnGround(true);
            mc.player.fallDistance = 0f;
        }
    }
}