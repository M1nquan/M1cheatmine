package com.m1cheatmine.mixin;

import com.m1cheatmine.module.ModuleManager;
import com.m1cheatmine.module.impl.render.FreelookModule;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(
        method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
        at = @At("TAIL"),
        require = 0
    )
    private void onSetup(BlockGetter area, Entity focusedEntity, boolean thirdPerson,
                         boolean inverseView, float tickDelta, CallbackInfo ci) {
        FreelookModule fl = ModuleManager.getModule(FreelookModule.class);
        if (fl != null && fl.active) {
            setRotation(fl.cameraYaw, fl.cameraPitch);
        }
    }
}