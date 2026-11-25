package com.m1cheatmine.module.impl.player;

import com.m1cheatmine.module.Category;
import com.m1cheatmine.module.Module;
import com.m1cheatmine.module.setting.BooleanSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FastPlaceModule extends Module {
    public final BooleanSetting blocksOnly = new BooleanSetting("BlocksOnly", true);

    public FastPlaceModule() {
        super("FastPlace", Category.PLAYER, false);
        addSetting(blocksOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || mc.screen != null) return;
        if (!mc.options.keyUse.isDown()) return;
        if (blocksOnly.value && !(mc.player.getMainHandItem().getItem() instanceof BlockItem)) return;

        if (mc.hitResult instanceof BlockHitResult bhr && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, bhr);
        }
    }
}