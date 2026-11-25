package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;

public class TimerModule extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", 1.2, 0.1, 5.0, 0.1);

    public TimerModule() {
        super("Timer", Category.PLAYER, false);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null || speed.value == 1.0) return;

        // ThÃ¡Â»Â±c hiÃ¡Â»â€¡n thÃƒÂªm chu kÃ¡Â»Â³ tick bÃ¡Â»â€¢ sung khi speed > 1.0
        if (speed.value > 1.0) {
            int extraTicks = (int) (speed.value - 1.0);
            double fractional = (speed.value - 1.0) - extraTicks;
            if (Math.random() < fractional) extraTicks++;

            for (int i = 0; i < extraTicks; i++) {
                mc.player.aiStep();
            }
        }
    }
}