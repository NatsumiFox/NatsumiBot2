package net.minecraft.server.v1_8_R3;

public interface IWorldInventory extends IInventory {

    int[] getSlotsForFace(EnumDirection enumdirection);

    boolean canPlaceItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection);

    boolean canTakeItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection);
}
