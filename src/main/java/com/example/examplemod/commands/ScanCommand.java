package com.example.examplemod.commands;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.RCD;
import com.example.examplemod.util.utility;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ScanCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        ExampleMod.LOGGER.info("trying to register Scan command");
        dispatcher.register(Commands.literal("scan").requires((commandSource) -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes((commandSource) ->
                                        doScan(
                                                BlockPosArgument.getLoadedBlockPos(commandSource, "from"),
                                                BlockPosArgument.getLoadedBlockPos(commandSource, "to"),
                                                commandSource,
                                                false))
                                .then(Commands.literal("all")
                                        .executes((commandSource) ->
                                                doScan(
                                                        BlockPosArgument.getLoadedBlockPos(commandSource, "from"),
                                                        BlockPosArgument.getLoadedBlockPos(commandSource, "to"),
                                                        commandSource,
                                                        true))
                                )
                        )));
        ExampleMod.LOGGER.info("past trying to register Scan command");
    }


    public static int doScan(BlockPos from, BlockPos to, CommandContext<CommandSource> commandSource, boolean all) {

//        if(Minecraft.getInstance().world.isRemote == false){
//            return 0;
//        }

        if (from.getY() == to.getY()) {
            from = from.up(10);
        }
        BlockState[][][] area = utility.scanArea(
                Minecraft.getInstance().world, from, to
        );

        new RCD(
                area, all
        );

        commandSource.getSource().sendFeedback(
                new StringTextComponent(
                        "scanned "+ (all?"ALL":"some")+" blocks, " + from.toString() + " " + to.toString()),
                true);
        System.out.println("done with scan Command");
        return 0;
    }


}
