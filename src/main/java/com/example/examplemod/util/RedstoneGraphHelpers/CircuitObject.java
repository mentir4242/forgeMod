package com.example.examplemod.util.RedstoneGraphHelpers;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class CircuitObject {
    public final CircuitObjectType circuitObjectType;
    public final BlockPos blockPos;
    public boolean unique=false;

    public CircuitObjectType getCircuitObjectType() {
        return circuitObjectType;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    //        public CircuitObject(CircuitObjectType circuitObjectType) {
//            this.circuitObjectType = circuitObjectType;
//        }
    public CircuitObject(CircuitObjectType circuitObjectType, BlockPos blockPos) {
        this.circuitObjectType = circuitObjectType;
        this.blockPos = blockPos;
    }

    public CircuitObject(CircuitObjectType circuitObjectType, BlockPos blockPos, boolean unique) {
        this.unique=unique;
        this.circuitObjectType = circuitObjectType;
        this.blockPos = blockPos;
    }

    @Override
    public String toString() {
        return (String.format("CircuitObject, pos: %d,%d,%d  type: %s", blockPos.getX(), blockPos.getY(), blockPos.getZ(), circuitObjectType.name()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(circuitObjectType, blockPos.getX(), blockPos.getY(), blockPos.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        //even if we are the same class, blockpos and everything, if we are unique we do still not equal
        //that is because if two dummys might be in the same exact space
        if (this.unique){
            return false;
        }
        if (o == null || getClass() != o.getClass()) return false;
        CircuitObject circuitObject = (CircuitObject) o;
        return circuitObject.circuitObjectType.equals(this.circuitObjectType) &&
                this.blockPos.equals(circuitObject.blockPos);
    }

    public CircuitObject clone(){
        return new CircuitObject(this.circuitObjectType, this.blockPos, this.unique);
    }
}
