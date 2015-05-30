package net.minecraft.server.v1_8_R3;

public class ItemCloth extends ItemBlock {

    public ItemCloth(Block block) {
        super(block);
        this.setMaxDurability(0);
        this.a(true);
    }

    public int filterData(int i) {
        return i;
    }

    public String e_(ItemStack itemstack) {
        return super.getName() + "." + EnumColor.fromColorIndex(itemstack.getData()).d();
    }
}
