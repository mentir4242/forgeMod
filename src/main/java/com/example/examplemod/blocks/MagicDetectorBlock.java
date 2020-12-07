package com.example.examplemod.blocks;

import com.example.examplemod.RCD;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class MagicDetectorBlock extends Block {
    public MagicDetectorBlock() {
        super(Block.Properties.create(Material.IRON));
    }

    public BlockPos blockPos=new BlockPos(0,0,0);
    public boolean blockPosIsSet=false;

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos,
                                             PlayerEntity player, Hand handIn, BlockRayTraceResult hit){
        if(worldIn.isRemote==false){
            return ActionResultType.SUCCESS;
        }

        doublePointScan(pos, worldIn, player);

        return ActionResultType.SUCCESS;
    }

    public void doublePointScan(BlockPos pos, World worldIn, PlayerEntity player){
        if(blockPosIsSet){
            System.out.println("did a thing to blockpos ("+blockPos.getX()+","+blockPos.getY()+","+blockPos.getZ()+
                    " and pos ("+pos.getX()+","+pos.getY()+","+pos.getZ());
            BlockState[][][] area = scanArea(worldIn,this.blockPos,pos,player);
//            for (int i = 0; i < area.length; i++) {
//                for (int j = 0; j < area[0].length; j++) {
//                    for (int k = 0; k < area[0][0].length; k++) {
//                        BlockState blockState = area[i][j][k];
//                        System.out.println(blockState.getBlock().getRegistryName());
//                        ITextComponent iTextComponent = ITextComponent.func_244388_a(i +" "+ j + " "+k+
//                                " block: "+blockState.getBlock().getRegistryName());
//                        player.sendMessage(iTextComponent,null);
//                    }
//                }
//            }
            RCD rcd = new RCD(area);
            blockPosIsSet=false;
        }else{
            System.out.println("not yet amigo, saving");
            blockPos=pos;
            blockPosIsSet=true;
        }
    }

    /**
     * takes 2 points in 3d space in a minecraft world and maps the blocks between them into an array
     * @param worldIn world we are in
     * @param pos1 one of the edge points of the area we are mapping
     * @param pos2 one of the edge points of the area we are mapping
     * @return array of the area between the two points
     */
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
                    null
                    );
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
}
