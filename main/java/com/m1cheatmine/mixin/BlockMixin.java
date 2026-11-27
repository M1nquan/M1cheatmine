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

    @Inject(
        method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void onShouldRenderFace(BlockState state, BlockState adjacentState, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (!XrayModule.isActive()) {
            return;
        }
        cir.setReturnValue(shouldRenderXrayFace(state, adjacentState));
    }

    private static boolean shouldRenderXrayFace(BlockState state, BlockState adjacentState) {
        if (state == null || !XrayModule.isTargetBlock(state)) {
            return false;
        }
        if (adjacentState == null || adjacentState.isAir()) {
            return true;
        }
        Block adjacentBlock = adjacentState.getBlock();
        if (adjacentBlock == Blocks.WATER || adjacentBlock == Blocks.LAVA || adjacentBlock == Blocks.GLASS || !adjacentState.canOcclude()) {
            return true;
        }
        return adjacentBlock != state.getBlock();
    }
}