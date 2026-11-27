package com.m1cheatmine.mixin;

import com.m1cheatmine.module.impl.render.XrayModule;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public class VisGraphMixin {

    @Inject(method = "setOpaque", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSetOpaque(BlockPos pos, CallbackInfo ci) {
        if (!XrayModule.isActive()) {
            return;
        }
        if (pos != null) {
            ci.cancel();
        }
    }
}