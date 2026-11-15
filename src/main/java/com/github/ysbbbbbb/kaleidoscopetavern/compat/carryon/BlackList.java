package com.github.ysbbbbbb.kaleidoscopetavern.compat.carryon;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.neoforged.fml.InterModComms;
import net.minecraft.core.registries.BuiltInRegistries;

public class BlackList {
    private static final String CARRY_ON_ID = "carryon";

    public static void addBlackList() {
        BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals(KaleidoscopeTavern.MOD_ID))
                .forEach(id -> InterModComms.sendTo(CARRY_ON_ID, "blacklistBlock", id::toString));
    }
}
