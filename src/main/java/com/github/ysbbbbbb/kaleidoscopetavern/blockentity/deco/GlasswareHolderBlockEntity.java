package com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class GlasswareHolderBlockEntity extends StorageBlockEntity {
    public GlasswareHolderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GLASSWARE_HOLDER_BE.get(), pos, state, 4);
    }
}
