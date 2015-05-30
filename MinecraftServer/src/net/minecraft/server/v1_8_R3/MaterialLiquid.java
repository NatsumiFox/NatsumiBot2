package net.minecraft.server.v1_8_R3;

public class MaterialLiquid extends Material {

    public MaterialLiquid(MaterialMapColor materialmapcolor) {
        super(materialmapcolor);
        this.i();
        this.n();
    }

    public boolean isLiquid() {
        return true;
    }

    public boolean isSolid() {
        return false;
    }

    public boolean isBuildable() {
        return false;
    }
}
