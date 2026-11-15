package com.github.ysbbbbbb.kaleidoscopetavern.client.render.misc;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SignatureCocktailColor {
    public static class Block implements BlockTintSource {
        @Override public int color(BlockState state) { return -1; }
        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (level == null || pos == null) {
                return 0xFFFFFF;
            }
            if (level.getBlockEntity(pos) instanceof SignatureCocktailBlockEntity be) {
                return be.getColor();
            }
            return 0xFFFFFF;
        }
    }

    public record Item() implements ItemTintSource {
        public static final com.mojang.serialization.MapCodec<Item> CODEC = com.mojang.serialization.MapCodec.unit(Item::new);
        @Override public int calculate(ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.LivingEntity owner) {
            return 0xFF000000 | SignatureCocktailBlockItem.getColor(stack);
        }
        @Override public com.mojang.serialization.MapCodec<Item> type() { return CODEC; }
    }
}
