package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class NoSlowModule extends Module {
    public final BooleanSetting items = new BooleanSetting("Items", true);
    public final BooleanSetting cobweb = new BooleanSetting("Cobweb", true);

    public NoSlowModule() {
        super("NoSlow", Category.MOVEMENT, true);
        addSetting(items);
        addSetting(cobweb);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (items.value && mc.player.isUsingItem() && !mc.player.isPassenger()) {
            float forward = mc.player.input.getMoveVector().y;
            float strafe = mc.player.input.getMoveVector().x;
            if (forward != 0 || strafe != 0) {
                double yaw = Math.toRadians(mc.player.getYRot());
                double spd = 0.22;
                double vx = (forward * -Math.sin(yaw) + strafe * Math.cos(yaw)) * spd;
                double vz = (forward * Math.cos(yaw) + strafe * Math.sin(yaw)) * spd;
                Vec3 vel = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(vx, vel.y, vz);
            }
        }

        if (cobweb.value && mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.COBWEB)) {
            Vec3 vel = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(vel.x * 3.5, vel.y, vel.z * 3.5);
        }
    }
}