package com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SignatureCocktailBlockEntity extends BaseBlockEntity {
    private static final Codec<List<DrinkEffectData.Entry>> EFFECTS_CODEC = Codec.list(DrinkEffectData.Entry.CODEC);

    private List<DrinkEffectData.Entry> effects = Lists.newArrayList();
    private int color = 0x5555ff;

    public SignatureCocktailBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SIGNATURE_COCKTAIL_BE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.effects = input.read("effects", EFFECTS_CODEC).orElse(List.of());
        this.color = input.getIntOr("color", 0x5555ff);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("effects", EFFECTS_CODEC, this.effects);
        output.putInt("color", this.color);
    }

    public List<DrinkEffectData.Entry> getEffects() {
        return effects;
    }

    public void setEffects(List<DrinkEffectData.Entry> effects) {
        this.effects = effects;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
