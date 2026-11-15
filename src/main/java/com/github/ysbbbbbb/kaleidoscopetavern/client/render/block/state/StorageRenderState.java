package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class StorageRenderState extends BlockEntityRenderState {
    public Direction direction = Direction.NORTH;
    public BlockModelRenderState[] items = new BlockModelRenderState[0];
}
