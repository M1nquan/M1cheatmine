package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.M1CheatMine;
import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;

public class FreelookModule extends Module {
    public boolean active = false;
    public float cameraYaw = 0f;
    public float cameraPitch = 0f;

    public FreelookModule() {
        super("Freelook", Category.RENDER, true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || M1CheatMine.freelookKey == null) return;

        boolean held = M1CheatMine.freelookKey.isDown();
        if (held && !active) {
            active = true;
            cameraYaw = mc.player.getYRot();
            cameraPitch = mc.player.getXRot();
        } else if (!held && active) {
            active = false;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }
}