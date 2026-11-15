package com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

public class ShakerRecipeSerializer {
    public static final int MAX_INGREDIENTS = 3;

    public static final MapCodec<ShakerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.sizeLimitedListOf(MAX_INGREDIENTS).fieldOf("ingredients").forGetter(ShakerRecipe::ingredients),
            net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShakerRecipe::result)
    ).apply(instance, (ingredients, result) -> new ShakerRecipe(ingredients, result, Int2ObjectMaps.emptyMap())));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShakerRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ShakerRecipe decode(RegistryFriendlyByteBuf buf) {
            int size = Math.min(MAX_INGREDIENTS, buf.readVarInt());
            java.util.List<Ingredient> ingredients = new java.util.ArrayList<>();
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            }

            net.minecraft.world.item.ItemStackTemplate result = net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(buf);

            Int2ObjectMap<ChatFormatting> ingredientColors = new Int2ObjectOpenHashMap<>();
            int colorSize = buf.readVarInt();
            for (int i = 0; i < colorSize; i++) {
                int index = buf.readVarInt();
                ChatFormatting formatting = ChatFormatting.getById(buf.readVarInt());
                if (formatting != null) {
                    ingredientColors.put(index, formatting);
                }
            }
            return new ShakerRecipe(ingredients, result, ingredientColors);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ShakerRecipe recipe) {
            var nonEmpty = recipe.ingredients();
            buf.writeVarInt(nonEmpty.size());
            for (Ingredient ingredient : nonEmpty) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }

            net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result());

            Int2ObjectMap<ChatFormatting> ingredientColors = recipe.ingredientColors();
            buf.writeVarInt(ingredientColors.size());
            ingredientColors.forEach((index, formatting) -> {
                buf.writeVarInt(index);
                buf.writeVarInt(formatting.getId());
            });
        }
    };

}
