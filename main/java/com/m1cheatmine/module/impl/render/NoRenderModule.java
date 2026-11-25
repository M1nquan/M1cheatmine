package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import net.minecraft.world.effect.MobEffects;

public class NoRenderModule extends Module {
    public final BooleanSetting fire = new BooleanSetting("Fire", true);
    public final BooleanSetting nausea = new BooleanSetting("Nausea", true);
    public final BooleanSetting blindness = new BooleanSetting("Blindness", true);

    public NoRenderModule() {
        super("NoRender", Category.RENDER, false);
        addSetting(fire);
        addSetting(nausea);
        addSetting(blindness);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (fire.value && mc.player.isOnFire()) {
            mc.player.clearFire();
        }
        if (nausea.value && mc.player.hasEffect(MobEffects.NAUSEA)) {
            mc.player.removeEffect(MobEffects.NAUSEA);
        }
        if (blindness.value) {
            if (mc.player.hasEffect(MobEffects.BLINDNESS)) {
                mc.player.removeEffect(MobEffects.BLINDNESS);
            }
            if (mc.player.hasEffect(MobEffects.DARKNESS)) {
                mc.player.removeEffect(MobEffects.DARKNESS);
            }
        }
    }
}