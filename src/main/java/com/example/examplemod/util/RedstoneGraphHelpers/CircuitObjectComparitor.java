package com.example.examplemod.util.RedstoneGraphHelpers;

import com.example.examplemod.util.utility;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.state.properties.BlockStateProperties.COMPARATOR_MODE;

public class CircuitObjectComparitor extends CircuitObject {
    public boolean subtractMode;/*you already know, false if off*/

    public CircuitObjectComparitor(BlockPos blockPos, boolean subtracksMode) {
        super(CircuitObjectType.COMPARATOR, blockPos);
        this.subtractMode = subtracksMode;
    }

    public CircuitObjectComparitor(BlockState[][][] area, BlockPos blockPos) {
        super(CircuitObjectType.COMPARATOR, blockPos);
        this.subtractMode = utility.cordGet(area, blockPos).get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT);
    }

    public CircuitObject clone(){
        return new CircuitObjectComparitor(this.blockPos,this.subtractMode);
    }
}
