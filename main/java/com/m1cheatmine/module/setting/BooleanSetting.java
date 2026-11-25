package com.m1cheatmine.module.setting;
public class BooleanSetting {
    public final String name; public boolean value;
    public BooleanSetting(String n, boolean d) { name = n; value = d; }
    public void toggle() { value = !value; }
}