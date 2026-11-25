package com.m1cheatmine.module.impl.misc;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlinkModule extends Module {
    public final NumberSetting maxPackets = new NumberSetting("MaxPackets", 100, 20, 500, 10);
    public final BooleanSetting showGhost = new BooleanSetting("ShowGhost", true);

    public final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private RemotePlayer ghostPlayer;

    public BlinkModule() {
        super("Blink", Category.MISC, false);
        addSetting(maxPackets);
        addSetting(showGhost);
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        if (mc.player == null || mc.level == null) return;

        if (showGhost.value) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), mc.player.getName().getString() + " [Blink]");
            ghostPlayer = new RemotePlayer(mc.level, profile);
            ghostPlayer.copyPosition(mc.player);
            ghostPlayer.setYHeadRot(mc.player.getYHeadRot());
            ghostPlayer.getInventory().replaceWith(mc.player.getInventory());
            mc.level.addEntity(ghostPlayer);
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) return;
        if (packetQueue.size() >= (int) maxPackets.value) {
            flushPackets();
        }
    }

    @Override
    public void onDisable() {
        flushPackets();
        if (mc.level != null && ghostPlayer != null) {
            mc.level.removeEntity(ghostPlayer.getId(), Entity.RemovalReason.DISCARDED);
            ghostPlayer = null;
        }
    }

    public void flushPackets() {
        if (mc.getConnection() == null) {
            packetQueue.clear();
            return;
        }
        while (!packetQueue.isEmpty()) {
            Packet<?> pkt = packetQueue.poll();
            if (pkt != null) {
                mc.getConnection().send(pkt);
            }
        }
    }
}