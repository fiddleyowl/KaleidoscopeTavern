package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.client.init.ClientRecipeEvent;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.BarrelRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.PressingTubCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.ShakerRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

@JeiPlugin
public final class JeiSmokeObserver implements IModPlugin {
    private static volatile IJeiRuntime runtime;

    @Override public Identifier getPluginUid() { return KaleidoscopeTavern.modLoc("port_validation"); }
    @Override public void onRuntimeAvailable(IJeiRuntime value) { runtime = value; }

    public static boolean ready() {
        if (runtime == null) return false;
        var manager = runtime.getRecipeManager();
        long barrel = manager.createRecipeLookup(BarrelRecipeCategory.TYPE).get().count();
        long pressing = manager.createRecipeLookup(PressingTubCategory.TYPE).get().count();
        long shaker = manager.createRecipeLookup(ShakerRecipeCategory.TYPE).get().count();
        if (barrel != ClientRecipeEvent.BARREL_RECIPES.size()
                || pressing != ClientRecipeEvent.PRESSING_TUB_RECIPES.size() || shaker != 12) {
            throw new IllegalStateException("JEI recipes: barrel=" + barrel + ", pressing=" + pressing + ", shaker=" + shaker);
        }
        KaleidoscopeTavern.LOGGER.info("JEI_SMOKE_PASS: barrel={}, pressing={}, shaker={}", barrel, pressing, shaker);
        return true;
    }
}
