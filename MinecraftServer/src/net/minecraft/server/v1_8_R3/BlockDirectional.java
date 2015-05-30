package net.minecraft.server.v1_8_R3;

import com.google.common.base.Predicate;

public abstract class BlockDirectional extends Block {

    public static final BlockStateDirection FACING = BlockStateDirection.of("facing", (Predicate) EnumDirection.EnumDirectionLimit.HORIZONTAL);

    protected BlockDirectional(Material material) {
        super(material);
    }

    protected BlockDirectional(Material material, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
    }
}
