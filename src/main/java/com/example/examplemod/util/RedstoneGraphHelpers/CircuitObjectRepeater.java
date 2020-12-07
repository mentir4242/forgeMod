package com.example.examplemod.util.RedstoneGraphHelpers;

import com.example.examplemod.util.utility;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.state.properties.BlockStateProperties.DELAY_1_4;

public class CircuitObjectRepeater extends CircuitObject {
    public final int delay; /*1 to 4 depending on repeater setting, 1 being fastest*/

    public CircuitObjectRepeater(BlockPos blockPos, int delay) {
        super(CircuitObjectType.REPEATER, blockPos);
        this.delay = delay;
    }

    public CircuitObjectRepeater(BlockState[][][] area, BlockPos blockPos) {
        super(CircuitObjectType.REPEATER, blockPos);
        this.delay = utility.cordGet(area, blockPos).get(DELAY_1_4);
    }

    public CircuitObject clone(){
        return new CircuitObjectRepeater(this.blockPos, this.delay);
    }
}
