package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.TiltedRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.StorageRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

@SuppressWarnings("deprecation")
public class TiltedRackBlockEntityRender extends StorageBlockEntityRender<TiltedRackBlockEntity> {
    public TiltedRackBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(StorageRenderState renderState, PoseStack poseStack, SubmitNodeCollector buffer, CameraRenderState camera) {
        Direction direction = renderState.direction;
        var items = renderState.items;

        float scale = 0.9f;
        float angle = 180 - direction.get2DDataValue() * 90f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, 0, -0.5);

        for (int i = 0; i < items.length; i++) {
            BlockModelRenderState stack = items[i];
            if (stack.isEmpty()) {
                continue;
            }

            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.425 - 0.375 * i, 0.3125, 0.02 + (i - 1) * 0.005);
            poseStack.mulPose(Axis.XP.rotationDegrees(22.5f));
            stack.submit(poseStack, buffer, renderState.lightCoords, 0, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
