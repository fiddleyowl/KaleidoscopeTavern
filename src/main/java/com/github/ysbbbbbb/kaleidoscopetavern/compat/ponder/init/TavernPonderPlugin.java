package com.github.ysbbbbbb.kaleidoscopetavern.compat.ponder.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.createmod.ponder.api.client.registration.PonderPlugin;
import net.createmod.ponder.api.client.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.client.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.resources.Identifier;

public class TavernPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return KaleidoscopeTavern.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        TavernPonderScreen.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
        TavernPonderTags.register(helper);
    }

    public static void init() {
        PonderIndex.addPlugin(new TavernPonderPlugin());
    }
}
