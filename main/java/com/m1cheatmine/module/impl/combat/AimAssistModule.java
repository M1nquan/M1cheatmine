package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.*;
import com.m1cheatmine.util.MathUtils;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;

public class AimAssistModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.5, 1.0, 8.0, 0.1);
    public final NumberSetting fov = new NumberSetting("FOV", 90, 10, 180, 5);
    public final NumberSetting speed = new NumberSetting("Speed", 5, 1, 30, 1);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);

    public AimAssistModule() {
        super("AimAssist", Category.COMBAT, false);
        addSetting(range); addSetting(fov); addSetting(speed); addSetting(playersOnly);
    }

    @Override public void onTick() {
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        Entity target = null; double bd = range.value;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (playersOnly.value && !(e instanceof Player)) continue;
            double d = mc.player.distanceTo(e);
            if (d > bd) continue;
            if (!inFov(e)) continue;
            bd = d; target = e;
        }
        if (target == null) return;

        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getEyeHeight()*0.5) - (mc.player.getY() + mc.player.getEyeHeight());
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx*dx + dz*dz);
        float ty = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float tp = (float)(-Math.toDegrees(Math.atan2(dy, dist)));

        float yd = ((ty - mc.player.getYRot() + 540) % 360) - 180;
        float pd = tp - mc.player.getXRot();

        float ys = MathUtils.clamp(yd * (float)(speed.value / 100.0), -3f, 3f);
        float ps = MathUtils.clamp(pd * (float)(speed.value / 100.0), -3f, 3f);

        mc.player.setYRot(mc.player.getYRot() + ys);
        mc.player.setXRot(MathUtils.clamp(mc.player.getXRot() + ps, -90, 90));
    }

    private boolean inFov(Entity e) {
        double dx = e.getX() - mc.player.getX();
        double dz = e.getZ() - mc.player.getZ();
        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float diff = Math.abs(((targetYaw - mc.player.getYRot() + 540) % 360) - 180);
        return diff <= fov.value / 2f;
    }
}