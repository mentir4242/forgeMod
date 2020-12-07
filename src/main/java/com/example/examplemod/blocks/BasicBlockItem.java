package com.example.examplemod.blocks;

import com.example.examplemod.ExampleMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class BasicBlockItem extends BlockItem {
    public BasicBlockItem(Block block) {
        super(block, new Item.Properties().group(ExampleMod.MOD_STUFF));
    }
}
