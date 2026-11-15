package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.CellarCabinetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.StorageRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class CellarCabinetBlockEntityRender extends StorageBlockEntityRender<CellarCabinetBlockEntity> {
    public CellarCabinetBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(StorageRenderState renderState, PoseStack poseStack, SubmitNodeCollector buffer, CameraRenderState camera) {
        Direction direction = renderState.direction;
        var items = renderState.items;

        poseStack.pushPose();
        this.applyFacingRotation(direction, poseStack);
        for (int i = 0; i < items.length; i++) {
            BlockModelRenderState stack = items[i];
            if (stack.isEmpty()) {
                continue;
            }

            int row = i / 3;
            int column = i % 3;
            double x = 0.825 - column * 0.325;
            double y = 0.78 - row * 0.29;

            this.renderStack(stack, poseStack, buffer, renderState.lightCoords, 0,
                    x, y, 0.875, 1, 0, -90);
        }
        poseStack.popPose();
    }
}

