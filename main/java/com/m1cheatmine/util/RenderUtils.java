package com.m1cheatmine.util;

import net.minecraft.client.gui.GuiGraphics;

public class RenderUtils {

    public static void rect(GuiGraphics g, float x, float y, float w, float h, int c) {
        if (w <= 0 || h <= 0) return;
        g.fill((int) x, (int) y, (int) (x + w), (int) (y + h), c);
    }

    public static void gradientV(GuiGraphics g, float x, float y, float w, float h, int t, int b) {
        if (w <= 0 || h <= 0) return;
        g.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h), t, b);
    }

    public static void roundedRect(GuiGraphics g, float x, float y, float w, float h, float r, int color) {
        roundedGradientV(g, x, y, w, h, r, color, color);
    }

    public static void roundedGradientV(GuiGraphics g, float x, float y, float w, float h, float r, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        int ix = (int) x, iy = (int) y, iw = (int) w, ih = (int) h;
        int ir = Math.min((int) r, Math.min(iw, ih) / 2);

        if (ir <= 1) {
            g.fillGradient(ix, iy, ix + iw, iy + ih, top, bottom);
            return;
        }

        // Pháº§n thÃ¢n chÃ­nh giá»¯a
        g.fillGradient(ix, iy + ir, ix + iw, iy + ih - ir,
                ColorUtils.interpolate(top, bottom, (float) ir / ih),
                ColorUtils.interpolate(top, bottom, (float) (ih - ir) / ih));

        // Cáº¯t bo trÃ²n 2 Ä‘áº§u trÃªn vÃ  dÆ°á»›i
        for (int i = 0; i < ir; i++) {
            double dy = ir - i - 0.5;
            int inset = (int) Math.round(ir - Math.sqrt(Math.max(0, ir * ir - dy * dy)));
            int topCol = ColorUtils.interpolate(top, bottom, (float) i / ih);
            int botCol = ColorUtils.interpolate(top, bottom, (float) (ih - i - 1) / ih);
            g.fill(ix + inset, iy + i, ix + iw - inset, iy + i + 1, topCol);
            g.fill(ix + inset, iy + ih - i - 1, ix + iw - inset, iy + ih - i, botCol);
        }
    }

    public static void roundedOutline(GuiGraphics g, float x, float y, float w, float h, float r, float lw, int color) {
        if (w <= 0 || h <= 0) return;
        int ix = (int) x, iy = (int) y, iw = (int) w, ih = (int) h;
        int t = Math.max(1, (int) lw);
        int ir = Math.min((int) r, Math.min(iw, ih) / 2);

        g.fill(ix + ir, iy, ix + iw - ir, iy + t, color);
        g.fill(ix + ir, iy + ih - t, ix + iw - ir, iy + ih, color);
        g.fill(ix, iy + ir, ix + t, iy + ih - ir, color);
        g.fill(ix + iw - t, iy + ir, ix + iw, iy + ih - ir, color);

        for (int i = 0; i < ir; i++) {
            double dy = ir - i - 0.5;
            int inset = (int) Math.round(ir - Math.sqrt(Math.max(0, ir * ir - dy * dy)));
            g.fill(ix + inset, iy + i, ix + inset + t, iy + i + 1, color);
            g.fill(ix + iw - inset - t, iy + i, ix + iw - inset, iy + i + 1, color);
            g.fill(ix + inset, iy + ih - i - 1, ix + inset + t, iy + ih - i, color);
            g.fill(ix + iw - inset - t, iy + ih - i - 1, ix + iw - inset, iy + ih - i, color);
        }
    }

    public static void circle(GuiGraphics g, float cx, float cy, float r, int color) {
        if (r <= 0) return;
        int ir = Math.max(1, Math.round(r));
        int icx = Math.round(cx), icy = Math.round(cy);
        for (int dy = -ir; dy <= ir; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            g.fill(icx - dx, icy + dy, icx + dx + 1, icy + dy + 1, color);
        }
    }

    public static void shadow(GuiGraphics g, float x, float y, float w, float h, float r, int spread) {
        if (spread <= 0) return;
        int layers = Math.min(spread, 5);
        for (int i = layers; i >= 1; i--) {
            float ratio = (float) i / layers;
            float offset = spread * ratio;
            int alpha = (int) (Math.pow(1f - ratio, 1.6) * 42);
            if (alpha > 0) {
                roundedRect(g, x - offset, y - offset, w + offset * 2, h + offset * 2, r + offset, (alpha << 24));
            }
        }
    }

    public static void glow(GuiGraphics g, float x, float y, float w, float h, float r, int color, int spread) {
        if (spread <= 0) return;
        int layers = Math.min(spread, 5);
        int baseAlpha = (color >> 24) & 0xFF;
        if (baseAlpha == 0) baseAlpha = 85;
        for (int i = layers; i >= 1; i--) {
            float ratio = (float) i / layers;
            float offset = spread * ratio;
            int alpha = (int) (Math.pow(1f - ratio, 1.6) * (baseAlpha * 0.6f));
            if (alpha > 0) {
                roundedRect(g, x - offset, y - offset, w + offset * 2, h + offset * 2, r + offset, ColorUtils.withAlpha(color, alpha));
            }
        }
    }

    public static void glassHighlight(GuiGraphics g, float x, float y, float w, float h, float r) {
        roundedGradientV(g, x, y, w, h * 0.35f, r, 0x26FFFFFF, 0x00FFFFFF);
        if (w > 12) {
            rect(g, x + 6, y + 1, w - 12, 1, 0x70FFFFFF);
        }
    }
}