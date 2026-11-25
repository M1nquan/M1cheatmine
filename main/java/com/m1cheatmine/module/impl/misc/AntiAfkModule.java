package com.m1cheatmine.module.impl.misc;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.world.InteractionHand;

import java.util.concurrent.ThreadLocalRandom;

public class AntiAfkModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay (s)", 5.0, 1.0, 60.0, 1.0);
    public final NumberSetting angle = new NumberSetting("Angle", 45.0, 5.0, 180.0, 5.0);
    public final BooleanSetting jump = new BooleanSetting("Jump", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);
    public final BooleanSetting rotate = new BooleanSetting("Rotate", true);

    private long lastAction = 0;

    public AntiAfkModule() {
        super("AntiAFK", Category.MISC, false);
        addSetting(delay);
        addSetting(angle);
        addSetting(jump);
        addSetting(swing);
        addSetting(rotate);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.input.getMoveVector().x != 0 || mc.player.input.getMoveVector().y != 0) {
            lastAction = System.currentTimeMillis();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastAction < (long) (delay.value * 1000L)) return;

        if (rotate.value) {
            float range = (float) angle.value;
            float yawOffset = ThreadLocalRandom.current().nextFloat(-range, range);
            mc.player.setYRot(mc.player.getYRot() + yawOffset);
        }

        if (jump.value && mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        if (swing.value) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }

        lastAction = now;
    }
}