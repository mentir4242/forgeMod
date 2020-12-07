package com.example.examplemod.util;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.blocks.BasicBlockItem;
import com.example.examplemod.blocks.MagicDetectorBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class RegistryHandler {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.Mod_ID );
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExampleMod.Mod_ID);
    //items
    public static final RegistryObject<Item> LOTUS = ITEMS.register("lotus", ()->
            new Item(new Item.Properties().group(ExampleMod.MOD_STUFF)) );

    //blocks
    public static final RegistryObject<Block> MAGIC_DETECTOR = BLOCKS.register("magic_detector", MagicDetectorBlock::new);

    //blocks thingys (items)
    public static final RegistryObject<Item> MAGIC_DETECTOR_ITEM = ITEMS.register("magic_detector", () -> new BasicBlockItem(MAGIC_DETECTOR.get()));

    //commands?



    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
