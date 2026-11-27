package com.m1cheatmine.mixin;

import com.m1cheatmine.module.ModuleManager;
import com.m1cheatmine.module.impl.render.*;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"), require = 0)
    private void onRenderLevel(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker,
                               boolean renderBlockOutline, Camera camera, Matrix4f frustumMatrix,
                               Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix,
                               GpuBufferSlice fogBuffer, Vector4f fogColor, boolean shouldRenderSky,
                               CallbackInfo ci) {
        if (camera == null || frustumMatrix == null) return;
        try {
            PoseStack ps = new PoseStack();
            ps.mulPose(frustumMatrix);

            EspModule esp = ModuleManager.getModule(EspModule.class);
            if (esp != null && esp.enabled) esp.onRenderWorld(ps, camera, deltaTracker);

            TracersModule tr = ModuleManager.getModule(TracersModule.class);
            if (tr != null && tr.enabled) tr.onRenderWorld(ps, camera, deltaTracker);

            HoleEspModule hole = ModuleManager.getModule(HoleEspModule.class);
            if (hole != null && hole.enabled) hole.onRenderWorld(ps, camera);

            StorageEspModule storage = ModuleManager.getModule(StorageEspModule.class);
            if (storage != null && storage.enabled) storage.onRenderWorld(ps, camera);
        } catch (Throwable ignored) {
        }
    }
}