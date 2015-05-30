package net.minecraft.server.v1_8_R3;

public interface ITileInventory extends IInventory, ITileEntityContainer {

    boolean r_();

    void a(ChestLock chestlock);

    ChestLock i();
}
