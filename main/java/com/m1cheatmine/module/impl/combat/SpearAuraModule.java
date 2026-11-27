package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SpearAuraModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.5, 2.0, 7.0, 0.1);
    public final NumberSetting delay = new NumberSetting("Delay", 140, 0, 1000, 10);
    public final BooleanSetting smartCooldown = new BooleanSetting("SmartCooldown", true);
    public final BooleanSetting aimTarget = new BooleanSetting("AimTarget", true);
    public final BooleanSetting ignoreInvisible = new BooleanSetting("IgnoreInvisible", true);
    public final BooleanSetting playersOnly = new BooleanSetting("PlayersOnly", true);
    public final BooleanSetting autoEquip = new BooleanSetting("AutoEquip", true);
    public final BooleanSetting swapBack = new BooleanSetting("SwapBack", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);

    private long lastAttack = 0;

    public SpearAuraModule() {
        super("SpearAura", Category.COMBAT, false);
        addSetting(range);
        addSetting(delay);
        addSetting(smartCooldown);
        addSetting(aimTarget);
        addSetting(ignoreInvisible);
        addSetting(playersOnly);
        addSetting(autoEquip);
        addSetting(swapBack);
        addSetting(swing);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.screen != null) return;

        long now = System.currentTimeMillis();
        if (!smartCooldown.value && now - lastAttack < delay.value) return;
        if (smartCooldown.value && (mc.player.getAttackStrengthScale(0.5f) < 0.92f || now - lastAttack < Math.min(delay.value, 80))) {
            return;
        }

        LivingEntity target = findTarget();
        if (target == null) return;

        int previousSlot = getSelectedSlot();
        int spearSlot = findSpearSlot();
        boolean swapped = false;

        if (autoEquip.value && !isSpearStack(mc.player.getMainHandItem()) && spearSlot != -1) {
            setSelectedSlot(spearSlot);
            swapped = true;
        }

        if (!isSpearStack(mc.player.getMainHandItem())) {
            if (swapped && swapBack.value) setSelectedSlot(previousSlot);
            return;
        }

        if (aimTarget.value) {
            lookAt(target);
        }

        mc.gameMode.attack(mc.player, target);
        if (swing.value) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        lastAttack = now;

        if (swapped && swapBack.value && previousSlot >= 0 && previousSlot < 9) {
            setSelectedSlot(previousSlot);
        }
    }

    private LivingEntity findTarget() {
        double maxDistSq = range.value * range.value;
        List<LivingEntity> candidates = mc.level.getEntitiesOfClass(
                LivingEntity.class,
                mc.player.getBoundingBox().inflate(range.value + 0.5)
        );

        return candidates.stream()
                .filter(e -> e != null && e != mc.player && e.isAlive() && !e.isRemoved())
                .filter(e -> !ignoreInvisible.value || !e.isInvisible())
                .filter(e -> playersOnly.value ? (e instanceof Player) : (e instanceof Player || e instanceof Monster))
                .filter(e -> mc.player.distanceToSqr(e) <= maxDistSq)
                .min(Comparator.comparingDouble(e -> mc.player.distanceToSqr(e)))
                .orElse(null);
    }

    private void lookAt(LivingEntity target) {
        Vec3 eyes = mc.player.getEyePosition(1.0f);
        Vec3 aimPoint = target.getBoundingBox().getCenter();
        double dx = aimPoint.x - eyes.x;
        double dy = aimPoint.y - eyes.y;
        double dz = aimPoint.z - eyes.z;
        double horiz = Math.hypot(dx, dz);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, horiz)));

        float newYaw = mc.player.getYRot() + Mth.wrapDegrees(targetYaw - mc.player.getYRot()) * 0.65f;
        float newPitch = Mth.clamp(mc.player.getXRot() + (targetPitch - mc.player.getXRot()) * 0.65f, -90.0f, 90.0f);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }

    private int findSpearSlot() {
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!isSpearStack(stack)) continue;
            int score = stack.is(Items.TRIDENT) ? 2 : 1;
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private boolean isSpearStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(Items.TRIDENT)) return true;
        Item item = stack.getItem();
        String name = item.toString().toLowerCase(Locale.ROOT);
        return name.contains("spear") || name.contains("halberd") || name.contains("lance") || name.contains("pike");
    }

    private int getSelectedSlot() {
        Object inv = mc.player.getInventory();
        try {
            Method m = inv.getClass().getMethod("getSelectedSlot");
            return (int) m.invoke(inv);
        } catch (Throwable ignored) {
        }
        try {
            Field f = inv.getClass().getField("selected");
            return f.getInt(inv);
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private void setSelectedSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        Object inv = mc.player.getInventory();
        try {
            Method m = inv.getClass().getMethod("setSelectedSlot", int.class);
            m.invoke(inv, slot);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Field f = inv.getClass().getField("selected");
            f.setInt(inv, slot);
        } catch (Throwable ignored) {
        }
    }
}