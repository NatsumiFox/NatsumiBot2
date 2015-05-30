package net.minecraft.server.v1_8_R3;

import java.util.Random;

public class BlockGlass extends BlockHalfTransparent {

    public BlockGlass(Material material, boolean flag) {
        super(material, flag);
        this.a(CreativeModeTab.b);
    }

    public int a(Random random) {
        return 0;
    }

    public boolean d() {
        return false;
    }

    protected boolean I() {
        return true;
    }
}
