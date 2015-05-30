package net.minecraft.server.v1_8_R3;

public class BlockTransparent extends Block {

    protected boolean R;

    protected BlockTransparent(Material material, boolean flag) {
        super(material);
        this.R = flag;
    }

    public boolean c() {
        return false;
    }
}
