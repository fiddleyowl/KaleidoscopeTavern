package com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class StorageBlockEntity extends BaseBlockEntity {
    private static final String ITEMS = "items";
    private final ItemStackHandler items;

    protected StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.items = new ItemStackHandler(size) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.deserialize(input.childOrEmpty(ITEMS));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.items.serialize(output.child(ITEMS));
    }

    public ItemStackHandler getItems() {
        return this.items;
    }

    public boolean hasAnyItem() {
        for (int i = 0; i < this.items.getSlots(); i++) {
            if (!this.items.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
