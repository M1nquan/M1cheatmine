package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoMineModule extends Module {
    public AutoMineModule() {
        super("AutoMine", Category.PLAYER, false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null) return;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) mc.hitResult;
            if (!mc.level.getBlockState(bhr.getBlockPos()).isAir()) {
                mc.options.keyAttack.setDown(true);
                return;
            }
        }
        mc.options.keyAttack.setDown(false);
    }

    @Override
    public void onDisable() {
        mc.options.keyAttack.setDown(false);
    }
}