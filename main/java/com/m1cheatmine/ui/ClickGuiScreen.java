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

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();
    private static int winX = -1, winY = -1;
    private static final int BASE_W = 360, BASE_H = 236;
    private static int winW = BASE_W, winH = BASE_H;
    private static final int SIDEBAR_W = 96;
    private static final float HUD_SCALE = M1CheatMine.HUD_SCALE;
    private static Category selected = Category.COMBAT;
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

    private String fitText(String text, int maxWidth) {
        if (maxWidth <= 0 || mc.font.width(text) <= maxWidth) return text;
        String dots = "..";
        int avail = Math.max(0, maxWidth - mc.font.width(dots));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (mc.font.width(sb.toString() + ch) > avail) break;
            sb.append(ch);
        }
        return sb + dots;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float d) {
        if (mc.font == null) return;

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
                    RenderUtils.roundedOutline(g, h.x - 1f, h.y - 1f, sw + 2f, sh + 2f, 2.5f, 0.8f, ColorUtils.AERO_ACCENT);
                    int closeX = Math.round(h.x + sw - 7f);
                    int closeY = Math.round(h.y - 8f);
                    if (closeY < 2) closeY = Math.round(h.y + sh + 1f);
                    RenderUtils.roundedRect(g, closeX, closeY, 8, 8, 2f, 0xE82A141C);
                    g.drawString(mc.font, "x", closeX + 2, closeY, ColorUtils.DANGER, false);
                }
            }
        }

        g.pose().pushMatrix();
        float scale = 0.96f + 0.04f * eased;
        float cx = winX + winW / 2f, cy = winY + winH / 2f;
        g.pose().translate(cx * (1 - scale), cy * (1 - scale));
        g.pose().scale(scale, scale);

        RenderUtils.glow(g, winX, winY, winW, winH, 8, ColorUtils.AERO_GLOW, 7);
        RenderUtils.shadow(g, winX, winY, winW, winH, 8, 5);
        RenderUtils.roundedGradientV(g, winX, winY, winW, winH, 8, 0xE61E2940, 0xE60F182A);
        RenderUtils.roundedOutline(g, winX, winY, winW, winH, 8, 1.1f, ColorUtils.AERO_BORDER_HI);
        RenderUtils.glassHighlight(g, winX, winY, winW, winH, 8);

        RenderUtils.roundedRect(g, winX, winY, SIDEBAR_W, winH, 8, ColorUtils.AERO_BG_SIDE);
        g.fill(winX + SIDEBAR_W - 6, winY + 3, winX + SIDEBAR_W, winY + winH - 3, ColorUtils.AERO_BG_SIDE);
        RenderUtils.rect(g, winX + SIDEBAR_W, winY + 3, 1, winH - 6, 0x40A8C8E8);

        g.drawString(mc.font, "M1CHEAT", winX + 9, winY + 9, ColorUtils.TEXT_BRIGHT, true);
        g.drawString(mc.font, "AERO // v2.2", winX + 9, winY + 18, ColorUtils.TEXT_DIM, false);
        RenderUtils.roundedRect(g, winX + 9, winY + 28, 22, 1.0f, 0.5f, ColorUtils.AERO_ACCENT);

        List<Category> categories = getVisibleCategories();
        int catStartY = winY + 40;
        int catStepY = 18;
        if (searchQuery.isBlank()) {
            float targetY = catStartY + getVisibleCategoryIndex(selected) * catStepY;
            tabIndicatorY = MathUtils.lerp(tabIndicatorY, targetY, animRate);
            RenderUtils.roundedGradientV(g, winX + 5, tabIndicatorY, SIDEBAR_W - 10, 15, 3f, 0xE62E3A56, 0xE6202C44);
            RenderUtils.roundedOutline(g, winX + 5, tabIndicatorY, SIDEBAR_W - 10, 15, 3f, 0.85f, ColorUtils.AERO_BORDER_HI);
            RenderUtils.roundedRect(g, winX + 7, tabIndicatorY + 2f, 1.8f, 11, 0.9f, ColorUtils.AERO_ACCENT);
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
                int bw = mc.font.width(badge) + 4;
                int bx = winX + SIDEBAR_W - 8 - bw;
                RenderUtils.roundedRect(g, bx, tY + 3, bw, 9, 2f, 0x505FA8FF);
                g.drawString(mc.font, badge, bx + 2, tY + 4, ColorUtils.AERO_ACCENT2, false);
                badgeReserve = bw + 4;
            }

            int maxCatTextW = SIDEBAR_W - 18 - badgeReserve;
            g.drawString(mc.font, fitText(c.name, maxCatTextW), winX + 12, tY + 4, col, sel);
            tY += catStepY;
        }

        String playerName = fitText((mc.player != null) ? mc.player.getName().getString() : "Player", SIDEBAR_W - 22);
        RenderUtils.rect(g, winX + 7, winY + winH - 24, SIDEBAR_W - 14, 1, 0x40A8C8E8);
        RenderUtils.circle(g, winX + 11, winY + winH - 13, 2.2f, ColorUtils.SUCCESS);
        g.drawString(mc.font, playerName, winX + 17, winY + winH - 16, ColorUtils.TEXT_BRIGHT, true);
        g.drawString(mc.font, "online", winX + 17, winY + winH - 8, ColorUtils.SUCCESS, false);

        int cX = winX + SIDEBAR_W + 8;
        int cW = winW - SIDEBAR_W - 16;
        List<Module> mods = getDisplayedModules();

        int sbW = Math.min(84, Math.max(58, cW / 3)), sbH = 14;
        int sbX = cX + cW - sbW, sbY = winY + 9;

        String headerTitle = searchQuery.isBlank() ? selected.name.toUpperCase() : "SEARCH";
        String subInfo = bindingModule != null
                ? "Press key for " + bindingModule.name + " (Del to clear)"
                : mods.size() + " mods";
        int headerMaxW = Math.max(24, sbX - cX - 6);
        g.drawString(mc.font, fitText(headerTitle, headerMaxW), cX, winY + 9, ColorUtils.TEXT_BRIGHT, true);
        g.drawString(mc.font, fitText(subInfo, headerMaxW), cX, winY + 18, bindingModule != null ? ColorUtils.AERO_ACCENT2 : ColorUtils.TEXT_MUTED, false);

        RenderUtils.roundedGradientV(g, sbX, sbY, sbW, sbH, 3.5f, 0xD8121C30, 0xD80B1220);
        RenderUtils.roundedOutline(g, sbX, sbY, sbW, sbH, 3.5f, 0.9f, searchFocused ? ColorUtils.AERO_ACCENT : ColorUtils.AERO_BORDER);
        String rawSearch = searchQuery.isEmpty() ? (searchFocused ? "|" : "Search...") : searchQuery + (searchFocused ? "|" : "");
        int clearPad = searchQuery.isEmpty() ? 6 : 12;
        String displaySearch = fitText(rawSearch, Math.max(8, sbW - clearPad));
        int searchCol = searchQuery.isEmpty() && !searchFocused ? ColorUtils.TEXT_MUTED : ColorUtils.TEXT_BRIGHT;
        g.drawString(mc.font, displaySearch, sbX + 4, sbY + 3, searchCol, false);

        if (!searchQuery.isEmpty()) {
            g.drawString(mc.font, "x", sbX + sbW - 8, sbY + 3, ColorUtils.TEXT_DIM, false);
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
                    ? ColorUtils.AERO_ACCENT2
                    : ColorUtils.interpolate(ColorUtils.AERO_BORDER, ColorUtils.AERO_ACCENT, Math.max(mod.hoverAnim * 0.45f, mod.toggleAnim));
            RenderUtils.roundedOutline(g, cardX, cardY, colW, cardH, 5, 0.9f, border);

            int tx = cardX + colW - 21, ty = cardY + 6, tw = 15, th = 9;
            int boundKey = moduleKeys.getOrDefault(mod, GLFW.GLFW_KEY_UNKNOWN);
            String keyLabel = (bindingModule == mod) ? "..." : formatKeyName(boundKey);
            int keyBadgeW = 0;
            if (keyLabel != null) {
                keyBadgeW = mc.font.width(keyLabel) + 4;
                int kx = tx - keyBadgeW - (settingsCount > 0 ? 11 : 4);
                RenderUtils.roundedRect(g, kx, cardY + 5, keyBadgeW, 10, 2f, 0x455FA8FF);
                g.drawString(mc.font, keyLabel, kx + 2, cardY + 6, ColorUtils.AERO_ACCENT2, false);
                keyBadgeW += 3;
            }

            int titleRightLimit = (settingsCount > 0) ? (cardX + colW - 31 - keyBadgeW) : (tx - 4 - keyBadgeW);
            int titleCol = ColorUtils.interpolate(ColorUtils.TEXT_SOFT, ColorUtils.TEXT_BRIGHT, mod.toggleAnim);
            g.drawString(mc.font, fitText(mod.name, titleRightLimit - (cardX + 6)), cardX + 6, cardY + 6, titleCol, true);

            if (settingsCount > 0) {
                String arrow = collapsed ? "+" : "-";
                g.drawString(mc.font, arrow, cardX + colW - 28, cardY + 6, ColorUtils.TEXT_MUTED, false);
            }

            int bg = ColorUtils.interpolate(0x90101A2E, ColorUtils.AERO_ACCENT, mod.toggleAnim);
            RenderUtils.roundedRect(g, tx, ty, tw, th, th / 2f, bg);
            RenderUtils.roundedOutline(g, tx, ty, tw, th, th / 2f, 0.9f, ColorUtils.interpolate(ColorUtils.AERO_BORDER, ColorUtils.AERO_ACCENT, mod.toggleAnim));
            float knobX = tx + 1.5f + (tw - th) * mod.toggleAnim;
            RenderUtils.circle(g, knobX + (th - 2) / 2f + 0.3f, ty + th / 2f, (th - 4) / 2f + 0.25f, 0xFFFFFFFF);

            if (!collapsed) {
                int sY = cardY + 22;
                for (NumberSetting set : mod.numberSettings) {
                    String vs = formatNumber(set.value, set.increment);
                    int vsW = mc.font.width(vs);
                    int setNameMaxW = Math.max(12, colW - 14 - vsW);
                    g.drawString(mc.font, fitText(set.name, setNameMaxW), cardX + 6, sY, ColorUtils.TEXT_DIM, false);
                    g.drawString(mc.font, vs, cardX + colW - vsW - 6, sY, ColorUtils.TEXT_BRIGHT, false);

                    float bX = cardX + 6, bY = sY + 9, bW = colW - 12, bH = 2.6f;
                    RenderUtils.roundedRect(g, bX, bY, bW, bH, 1.3f, 0x90101A2E);
                    float fW = bW * set.ratio();
                    if (fW > 0) {
                        RenderUtils.roundedGradientV(g, bX, bY, fW, bH, 1.3f, ColorUtils.AERO_ACCENT, ColorUtils.AERO_ACCENT2);
                    }
                    RenderUtils.circle(g, bX + fW, bY + bH / 2f, 2.8f, ColorUtils.AERO_ACCENT);
                    RenderUtils.circle(g, bX + fW, bY + bH / 2f, 1.3f, 0xFFFFFFFF);
                    sY += 20;
                }

                for (BooleanSetting bs : mod.booleanSettings) {
                    int bxx = cardX + colW - 15, bxy = sY, cs = 9;
                    int bsMaxW = Math.max(12, bxx - (cardX + 6) - 4);
                    g.drawString(mc.font, fitText(bs.name, bsMaxW), cardX + 6, sY + 1, bs.value ? ColorUtils.TEXT_BRIGHT : ColorUtils.TEXT_DIM, bs.value);
                    RenderUtils.roundedGradientV(g, bxx, bxy, cs, cs, 1.8f, 0xC8101A2E, 0xC80A1220);
                    RenderUtils.roundedOutline(g, bxx, bxy, cs, cs, 1.8f, 0.9f, bs.value ? ColorUtils.AERO_ACCENT : ColorUtils.AERO_BORDER);
                    if (bs.value) {
                        RenderUtils.rect(g, bxx + 2f, bxy + 4f, 1.4f, 2.2f, ColorUtils.AERO_ACCENT);
                        RenderUtils.rect(g, bxx + 3.4f, bxy + 5.4f, 1.4f, 1.4f, ColorUtils.AERO_ACCENT);
                        RenderUtils.rect(g, bxx + 4.8f, bxy + 2f, 1.4f, 4.2f, ColorUtils.AERO_ACCENT);
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
            RenderUtils.roundedRect(g, barX, thumbY, 2f, thumbH, 1, ColorUtils.AERO_ACCENT);
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
        int sbW = Math.min(84, Math.max(58, cW / 3)), sbH = 14;
        int sbX = cX + cW - sbW, sbY = winY + 9;

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
