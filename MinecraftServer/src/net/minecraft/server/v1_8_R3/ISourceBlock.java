package net.minecraft.server.v1_8_R3;

public interface ISourceBlock extends ILocationSource {

    double getX();

    double getY();

    double getZ();

    BlockPosition getBlockPosition();

    int f();

    <T extends TileEntity> T getTileEntity();
}
