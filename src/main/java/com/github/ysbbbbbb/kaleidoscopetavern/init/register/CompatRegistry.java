package com.github.ysbbbbbb.kaleidoscopetavern.init.register;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.carryon.BlackList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID)
public class CompatRegistry {
    public static final String CARRY_ON = "carryon";

    @SubscribeEvent
    public static void onRegister(net.neoforged.neoforge.registries.RegisterEvent event) {
        // Keep this workaround confined to the affected dependency release.
        boolean affected = ModList.get().getModContainerById("catnip")
                .map(mod -> mod.getModInfo().getVersion().toString().equals("1.0.52+mc26.1.2"))
                .orElse(false);
        if (affected) {
            com.github.ysbbbbbb.kaleidoscopetavern.compat.ponder.init.PonderCommandCompat.register(event);
        }
    }

    @SubscribeEvent
    public static void onEnqueue(final InterModEnqueueEvent event) {
        event.enqueueWork(() -> checkModLoad(CARRY_ON, BlackList::addBlackList));
    }

    private static void checkModLoad(String modId, Runnable runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.run();
        }
    }
}
