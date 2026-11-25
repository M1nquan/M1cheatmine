package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class NametagsModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 64.0, 8.0, 128.0, 4.0);
    public final BooleanSetting showHealth = new BooleanSetting("Show Health", true);
    public final BooleanSetting showDistance = new BooleanSetting("Show Distance", true);

    public NametagsModule() {
        super("Nametags", Category.RENDER, false);
        addSetting(range);
        addSetting(showHealth);
        addSetting(showDistance);
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        double maxDistSq = range.value * range.value;
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;

            if (p.distanceToSqr(mc.player) <= maxDistSq) {
                p.setCustomNameVisible(true);
                if (showHealth.value || showDistance.value) {
                    p.setCustomName(buildTag(p));
                }
            } else {
                p.setCustomNameVisible(false);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.level == null) return;
        for (Player p : mc.level.players()) {
            if (p != mc.player) {
                p.setCustomNameVisible(false);
                p.setCustomName(null);
            }
        }
    }

    private Component buildTag(Player p) {
        StringBuilder sb = new StringBuilder(p.getName().getString());
        if (showHealth.value) {
            int hp = Math.round(p.getHealth() + p.getAbsorptionAmount());
            sb.append(" [").append(hp).append(" HP]");
        }
        if (showDistance.value) {
            int dist = Math.round(mc.player.distanceTo(p));
            sb.append(" [").append(dist).append("m]");
        }
        return Component.literal(sb.toString());
    }
}