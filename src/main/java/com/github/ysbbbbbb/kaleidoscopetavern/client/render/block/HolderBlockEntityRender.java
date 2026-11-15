package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.HolderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.StorageRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class HolderBlockEntityRender extends StorageBlockEntityRender<HolderBlockEntity> {
    public HolderBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(StorageRenderState renderState, PoseStack poseStack, SubmitNodeCollector buffer, CameraRenderState camera) {
        Direction direction = renderState.direction;
        BlockModelRenderState stack = renderState.items[0];
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        this.applyFacingRotation(direction, poseStack);
        this.renderStack(stack, poseStack, buffer, renderState.lightCoords, 0,
                0.5, 0.125, 0.75, 0.95f, 0, -45);
        poseStack.popPose();
    }
}

