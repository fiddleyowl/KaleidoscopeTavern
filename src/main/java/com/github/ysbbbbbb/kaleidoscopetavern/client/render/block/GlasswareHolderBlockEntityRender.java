package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.GlasswareHolderBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.StorageRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class GlasswareHolderBlockEntityRender extends StorageBlockEntityRender<GlasswareHolderBlockEntity> {
    public GlasswareHolderBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(StorageRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.items.length; i++) {
            if (state.items[i].isEmpty()) continue;
            poseStack.pushPose();
            poseStack.translate(-0.25, 0.76, 0.75);
            poseStack.translate(0.5 * (i % 2), 0, 0.5 * (i / 2));
            poseStack.mulPose(Axis.XN.rotationDegrees(180));
            state.items[i].submit(poseStack, collector, state.lightCoords, 0, 0);
            poseStack.popPose();
        }
    }
}
