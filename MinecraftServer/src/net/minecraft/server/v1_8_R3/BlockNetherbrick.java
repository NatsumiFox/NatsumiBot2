package net.minecraft.server.v1_8_R3;

public class BlockNetherbrick extends Block {

    public BlockNetherbrick() {
        super(Material.STONE);
        this.a(CreativeModeTab.b);
    }

    public MaterialMapColor g(IBlockData iblockdata) {
        return MaterialMapColor.K;
    }
}
