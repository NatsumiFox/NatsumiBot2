package net.minecraft.server.v1_8_R3;

import java.util.Random;

public class BlockClay extends Block {

    public BlockClay() {
        super(Material.CLAY);
        this.a(CreativeModeTab.b);
    }

    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Items.CLAY_BALL;
    }

    public int a(Random random) {
        return 4;
    }
}
