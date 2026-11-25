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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class EspModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 64.0, 8.0, 128.0, 4.0);
    public final BooleanSetting players = new BooleanSetting("Players", true);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    public final BooleanSetting items = new BooleanSetting("Items", false);

    public EspModule() {
        super("PlayerESP", Category.RENDER, false);
        addSetting(range);
        addSetting(players);
        addSetting(mobs);
        addSetting(items);
    }

    public void onRenderWorld(PoseStack ps, Camera camera, DeltaTracker deltaTracker) {
        if (!enabled || mc.level == null || mc.player == null) return;

        Vec3 cam = camera.getPosition();
        float pt = deltaTracker != null ? deltaTracker.getGameTimeDeltaPartialTick(true) : 1.0f;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ps.last().pose();
        double maxDistSq = range.value * range.value;

        for (Entity ent : mc.level.entitiesForRendering()) {
            if (ent == mc.player || !ent.isAlive()) continue;
            if (ent.distanceToSqr(mc.player) > maxDistSq) continue;

            float r, g, bl;
            if (players.value && ent instanceof Player) {
                r = 0.37f; g = 0.66f; bl = 1.0f;
            } else if (mobs.value && ent instanceof Monster) {
                r = 0.95f; g = 0.30f; bl = 0.30f;
            } else if (items.value && ent instanceof ItemEntity) {
                r = 0.30f; g = 0.90f; bl = 0.55f;
            } else {
                continue;
            }

            double ix = Mth.lerp(pt, ent.xOld, ent.getX()) - ent.getX();
            double iy = Mth.lerp(pt, ent.yOld, ent.getY()) - ent.getY();
            double iz = Mth.lerp(pt, ent.zOld, ent.getZ()) - ent.getZ();
            AABB bb = ent.getBoundingBox().move(ix, iy, iz);

            drawBox(vc, m, (float) bb.minX, (float) bb.minY, (float) bb.minZ,
                    (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, r, g, bl, 1.0f);
        }

        ps.popPose();
        bufferSource.endBatch(RenderType.lines());
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
        float nx = len > 0 ? dx / len : 0f;
        float ny = len > 0 ? dy / len : 1f;
        float nz = len > 0 ? dz / len : 0f;
        vc.addVertex(m, x1, y1, z1).setColor(r, g, bl, a).setNormal(nx, ny, nz);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, bl, a).setNormal(nx, ny, nz);
    }
}