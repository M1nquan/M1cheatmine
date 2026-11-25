package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NukerModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.5);
    public final BooleanSetting flatten = new BooleanSetting("Flatten", true);

    public NukerModule() {
        super("Nuker", Category.PLAYER, false);
        addSetting(range);
        addSetting(flatten);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        int r = (int) Math.ceil(range.value);
        BlockPos playerPos = mc.player.blockPosition();

        for (int x = -r; x <= r; x++) {
            for (int y = r; y >= -r; y--) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (flatten.value && pos.getY() < playerPos.getY()) continue;
                    if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > range.value * range.value) continue;

                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.BEDROCK) || !state.getFluidState().isEmpty()) continue;

                    mc.gameMode.continueDestroyBlock(pos, Direction.UP);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    return;
                }
            }
        }
    }
}