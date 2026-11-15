package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.SandwichBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.TextBlockRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;

public class SandwichBlockEntityRender extends TextBlockEntityRender<SandwichBlockEntity, TextBlockRenderState> {
    private static final float TEXT_SCALE = 0.01f;
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_LINES = 8;
    private static final int MAX_WIDTH = 55;

    public SandwichBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TextBlockRenderState createRenderState() {
        return new TextBlockRenderState();
    }

    @Override
    public void submit(TextBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (state.text == null || state.text.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        float angle = state.rotation;
        float radians = (float)Math.toRadians(angle);
        float xOffset = (float)(-Math.sin(radians) * 0.06F);
        float zOffset = (float)(Math.cos(radians) * 0.06F);
        poseStack.translate(0.5 + xOffset, 1.06, 0.5 + zOffset);
        poseStack.mulPose(new org.joml.Quaternionf().rotateAxis((float)Math.toRadians(22.5F), (float)-Math.cos(radians), 0, (float)-Math.sin(radians)));
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));
        doTextRender(state, poseStack, submitNodeCollector, state.text, MAX_WIDTH, TEXT_SCALE, MAX_LINES, LINE_HEIGHT);

        poseStack.popPose();
    }

    @Override
    protected Component getRenderText(String text) {
        return Component.literal(text).withStyle(ChatFormatting.BOLD);
    }
}
