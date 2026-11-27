package com.m1cheatmine.module.impl.render;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import com.m1cheatmine.module.setting.NumberSetting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class HoleEspModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 8.0, 2.0, 20.0, 1.0);
    public final NumberSetting height = new NumberSetting("Height", 0.25, 0.05, 1.0, 0.05);
    public final BooleanSetting bedrockHoles = new BooleanSetting("Bedrock Holes", true);
    public final BooleanSetting obsidianHoles = new BooleanSetting("Obsidian Holes", true);

    private final List<HoleEntry> holes = new ArrayList<>();
    private long lastScan = 0;

    public HoleEspModule() {
        super("HoleESP", Category.RENDER, false);
        addSetting(range);
        addSetting(height);
        addSetting(bedrockHoles);
        addSetting(obsidianHoles);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            holes.clear();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastScan < 100) return;
        lastScan = now;
        scanHoles();
    }

    @Override
    public void onDisable() {
        holes.clear();
    }

    public void onRenderWorld(PoseStack ps, Camera camera) {
        onRenderWorld(ps, camera, null);
    }

    public void onRenderWorld(PoseStack ps, Camera camera, DeltaTracker deltaTracker) {
        if (!enabled || mc.player == null || mc.level == null || holes.isEmpty()) return;

        Vec3 cam = resolveCameraPos(camera);
        RenderType lineType = resolveLineRenderType();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(lineType);

        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ps.last().pose();

        for (HoleEntry hole : holes) {
            BlockPos p = hole.pos;
            float x1 = p.getX(), y1 = p.getY(), z1 = p.getZ();
            float x2 = x1 + 1.0f, y2 = y1 + (float) height.value, z2 = z1 + 1.0f;

            float r = hole.isBedrock ? 0.20f : 0.37f;
            float g = hole.isBedrock ? 0.85f : 0.66f;
            float bl = hole.isBedrock ? 0.55f : 1.00f;

            drawBox(vc, m, x1, y1, z1, x2, y2, z2, r, g, bl, 1.0f);
        }

        ps.popPose();
        bufferSource.endBatch(lineType);
    }

    private static RenderType resolveLineRenderType() {
        String[] candidates = {"lines", "debugLineStrip", "debugLines"};
        for (String name : candidates) {
            try {
                Method m = RenderType.class.getMethod(name);
                if (Modifier.isStatic(m.getModifiers()) && RenderType.class.isAssignableFrom(m.getReturnType())) {
                    Object val = m.invoke(null);
                    if (val instanceof RenderType rt) return rt;
                }
            } catch (Throwable ignored) {
            }
        }
        for (Field f : RenderType.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && RenderType.class.isAssignableFrom(f.getType())) {
                try {
                    Object val = f.get(null);
                    if (val instanceof RenderType rt) return rt;
                } catch (Throwable ignored) {
                }
            }
        }
        throw new IllegalStateException("Cannot resolve line RenderType");
    }

    private static Vec3 resolveCameraPos(Camera camera) {
        try {
            return (Vec3) camera.getClass().getMethod("position").invoke(camera);
        } catch (Throwable ignored) {
        }
        try {
            return (Vec3) camera.getClass().getMethod("getPosition").invoke(camera);
        } catch (Throwable ignored) {
        }
        return Vec3.ZERO;
    }

    private void scanHoles() {
        holes.clear();
        int r = (int) range.value;
        BlockPos center = mc.player.blockPosition();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > range.value * range.value) continue;
                    HoleEntry entry = classifyHole(pos);
                    if (entry != null) holes.add(entry);
                }
            }
        }
    }

    private HoleEntry classifyHole(BlockPos pos) {
        if (!isAirOrPassable(pos) || !isAirOrPassable(pos.above()) || !isAirOrPassable(pos.above(2))) return null;
        BlockPos floor = pos.below();
        BlockPos[] walls = { pos.north(), pos.south(), pos.east(), pos.west() };
        boolean allBedrock = isBedrock(floor);
        if (!isBedrockOrObsidian(floor)) return null;
        for (BlockPos w : walls) {
            if (!isBedrockOrObsidian(w)) return null;
            if (!isBedrock(w)) allBedrock = false;
        }
        if (allBedrock && bedrockHoles.value) return new HoleEntry(pos.immutable(), true);
        if (!allBedrock && obsidianHoles.value) return new HoleEntry(pos.immutable(), false);
        return null;
    }

    private boolean isAirOrPassable(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(mc.level, pos).isEmpty();
    }

    private boolean isBedrock(BlockPos pos) { return mc.level.getBlockState(pos).is(Blocks.BEDROCK); }
    private boolean isBedrockOrObsidian(BlockPos pos) {
        Block b = mc.level.getBlockState(pos).getBlock();
        return b == Blocks.BEDROCK || b == Blocks.OBSIDIAN;
    }

    private void drawBox(VertexConsumer vc, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float bl, float a) {
        line(vc, m, x1, y1, z1, x2, y1, z1, r, g, bl, a);
        line(vc, m, x2, y1, z1, x2, y1, z2, r, g, bl, a);
        line(vc, m, x2, y1, z2, x1, y1, z2, r, g, bl, a);
        line(vc, m, x1, y1, z2, x1, y1, z1, r, g, bl, a);

        line(vc, m, x1, y2, z1, x2, y2, z1, r, g, bl, a);
        line(vc, m, x2, y2, z1, x2, y2, z2, r, g, bl, a);
        line(vc, m, x2, y2, z2, x1, y2, z2, r, g, bl, a);
        line(vc, m, x1, y2, z2, x1, y2, z1, r, g, bl, a);

        line(vc, m, x1, y1, z1, x1, y2, z1, r, g, bl, a);
        line(vc, m, x2, y1, z1, x2, y2, z1, r, g, bl, a);
        line(vc, m, x2, y1, z2, x2, y2, z2, r, g, bl, a);
        line(vc, m, x1, y1, z2, x1, y2, z2, r, g, bl, a);
    }

    private void line(VertexConsumer vc, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2,
                      float r, float g, float bl, float a) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        vc.addVertex(m, x1, y1, z1).setColor(r, g, bl, a).setNormal(len > 0 ? dx / len : 0f, len > 0 ? dy / len : 1f, len > 0 ? dz / len : 0f);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, bl, a).setNormal(len > 0 ? dx / len : 0f, len > 0 ? dy / len : 1f, len > 0 ? dz / len : 0f);
    }

    private static class HoleEntry {
        final BlockPos pos;
        final boolean isBedrock;
        HoleEntry(BlockPos pos, boolean isBedrock) { this.pos = pos; this.isBedrock = isBedrock; }
    }
}