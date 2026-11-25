package com.m1cheatmine.mixin;

import com.m1cheatmine.module.ModuleManager;
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
    private static void onFace(BlockState state, BlockState adjacentState, Direction face, CallbackInfoReturnable<Boolean> cir) {
        XrayModule xray = ModuleManager.getModule(XrayModule.class);
        if (xray != null && xray.enabled) {
            Block b = state.getBlock();
            cir.setReturnValue(
                b == Blocks.DIAMOND_ORE ||
                b == Blocks.DEEPSLATE_DIAMOND_ORE ||
                b == Blocks.GOLD_ORE ||
                b == Blocks.DEEPSLATE_GOLD_ORE ||
                b == Blocks.IRON_ORE ||
                b == Blocks.DEEPSLATE_IRON_ORE ||
                b == Blocks.ANCIENT_DEBRIS ||
                b == Blocks.EMERALD_ORE ||
                b == Blocks.DEEPSLATE_EMERALD_ORE
            );
        }
    }
}