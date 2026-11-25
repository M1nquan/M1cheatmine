package com.m1cheatmine.mixin;

import com.m1cheatmine.module.impl.render.XrayModule;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void onShouldRenderFace(BlockState state, BlockState adjacentState, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (!XrayModule.isActive()) {
            return;
        }

        if (state == null) {
            cir.setReturnValue(false);
            return;
        }

        if (!XrayModule.isTargetBlock(state)) {
            cir.setReturnValue(false);
            return;
        }

        if (adjacentState == null || adjacentState.isAir()) {
            cir.setReturnValue(true);
            return;
        }

        Block adjacentBlock = adjacentState.getBlock();
        if (adjacentBlock == Blocks.WATER || adjacentBlock == Blocks.LAVA || adjacentBlock == Blocks.GLASS || !adjacentState.canOcclude()) {
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(adjacentBlock != state.getBlock());
    }
}