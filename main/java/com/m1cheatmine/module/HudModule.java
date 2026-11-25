package com.m1cheatmine.module;

import com.m1cheatmine.util.MathUtils;
import net.minecraft.client.gui.GuiGraphics;

public abstract class HudModule extends Module {
    public int x, y, width, height;
    public HudModule(String n, int x, int y, int w, int h) {
        super(n, Category.HUD, true);
        this.x = x; this.y = y; this.width = w; this.height = h;
    }
    public abstract void render(GuiGraphics g);
    public boolean isHovered(double mx, double my) { return MathUtils.isHovered(mx, my, x, y, width, height); }
}