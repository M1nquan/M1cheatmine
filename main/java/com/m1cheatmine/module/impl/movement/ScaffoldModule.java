package com.m1cheatmine.module.impl.movement;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ScaffoldModule extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", 30, 0, 200, 10);
    public final BooleanSetting tower = new BooleanSetting("Tower", true);
    private long lastPlace = 0;

    public ScaffoldModule() {
        super("Scaffold", Category.MOVEMENT, false);
        addSetting(delay);
        addSetting(tower);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;

        long now = System.currentTimeMillis();
        if (now - lastPlace < delay.value) return;

        int slot = findBlockSlot();
        if (slot == -1) return;

        BlockPos targetPos = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(targetPos).canBeReplaced()) return;

        Direction placeFace = null;
        BlockPos neighborPos = null;
        for (Direction dir : Direction.values()) {
            BlockPos check = targetPos.relative(dir);
            if (!mc.level.getBlockState(check).isAir() && mc.level.getFluidState(check).isEmpty()) {
                neighborPos = check;
                placeFace = dir.getOpposite();
                break;
            }
        }

        if (neighborPos == null || placeFace == null) return;

        int oldSlot = getSelectedSlot();
        if (oldSlot != slot) {
            setSelectedSlot(slot);
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }

        Vec3 hitVec = Vec3.atCenterOf(neighborPos).add(Vec3.atLowerCornerOf(placeFace.getUnitVec3i()).scale(0.5));
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(hitVec, placeFace, neighborPos, false));
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (tower.value && mc.options.keyJump.isDown()
                && mc.player.input.getMoveVector().y == 0
                && mc.player.input.getMoveVector().x == 0) {
            mc.player.setDeltaMovement(0, 0.42, 0);
        }

        if (oldSlot != slot) {
            setSelectedSlot(oldSlot);
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(oldSlot));
        }

        lastPlace = now;
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        if (mc.player.getMainHandItem().getItem() instanceof BlockItem) {
            return getSelectedSlot();
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private int getSelectedSlot() {
        Object inv = mc.player.getInventory();
        try {
            Method m = inv.getClass().getMethod("getSelectedSlot");
            return (int) m.invoke(inv);
        } catch (Throwable ignored) {
        }
        try {
            Field f = inv.getClass().getField("selected");
            return f.getInt(inv);
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private void setSelectedSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        Object inv = mc.player.getInventory();
        try {
            Method m = inv.getClass().getMethod("setSelectedSlot", int.class);
            m.invoke(inv, slot);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Field f = inv.getClass().getField("selected");
            f.setInt(inv, slot);
        } catch (Throwable ignored) {
        }
    }
}