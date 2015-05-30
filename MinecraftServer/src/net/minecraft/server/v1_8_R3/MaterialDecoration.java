package net.minecraft.server.v1_8_R3;

public class MaterialDecoration extends Material {

    public MaterialDecoration(MaterialMapColor materialmapcolor) {
        super(materialmapcolor);
        this.p();
    }

    public boolean isBuildable() {
        return false;
    }

    public boolean blocksLight() {
        return false;
    }

    public boolean isSolid() {
        return false;
    }
}
