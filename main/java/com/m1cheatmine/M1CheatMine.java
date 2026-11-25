package com.m1cheatmine;

import com.m1cheatmine.module.HudModule;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.ModuleManager;
import com.m1cheatmine.ui.ClickGuiScreen;
import com.m1cheatmine.util.MathUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(M1CheatMine.MODID)
public class M1CheatMine {
    public static final String MODID = "m1cheatmine";
    public static final float HUD_SCALE = 0.45f;
    public static M1CheatMine INSTANCE;
    public static KeyMapping openGuiKey;
    public static KeyMapping freelookKey;

    public M1CheatMine(FMLJavaModLoadingContext context) {
        INSTANCE = this;
        ModuleManager.init();

        RegisterKeyMappingsEvent.BUS.addListener(this::registerKeys);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void renderScaledHud(GuiGraphics g, HudModule h, int screenW, int screenH) {
        float sw = Math.max(8f, h.width * HUD_SCALE);
        float sh = Math.max(8f, h.height * HUD_SCALE);
        h.x = Math.round(MathUtils.clamp(h.x, 2f, Math.max(2f, screenW - sw - 2f)));
        h.y = Math.round(MathUtils.clamp(h.y, 2f, Math.max(2f, screenH - sh - 2f)));

        g.pose().pushMatrix();
        g.pose().translate(h.x, h.y);
        g.pose().scale(HUD_SCALE, HUD_SCALE);
        g.pose().translate(-h.x, -h.y);
        h.render(g);
        g.pose().popMatrix();
    }

    private void registerKeys(RegisterKeyMappingsEvent e) {
        if (openGuiKey == null) {
            openGuiKey = new KeyMapping(
                "key.m1cheatmine.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
            );
        }
        if (freelookKey == null) {
            freelookKey = new KeyMapping(
                "key.m1cheatmine.freelook",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyMapping.Category.MISC
            );
        }

        e.register(openGuiKey);
        e.register(freelookKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key e) {
        if (e.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        int key = e.getKey();
        for (Module m : ModuleManager.modules) {
            if (m.keyCode > 0 && key == m.keyCode) {
                m.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        while (openGuiKey != null && openGuiKey.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new ClickGuiScreen());
            }
        }

        for (Module m : ModuleManager.modules) {
            if (m.enabled) {
                m.onTick();
            }
        }
    }

    @SubscribeEvent
    public void onRenderGui(CustomizeGuiOverlayEvent.Chat e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null || mc.options.hideGui) return;
        if (mc.screen instanceof ClickGuiScreen) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        for (Module m : ModuleManager.modules) {
            if (m instanceof HudModule h && h.enabled) {
                renderScaledHud(e.getGuiGraphics(), h, sw, sh);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        for (Module m : ModuleManager.modules) {
            if (!(m instanceof HudModule) && m.enabled) {
                m.enabled = false;
                m.onDisable();
            }
        }
    }
}