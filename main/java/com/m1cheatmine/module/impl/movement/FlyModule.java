package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class FlyModule extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", 0.08, 0.02, 0.50, 0.01);
    public final BooleanSetting antiKick = new BooleanSetting("AntiKick", true);
    private int tickCounter = 0;

    public FlyModule() {
        super("Fly", Category.MOVEMENT, false);
        addSetting(speed);
        addSetting(antiKick);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlyingSpeed((float) speed.value);

        if (antiKick.value && !mc.player.onGround()) {
            tickCounter++;
            if (tickCounter >= 40) {
                tickCounter = 0;
                Vec3 pos = mc.player.position();
                if (mc.level != null && mc.level.noCollision(mc.player.getBoundingBox().move(0, -0.04, 0))) {
                    mc.player.setPos(pos.x, pos.y - 0.04, pos.z);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().mayfly = false;
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().setFlyingSpeed(0.05f);
        }
        tickCounter = 0;
    }
}