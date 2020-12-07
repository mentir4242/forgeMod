package com.example.examplemod.Events;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.RCD;
import com.example.examplemod.util.utility;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.util.Integers;

@Mod.EventBusSubscriber(modid = ExampleMod.Mod_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventsHandlerClient {

    @SubscribeEvent
    public static void chatInteraction(ClientChatEvent event){

    }

    @SubscribeEvent
    public static void detectorPlaced(BlockEvent.EntityPlaceEvent event ){
        System.out.println( event.getPlacedBlock().getBlock().getRegistryName().toString() +", lul");
//        if( event.getPlacedBlock().getBlock().getRegistryName().toString()
//                .compareTo("examplemod:magic_detector") ==0 ){
//            int x= event.getPos().getX();
//            int y= event.getPos().getY();
//            int z= event.getPos().getZ();
//            System.out.println(x+" "+y+" "+z+" sind koordinaten");
//            BlockPos blockPos = event.getPos();
//            for(int xOffset = 0; xOffset<3; xOffset++){
//                for(int yOffset = 0; yOffset<3; yOffset++){
//                    for(int zOffset = 0 ; zOffset<3; zOffset++){
//                        Chunk chunk= Minecraft.getInstance().world.getChunkAt(blockPos);
//                        BlockPos pos = new BlockPos(blockPos.getX()+xOffset, blockPos.getY()+yOffset, blockPos.getZ() + zOffset);
//                        BlockState blockState = chunk.getBlockState(
//                               pos );
//                        System.out.print(pos.getX() +" "+ pos.getY() + " "+pos.getZ()+ " block: ");
//                        System.out.println(blockState.getBlock().getRegistryName());
//                    }
//                }
//            }
//        }

    }

}
