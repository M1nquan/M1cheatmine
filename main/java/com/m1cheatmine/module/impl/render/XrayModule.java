package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.module.*;
import com.m1cheatmine.module.Module;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class XrayModule extends Module {
    public static XrayModule INSTANCE;
    private double savedGamma = 1.0;

    public static final Set<Block> TARGET_BLOCKS = Set.of(
        Blocks.DIAMOND_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE,
        Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.GOLD_ORE,
        Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.IRON_ORE,
        Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE,
        Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.COAL_ORE,
        Blocks.DEEPSLATE_COAL_ORE,
        Blocks.LAPIS_ORE,
        Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.REDSTONE_ORE,
        Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.ANCIENT_DEBRIS,
        Blocks.NETHER_QUARTZ_ORE,
        Blocks.NETHER_GOLD_ORE,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.BARREL,
        Blocks.SPAWNER
    );

    public XrayModule() {
        super("X-Ray", Category.RENDER, false);
        INSTANCE = this;
    }

    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.enabled;
    }

    public static boolean isTargetBlock(BlockState state) {
        return state != null && TARGET_BLOCKS.contains(state.getBlock());
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedGamma = mc.options.gamma().get();
            mc.options.gamma().set(15.0);
        }
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.gamma().set(savedGamma);
        }
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }
}