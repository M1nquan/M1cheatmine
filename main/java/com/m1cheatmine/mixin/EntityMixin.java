package com.m1cheatmine.mixin;

import com.m1cheatmine.module.ModuleManager;
import com.m1cheatmine.module.impl.combat.HitboxesModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetBoundingBox(CallbackInfoReturnable<AABB> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || self == mc.player) return;

        HitboxesModule hb = ModuleManager.getModule(HitboxesModule.class);
        if (hb == null || !hb.enabled) return;

        double scale = hb.scale.value;
        if (scale <= 1.001) return;

        AABB original = cir.getReturnValue();
        if (original == null) return;

        double expandX = (original.getXsize() * (scale - 1.0)) * 0.5;
        double expandZ = (original.getZsize() * (scale - 1.0)) * 0.5;
        cir.setReturnValue(original.inflate(expandX, 0.0, expandZ));
    }
}