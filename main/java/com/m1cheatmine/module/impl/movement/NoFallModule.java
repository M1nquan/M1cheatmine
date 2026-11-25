package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFallModule extends Module {
    public NoFallModule() {
        super("NoFall", Category.MOVEMENT, false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) return;
        if (mc.player.fallDistance > 2.5f) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
            mc.player.fallDistance = 0f;
        }
    }
}