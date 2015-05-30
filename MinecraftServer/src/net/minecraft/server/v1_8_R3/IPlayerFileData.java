package net.minecraft.server.v1_8_R3;

public interface IPlayerFileData {

    void save(EntityHuman entityhuman);

    NBTTagCompound load(EntityHuman entityhuman);

    String[] getSeenPlayers();
}
