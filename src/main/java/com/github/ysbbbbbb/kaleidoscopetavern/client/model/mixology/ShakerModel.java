package com.github.ysbbbbbb.kaleidoscopetavern.client.model.mixology;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.ShakerRenderState;
import com.github.ysbbbbbb.kaleidoscopetavern.client.animation.ShakerAnimation;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ShakerModel extends Model<ShakerRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(KaleidoscopeTavern.modLoc("shaker"), "main");

    private final KeyframeAnimation putAnimation;

    public ShakerModel(ModelPart root) {
        super(root.getChild("root"), RenderTypes::entityCutout);
        this.putAnimation = ShakerAnimation.PUT.bake(this.root());
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(23, 28).addBox(-3.0F, 0.75F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(-3.5F, -6.25F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.25F, 0.0F));

        PartDefinition bone2 = root.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(3, 36).addBox(-1.5F, -3.8333F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(-3.5F, 0.1667F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(-3.0F, -1.8333F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.4167F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ShakerRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (state.putMillis >= 0) this.putAnimation.apply(state.putMillis, 1.0F);
    }
}
