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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mod(M1CheatMine.MODID)
public class M1CheatMine {
    public static final String MODID = "m1cheatmine";
    public static final float HUD_SCALE = 0.45f;
    public static M1CheatMine INSTANCE;
    public static KeyMapping openGuiKey;
    public static KeyMapping freelookKey;

    public M1CheatMine() {
        this(null);
    }

    public M1CheatMine(FMLJavaModLoadingContext context) {
        INSTANCE = this;
        try {
            ModuleManager.init();
        } catch (Throwable ignored) {
        }

        registerModBusListeners(context);

        try {
            MinecraftForge.EVENT_BUS.register(this);
        } catch (Throwable ignored) {
        }
    }

    private void registerModBusListeners(FMLJavaModLoadingContext context) {
        try {
            Field busField = RegisterKeyMappingsEvent.class.getField("BUS");
            Object bus = busField.get(null);
            Method addListener = bus.getClass().getMethod("addListener", java.util.function.Consumer.class);
            addListener.invoke(bus, (java.util.function.Consumer<RegisterKeyMappingsEvent>) this::registerKeys);
            return;
        } catch (Throwable ignored) {
        }

        try {
            Object modBus = null;
            if (context != null) {
                Method getModEventBus = context.getClass().getMethod("getModEventBus");
                modBus = getModEventBus.invoke(context);
            }
            if (modBus == null) {
                Method getCtx = FMLJavaModLoadingContext.class.getMethod("get");
                Object ctx = getCtx.invoke(null);
                Method getModEventBus = ctx.getClass().getMethod("getModEventBus");
                modBus = getModEventBus.invoke(ctx);
            }
            if (modBus != null) {
                Method addListener = modBus.getClass().getMethod("addListener", java.util.function.Consumer.class);
                addListener.invoke(modBus, (java.util.function.Consumer<RegisterKeyMappingsEvent>) this::registerKeys);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void pushPose(GuiGraphics g) {
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("pushMatrix").invoke(pose);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("pushPose").invoke(pose);
        } catch (Throwable ignored) {
        }
    }

    private static void popPose(GuiGraphics g) {
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("popMatrix").invoke(pose);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("popPose").invoke(pose);
        } catch (Throwable ignored) {
        }
    }

    private static void translatePose(GuiGraphics g, float x, float y) {
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("translate", float.class, float.class).invoke(pose, x, y);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("translate", double.class, double.class, double.class).invoke(pose, (double) x, (double) y, 0.0);
        } catch (Throwable ignored) {
        }
    }

    private static void scalePose(GuiGraphics g, float sx, float sy) {
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("scale", float.class, float.class).invoke(pose, sx, sy);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Object pose = g.pose();
            pose.getClass().getMethod("scale", float.class, float.class, float.class).invoke(pose, sx, sy, 1.0f);
        } catch (Throwable ignored) {
        }
    }

    public static void renderScaledHud(GuiGraphics g, HudModule h, int screenW, int screenH) {
        if (g == null || h == null) return;
        float sw = Math.max(8f, h.width * HUD_SCALE);
        float sh = Math.max(8f, h.height * HUD_SCALE);
        h.x = Math.round(MathUtils.clamp(h.x, 2f, Math.max(2f, screenW - sw - 2f)));
        h.y = Math.round(MathUtils.clamp(h.y, 2f, Math.max(2f, screenH - sh - 2f)));

        pushPose(g);
        translatePose(g, h.x, h.y);
        scalePose(g, HUD_SCALE, HUD_SCALE);
        translatePose(g, -h.x, -h.y);
        try {
            h.render(g);
        } catch (Throwable ignored) {
        }
        popPose(g);
    }

    private void registerKeys(RegisterKeyMappingsEvent e) {
        if (e == null) return;
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

        try {
            e.register(openGuiKey);
            e.register(freelookKey);
        } catch (Throwable ignored) {
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key e) {
        if (e == null || e.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.screen != null) return;

        int key = e.getKey();
        if (key <= 0) return;

        for (Module m : ModuleManager.modules) {
            if (m.keyCode > 0 && key == m.keyCode) {
                m.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) return;

        try {
            while (openGuiKey != null && openGuiKey.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new ClickGuiScreen());
                }
            }
        } catch (Throwable ignored) {
        }

        for (Module m : ModuleManager.modules) {
            if (m.enabled) {
                try {
                    m.onTick();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderGui(CustomizeGuiOverlayEvent.Chat e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.options == null || mc.options.hideGui) return;
        if (mc.screen instanceof ClickGuiScreen) return;
        if (mc.getWindow() == null) return;

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
                try {
                    m.onDisable();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}