package com.m1cheatmine.module.setting;

import com.m1cheatmine.util.MathUtils;

public class NumberSetting {
    public final String name;
    public double value, min, max, increment;

    public NumberSetting(String n, double d, double mn, double mx, double inc) {
        name = n;
        min = mn;
        max = mx;
        increment = inc > 0 ? inc : 0.1;
        setValue(d);
    }

    public void setValue(double v) {
        double c = MathUtils.clamp(v, min, max);
        double snapped = Math.round(c / increment) * increment;
        value = MathUtils.clamp(snapped, min, max);
    }

    public float ratio() {
        if (max <= min) return 0f;
        return MathUtils.clamp((float) ((value - min) / (max - min)), 0f, 1f);
    }
}