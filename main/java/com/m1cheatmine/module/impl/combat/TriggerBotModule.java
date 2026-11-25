package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBotModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 50, 0, 500, 10);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    public final BooleanSetting fullCharge = new BooleanSetting("Full Charge", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);
    private long lastAttack = 0;

    public TriggerBotModule() {
        super("TriggerBot", Category.COMBAT, false);
        addSetting(delay);
        addSetting(playersOnly);
        addSetting(fullCharge);
        addSetting(swing);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.hitResult == null || mc.gameMode == null) return;
        if (!(mc.hitResult instanceof EntityHitResult ehr)) return;

        Entity e = ehr.getEntity();
        if (!(e instanceof LivingEntity le) || !le.isAlive()) return;
        if (playersOnly.value && !(e instanceof Player)) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < delay.value) return;
        if (fullCharge.value && mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;

        mc.gameMode.attack(mc.player, e);
        if (swing.value) mc.player.swing(InteractionHand.MAIN_HAND);
        lastAttack = now;
    }
}