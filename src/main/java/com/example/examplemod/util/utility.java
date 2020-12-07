package com.example.examplemod.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class utility {

    public static boolean debug= true;

    public static void debugout(String message){
        if(debug)
            return;
        System.out.println(message);
    }

    //there is a smart way to do it, and then there is a fast way to code it.
    public static String getStringRepresentationOfInt(int i){
        //A for 0, B for 1 and so on. you get the picture
        if(i<0){
            return "NO ID SET";
        }
        if(i>25){
            return getStringRepresentationOfInt((i/26)-1)+getStringRepresentationOfInt(i%26);
        } else {
            switch (i){
                case 0: return "A";
                case 1: return "B";
                case 2: return "C";
                case 3: return "D";
                case 4: return "E";
                case 5: return "F";
                case 6: return "G";
                case 7: return "H";
                case 8: return "I";
                case 9: return "J";
                case 10: return "K";
                case 11: return "L";
                case 12: return "M";
                case 13: return "N";
                case 14: return "O";
                case 15: return "P";
                case 16: return "Q";
                case 17: return "R";
                case 18: return "S";
                case 19: return "T";
                case 20: return "U";
                case 21: return "V";
                case 22: return "W";
                case 23: return "X";
                case 24: return "Y";
                case 25: return "Z";
            }
            throw new IllegalStateException("we should not be here");
        }
    }

    public static BlockState[][][] addPadding(BlockState[][][] area){
        BlockState[][][] newArea = new BlockState[area.length+2][area[0].length+2][area[0][0].length+2];
        for (int i = 0; i < newArea.length; i++) {
            for (int j = 0; j < newArea[0].length; j++) {
                for (int k = 0; k < newArea[0][0].length; k++) {
                    if(i==0 || j == 0 || k==0 ||
                            i==newArea.length-1 || j==newArea[0].length-1 || k==newArea[0][0].length-1){
                        newArea[i][j][k] = new BlockState(Blocks.AIR, null, null);
                    } else {
                        newArea[i][j][k] = area[i-1][j-1][k-1];
                    }
                }
            }
        }
        return newArea;
    }

    public static BlockState[][][] scanArea(World worldIn, BlockPos pos1, BlockPos pos2, PlayerEntity player){
        BlockState[][][] result= new BlockState[Math.abs(pos1.getX()-pos2.getX())+1]
                [Math.abs(pos1.getY()-pos2.getY())+1][Math.abs(pos1.getZ()-pos2.getZ())+1];
        //point can be placed anywhere, so here we make sure we always scan "from small to big
        //as to stay consistent
        int xStart = Math.min(pos1.getX(), pos2.getX());
        int xEnd = Math.max(pos1.getX(), pos2.getX());
        int yStart = Math.min(pos1.getY(), pos2.getY());
        int yEnd = Math.max(pos1.getY(), pos2.getY());
        int zStart = Math.min(pos1.getZ(), pos2.getZ());
        int zEnd = Math.max(pos1.getZ(), pos2.getZ());

        if((xEnd-xStart)*(yEnd-yStart)*(zEnd-zStart)>10000){
            player.sendMessage(new StringTextComponent("more than 10000 blocks, OH BOY, lul"),
                    null);
            //return new BlockState[0][0][0];
        }

        for (int xCurrent=xStart;xCurrent<=xEnd; xCurrent++){
            for(int yCurrent=yStart;yCurrent<=yEnd; yCurrent++){
                for(int zCurrent=zStart; zCurrent<=zEnd; zCurrent++){
                    //could be done outside of here
                    BlockPos posCurrent=new BlockPos(xCurrent,yCurrent,zCurrent);
                    BlockState blockState=worldIn.getBlockState(posCurrent);
                    result[xCurrent-xStart][yCurrent-yStart][zCurrent-zStart] = blockState;
                }
            }
        }
        return result;
    }

    public static BlockState[][][] scanArea(World worldIn, BlockPos pos1, BlockPos pos2){
        BlockState[][][] result= new BlockState[Math.abs(pos1.getX()-pos2.getX())+1]
                [Math.abs(pos1.getY()-pos2.getY())+1][Math.abs(pos1.getZ()-pos2.getZ())+1];
        //point can be placed anywhere, so here we make sure we always scan "from small to big
        //as to stay consistent
        int xStart = Math.min(pos1.getX(), pos2.getX());
        int xEnd = Math.max(pos1.getX(), pos2.getX());
        int yStart = Math.min(pos1.getY(), pos2.getY());
        int yEnd = Math.max(pos1.getY(), pos2.getY());
        int zStart = Math.min(pos1.getZ(), pos2.getZ());
        int zEnd = Math.max(pos1.getZ(), pos2.getZ());

        if((xEnd-xStart)*(yEnd-yStart)*(zEnd-zStart)>10000){
            System.out.println("more than 10000 blocks, OH BOY, lul");

            //return new BlockState[0][0][0];
        }

        for (int xCurrent=xStart;xCurrent<=xEnd; xCurrent++){
            for(int yCurrent=yStart;yCurrent<=yEnd; yCurrent++){
                for(int zCurrent=zStart; zCurrent<=zEnd; zCurrent++){
                    //could be done outside of here
                    BlockPos posCurrent=new BlockPos(xCurrent,yCurrent,zCurrent);
                    BlockState blockState=worldIn.getBlockState(posCurrent);
                    result[xCurrent-xStart][yCurrent-yStart][zCurrent-zStart] = blockState;
                }
            }
        }
        return result;
    }

    public static BlockState cordGet(BlockState[][][] area, BlockPos pos) {
        try {
            return area[pos.getX()][pos.getY()][pos.getZ()];
        } catch (ArrayIndexOutOfBoundsException e) {
            //tried getting something out of area, everything around us is air
            return Blocks.AIR.getDefaultState();
        }
    }
}
