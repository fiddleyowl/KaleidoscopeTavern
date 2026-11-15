package com.github.ysbbbbbb.kaleidoscopetavern.client.render.entity;

import com.github.ysbbbbbb.kaleidoscopetavern.client.event.RenderStateEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class StringLightsLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public StringLightsLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        var item = state.getRenderData(RenderStateEvent.STRING_LIGHTS);
        if (item == null || item.isEmpty() || !(this.getParentModel() instanceof HumanoidModel<?> model)) return;
        poseStack.pushPose();
        model.body.translateAndRotate(poseStack);
        poseStack.translate(0, -0.1875F, -0.4375F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.scale(-0.625F, -0.625F, 0.625F);
        item.submit(poseStack, collector, light, 0, 0);
        poseStack.popPose();
    }
}
