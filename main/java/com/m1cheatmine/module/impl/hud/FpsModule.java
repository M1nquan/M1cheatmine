package com.m1cheatmine.module.impl.hud;

import com.m1cheatmine.module.HudModule;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.util.ColorUtils;
import com.m1cheatmine.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class FpsModule extends HudModule {
    public final BooleanSetting highlight = new BooleanSetting("Glass Highlight", true);

    public FpsModule() {
        super("FPS", 8, 30, 65, 18);
        addSetting(highlight);
    }

    @Override
    public void render(GuiGraphics g) {
        if (mc.font == null) return;

        String text = "FPS " + mc.getFps();
        width = mc.font.width(text) + 16;
        height = 18;

        RenderUtils.glow(g, x, y, width, height, 5, ColorUtils.AERO_GLOW, 6);
        RenderUtils.roundedGradientV(g, x, y, width, height, 5, 0xD0202C44, 0xD0101A2E);
        RenderUtils.roundedOutline(g, x, y, width, height, 5, 1.2f, ColorUtils.AERO_BORDER);
        if (highlight.value) {
            RenderUtils.glassHighlight(g, x, y, width, height, 5);
        }
        RenderUtils.roundedRect(g, x + 3, y, width - 6, 1.5f, 0.75f, ColorUtils.AERO_ACCENT);
        g.drawString(mc.font, text, x + 8, y + 5, ColorUtils.TEXT_BRIGHT, true);
    }
}