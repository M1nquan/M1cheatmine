package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import com.m1cheatmine.util.MathUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class KillAuraModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 3.8, 3.0, 6.0, 0.1);
    public final NumberSetting cps = new NumberSetting("CPS", 10, 1, 20, 1);
    public final NumberSetting rotSpeed = new NumberSetting("Rot Speed", 180, 10, 360, 5);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    public final BooleanSetting mobsOnly = new BooleanSetting("Mobs Only", false);
    public final BooleanSetting throughWalls = new BooleanSetting("Through Walls", false);
    public final BooleanSetting fullCharge = new BooleanSetting("Full Charge", true);
    public final BooleanSetting rotate = new BooleanSetting("Rotate", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);
    private long lastAttack = 0;

    public KillAuraModule() {
        super("KillAura", Category.COMBAT, false);
        addSetting(range);
        addSetting(cps);
        addSetting(rotSpeed);
        addSetting(playersOnly);
        addSetting(mobsOnly);
        addSetting(throughWalls);
        addSetting(fullCharge);
        addSetting(rotate);
        addSetting(swing);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        Entity target = findTarget();
        if (target == null) return;

        if (rotate.value) face(target, (float) rotSpeed.value);

        long now = System.currentTimeMillis();
        if (now - lastAttack < (1000.0 / cps.value)) return;
        if (fullCharge.value && mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;

        mc.gameMode.attack(mc.player, target);
        if (swing.value) mc.player.swing(InteractionHand.MAIN_HAND);
        lastAttack = now;
    }

    private Entity findTarget() {
        Entity best = null;
        double bestDist = range.value;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (playersOnly.value && !(e instanceof Player)) continue;
            if (mobsOnly.value && !(e instanceof Monster)) continue;
            if (!throughWalls.value && !mc.player.hasLineOfSight(e)) continue;

            double d = mc.player.distanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    private void face(Entity t, float spd) {
        double dx = t.getX() - mc.player.getX();
        double dy = (t.getY() + t.getEyeHeight() * 0.5) - (mc.player.getY() + mc.player.getEyeHeight());
        double dz = t.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float ty = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float tp = (float) (-Math.toDegrees(Math.atan2(dy, dist)));
        float yd = ((ty - mc.player.getYRot() + 540) % 360) - 180;
        float pd = tp - mc.player.getXRot();
        float ys = Math.min(Math.abs(yd), spd);
        float ps = Math.min(Math.abs(pd), spd);
        mc.player.setYRot(mc.player.getYRot() + Math.signum(yd) * ys);
        mc.player.setXRot(MathUtils.clamp(mc.player.getXRot() + Math.signum(pd) * ps, -90, 90));
    }
}