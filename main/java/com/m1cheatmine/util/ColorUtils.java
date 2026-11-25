package com.m1cheatmine.util;

import java.awt.Color;

public class ColorUtils {
    public static final int AERO_BG        = 0xC8101A2E;
    public static final int AERO_BG_SIDE   = 0xD80B1220;
    public static final int AERO_CARD      = 0xB81A2438;
    public static final int AERO_CARD_HI   = 0xE0202C44;
    public static final int AERO_CARD_LO   = 0xB0101828;
    public static final int AERO_BORDER    = 0x60A8C8E8;
    public static final int AERO_BORDER_HI = 0xC0D0E8FF;
    public static final int AERO_GLOW      = 0x5060A0FF;
    public static final int AERO_ACCENT    = 0xFF5FA8FF;
    public static final int AERO_ACCENT2   = 0xFF8AB4F8;
    public static final int AERO_HIGHLIGHT = 0xFFFFFFFF;
    public static final int TEXT_BRIGHT    = 0xFFFFFFFF;
    public static final int TEXT_SOFT      = 0xFFD8E4F8;
    public static final int TEXT_DIM       = 0xB0C8D8E8;
    public static final int TEXT_MUTED     = 0x8098A8C0;
    public static final int SUCCESS        = 0xFF34D399;
    public static final int WARNING        = 0xFFFBBF24;
    public static final int DANGER         = 0xFFEF4444;

    public static int withAlpha(int c, int a) {
        a = Math.max(0, Math.min(255, a));
        return (c & 0x00FFFFFF) | (a << 24);
    }

    public static int scaleAlpha(int c, float factor) {
        int a = (c >> 24) & 0xFF;
        return withAlpha(c, Math.round(a * MathUtils.clamp(factor, 0f, 1f)));
    }

    public static int interpolate(int c1, int c2, float f) {
        if (c1 == c2 || f <= 0f) return c1;
        if (f >= 1f) return c2;
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * f);
        int r = (int) (r1 + (r2 - r1) * f);
        int g = (int) (g1 + (g2 - g1) * f);
        int b = (int) (b1 + (b2 - b1) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rainbow(float sec, float sat, float bri, long off) {
        long periodMs = Math.max(1L, (long) (sec * 1000f));
        float hue = ((System.currentTimeMillis() + off) % periodMs) / (float) periodMs;
        return Color.HSBtoRGB(hue, MathUtils.clamp(sat, 0f, 1f), MathUtils.clamp(bri, 0f, 1f));
    }
}