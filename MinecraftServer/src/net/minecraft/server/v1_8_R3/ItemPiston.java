package net.minecraft.server.v1_8_R3;

public class ItemPiston extends ItemBlock {

    public ItemPiston(Block block) {
        super(block);
    }

    public int filterData(int i) {
        return 7;
    }
}
