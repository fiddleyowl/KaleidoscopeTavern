package com.github.ysbbbbbb.kaleidoscopetavern.compat.ponder.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.createmod.ponder.api.client.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class TavernPonderTags {
    public static final Identifier BREWING = KaleidoscopeTavern.modLoc("brewing");

    public static void register(PonderTagRegistrationHelper<Identifier> helper) {
        helper.registerTag(BREWING).addToIndex()
                .icon("brewing")
                .title("")
                .description("")
                .register();

        helper.addToTag(BREWING)
                .add(Objects.requireNonNull(ModItems.GRAPEVINE.getId()))
                .add(Objects.requireNonNull(ModItems.PRESSING_TUB.getId()))
                .add(Objects.requireNonNull(ModItems.BARREL.getId()));
    }
}
