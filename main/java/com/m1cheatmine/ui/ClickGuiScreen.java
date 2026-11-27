package com.m1cheatmine.ui;

import com.m1cheatmine.M1CheatMine;
import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.*;
import com.m1cheatmine.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    private enum ColorMode {
        AERO_BLUE("Aero Blue", ColorUtils.AERO_ACCENT, ColorUtils.AERO_ACCENT2, ColorUtils.AERO_GLOW),
        CYBER_CYAN("Cyan", 0xFF00E5FF, 0xFF70F3FF, 0x3800E5FF),
        NEON_ROSE("Rose", 0xFFFF4D8D, 0xFFFF9EBB, 0x38FF4D8D),
        EMERALD("Emerald", 0xFF10E89A, 0xFF72F7C6, 0x3810E89A),
        SOLAR_GOLD("Gold", 0xFFFFB020, 0xFFFFD470, 0x38FFB020),
        RAINBOW("Rainbow", 0xFF5FA8FF, 0xFFA8D0FF, 0x385FA8FF);

        final String label;
        final int primary;
        final int secondary;
        final int glow;

        ColorMode(String label, int primary, int secondary, int glow) {
            this.label = label;
            this.primary = primary;
            this.secondary = secondary;
            this.glow = glow;
        }
    }

    private final Minecraft mc = Minecraft.getInstance();
    private static int winX = -1, winY = -1;
    private static final int BASE_W = 360, BASE_H = 236;
    private static int winW = BASE_W, winH = BASE_H;
    private static final int SIDEBAR_W = 96;
    private static final float HUD_SCALE = M1CheatMine.HUD_SCALE;
    private static final float UI_TEXT_SCALE = 0.92f;
    private static Category selected = Category.COMBAT;
    private static ColorMode colorMode = ColorMode.AERO_BLUE;
    private static final Set<Module> collapsedModules = new HashSet<>();
    private static final Map<Module, Integer> moduleKeys = new IdentityHashMap<>();

    private float openAnim = 0f, tabIndicatorY = 0f;
    private boolean draggingWin = false;
    private int dragOX, dragOY;

    private HudModule draggingHud = null;
    private int hudOX, hudOY;

    private NumberSetting draggingSlider = null;
    private float sliderX, sliderW;

    private Module bindingModule = null;

    private double scroll = 0, targetScroll = 0, maxScroll = 0;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private long lastFrameTime = System.nanoTime();

    public ClickGuiScreen() {
        super(Component.literal("M1cheatmine"));
    }

    private int activeAccent() {
        if (colorMode == ColorMode.RAINBOW) {
            float hue = ((System.nanoTime() / 1_000_000_000f) * 0.14f) % 1f;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 0.78f, 1.0f) & 0x00FFFFFF);
        }
        return colorMode.primary;
    }

    private int activeAccent2() {
        if (colorMode == ColorMode.RAINBOW) {
            float hue = (((System.nanoTime() / 1_000_000_000f) * 0.14f) + 0.12f) % 1f;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 0.55f, 1.0f) & 0x00FFFFFF);
        }
        return colorMode.secondary;
    }

    private int activeGlow() {
        int rgb = activeAccent() & 0x00FFFFFF;
        return 0x38000000 | rgb;
    }

    private void cycleColorMode(int step) {
        ColorMode[] vals = ColorMode.values();
        int idx = (colorMode.ordinal() + step) % vals.length;
        if (idx < 0) idx += vals.length;
        colorMode = vals[idx];
    }

    @Override
    protected void init() {
        winW = Math.min(BASE_W, Math.max(276, width - 16));
        winH = Math.min(BASE_H, Math.max(184, height - 20));
        if (winX == -1 || winY == -1) {
            winX = (width - winW) / 2;
            winY = Math.max(4, (height - winH) / 2);
        }
        clampWindowPosition();
        clampAllHudPositions();
        tabIndicatorY = winY + 40 + getVisibleCategoryIndex(selected) * 18;
        lastFrameTime = System.nanoTime();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float partialTick) {
    }

    private List<Category> getVisibleCategories() {
        return List.of(Category.values());
    }

    private int getVisibleCategoryIndex(Category target) {
        List<Category> cats = getVisibleCategories();
        int idx = cats.indexOf(target);
        return Math.max(0, idx);
    }

    private List<Module> getDisplayedModules() {
        if (!searchQuery.isBlank()) {
            String q = searchQuery.toLowerCase(Locale.ROOT).trim();
            return ModuleManager.modules.stream()
                    .filter(m -> m.name.toLowerCase(Locale.ROOT).contains(q))
                    .collect(Collectors.toList());
        }
        if (selected.name().equals("HUD")) {
            return ModuleManager.modules.stream()
                    .filter(m -> m instanceof HudModule)
                    .collect(Collectors.toList());
        }
        return ModuleManager.getByCategory(selected);
    }

    private float scaledHudW(HudModule h) {
        return Math.max(8f, h.width * HUD_SCALE);
    }

    private float scaledHudH(HudModule h) {
        return Math.max(8f, h.height * HUD_SCALE);
    }

    private void clampHudPosition(HudModule h) {
        float sw = scaledHudW(h);
        float sh = scaledHudH(h);
        h.x = Math.round(MathUtils.clamp(h.x, 2f, Math.max(2f, width - sw - 2f)));
        h.y = Math.round(MathUtils.clamp(h.y, 2f, Math.max(2f, height - sh - 2f)));
    }

    private void clampAllHudPositions() {
        for (Module m : ModuleManager.modules) {
            if (m instanceof HudModule h) {
                clampHudPosition(h);
            }
        }
    }

    private String formatKeyName(int keyCode) {
        if (keyCode <= 0 || keyCode == GLFW.GLFW_KEY_UNKNOWN) return null;
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.isBlank()) {
            return name.toUpperCase(Locale.ROOT);
        }
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPC";
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "SHF";
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "CTL";
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> "ALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> "ENT";
            default -> "K" + keyCode;
        };
    }

    private int measureText(String text, boolean bold) {
        if (mc.font == null || text == null || text.isEmpty()) return 0;
        float spacing = bold ? 0.55f : 0.35f;
        float raw = mc.font.width(text) + Math.max(0, text.length() - 1) * spacing;
        return Math.round(raw * UI_TEXT_SCALE);
    }

    private void drawText(GuiGraphics g, String text, float x, float y, int color, boolean bold) {
        if (mc.font == null || text == null || text.isEmpty()) return;
        float spacing = bold ? 0.55f : 0.35f;
        float cursorX = x;
        int shadowColor = ColorUtils.withAlpha(0x050811, 130);

        g.pose().pushMatrix();
        g.pose().translate(cursorX, y);
        g.pose().scale(UI_TEXT_SCALE, UI_TEXT_SCALE);

        float localX = 0f;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            int ix = Math.round(localX);
            g.drawString(mc.font, ch, ix + 1, 1, shadowColor, false);
            g.drawString(mc.font, ch, ix, 0, color, bold);
            localX += mc.font.width(ch) + spacing;
        }

        g.pose().popMatrix();
    }

    private String fitText(String text, int maxWidth, boolean bold) {
        if (maxWidth <= 0 || measureText(text, bold) <= maxWidth) return text;
        String dots = "..";
        int avail = Math.max(0, maxWidth - measureText(dots, bold));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (measureText(sb.toString() + ch, bold) > avail) break;
            sb.append(ch);
        }
        return sb + dots;
    }

    private String fitText(String text, int maxWidth) {
        return fitText(text, maxWidth, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float d) {
        if (mc.font == null) return;

        int accent = activeAccent();
        int accent2 = activeAccent2();
        int glowCol = activeGlow();

        long now = System.nanoTime();
        float dt = MathUtils.clamp((now - lastFrameTime) / 1_000_000_000f, 0.001f, 0.1f);
        lastFrameTime = now;

        float animRate = 1f - (float) Math.pow(0.001f, dt);
        openAnim = MathUtils.lerp(openAnim, 1f, animRate);
        scroll = MathUtils.lerp((float) scroll, (float) targetScroll, animRate);

        float eased = MathUtils.easeOutCubic(openAnim);

        for (Module m : ModuleManager.modules) {
            if (m instanceof HudModule h && h.enabled) {
                M1CheatMine.renderScaledHud(g, h, width, height);
                float sw = scaledHudW(h);
                float sh = scaledHudH(h);
                boolean hovered = MathUtils.isHovered(mx, my, h.x - 1, h.y - 1, sw + 2, sh + 2) || draggingHud == h;
                if (hovered) {
                    RenderUtils.roundedOutline(g, h.x - 1f, h.y - 1f, sw + 2f, sh + 2f, 2.5f, 0.8f, accent);
                    int closeX = Math.round(h.x + sw - 7f);
                    int closeY = Math.round(h.y - 8f);
                    if (closeY < 2) closeY = Math.round(h.y + sh + 1f);
                    RenderUtils.roundedRect(g, closeX, closeY, 8, 8, 2f, 0xE82A141C);
                    drawText(g, "x", closeX + 2.2f, closeY + 0.5f, ColorUtils.DANGER, true);
                }
            }
        }

        g.pose().pushMatrix();
        float scale = 0.96f + 0.04f * eased;
        float cx = winX + winW / 2f, cy = winY + winH / 2f;
        g.pose().translate(cx * (1 - scale), cy * (1 - scale));
        g.pose().scale(scale, scale);

        RenderUtils.glow(g, winX, winY, winW, winH, 8, glowCol, 7);
        RenderUtils.shadow(g, winX, winY, winW, winH, 8, 5);
        RenderUtils.roundedGradientV(g, winX, winY, winW, winH, 8, 0xE61E2940, 0xE60F182A);
        RenderUtils.roundedOutline(g, winX, winY, winW, winH, 8, 1.1f, ColorUtils.AERO_BORDER_HI);
        RenderUtils.glassHighlight(g, winX, winY, winW, winH, 8);

        RenderUtils.roundedRect(g, winX, winY, SIDEBAR_W, winH, 8, ColorUtils.AERO_BG_SIDE);
        g.fill(winX + SIDEBAR_W - 6, winY + 3, winX + SIDEBAR_W, winY + winH - 3, ColorUtils.AERO_BG_SIDE);
        RenderUtils.rect(g, winX + SIDEBAR_W, winY + 3, 1, winH - 6, 0x40A8C8E8);

        drawText(g, "M1CHEAT", winX + 9, winY + 9, ColorUtils.TEXT_BRIGHT, true);
        drawText(g, "AERO // v2.2", winX + 9, winY + 18, ColorUtils.TEXT_DIM, false);
        RenderUtils.roundedRect(g, winX + 9, winY + 28, 22, 1.0f, 0.5f, accent);

        List<Category> categories = getVisibleCategories();
        int catStartY = winY + 40;
        int catStepY = 18;
        if (searchQuery.isBlank()) {
            float targetY = catStartY + getVisibleCategoryIndex(selected) * catStepY;
            tabIndicatorY = MathUtils.lerp(tabIndicatorY, targetY, animRate);
            RenderUtils.roundedGradientV(g, winX + 5, tabIndicatorY, SIDEBAR_W - 10, 15, 3f, 0xE62E3A56, 0xE6202C44);
            RenderUtils.roundedOutline(g, winX + 5, tabIndicatorY, SIDEBAR_W - 10, 15, 3f, 0.85f, ColorUtils.AERO_BORDER_HI);
            RenderUtils.roundedRect(g, winX + 7, tabIndicatorY + 2f, 1.8f, 11, 0.9f, accent);
        }

        int tY = catStartY;
        for (Category c : categories) {
            boolean sel = searchQuery.isBlank() && c == selected;
            boolean catHover = MathUtils.isHovered(mx, my, winX + 5, tY, SIDEBAR_W - 10, 15);
            int col = sel ? ColorUtils.TEXT_BRIGHT : (catHover ? ColorUtils.TEXT_SOFT : ColorUtils.TEXT_DIM);

            long enabledCount = ModuleManager.getByCategory(c).stream().filter(mod -> mod.enabled).count();
            int badgeReserve = 0;
            if (enabledCount > 0) {
                String badge = String.valueOf(enabledCount);
                int bw = measureText(badge, false) + 4;
                int bx = winX + SIDEBAR_W - 8 - bw;
                RenderUtils.roundedRect(g, bx, tY + 3, bw, 9, 2f, 0x505FA8FF);
                drawText(g, badge, bx + 2, tY + 4.2f, accent2, false);
                badgeReserve = bw + 4;
            }

            int maxCatTextW = SIDEBAR_W - 18 - badgeReserve;
            drawText(g, fitText(c.name, maxCatTextW, sel), winX + 12, tY + 4.2f, col, sel);
            tY += catStepY;
        }

        String playerName = fitText((mc.player != null) ? mc.player.getName().getString() : "Player", SIDEBAR_W - 22, true);
        RenderUtils.rect(g, winX + 7, winY + winH - 24, SIDEBAR_W - 14, 1, 0x40A8C8E8);
        RenderUtils.circle(g, winX + 11, winY + winH - 13, 2.2f, ColorUtils.SUCCESS);
        drawText(g, playerName, winX + 17, winY + winH - 16, ColorUtils.TEXT_BRIGHT, true);
        drawText(g, "online", winX + 17, winY + winH - 8, ColorUtils.SUCCESS, false);

        int cX = winX + SIDEBAR_W + 8;
        int cW = winW - SIDEBAR_W - 16;
        List<Module> mods = getDisplayedModules();

        int sbW = Math.min(72, Math.max(50, cW / 4)), sbH = 14;
        int sbX = cX + cW - sbW, sbY = winY + 9;

        int cbW = 14, cbH = 14;
        int cbX = sbX - cbW - 4, cbY = winY + 9;

        String headerTitle = searchQuery.isBlank() ? selected.name.toUpperCase() : "SEARCH";
        String subInfo = bindingModule != null
                ? "Press key for " + bindingModule.name + " (Del to clear)"
                : mods.size() + " mods // " + colorMode.label;
        int headerMaxW = Math.max(24, cbX - cX - 6);
        drawText(g, fitText(headerTitle, headerMaxW, true), cX, winY + 9, ColorUtils.TEXT_BRIGHT, true);
        drawText(g, fitText(subInfo, headerMaxW, false), cX, winY + 18, bindingModule != null ? accent2 : ColorUtils.TEXT_MUTED, false);

        boolean colorHover = MathUtils.isHovered(mx, my, cbX, cbY, cbW, cbH);
        RenderUtils.roundedGradientV(g, cbX, cbY, cbW, cbH, 3.5f, 0xD8121C30, 0xD80B1220);
        RenderUtils.roundedOutline(g, cbX, cbY, cbW, cbH, 3.5f, 0.9f, colorHover ? accent : ColorUtils.AERO_BORDER);
        RenderUtils.circle(g, cbX + cbW / 2f, cbY + cbH / 2f, 3.6f, accent);

        RenderUtils.roundedGradientV(g, sbX, sbY, sbW, sbH, 3.5f, 0xD8121C30, 0xD80B1220);
        RenderUtils.roundedOutline(g, sbX, sbY, sbW, sbH, 3.5f, 0.9f, searchFocused ? accent : ColorUtils.AERO_BORDER);
        String rawSearch = searchQuery.isEmpty() ? (searchFocused ? "|" : "Search...") : searchQuery + (searchFocused ? "|" : "");
        int clearPad = searchQuery.isEmpty() ? 6 : 12;
        String displaySearch = fitText(rawSearch, Math.max(8, sbW - clearPad), false);
        int searchCol = searchQuery.isEmpty() && !searchFocused ? ColorUtils.TEXT_MUTED : ColorUtils.TEXT_BRIGHT;
        drawText(g, displaySearch, sbX + 4, sbY + 3.5f, searchCol, false);

        if (!searchQuery.isEmpty()) {
            drawText(g, "x", sbX + sbW - 8, sbY + 3.5f, ColorUtils.TEXT_DIM, true);
        }

        RenderUtils.rect(g, cX, winY + 28, cW, 1, 0x40A8C8E8);

        int viewTop = winY + 32;
        int viewBot = winY + winH - 7;
        int viewH = viewBot - viewTop;

        g.enableScissor(cX - 2, viewTop, cX + cW + 3, viewBot);
        g.pose().pushMatrix();
        g.pose().translate(0f, (float) -scroll);

        int colW = (cW - 6) / 2;
        int[] colY = { viewTop + 2, viewTop + 2 };
        boolean insideViewport = MathUtils.isHovered(mx, my, cX, viewTop, cW, viewH);

        for (Module mod : mods) {
            mod.toggleAnim = MathUtils.lerp(mod.toggleAnim, mod.enabled ? 1f : 0f, animRate);
            boolean collapsed = collapsedModules.contains(mod);

            int col = (colY[0] <= colY[1]) ? 0 : 1;
            int cardX = cX + col * (colW + 6);
            int cardY = colY[col];
            int settingsCount = mod.numberSettings.size() + mod.booleanSettings.size();
            int cardH = collapsed ? 21 : (25 + mod.numberSettings.size() * 20 + mod.booleanSettings.size() * 15);

            boolean hover = insideViewport && MathUtils.isHovered(mx, my + scroll, cardX, cardY, colW, cardH);
            mod.hoverAnim = MathUtils.lerp(mod.hoverAnim, hover ? 1f : 0f, animRate);

            int cardTop = ColorUtils.interpolate(ColorUtils.AERO_CARD, ColorUtils.AERO_CARD_HI, Math.max(mod.hoverAnim * 0.7f, mod.toggleAnim * 0.45f));
            int cardBot = ColorUtils.interpolate(ColorUtils.AERO_CARD_LO, ColorUtils.AERO_CARD_HI, Math.max(mod.hoverAnim * 0.5f, mod.toggleAnim * 0.25f));

            RenderUtils.roundedGradientV(g, cardX, cardY, colW, cardH, 5, cardTop, cardBot);
            int border = (bindingModule == mod)
                    ? accent2
                    : ColorUtils.interpolate(ColorUtils.AERO_BORDER, accent, Math.max(mod.hoverAnim * 0.45f, mod.toggleAnim));
            RenderUtils.roundedOutline(g, cardX, cardY, colW, cardH, 5, 0.9f, border);

            int tx = cardX + colW - 21, ty = cardY + 6, tw = 15, th = 9;
            int boundKey = moduleKeys.getOrDefault(mod, GLFW.GLFW_KEY_UNKNOWN);
            String keyLabel = (bindingModule == mod) ? "..." : formatKeyName(boundKey);
            int keyBadgeW = 0;
            if (keyLabel != null) {
                keyBadgeW = measureText(keyLabel, false) + 4;
                int kx = tx - keyBadgeW - (settingsCount > 0 ? 11 : 4);
                RenderUtils.roundedRect(g, kx, cardY + 5, keyBadgeW, 10, 2f, 0x455FA8FF);
                drawText(g, keyLabel, kx + 2, cardY + 6.3f, accent2, false);
                keyBadgeW += 3;
            }

            int titleRightLimit = (settingsCount > 0) ? (cardX + colW - 31 - keyBadgeW) : (tx - 4 - keyBadgeW);
            int titleCol = ColorUtils.interpolate(ColorUtils.TEXT_SOFT, ColorUtils.TEXT_BRIGHT, mod.toggleAnim);
            drawText(g, fitText(mod.name, titleRightLimit - (cardX + 6), true), cardX + 6, cardY + 6.2f, titleCol, true);

            if (settingsCount > 0) {
                String arrow = collapsed ? "+" : "-";
                drawText(g, arrow, cardX + colW - 28, cardY + 6.2f, ColorUtils.TEXT_MUTED, true);
            }

            int bg = ColorUtils.interpolate(0x90101A2E, accent, mod.toggleAnim);
            RenderUtils.roundedRect(g, tx, ty, tw, th, th / 2f, bg);
            RenderUtils.roundedOutline(g, tx, ty, tw, th, th / 2f, 0.9f, ColorUtils.interpolate(ColorUtils.AERO_BORDER, accent, mod.toggleAnim));
            float knobX = tx + 1.5f + (tw - th) * mod.toggleAnim;
            RenderUtils.circle(g, knobX + (th - 2) / 2f + 0.3f, ty + th / 2f, (th - 4) / 2f + 0.25f, 0xFFFFFFFF);

            if (!collapsed) {
                int sY = cardY + 22;
                for (NumberSetting set : mod.numberSettings) {
                    String vs = formatNumber(set.value, set.increment);
                    int vsW = measureText(vs, true);
                    int setNameMaxW = Math.max(12, colW - 14 - vsW);
                    drawText(g, fitText(set.name, setNameMaxW, false), cardX + 6, sY + 0.3f, ColorUtils.TEXT_DIM, false);
                    drawText(g, vs, cardX + colW - vsW - 6, sY + 0.3f, ColorUtils.TEXT_BRIGHT, true);

                    float bX = cardX + 6, bY = sY + 9, bW = colW - 12, bH = 2.6f;
                    RenderUtils.roundedRect(g, bX, bY, bW, bH, 1.3f, 0x90101A2E);
                    float fW = bW * set.ratio();
                    if (fW > 0) {
                        RenderUtils.roundedGradientV(g, bX, bY, fW, bH, 1.3f, accent, accent2);
                    }
                    RenderUtils.circle(g, bX + fW, bY + bH / 2f, 2.8f, accent);
                    RenderUtils.circle(g, bX + fW, bY + bH / 2f, 1.3f, 0xFFFFFFFF);
                    sY += 20;
                }

                for (BooleanSetting bs : mod.booleanSettings) {
                    int bxx = cardX + colW - 15, bxy = sY, cs = 9;
                    int bsMaxW = Math.max(12, bxx - (cardX + 6) - 4);
                    drawText(g, fitText(bs.name, bsMaxW, bs.value), cardX + 6, sY + 1.3f, bs.value ? ColorUtils.TEXT_BRIGHT : ColorUtils.TEXT_DIM, bs.value);
                    RenderUtils.roundedGradientV(g, bxx, bxy, cs, cs, 1.8f, 0xC8101A2E, 0xC80A1220);
                    RenderUtils.roundedOutline(g, bxx, bxy, cs, cs, 1.8f, 0.9f, bs.value ? accent : ColorUtils.AERO_BORDER);
                    if (bs.value) {
                        RenderUtils.rect(g, bxx + 2f, bxy + 4f, 1.4f, 2.2f, accent);
                        RenderUtils.rect(g, bxx + 3.4f, bxy + 5.4f, 1.4f, 1.4f, accent);
                        RenderUtils.rect(g, bxx + 4.8f, bxy + 2f, 1.4f, 4.2f, accent);
                    }
                    sY += 15;
                }
            }
            colY[col] += cardH + 6;
        }

        int totalContentH = Math.max(colY[0], colY[1]) - (viewTop + 2);
        maxScroll = Math.max(0, totalContentH - viewH + 4);
        targetScroll = MathUtils.clamp(targetScroll, 0, maxScroll);

        g.pose().popMatrix();
        g.disableScissor();

        if (maxScroll > 0) {
            float barX = cX + cW - 2;
            float thumbH = Math.max(16f, viewH * (viewH / (float) (viewH + maxScroll)));
            float thumbY = viewTop + (viewH - thumbH) * (float) (scroll / maxScroll);
            RenderUtils.roundedRect(g, barX, viewTop, 2f, viewH, 1, 0x30A8C8E8);
            RenderUtils.roundedRect(g, barX, thumbY, 2f, thumbH, 1, accent);
        }

        g.pose().popMatrix();
        super.render(g, mx, my, d);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        int btn = event.button();

        if (bindingModule != null) {
            bindingModule = null;
            return true;
        }

        if (btn == 0) {
            for (Module m : ModuleManager.modules) {
                if (m instanceof HudModule h && h.enabled) {
                    float sw = scaledHudW(h);
                    float sh = scaledHudH(h);
                    int closeX = Math.round(h.x + sw - 7f);
                    int closeY = Math.round(h.y - 8f);
                    if (closeY < 2) closeY = Math.round(h.y + sh + 1f);
                    if (MathUtils.isHovered(mx, my, closeX, closeY, 8, 8)) {
                        h.enabled = false;
                        h.onDisable();
                        return true;
                    }
                }
            }
        }

        int cX = winX + SIDEBAR_W + 8;
        int cW = winW - SIDEBAR_W - 16;
        int sbW = Math.min(72, Math.max(50, cW / 4)), sbH = 14;
        int sbX = cX + cW - sbW, sbY = winY + 9;

        int cbW = 14, cbH = 14;
        int cbX = sbX - cbW - 4, cbY = winY + 9;

        if (MathUtils.isHovered(mx, my, cbX, cbY, cbW, cbH)) {
            if (btn == 0) {
                cycleColorMode(1);
                return true;
            } else if (btn == 1) {
                cycleColorMode(-1);
                return true;
            }
        }

        if (btn == 0) {
            if (MathUtils.isHovered(mx, my, sbX, sbY, sbW, sbH)) {
                if (!searchQuery.isEmpty() && mx >= sbX + sbW - 10) {
                    searchQuery = "";
                    scroll = 0;
                    targetScroll = 0;
                }
                searchFocused = true;
                return true;
            } else {
                searchFocused = false;
            }

            int tY = winY + 40;
            for (Category c : getVisibleCategories()) {
                if (MathUtils.isHovered(mx, my, winX + 5, tY, SIDEBAR_W - 10, 15)) {
                    selected = c;
                    searchQuery = "";
                    scroll = 0;
                    targetScroll = 0;
                    return true;
                }
                tY += 18;
            }
        }

        int viewTop = winY + 32;
        int viewBot = winY + winH - 7;
        if (MathUtils.isHovered(mx, my, cX, viewTop, cW, viewBot - viewTop)) {
            int colW = (cW - 6) / 2;
            int[] colY = { viewTop + 2, viewTop + 2 };

            for (Module mod : getDisplayedModules()) {
                boolean collapsed = collapsedModules.contains(mod);
                int col = (colY[0] <= colY[1]) ? 0 : 1;
                int cardX = cX + col * (colW + 6);
                int cardY = colY[col];
                int cardH = collapsed ? 21 : (25 + mod.numberSettings.size() * 20 + mod.booleanSettings.size() * 15);

                if (MathUtils.isHovered(mx, my + scroll, cardX, cardY, colW, 20)) {
                    if (btn == 0) {
                        mod.toggle();
                        return true;
                    } else if (btn == 1 && (!mod.numberSettings.isEmpty() || !mod.booleanSettings.isEmpty())) {
                        if (collapsed) collapsedModules.remove(mod);
                        else collapsedModules.add(mod);
                        return true;
                    } else if (btn == 2) {
                        bindingModule = mod;
                        searchFocused = false;
                        return true;
                    }
                }

                if (btn == 0 && !collapsed) {
                    int sY = cardY + 22;
                    for (NumberSetting set : mod.numberSettings) {
                        float bX = cardX + 6, bW = colW - 12;
                        if (MathUtils.isHovered(mx, my + scroll, bX - 3, sY + 4, bW + 6, 11)) {
                            draggingSlider = set;
                            sliderX = bX;
                            sliderW = bW;
                            updateSlider(mx);
                            return true;
                        }
                        sY += 20;
                    }
                    for (BooleanSetting bs : mod.booleanSettings) {
                        if (MathUtils.isHovered(mx, my + scroll, cardX + 5, sY - 2, colW - 10, 12)) {
                            bs.toggle();
                            return true;
                        }
                        sY += 15;
                    }
                }
                colY[col] += cardH + 6;
            }
        }

        if (btn == 0) {
            if (MathUtils.isHovered(mx, my, winX, winY, winW, 28)) {
                draggingWin = true;
                dragOX = (int) mx - winX;
                dragOY = (int) my - winY;
                return true;
            }

            if (!MathUtils.isHovered(mx, my, winX, winY, winW, winH)) {
                for (Module m : ModuleManager.modules) {
                    if (m instanceof HudModule h && h.enabled) {
                        float sw = scaledHudW(h);
                        float sh = scaledHudH(h);
                        if (MathUtils.isHovered(mx, my, h.x, h.y, sw, sh)) {
                            draggingHud = h;
                            hudOX = (int) mx - h.x;
                            hudOY = (int) my - h.y;
                            return true;
                        }
                    }
                }
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mx = event.x();
        double my = event.y();

        if (draggingSlider != null) {
            updateSlider(mx);
            return true;
        }
        if (draggingWin) {
            winX = (int) mx - dragOX;
            winY = (int) my - dragOY;
            clampWindowPosition();
            return true;
        }
        if (draggingHud != null) {
            draggingHud.x = (int) mx - hudOX;
            draggingHud.y = (int) my - hudOY;
            clampHudPosition(draggingHud);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingWin = false;
        draggingSlider = null;
        draggingHud = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (MathUtils.isHovered(mx, my, winX + SIDEBAR_W, winY, winW - SIDEBAR_W, winH)) {
            targetScroll = MathUtils.clamp(targetScroll - sy * 22, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                moduleKeys.remove(bindingModule);
            } else {
                moduleKeys.put(bindingModule, keyCode);
            }
            bindingModule = null;
            return true;
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                searchFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                scroll = 0;
                targetScroll = 0;
                return true;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int codePoint = event.codepoint();
        if (bindingModule == null && searchFocused && !Character.isISOControl(codePoint) && searchQuery.length() < 24) {
            searchQuery += Character.toString(codePoint);
            scroll = 0;
            targetScroll = 0;
            return true;
        }
        return super.charTyped(event);
    }

    private void updateSlider(double mx) {
        double r = MathUtils.clamp((mx - sliderX) / sliderW, 0, 1);
        draggingSlider.setValue(draggingSlider.min + r * (draggingSlider.max - draggingSlider.min));
    }

    private void clampWindowPosition() {
        winX = MathUtils.clamp(winX, 2, Math.max(2, width - winW - 2));
        winY = MathUtils.clamp(winY, 2, Math.max(2, height - winH - 2));
    }

    private String formatNumber(double val, double inc) {
        if (inc >= 1.0) return String.format(Locale.ROOT, "%.0f", val);
        if (inc >= 0.1) return String.format(Locale.ROOT, "%.1f", val);
        return String.format(Locale.ROOT, "%.2f", val);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}