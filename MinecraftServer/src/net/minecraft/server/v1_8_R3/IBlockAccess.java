package net.minecraft.server.v1_8_R3;

public interface IBlockAccess {

    TileEntity getTileEntity(BlockPosition blockposition);

    IBlockData getType(BlockPosition blockposition);

    boolean isEmpty(BlockPosition blockposition);

    int getBlockPower(BlockPosition blockposition, EnumDirection enumdirection);
}
