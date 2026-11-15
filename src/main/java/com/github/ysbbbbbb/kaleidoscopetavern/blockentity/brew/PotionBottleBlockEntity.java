package com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PotionBottleBlockEntity extends BaseBlockEntity {
    private static final String ITEM_KEY = "Item";
    private ItemStack potionStack = ItemStack.EMPTY;

    public PotionBottleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.POTION_BOTTLE_BE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.potionStack = input.read(ITEM_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(ITEM_KEY, ItemStack.OPTIONAL_CODEC, this.potionStack);
    }

    public ItemStack getPotionStack() {
        return this.potionStack;
    }

    public void setPotionStack(ItemStack stack) {
        this.potionStack = stack.copyWithCount(1);
        this.refresh();
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        super.onDataPacket(net, input);
        if (net.getDirection() == PacketFlow.CLIENTBOUND) {
            com.github.ysbbbbbb.kaleidoscopetavern.client.PotionBottleClient.refreshChunk(worldPosition);
        }
    }

}
