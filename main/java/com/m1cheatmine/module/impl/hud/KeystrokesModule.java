package com.m1cheatmine.module.impl.hud;

import com.m1cheatmine.module.HudModule;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.util.ColorUtils;
import com.m1cheatmine.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class KeystrokesModule extends HudModule {
    public final BooleanSetting showSpace = new BooleanSetting("Spacebar", true);
    public final BooleanSetting glowPressed = new BooleanSetting("Glow Pressed", true);

    public KeystrokesModule() {
        super("Keystrokes", 8, 74, 76, 90);
        addSetting(showSpace);
        addSetting(glowPressed);
    }

    @Override
    public void render(GuiGraphics g) {
        if (mc.options == null || mc.font == null) return;

        int s = 24;
        int gp = 2;
        int mw = (s * 3 + gp * 2 - gp) / 2;

        drawKey(g, "W", x + s + gp, y, s, s, mc.options.keyUp.isDown());
        drawKey(g, "A", x, y + s + gp, s, s, mc.options.keyLeft.isDown());
        drawKey(g, "S", x + s + gp, y + s + gp, s, s, mc.options.keyDown.isDown());
        drawKey(g, "D", x + (s + gp) * 2, y + s + gp, s, s, mc.options.keyRight.isDown());

        int mouseY = y + (s + gp) * 2;
        drawKey(g, "LMB", x, mouseY, mw, 20, mc.options.keyAttack.isDown());
        drawKey(g, "RMB", x + mw + gp, mouseY, mw, 20, mc.options.keyUse.isDown());

        if (showSpace.value) {
            int fullW = s * 3 + gp * 2;
            drawKey(g, "SPACE", x, mouseY + 20 + gp, fullW, 14, mc.options.keyJump.isDown());
            height = (s + gp) * 2 + 20 + gp + 14;
        } else {
            height = (s + gp) * 2 + 20;
        }
        width = s * 3 + gp * 2;
    }

    private void drawKey(GuiGraphics ctx, String label, int kx, int ky, int w, int h, boolean pressed) {
        int c1 = pressed ? ColorUtils.AERO_ACCENT : 0xC01A2438;
        int c2 = pressed ? ColorUtils.AERO_ACCENT2 : 0xC0101A2E;

        if (pressed && glowPressed.value) {
            RenderUtils.glow(ctx, kx, ky, w, h, 4, ColorUtils.AERO_ACCENT, 5);
        }
        RenderUtils.roundedGradientV(ctx, kx, ky, w, h, 4, c1, c2);
        RenderUtils.roundedOutline(ctx, kx, ky, w, h, 4, 1.2f, pressed ? ColorUtils.AERO_HIGHLIGHT : ColorUtils.AERO_BORDER);
        RenderUtils.glassHighlight(ctx, kx, ky, w, h, 4);
        ctx.drawString(mc.font, label, kx + (w - mc.font.width(label)) / 2, ky + (h - 8) / 2, ColorUtils.TEXT_BRIGHT, true);
    }
}