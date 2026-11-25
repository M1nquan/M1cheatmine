package com.m1cheatmine.module.impl.hud;

import com.m1cheatmine.module.HudModule;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.util.ColorUtils;
import com.m1cheatmine.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends HudModule {
    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

    public final BooleanSetting showRightClick = new BooleanSetting("Right Click", true);
    public final BooleanSetting highlight = new BooleanSetting("Glass Highlight", true);

    public CpsModule() {
        super("CPS", 8, 52, 90, 18);
        addSetting(showRightClick);
        addSetting(highlight);
    }

    public static void addLeft() {
        LEFT_CLICKS.addLast(System.currentTimeMillis());
    }

    public static void addRight() {
        RIGHT_CLICKS.addLast(System.currentTimeMillis());
    }

    @Override
    public void render(GuiGraphics g) {
        if (mc.font == null) return;

        long now = System.currentTimeMillis();
        pruneOld(LEFT_CLICKS, now);
        pruneOld(RIGHT_CLICKS, now);

        String text = showRightClick.value
            ? "CPS " + LEFT_CLICKS.size() + " | " + RIGHT_CLICKS.size()
            : "CPS " + LEFT_CLICKS.size();

        width = mc.font.width(text) + 16;
        height = 18;

        RenderUtils.glow(g, x, y, width, height, 5, ColorUtils.AERO_GLOW, 6);
        RenderUtils.roundedGradientV(g, x, y, width, height, 5, 0xD0202C44, 0xD0101A2E);
        RenderUtils.roundedOutline(g, x, y, width, height, 5, 1.2f, ColorUtils.AERO_BORDER);
        if (highlight.value) {
            RenderUtils.glassHighlight(g, x, y, width, height, 5);
        }
        g.drawString(mc.font, text, x + 8, y + 5, ColorUtils.TEXT_BRIGHT, true);
    }

    private static void pruneOld(Deque<Long> queue, long now) {
        while (!queue.isEmpty() && now - queue.peekFirst() > 1000L) {
            queue.removeFirst();
        }
    }
}