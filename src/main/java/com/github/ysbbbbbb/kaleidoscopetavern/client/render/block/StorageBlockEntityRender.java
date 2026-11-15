package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.StorageRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class StorageBlockEntityRender<T extends StorageBlockEntity> implements BlockEntityRenderer<T, StorageRenderState> {
    private final BlockModelResolver resolver;

    protected StorageBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.resolver = context.blockModelResolver();
    }

    @Override
    public StorageRenderState createRenderState() {
        return new StorageRenderState();
    }

    @Override
    public void extractRenderState(T entity, StorageRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.direction = entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        var items = entity.getItems();
        state.items = new BlockModelRenderState[items.getSlots()];
        for (int i = 0; i < state.items.length; i++) {
            var model = state.items[i] = new BlockModelRenderState();
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem item) {
                resolver.update(model, item.getBlock().defaultBlockState(), BarCabinetBlockEntityRender.BLOCK_DISPLAY_CONTEXT);
            }
        }
    }

    protected void applyFacingRotation(Direction direction, PoseStack poseStack) {
        float angle = 180 - direction.get2DDataValue() * 90f;
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, 0, -0.5);
    }

    protected void renderStack(BlockModelRenderState stack, PoseStack poseStack, SubmitNodeCollector collector,
                               int packedLight, int packedOverlay, double x, double y, double z,
                               float scale, double yRot, double xRot) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) xRot));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, 0, -0.5);
        stack.submit(poseStack, collector, packedLight, packedOverlay, 0);
        poseStack.popPose();
    }
}
