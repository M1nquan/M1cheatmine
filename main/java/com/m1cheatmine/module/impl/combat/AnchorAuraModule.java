package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AnchorAuraModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 4.5, 2.0, 6.0, 0.1);
    public final NumberSetting targetRange = new NumberSetting("Target Range", 8.0, 3.0, 15.0, 0.5);
    public final NumberSetting delay = new NumberSetting("Delay", 150, 0, 500, 10);
    public final BooleanSetting autoPlace = new BooleanSetting("Auto Place", true);
    public final BooleanSetting autoCharge = new BooleanSetting("Auto Charge", true);
    public final BooleanSetting autoDetonate = new BooleanSetting("Auto Detonate", true);
    private long lastAction = 0;

    public AnchorAuraModule() {
        super("AnchorAura", Category.COMBAT, false);
        addSetting(range); addSetting(targetRange); addSetting(delay);
        addSetting(autoPlace); addSetting(autoCharge); addSetting(autoDetonate);
    }

    @Override public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        Player target = findTarget();
        if (target == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAction < delay.value) return;

        BlockPos anchorPos = null;
        for (BlockPos p : BlockPos.betweenClosed(target.blockPosition().offset(-2,-1,-2), target.blockPosition().offset(2,1,2))) {
            if (mc.level.getBlockState(p).is(Blocks.RESPAWN_ANCHOR)) { anchorPos = p.immutable(); break; }
        }

        if (anchorPos != null && autoDetonate.value) {
            BlockPos below = anchorPos.below();
            if (mc.level.getBlockState(anchorPos).getValue(net.minecraft.world.level.block.RespawnAnchorBlock.CHARGE) > 0) {
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(anchorPos), Direction.UP, anchorPos, false));
                lastAction = now;
                return;
            }
        }

        if (autoPlace.value) {
            int slot = findHotbar(Items.RESPAWN_ANCHOR);
            if (slot != -1) {
                for (BlockPos p : BlockPos.betweenClosed(target.blockPosition().offset(-2,0,-2), target.blockPosition().offset(2,1,2))) {
                    if (!mc.level.getBlockState(p).isAir()) continue;
                    if (mc.level.getBlockState(p.below()).isAir()) continue;
                    if (mc.player.distanceToSqr(p.getX(), p.getY(), p.getZ()) > range.value * range.value) continue;
                    int old = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(slot);
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p.below(), false));
                    mc.player.getInventory().setSelectedSlot(old);
                    lastAction = now;
                    return;
                }
            }
        }

        if (autoCharge.value && anchorPos != null) {
            int slot = findHotbar(Items.GLOWSTONE);
            if (slot != -1) {
                int old = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(slot);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(anchorPos), Direction.UP, anchorPos, false));
                mc.player.getInventory().setSelectedSlot(old);
                lastAction = now;
            }
        }
    }

    private Player findTarget() {
        Player b = null; double bd = targetRange.value;
        for (Player p : mc.level.players()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.distanceTo(p);
            if (d < bd) { bd = d; b = p; }
        }
        return b;
    }

    private int findHotbar(net.minecraft.world.item.Item item) {
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getItem(i).is(item)) return i;
        return -1;
    }
}