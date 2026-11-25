package com.m1cheatmine.module;

import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();
    public final String name;
    public final Category category;
    public boolean enabled;
    public int keyCode = -1;
    public float toggleAnim = 0f;
    public float hoverAnim = 0f;
    public final List<NumberSetting> numberSettings = new ArrayList<>();
    public final List<BooleanSetting> booleanSettings = new ArrayList<>();

    public Module(String n, Category c, boolean d) {
        this.name = n;
        this.category = c;
        this.enabled = d;
        this.toggleAnim = d ? 1f : 0f;
    }

    public void addSetting(NumberSetting s) { numberSettings.add(s); }
    public void addSetting(BooleanSetting s) { booleanSettings.add(s); }
    public void toggle() { enabled = !enabled; if (enabled) onEnable(); else onDisable(); }
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}