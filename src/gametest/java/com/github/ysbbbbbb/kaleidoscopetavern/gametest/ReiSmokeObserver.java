package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.client.init.ClientRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.rei.category.ReiBarrelRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.rei.category.ReiPressingTubRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.rei.category.ReiShakerRecipeCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;

import java.util.List;

public final class ReiSmokeObserver {
    public static boolean ready() {
        var displays = DisplayRegistry.getInstance().getAll();
        int barrel = displays.getOrDefault(ReiBarrelRecipeCategory.ID, List.of()).size();
        int pressing = displays.getOrDefault(ReiPressingTubRecipeCategory.ID, List.of()).size();
        int shaker = displays.getOrDefault(ReiShakerRecipeCategory.ID, List.of()).size();
        if (barrel == 0 && pressing == 0 && shaker == 0) return false;
        if (barrel != ClientRecipeEvent.BARREL_RECIPES.size() || pressing != 6 || shaker != 12) {
            throw new IllegalStateException("REI recipes: barrel=" + barrel + ", pressing=" + pressing + ", shaker=" + shaker);
        }
        var categories = CategoryRegistry.getInstance();
        for (var id : List.of(ReiBarrelRecipeCategory.ID, ReiPressingTubRecipeCategory.ID, ReiShakerRecipeCategory.ID)) {
            var title = categories.get(id).getCategory().getTitle();
            ClientSmokeTest.assertTranslated(title, id.toString());
            KaleidoscopeTavern.LOGGER.info("REI_TITLE_PASS: {} = {}", id, title.getString());
        }
        KaleidoscopeTavern.LOGGER.info("REI_SMOKE_PASS: barrel={}, pressing={}, shaker={}", barrel, pressing, shaker);
        return true;
    }
}
