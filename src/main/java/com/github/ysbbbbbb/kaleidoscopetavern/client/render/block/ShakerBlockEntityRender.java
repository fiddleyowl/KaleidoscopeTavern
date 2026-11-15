package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.mixology.ShakerModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.ShakerRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ShakerBlockEntityRender implements BlockEntityRenderer<ShakerBlockEntity, ShakerRenderState> {
    private static final Identifier TEXTURE = KaleidoscopeTavern.modLoc("textures/block/mixology/shaker.png");
    private final ShakerModel model;

    public ShakerBlockEntityRender(BlockEntityRendererProvider.Context context) {
        model = new ShakerModel(context.bakeLayer(ShakerModel.LAYER_LOCATION));
    }

    @Override
    public ShakerRenderState createRenderState() {
        return new ShakerRenderState();
    }

    @Override
    public void extractRenderState(ShakerBlockEntity entity, ShakerRenderState state, float partialTicks, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, camera, breakProgress);
        state.putMillis = entity.getLevel() != null && entity.putState.isStarted()
                ? entity.putState.getTimeInMillis(entity.getLevel().getGameTime() + partialTicks) : -1;
    }

    @Override
    public void submit(ShakerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180));
        collector.submitModel(model, state, poseStack, model.renderType(TEXTURE), state.lightCoords, 0, -1, null, 0, null);
        poseStack.popPose();
    }
}
