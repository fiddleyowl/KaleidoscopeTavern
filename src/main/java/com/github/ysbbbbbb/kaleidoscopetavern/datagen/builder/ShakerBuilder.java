package com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.ShakerRecipeSerializer;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShakerBuilder implements RecipeBuilder {
    private static final String NAME = "shaker";

    private final List<Ingredient> ingredients = Lists.newArrayList();
    private ItemStackTemplate result;

    public static ShakerBuilder builder() {
        return new ShakerBuilder();
    }

    public ShakerBuilder addIngredient(ItemLike itemLike) {
        this.ingredients.add(Ingredient.of(itemLike));
        return this;
    }

    public ShakerBuilder addIngredient(HolderSet<Item> tag) {
        this.ingredients.add(Ingredient.of(tag));
        return this;
    }

    public ShakerBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public ShakerBuilder setResult(ItemLike itemLike) {
        this.result = new ItemStackTemplate(itemLike.asItem());
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override public ResourceKey<Recipe<?>> defaultId() {
        String path = RecipeBuilder.getDefaultRecipeId(this.result).identifier().getPath();
        return ResourceKey.create(Registries.RECIPE, KaleidoscopeTavern.modLoc(NAME + "/" + path));
    }
    @Override public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        if (ingredients.size() != ShakerRecipeSerializer.MAX_INGREDIENTS) throw new IllegalStateException("Shaker recipes require three ingredients");
        output.accept(key, new ShakerRecipe(List.copyOf(ingredients), result, Int2ObjectMaps.emptyMap()), null);
    }
}
