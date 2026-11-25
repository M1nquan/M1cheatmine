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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TracersModule extends Module {
    public final NumberSetting range = new NumberSetting("Range", 128.0, 16.0, 256.0, 8.0);
    public final BooleanSetting colorByDist = new BooleanSetting("DistanceColor", true);

    public TracersModule() {
        super("Tracers", Category.RENDER, false);
        addSetting(range);
        addSetting(colorByDist);
    }

    public void onRenderWorld(PoseStack ps, Camera camera, DeltaTracker deltaTracker) {
        if (!enabled || mc.level == null || mc.player == null) return;

        Vec3 cam = camera.getPosition();
        Vector3f look = camera.getLookVector();
        Vec3 start = cam.add(look.x * 0.2, look.y * 0.2, look.z * 0.2);
        float pt = deltaTracker != null ? deltaTracker.getGameTimeDeltaPartialTick(true) : 1.0f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ps.last().pose();
        double maxDistSq = range.value * range.value;

        for (Player p : mc.level.players()) {
            if (p == mc.player || !p.isAlive()) continue;
            double distSq = p.distanceToSqr(mc.player);
            if (distSq > maxDistSq) continue;

            double tx = Mth.lerp(pt, p.xOld, p.getX());
            double ty = Mth.lerp(pt, p.yOld, p.getY()) + p.getBbHeight() * 0.5;
            double tz = Mth.lerp(pt, p.zOld, p.getZ());

            float r = 0.37f, g = 0.66f, bl = 1.0f;
            if (colorByDist.value) {
                float factor = Mth.clamp((float) (Math.sqrt(distSq) / 40.0), 0f, 1f);
                r = 1.0f - factor * 0.63f;
                g = 0.25f + factor * 0.41f;
                bl = 0.25f + factor * 0.75f;
            }

            float dx = (float) (tx - start.x), dy = (float) (ty - start.y), dz = (float) (tz - start.z);
            float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float nx = len > 0 ? dx / len : 0f, ny = len > 0 ? dy / len : 1f, nz = len > 0 ? dz / len : 0f;

            vc.addVertex(m, (float) start.x, (float) start.y, (float) start.z).setColor(r, g, bl, 1f).setNormal(nx, ny, nz);
            vc.addVertex(m, (float) tx, (float) ty, (float) tz).setColor(r, g, bl, 1f).setNormal(nx, ny, nz);
        }

        ps.popPose();
        bufferSource.endBatch(RenderType.lines());
    }
}