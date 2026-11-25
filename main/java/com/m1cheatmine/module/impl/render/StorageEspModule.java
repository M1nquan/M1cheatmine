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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class StorageEspModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 24.0, 4.0, 64.0, 2.0);
    public final BooleanSetting chests = new BooleanSetting("Chests", true);
    public final BooleanSetting enderChests = new BooleanSetting("Ender Chests", true);
    public final BooleanSetting shulkers = new BooleanSetting("Shulkers", true);
    public final BooleanSetting barrels = new BooleanSetting("Barrels", true);

    private final List<StorageEntry> entries = new ArrayList<>();
    private long lastScan = 0;

    public StorageEspModule() {
        super("StorageESP", Category.RENDER, false);
        addSetting(range);
        addSetting(chests);
        addSetting(enderChests);
        addSetting(shulkers);
        addSetting(barrels);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            entries.clear();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastScan < 150) return;
        lastScan = now;
        scanStorages();
    }

    @Override
    public void onDisable() {
        entries.clear();
    }

    public void onRenderWorld(PoseStack ps, Camera camera) {
        onRenderWorld(ps, camera, null);
    }

    public void onRenderWorld(PoseStack ps, Camera camera, DeltaTracker deltaTracker) {
        if (!enabled || mc.player == null || mc.level == null || entries.isEmpty()) return;

        Vec3 cam = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ps.last().pose();

        for (StorageEntry entry : entries) {
            BlockPos p = entry.pos;
            drawBox(vc, m, p.getX() + 0.0625f, p.getY(), p.getZ() + 0.0625f,
                    p.getX() + 0.9375f, p.getY() + 0.875f, p.getZ() + 0.9375f,
                    entry.r, entry.g, entry.bl, 1.0f);
        }

        ps.popPose();
        bufferSource.endBatch(RenderType.lines());
    }

    private void scanStorages() {
        entries.clear();
        double maxDistSq = range.value * range.value;
        int chunkRadius = ((int) Math.ceil(range.value)) >> 4;
        int pcx = mc.player.chunkPosition().x;
        int pcz = mc.player.chunkPosition().z;

        for (int cx = pcx - chunkRadius; cx <= pcx + chunkRadius; cx++) {
            for (int cz = pcz - chunkRadius; cz <= pcz + chunkRadius; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getBlockPos();
                    if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxDistSq) continue;
                    StorageEntry matched = classifyBlock(pos, be.getBlockState().getBlock());
                    if (matched != null) entries.add(matched);
                }
            }
        }
    }

    private StorageEntry classifyBlock(BlockPos pos, Block block) {
        if (chests.value && block instanceof ChestBlock) return new StorageEntry(pos.immutable(), 1.00f, 0.75f, 0.20f);
        if (enderChests.value && block instanceof EnderChestBlock) return new StorageEntry(pos.immutable(), 0.65f, 0.35f, 1.00f);
        if (shulkers.value && block instanceof ShulkerBoxBlock) return new StorageEntry(pos.immutable(), 0.95f, 0.35f, 0.70f);
        if (barrels.value && block instanceof BarrelBlock) return new StorageEntry(pos.immutable(), 0.37f, 0.66f, 1.00f);
        return null;
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

    private static class StorageEntry {
        final BlockPos pos;
        final float r, g, bl;
        StorageEntry(BlockPos pos, float r, float g, float bl) { this.pos = pos; this.r = r; this.g = g; this.bl = bl; }
    }
}