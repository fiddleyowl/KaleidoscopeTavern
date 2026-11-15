package com.github.ysbbbbbb.kaleidoscopetavern.compat.rei;

import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;

public abstract class TavernRecipeDisplay extends DefaultCustomDisplay {
    private final RecipeHolder<?> recipe;

    public TavernRecipeDisplay(RecipeHolder<?> recipe, List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs, Optional.of(recipe.id().identifier()));
        this.recipe = recipe;
    }

    public Optional<RecipeHolder<?>> getOptionalRecipe() {
        return Optional.of(recipe);
    }
}
