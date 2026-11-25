package com.m1cheatmine.module.setting;

import com.m1cheatmine.util.MathUtils;

public class NumberSetting {
    public final String name;
    public double value, min, max, increment;
    public NumberSetting(String n, double d, double mn, double mx, double inc) { name = n; value = d; min = mn; max = mx; increment = inc; }
    public void setValue(double v) { double c = MathUtils.clamp(v, min, max); double p = 1.0 / increment; value = Math.round(c * p) / p; }
    public float ratio() { return (float)((value - min) / (max - min)); }
}