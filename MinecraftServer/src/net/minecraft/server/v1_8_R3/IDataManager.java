package net.minecraft.server.v1_8_R3;

import java.io.File;
import java.util.UUID;

public interface IDataManager {

    WorldData getWorldData();

    void checkSession() throws ExceptionWorldConflict;

    IChunkLoader createChunkLoader(WorldProvider worldprovider);

    void saveWorldData(WorldData worlddata, NBTTagCompound nbttagcompound);

    void saveWorldData(WorldData worlddata);

    IPlayerFileData getPlayerFileData();

    void a();

    File getDirectory();

    File getDataFile(String s);

    String g();

    UUID getUUID();
}
