package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;

public class FullbrightModule extends Module {
    public final NumberSetting brightness = new NumberSetting("Gamma", 15.0, 1.0, 15.0, 0.5);
    private double previousGamma = 1.0;
    private boolean hasSavedGamma = false;

    public FullbrightModule() {
        super("Fullbright", Category.RENDER, false);
        addSetting(brightness);
    }

    @Override
    public void onEnable() {
        if (mc.options == null) return;
        if (!hasSavedGamma) {
            previousGamma = mc.options.gamma().get();
            hasSavedGamma = true;
        }
        mc.options.gamma().set(brightness.value);
    }

    @Override
    public void onTick() {
        if (mc.options == null || !enabled) return;
        if (mc.options.gamma().get() != brightness.value) {
            mc.options.gamma().set(brightness.value);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && hasSavedGamma) {
            mc.options.gamma().set(previousGamma);
            hasSavedGamma = false;
        }
    }
}