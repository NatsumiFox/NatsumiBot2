package net.minecraft.server.v1_8_R3;

public class ItemAnvil extends ItemMultiTexture {

    public ItemAnvil(Block block) {
        super(block, block, new String[] { "intact", "slightlyDamaged", "veryDamaged"});
    }

    public int filterData(int i) {
        return i << 2;
    }
}
