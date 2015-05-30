package net.minecraft.server.v1_8_R3;

public interface INamableTileEntity {

    String getName();

    boolean hasCustomName();

    IChatBaseComponent getScoreboardDisplayName();
}
