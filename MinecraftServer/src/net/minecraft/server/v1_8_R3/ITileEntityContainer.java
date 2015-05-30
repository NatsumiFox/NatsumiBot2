package net.minecraft.server.v1_8_R3;

public interface ITileEntityContainer extends INamableTileEntity {

    Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman);

    String getContainerName();
}
