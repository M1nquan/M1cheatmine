package com.m1cheatmine.module.impl.hud;

import com.m1cheatmine.module.HudModule;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.util.ColorUtils;
import com.m1cheatmine.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class WatermarkModule extends HudModule {
    public final BooleanSetting showFps = new BooleanSetting("Show FPS", true);

    public WatermarkModule() {
        super("Watermark", 8, 8, 150, 22);
        addSetting(showFps);
    }

    @Override
    public void render(GuiGraphics g) {
        if (mc.font == null) return;

        String text = showFps.value
            ? "M1CHEATMINE  |  " + mc.getFps() + " FPS"
            : "M1CHEATMINE";

        width = mc.font.width(text) + 22;
        height = 22;

        RenderUtils.glow(g, x, y, width, height, 6, ColorUtils.AERO_GLOW, 8);
        RenderUtils.roundedGradientV(g, x, y, width, height, 6, 0xE0202C44, 0xE0101A2E);
        RenderUtils.roundedOutline(g, x, y, width, height, 6, 1.4f, ColorUtils.AERO_BORDER_HI);
        RenderUtils.glassHighlight(g, x, y, width, height, 6);
        RenderUtils.roundedRect(g, x + 2, y, width - 4, 2, 1, ColorUtils.AERO_ACCENT);
        g.drawString(mc.font, text, x + 11, y + 7, ColorUtils.TEXT_BRIGHT, true);
    }
}