package com.github.ysbbbbbb.kaleidoscopetavern.client.render.misc;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.PotionBottleBlockEntity;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class PotionBottleColor implements BlockTintSource {
    @Override public int color(BlockState state) { return -1; }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0xFFFFFF;
        }
        if (level.getBlockEntity(pos) instanceof PotionBottleBlockEntity be && !be.getPotionStack().isEmpty()) {
            PotionContents contents = be.getPotionStack().get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                return contents.getColor();
            }
        }
        return 0xFFFFFF;
    }
}
