package com.github.ysbbbbbb.kaleidoscopetavern.compat.jade;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jade.block.BarrelComponentProvider;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ModPlugin implements IWailaPlugin {
    public static final Identifier BARREL = Identifier.fromNamespaceAndPath(KaleidoscopeTavern.MOD_ID, "barrel");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BarrelComponentProvider.INSTANCE, BarrelBlock.class);
        registration.registerBlockIcon(new snownee.jade.api.IBlockComponentProvider() {
            @Override
            public Identifier getUid() {
                return KaleidoscopeTavern.modLoc("potion_bottle");
            }

            @Override
            public void appendTooltip(snownee.jade.api.ITooltip tooltip, snownee.jade.api.BlockAccessor accessor,
                                      snownee.jade.api.config.IPluginConfig config) {
            }

            @Override
            public snownee.jade.api.ui.Element getIcon(snownee.jade.api.BlockAccessor accessor,
                                                       snownee.jade.api.config.IPluginConfig config,
                                                       snownee.jade.api.ui.Element current) {
                if (accessor.getBlockEntity() instanceof com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.PotionBottleBlockEntity bottle
                        && !bottle.getPotionStack().isEmpty()) {
                    return snownee.jade.api.ui.JadeUI.item(bottle.getPotionStack());
                }
                return current;
            }
        }, com.github.ysbbbbbb.kaleidoscopetavern.block.brew.PotionBottleBlock.class);
    }
}
