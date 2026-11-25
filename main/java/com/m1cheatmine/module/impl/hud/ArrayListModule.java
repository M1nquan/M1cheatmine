package com.m1cheatmine.module.impl.hud;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.HudModule;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.ModuleManager;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.util.ColorUtils;
import com.m1cheatmine.util.MathUtils;
import com.m1cheatmine.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Comparator;
import java.util.List;

public class ArrayListModule extends HudModule {
    public final BooleanSetting showBar = new BooleanSetting("Accent Bar", true);
    public final BooleanSetting compact = new BooleanSetting("Compact", false);

    public ArrayListModule() {
        super("ArrayList", 0, 8, 120, 120);
        addSetting(showBar);
        addSetting(compact);
    }

    @Override
    public void render(GuiGraphics g) {
        if (mc.font == null || mc.getWindow() == null) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        List<Module> active = ModuleManager.modules.stream()
            .filter(m -> m.enabled && m.category != Category.HUD)
            .sorted(Comparator.comparingInt((Module m) -> mc.font.width(m.name)).reversed())
            .toList();

        int y = this.y;
        int rowH = compact.value ? 14 : 16;
        int gap = compact.value ? 1 : 2;
        int index = 0;

        for (Module m : active) {
            m.toggleAnim = MathUtils.lerp(m.toggleAnim, m.enabled ? 1f : 0f, 0.25f);
            float anim = MathUtils.clamp(m.toggleAnim, 0f, 1f);
            if (anim <= 0.02f) continue;

            int textW = mc.font.width(m.name);
            int pillW = textW + 18;
            int targetX = sw - pillW - 10;
            int slideOffset = (int) ((1f - anim) * (pillW + 16));
            int boxX = targetX + slideOffset;
            int boxY = y;

            int accentColor = ColorUtils.interpolate(
                ColorUtils.AERO_ACCENT,
                ColorUtils.AERO_ACCENT2,
                (float) ((index * 0.18 + (System.currentTimeMillis() % 2400) / 2400.0) % 1.0)
            );
            int topBg = ColorUtils.withAlpha(0xFF202C44, (int) (215 * anim));
            int botBg = ColorUtils.withAlpha(0xFF101A2E, (int) (215 * anim));
            int borderCol = ColorUtils.withAlpha(ColorUtils.AERO_BORDER_HI, (int) (180 * anim));
            int accentAlpha = ColorUtils.withAlpha(accentColor, (int) (255 * anim));
            int textCol = ColorUtils.withAlpha(ColorUtils.TEXT_BRIGHT, (int) (255 * anim));

            RenderUtils.glow(g, boxX, boxY, pillW, rowH, 4, ColorUtils.withAlpha(accentColor, (int) (55 * anim)), 5);
            RenderUtils.roundedGradientV(g, boxX, boxY, pillW, rowH, 4, topBg, botBg);
            RenderUtils.roundedOutline(g, boxX, boxY, pillW, rowH, 4, 1.1f, borderCol);
            RenderUtils.glassHighlight(g, boxX, boxY, pillW, rowH, 4);

            if (showBar.value) {
                RenderUtils.roundedRect(g, boxX + pillW - 4, boxY + 3, 2, rowH - 6, 1f, accentAlpha);
            }

            g.drawString(mc.font, m.name, boxX + 7, boxY + (rowH - 8) / 2, textCol, true);

            y += Math.round((rowH + gap) * anim);
            index++;
        }
    }
}