package com.m1cheatmine.module.impl.combat;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CrystalAuraModule extends Module {
    public final NumberSetting placeRange = new NumberSetting("Place Range", 4.5, 2.0, 6.0, 0.1);
    public final NumberSetting breakRange = new NumberSetting("Break Range", 4.5, 2.0, 6.0, 0.1);
    public final NumberSetting targetRange = new NumberSetting("Target Range", 8.0, 3.0, 15.0, 0.5);
    public final NumberSetting placeDelay = new NumberSetting("Place Delay", 100, 0, 500, 10);
    public final NumberSetting breakDelay = new NumberSetting("Break Delay", 50, 0, 500, 10);
    public final BooleanSetting autoPlace = new BooleanSetting("Auto Place", true);
    public final BooleanSetting autoBreak = new BooleanSetting("Auto Break", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);
    private long lp = 0;
    private long lb = 0;

    public CrystalAuraModule() {
        super("CrystalAura", Category.COMBAT, false);
        addSetting(placeRange);
        addSetting(breakRange);
        addSetting(targetRange);
        addSetting(placeDelay);
        addSetting(breakDelay);
        addSetting(autoPlace);
        addSetting(autoBreak);
        addSetting(swing);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        Player target = findTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (autoBreak.value && now - lb >= breakDelay.value) {
            EndCrystal c = findCrystal(target);
            if (c != null) {
                mc.gameMode.attack(mc.player, c);
                if (swing.value) mc.player.swing(InteractionHand.MAIN_HAND);
                lb = now;
            }
        }

        if (autoPlace.value && now - lp >= placeDelay.value) {
            BlockPos pos = findPlacePos(target);
            if (pos != null) {
                int slot = findHotbar(Items.END_CRYSTAL);
                if (slot != -1) {
                    int old = getSelectedSlot();
                    setSelectedSlot(slot);
                    mc.gameMode.useItemOn(
                        mc.player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)
                    );
                    if (swing.value) mc.player.swing(InteractionHand.MAIN_HAND);
                    setSelectedSlot(old);
                    lp = now;
                }
            }
        }
    }

    private Player findTarget() {
        Player best = null;
        double bestDist = targetRange.value;
        for (Player p : mc.level.players()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private EndCrystal findCrystal(Player t) {
        EndCrystal best = null;
        double bestDist = breakRange.value;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof EndCrystal c) || !c.isAlive()) continue;
            double d = mc.player.distanceTo(c);
            if (d > bestDist || c.position().distanceTo(t.position()) > 6.0) continue;
            bestDist = d;
            best = c;
        }
        return best;
    }

    private BlockPos findPlacePos(Player t) {
        BlockPos tp = t.blockPosition();
        BlockPos[] candidates = {
            tp.offset(1, 0, 0), tp.offset(-1, 0, 0), tp.offset(0, 0, 1), tp.offset(0, 0, -1),
            tp.offset(0, -1, 0), tp.offset(0, -2, 0),
            tp.offset(1, 1, 0), tp.offset(-1, 1, 0), tp.offset(0, 1, 1), tp.offset(0, 1, -1)
        };

        double maxDistSq = placeRange.value * placeRange.value;
        for (BlockPos p : candidates) {
            if (mc.player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > maxDistSq) continue;
            if (!mc.level.getBlockState(p).isAir() || !mc.level.getBlockState(p.above()).isAir()) continue;
            BlockPos base = p.below();
            if (!mc.level.getBlockState(base).is(Blocks.OBSIDIAN) && !mc.level.getBlockState(base).is(Blocks.BEDROCK)) continue;

            AABB box = new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1.0, p.getY() + 2.0, p.getZ() + 1.0);
            if (!mc.level.getEntities(null, box).isEmpty()) continue;
            return p;
        }
        return null;
    }

    private int findHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(item)) return i;
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