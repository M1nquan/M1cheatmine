package com.m1cheatmine.module.impl.misc;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class AutoReconnectModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay (s)", 5.0, 1.0, 30.0, 1.0);

    private ServerData lastServer;
    private long disconnectTime = -1;

    public AutoReconnectModule() {
        super("AutoReconnect", Category.MISC, true);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.getCurrentServer() != null) {
            lastServer = mc.getCurrentServer();
            disconnectTime = -1;
            return;
        }

        if (mc.screen instanceof DisconnectedScreen && lastServer != null && lastServer.ip != null) {
            long now = System.currentTimeMillis();
            if (disconnectTime == -1) {
                disconnectTime = now;
            } else if (now - disconnectTime >= (long) (delay.value * 1000L)) {
                disconnectTime = -1;
                try {
                    ServerAddress address = ServerAddress.parseString(lastServer.ip);
                    ConnectScreen.startConnecting(
                        new JoinMultiplayerScreen(new TitleScreen()),
                        mc,
                        address,
                        lastServer,
                        false,
                        null
                    );
                } catch (Throwable ignored) {
                }
            }
        } else {
            disconnectTime = -1;
        }
    }
}