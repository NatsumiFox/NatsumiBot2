package net.minecraft.server.v1_8_R3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class WorldNBTStorage implements IDataManager, IPlayerFileData {

    private static final Logger a = LogManager.getLogger();
    private final File baseDir;
    private final File playerDir;
    private final File dataDir;
    private final long sessionId = MinecraftServer.az();
    private final String f;
    private UUID uuid = null;

    public WorldNBTStorage(File file, String s, boolean flag) {
        this.baseDir = new File(file, s);
        this.baseDir.mkdirs();
        this.playerDir = new File(this.baseDir, "playerdata");
        this.dataDir = new File(this.baseDir, "data");
        this.dataDir.mkdirs();
        this.f = s;
        if (flag) {
            this.playerDir.mkdirs();
        }

        this.h();
    }

    private void h() {
        try {
            File file = new File(this.baseDir, "session.lock");
            DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));

            try {
                dataoutputstream.writeLong(this.sessionId);
            } finally {
                dataoutputstream.close();
            }

        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            throw new RuntimeException("Failed to check session lock for world located at " + this.baseDir + ", aborting. Stop the server and delete the session.lock in this world to prevent further issues.");
        }
    }

    public File getDirectory() {
        return this.baseDir;
    }

    public void checkSession() throws ExceptionWorldConflict {
        try {
            File file = new File(this.baseDir, "session.lock");
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));

            try {
                if (datainputstream.readLong() != this.sessionId) {
                    throw new ExceptionWorldConflict("The save for world located at " + this.baseDir + " is being accessed from another location, aborting");
                }
            } finally {
                datainputstream.close();
            }

        } catch (IOException ioexception) {
            throw new ExceptionWorldConflict("Failed to check session lock for world located at " + this.baseDir + ", aborting. Stop the server and delete the session.lock in this world to prevent further issues.");
        }
    }

    public IChunkLoader createChunkLoader(WorldProvider worldprovider) {
        throw new RuntimeException("Old Chunk Storage is no longer supported.");
    }

    public WorldData getWorldData() {
        File file = new File(this.baseDir, "level.dat");
        NBTTagCompound nbttagcompound;
        NBTTagCompound nbttagcompound1;

        if (file.exists()) {
            try {
                nbttagcompound = NBTCompressedStreamTools.a((InputStream) (new FileInputStream(file)));
                nbttagcompound1 = nbttagcompound.getCompound("Data");
                return new WorldData(nbttagcompound1);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        file = new File(this.baseDir, "level.dat_old");
        if (file.exists()) {
            try {
                nbttagcompound = NBTCompressedStreamTools.a((InputStream) (new FileInputStream(file)));
                nbttagcompound1 = nbttagcompound.getCompound("Data");
                return new WorldData(nbttagcompound1);
            } catch (Exception exception1) {
                exception1.printStackTrace();
            }
        }

        return null;
    }

    public void saveWorldData(WorldData worlddata, NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = worlddata.a(nbttagcompound);
        NBTTagCompound nbttagcompound2 = new NBTTagCompound();

        nbttagcompound2.set("Data", nbttagcompound1);

        try {
            File file = new File(this.baseDir, "level.dat_new");
            File file1 = new File(this.baseDir, "level.dat_old");
            File file2 = new File(this.baseDir, "level.dat");

            NBTCompressedStreamTools.a(nbttagcompound2, (OutputStream) (new FileOutputStream(file)));
            if (file1.exists()) {
                file1.delete();
            }

            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }

            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void saveWorldData(WorldData worlddata) {
        NBTTagCompound nbttagcompound = worlddata.a();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

        nbttagcompound1.set("Data", nbttagcompound);

        try {
            File file = new File(this.baseDir, "level.dat_new");
            File file1 = new File(this.baseDir, "level.dat_old");
            File file2 = new File(this.baseDir, "level.dat");

            NBTCompressedStreamTools.a(nbttagcompound1, (OutputStream) (new FileOutputStream(file)));
            if (file1.exists()) {
                file1.delete();
            }

            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }

            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void save(EntityHuman entityhuman) {
        try {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            entityhuman.e(nbttagcompound);
            File file = new File(this.playerDir, entityhuman.getUniqueID().toString() + ".dat.tmp");
            File file1 = new File(this.playerDir, entityhuman.getUniqueID().toString() + ".dat");

            NBTCompressedStreamTools.a(nbttagcompound, (OutputStream) (new FileOutputStream(file)));
            if (file1.exists()) {
                file1.delete();
            }

            file.renameTo(file1);
        } catch (Exception exception) {
            WorldNBTStorage.a.warn("Failed to save player data for " + entityhuman.getName());
        }

    }

    public NBTTagCompound load(EntityHuman entityhuman) {
        NBTTagCompound nbttagcompound = null;

        try {
            File file = new File(this.playerDir, entityhuman.getUniqueID().toString() + ".dat");
            boolean flag = false;

            if (!file.exists()) {
                file = new File(this.playerDir, UUID.nameUUIDFromBytes(("OfflinePlayer:" + entityhuman.getName()).getBytes("UTF-8")).toString() + ".dat");
                if (file.exists()) {
                    flag = true;
                    Bukkit.getServer().getLogger().warning("Using offline mode UUID file for player " + entityhuman.getName() + " as it is the only copy we can find.");
                }
            }

            if (file.exists() && file.isFile()) {
                nbttagcompound = NBTCompressedStreamTools.a((InputStream) (new FileInputStream(file)));
            }

            if (flag) {
                file.renameTo(new File(file.getPath() + ".offline-read"));
            }
        } catch (Exception exception) {
            WorldNBTStorage.a.warn("Failed to load player data for " + entityhuman.getName());
        }

        if (nbttagcompound != null) {
            if (entityhuman instanceof EntityPlayer) {
                CraftPlayer craftplayer = (CraftPlayer) entityhuman.getBukkitEntity();
                long i = (new File(this.playerDir, entityhuman.getUniqueID().toString() + ".dat")).lastModified();

                if (i < craftplayer.getFirstPlayed()) {
                    craftplayer.setFirstPlayed(i);
                }
            }

            entityhuman.f(nbttagcompound);
        }

        return nbttagcompound;
    }

    public NBTTagCompound getPlayerData(String s) {
        try {
            File file = new File(this.playerDir, s + ".dat");

            if (file.exists()) {
                return NBTCompressedStreamTools.a((InputStream) (new FileInputStream(file)));
            }
        } catch (Exception exception) {
            WorldNBTStorage.a.warn("Failed to load player data for " + s);
        }

        return null;
    }

    public IPlayerFileData getPlayerFileData() {
        return this;
    }

    public String[] getSeenPlayers() {
        String[] astring = this.playerDir.list();

        if (astring == null) {
            astring = new String[0];
        }

        for (int i = 0; i < astring.length; ++i) {
            if (astring[i].endsWith(".dat")) {
                astring[i] = astring[i].substring(0, astring[i].length() - 4);
            }
        }

        return astring;
    }

    public void a() {}

    public File getDataFile(String s) {
        return new File(this.dataDir, s + ".dat");
    }

    public String g() {
        return this.f;
    }

    public UUID getUUID() {
        if (this.uuid != null) {
            return this.uuid;
        } else {
            File file = new File(this.baseDir, "uid.dat");

            if (file.exists()) {
                label204: {
                    DataInputStream datainputstream = null;

                    UUID uuid;

                    try {
                        datainputstream = new DataInputStream(new FileInputStream(file));
                        uuid = this.uuid = new UUID(datainputstream.readLong(), datainputstream.readLong());
                    } catch (IOException ioexception) {
                        WorldNBTStorage.a.warn("Failed to read " + file + ", generating new random UUID", ioexception);
                        break label204;
                    } finally {
                        if (datainputstream != null) {
                            try {
                                datainputstream.close();
                            } catch (IOException ioexception1) {
                                ;
                            }
                        }

                    }

                    return uuid;
                }
            }

            this.uuid = UUID.randomUUID();
            DataOutputStream dataoutputstream = null;

            try {
                dataoutputstream = new DataOutputStream(new FileOutputStream(file));
                dataoutputstream.writeLong(this.uuid.getMostSignificantBits());
                dataoutputstream.writeLong(this.uuid.getLeastSignificantBits());
            } catch (IOException ioexception2) {
                WorldNBTStorage.a.warn("Failed to write " + file, ioexception2);
            } finally {
                if (dataoutputstream != null) {
                    try {
                        dataoutputstream.close();
                    } catch (IOException ioexception3) {
                        ;
                    }
                }

            }

            return this.uuid;
        }
    }

    public File getPlayerDir() {
        return this.playerDir;
    }
}
