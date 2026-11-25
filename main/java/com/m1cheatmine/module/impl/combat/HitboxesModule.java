package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;

public class HitboxesModule extends Module {
    public final NumberSetting scale = new NumberSetting("Scale", 1.5, 1.0, 3.0, 0.1);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    public final BooleanSetting expandHeight = new BooleanSetting("Expand Height", true);

    public HitboxesModule() {
        super("Hitboxes", Category.COMBAT, false);
        addSetting(scale);
        addSetting(playersOnly);
        addSetting(expandHeight);
    }
}