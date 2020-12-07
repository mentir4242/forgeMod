package com.example.examplemod.commands;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.RCD;
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
import net.minecraft.world.World;

public class InfoCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        ExampleMod.LOGGER.info("trying to register info command");
        dispatcher.register(Commands.literal("info").requires((commandSource) -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                                .executes((commandSource) ->
                                        doInfo(
                                                BlockPosArgument.getLoadedBlockPos(commandSource, "from"),
                                                commandSource))
                        ));
        ExampleMod.LOGGER.info("past trying to register info command");
    }

    public static int doInfo(BlockPos from, CommandContext<CommandSource> commandSource) {
        String info = "";
        World worldIn = Minecraft.getInstance().world;
        BlockState blockState = worldIn.getBlockState(from);
        info+=blockState.toString();
        info+=" stopsRedstoneWire: "+ RCD.stopsRedstoneWire(blockState);
        commandSource.getSource().sendFeedback(
                new StringTextComponent(
                        info ),
                true);
        return 0;
    }
}
