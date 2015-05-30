package net.minecraft.server.v1_8_R3;

public abstract class BlockRotatable extends Block {

    public static final BlockStateEnum<EnumDirection.EnumAxis> AXIS = BlockStateEnum.of("axis", EnumDirection.EnumAxis.class);

    protected BlockRotatable(Material material) {
        super(material, material.r());
    }

    protected BlockRotatable(Material material, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
    }
}
