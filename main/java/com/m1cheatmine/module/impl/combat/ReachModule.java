package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;

public class ReachModule extends Module {
    public final NumberSetting reach = new NumberSetting("Reach", 4.5, 3.0, 6.0, 0.1);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", false);

    public ReachModule() {
        super("Reach", Category.COMBAT, false);
        addSetting(reach);
        addSetting(playersOnly);
    }
}