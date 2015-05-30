package net.minecraft.server.v1_8_R3;

public class ItemSoup extends ItemFood {

    public ItemSoup(int i) {
        super(i, false);
        this.c(1);
    }

    public ItemStack b(ItemStack itemstack, World world, EntityHuman entityhuman) {
        super.b(itemstack, world, entityhuman);
        return new ItemStack(Items.BOWL);
    }
}
