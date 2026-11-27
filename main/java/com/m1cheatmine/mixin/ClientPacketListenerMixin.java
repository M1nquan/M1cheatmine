package com.m1cheatmine.mixin;

import com.m1cheatmine.module.ModuleManager;
import com.m1cheatmine.module.impl.combat.VelocityModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (packet == null) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || packet.getId() != mc.player.getId()) {
                return;
            }

            VelocityModule velocity = ModuleManager.getModule(VelocityModule.class);
            if (velocity != null && velocity.enabled) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}