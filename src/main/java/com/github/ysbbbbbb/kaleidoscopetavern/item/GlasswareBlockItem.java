package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class GlasswareBlockItem extends BlockItem {
    public GlasswareBlockItem(Identifier id, Block block) {
        this(block, new Properties().useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(16));
    }

    public GlasswareBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
