package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SurroundModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 50, 0, 200, 10);
    public final BooleanSetting autoCenter = new BooleanSetting("Auto Center", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);
    private long lastPlace = 0;

    public SurroundModule() {
        super("Surround", Category.COMBAT, false);
        addSetting(delay);
        addSetting(autoCenter);
        addSetting(swing);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        long now = System.currentTimeMillis();
        if (now - lastPlace < delay.value) return;

        if (autoCenter.value) {
            double cx = mc.player.blockPosition().getX() + 0.5;
            double cz = mc.player.blockPosition().getZ() + 0.5;
            if (Math.abs(mc.player.getX() - cx) > 0.1 || Math.abs(mc.player.getZ() - cz) > 0.1) {
                mc.player.setPos(cx, mc.player.getY(), cz);
            }
        }

        int slot = findBlockSlot();
        if (slot == -1) return;

        BlockPos p = mc.player.blockPosition();
        int[][] offsets = {
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1}
        };

        for (int[] o : offsets) {
            BlockPos target = p.offset(o[0], o[1], o[2]);
            if (!mc.level.getBlockState(target).isAir()) continue;
            BlockPos base = target.below();
            if (mc.level.getBlockState(base).isAir()) continue;
            if (!mc.level.getEntities(null, new AABB(target)).isEmpty()) continue;

            int old = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(slot);
            mc.gameMode.useItemOn(
                mc.player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, base, false)
            );
            if (swing.value) mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.getInventory().setSelectedSlot(old);
            lastPlace = now;
            return;
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.OBSIDIAN)) return i;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}