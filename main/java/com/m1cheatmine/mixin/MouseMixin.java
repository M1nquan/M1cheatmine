package com.m1cheatmine.mixin;

import com.m1cheatmine.module.impl.hud.CpsModule;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void onClick(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            if (info.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) CpsModule.addLeft();
            if (info.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) CpsModule.addRight();
        }
    }
}