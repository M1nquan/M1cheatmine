package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;

public class AutoLogModule extends Module {
    public final NumberSetting health = new NumberSetting("Health", 6.0, 1.0, 20.0, 0.5);
    public final BooleanSetting onlyPlayers = new BooleanSetting("Only Players Nearby", true);

    public AutoLogModule() {
        super("AutoLog", Category.COMBAT, false);
        addSetting(health); addSetting(onlyPlayers);
    }

    @Override public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.getHealth() > health.value) return;
        if (onlyPlayers.value) {
            boolean near = false;
            for (var p : mc.level.players()) {
                if (p != mc.player && p.distanceTo(mc.player) < 12) { near = true; break; }
            }
            if (!near) return;
        }
        mc.player.connection.getConnection().disconnect(net.minecraft.network.chat.Component.literal("[AutoLog] Health low"));
    }
}