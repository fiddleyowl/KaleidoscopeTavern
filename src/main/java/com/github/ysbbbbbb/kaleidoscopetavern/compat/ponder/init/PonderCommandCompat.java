package com.github.ysbbbbbb.kaleidoscopetavern.compat.ponder.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.createmod.catnip.impl.command.argument.ConfigIdArgument;
import net.createmod.catnip.impl.command.argument.ConfigPathArgument;
import net.createmod.catnip.impl.command.argument.ModIdArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Catnip 1.0.52's NeoForge port leaves these registrations commented out.
 */
public final class PonderCommandCompat {
    public static void register(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, helper -> {
            helper.register(KaleidoscopeTavern.modLoc("catnip_mod_id"),
                    ArgumentTypeInfos.registerByClass(ModIdArgument.class, SingletonArgumentInfo.contextFree(ModIdArgument::modId)));
            helper.register(KaleidoscopeTavern.modLoc("catnip_config_id"),
                    ArgumentTypeInfos.registerByClass(ConfigIdArgument.class, SingletonArgumentInfo.contextFree(ConfigIdArgument::configId)));
            helper.register(KaleidoscopeTavern.modLoc("catnip_config_path"),
                    ArgumentTypeInfos.registerByClass(ConfigPathArgument.class, SingletonArgumentInfo.contextFree(ConfigPathArgument::path)));
        });
    }
}
