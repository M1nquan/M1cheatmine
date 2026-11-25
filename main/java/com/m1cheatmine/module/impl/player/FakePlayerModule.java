package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import java.util.UUID;

public class FakePlayerModule extends Module {
    private RemotePlayer fakePlayer;

    public FakePlayerModule() {
        super("FakePlayer", Category.PLAYER, false);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.level == null) return;
        GameProfile profile = new GameProfile(UUID.randomUUID(), "M1_Dummy");
        fakePlayer = new RemotePlayer(mc.level, profile);
        fakePlayer.copyPosition(mc.player);
        fakePlayer.setYHeadRot(mc.player.getYHeadRot());
        fakePlayer.getInventory().replaceWith(mc.player.getInventory());
        fakePlayer.setHealth(20.0f);
        mc.level.addEntity(fakePlayer);
    }

    @Override
    public void onDisable() {
        if (mc.level != null && fakePlayer != null) {
            mc.level.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
            fakePlayer = null;
        }
    }
}