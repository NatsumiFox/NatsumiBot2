package net.minecraft.server.v1_8_R3;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHashSet;
import org.bukkit.craftbukkit.v1_8_R3.util.LongObjectHashMap;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.generator.BlockPopulator;

public class ChunkProviderServer implements IChunkProvider {

    private static final Logger b = LogManager.getLogger();
    public LongHashSet unloadQueue = new LongHashSet();
    public Chunk emptyChunk;
    public IChunkProvider chunkProvider;
    private IChunkLoader chunkLoader;
    public boolean forceChunkLoad = false;
    public LongObjectHashMap<Chunk> chunks = new LongObjectHashMap();
    public WorldServer world;

    public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, IChunkProvider ichunkprovider) {
        this.emptyChunk = new EmptyChunk(worldserver, 0, 0);
        this.world = worldserver;
        this.chunkLoader = ichunkloader;
        this.chunkProvider = ichunkprovider;
    }

    public boolean isChunkLoaded(int i, int j) {
        return this.chunks.containsKey(LongHash.toLong(i, j));
    }

    public Collection a() {
        return this.chunks.values();
    }

    public void queueUnload(int i, int j) {
        Chunk chunk;

        if (this.world.worldProvider.e()) {
            if (!this.world.c(i, j)) {
                this.unloadQueue.add(i, j);
                chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));
                if (chunk != null) {
                    chunk.mustSave = true;
                }
            }
        } else {
            this.unloadQueue.add(i, j);
            chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));
            if (chunk != null) {
                chunk.mustSave = true;
            }
        }

    }

    public void b() {
        Iterator iterator = this.chunks.values().iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();

            this.queueUnload(chunk.locX, chunk.locZ);
        }

    }

    public Chunk getChunkIfLoaded(int i, int j) {
        return (Chunk) this.chunks.get(LongHash.toLong(i, j));
    }

    public Chunk getChunkAt(int i, int j) {
        return this.getChunkAt(i, j, (Runnable) null);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable) {
        this.unloadQueue.remove(i, j);
        Chunk chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));
        ChunkRegionLoader chunkregionloader = null;

        if (this.chunkLoader instanceof ChunkRegionLoader) {
            chunkregionloader = (ChunkRegionLoader) this.chunkLoader;
        }

        if (chunk == null && chunkregionloader != null && chunkregionloader.chunkExists(this.world, i, j)) {
            if (runnable != null) {
                ChunkIOExecutor.queueChunkLoad(this.world, chunkregionloader, this, i, j, runnable);
                return null;
            }

            chunk = ChunkIOExecutor.syncChunkLoad(this.world, chunkregionloader, this, i, j);
        } else if (chunk == null) {
            chunk = this.originalGetChunkAt(i, j);
        }

        if (runnable != null) {
            runnable.run();
        }

        return chunk;
    }

    public Chunk originalGetChunkAt(int i, int j) {
        this.unloadQueue.remove(i, j);
        Chunk chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));
        boolean flag = false;

        if (chunk == null) {
            this.world.timings.syncChunkLoadTimer.startTiming();
            chunk = this.loadChunk(i, j);
            if (chunk == null) {
                if (this.chunkProvider == null) {
                    chunk = this.emptyChunk;
                } else {
                    try {
                        chunk = this.chunkProvider.getOrCreateChunk(i, j);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.a(throwable, "Exception generating new chunk");
                        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Chunk to be generated");

                        crashreportsystemdetails.a("Location", (Object) String.format("%d,%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j)}));
                        crashreportsystemdetails.a("Position hash", (Object) Long.valueOf(LongHash.toLong(i, j)));
                        crashreportsystemdetails.a("Generator", (Object) this.chunkProvider.getName());
                        throw new ReportedException(crashreport);
                    }
                }

                flag = true;
            }

            this.chunks.put(LongHash.toLong(i, j), chunk);
            chunk.addEntities();
            CraftServer craftserver = this.world.getServer();

            if (craftserver != null) {
                craftserver.getPluginManager().callEvent(new ChunkLoadEvent(chunk.bukkitChunk, flag));
            }

            for (int k = -2; k < 3; ++k) {
                for (int l = -2; l < 3; ++l) {
                    if (k != 0 || l != 0) {
                        Chunk chunk1 = this.getChunkIfLoaded(chunk.locX + k, chunk.locZ + l);

                        if (chunk1 != null) {
                            chunk1.setNeighborLoaded(-k, -l);
                            chunk.setNeighborLoaded(k, l);
                        }
                    }
                }
            }

            chunk.loadNearby(this, this, i, j);
            this.world.timings.syncChunkLoadTimer.stopTiming();
        }

        return chunk;
    }

    public Chunk getOrCreateChunk(int i, int j) {
        Chunk chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));

        chunk = chunk == null ? (!this.world.ad() && !this.forceChunkLoad ? this.emptyChunk : this.getChunkAt(i, j)) : chunk;
        if (chunk == this.emptyChunk) {
            return chunk;
        } else {
            if (i != chunk.locX || j != chunk.locZ) {
                ChunkProviderServer.b.error("Chunk (" + chunk.locX + ", " + chunk.locZ + ") stored at  (" + i + ", " + j + ") in world \'" + this.world.getWorld().getName() + "\'");
                ChunkProviderServer.b.error(chunk.getClass().getName());
                Throwable throwable = new Throwable();

                throwable.fillInStackTrace();
                throwable.printStackTrace();
            }

            return chunk;
        }
    }

    public Chunk loadChunk(int i, int j) {
        if (this.chunkLoader == null) {
            return null;
        } else {
            try {
                Chunk chunk = this.chunkLoader.a(this.world, i, j);

                if (chunk != null) {
                    chunk.setLastSaved(this.world.getTime());
                    if (this.chunkProvider != null) {
                        this.world.timings.syncChunkLoadStructuresTimer.startTiming();
                        this.chunkProvider.recreateStructures(chunk, i, j);
                        this.world.timings.syncChunkLoadStructuresTimer.stopTiming();
                    }
                }

                return chunk;
            } catch (Exception exception) {
                ChunkProviderServer.b.error("Couldn\'t load chunk", exception);
                return null;
            }
        }
    }

    public void saveChunkNOP(Chunk chunk) {
        if (this.chunkLoader != null) {
            try {
                this.chunkLoader.b(this.world, chunk);
            } catch (Exception exception) {
                ChunkProviderServer.b.error("Couldn\'t save entities", exception);
            }
        }

    }

    public void saveChunk(Chunk chunk) {
        if (this.chunkLoader != null) {
            try {
                chunk.setLastSaved(this.world.getTime());
                this.chunkLoader.a(this.world, chunk);
            } catch (IOException ioexception) {
                ChunkProviderServer.b.error("Couldn\'t save chunk", ioexception);
            } catch (ExceptionWorldConflict exceptionworldconflict) {
                ChunkProviderServer.b.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", exceptionworldconflict);
            }
        }

    }

    public void getChunkAt(IChunkProvider ichunkprovider, int i, int j) {
        Chunk chunk = this.getOrCreateChunk(i, j);

        if (!chunk.isDone()) {
            chunk.n();
            if (this.chunkProvider != null) {
                this.chunkProvider.getChunkAt(ichunkprovider, i, j);
                BlockSand.instaFall = true;
                Random random = new Random();

                random.setSeed(this.world.getSeed());
                long k = random.nextLong() / 2L * 2L + 1L;
                long l = random.nextLong() / 2L * 2L + 1L;

                random.setSeed((long) i * k + (long) j * l ^ this.world.getSeed());
                CraftWorld craftworld = this.world.getWorld();

                if (craftworld != null) {
                    this.world.populating = true;

                    try {
                        Iterator iterator = craftworld.getPopulators().iterator();

                        while (iterator.hasNext()) {
                            BlockPopulator blockpopulator = (BlockPopulator) iterator.next();

                            blockpopulator.populate(craftworld, random, chunk.bukkitChunk);
                        }
                    } finally {
                        this.world.populating = false;
                    }
                }

                BlockSand.instaFall = false;
                this.world.getServer().getPluginManager().callEvent(new ChunkPopulateEvent(chunk.bukkitChunk));
                chunk.e();
            }
        }

    }

    public boolean a(IChunkProvider ichunkprovider, Chunk chunk, int i, int j) {
        if (this.chunkProvider != null && this.chunkProvider.a(ichunkprovider, chunk, i, j)) {
            Chunk chunk1 = this.getOrCreateChunk(i, j);

            chunk1.e();
            return true;
        } else {
            return false;
        }
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        int i = 0;
        Iterator iterator = this.chunks.values().iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();

            if (flag) {
                this.saveChunkNOP(chunk);
            }

            if (chunk.a(flag)) {
                this.saveChunk(chunk);
                chunk.f(false);
                ++i;
                if (i == 24 && !flag) {
                    return false;
                }
            }
        }

        return true;
    }

    public void c() {
        if (this.chunkLoader != null) {
            this.chunkLoader.b();
        }

    }

    public boolean unloadChunks() {
        if (!this.world.savingDisabled) {
            CraftServer craftserver = this.world.getServer();

            for (int i = 0; i < 100 && !this.unloadQueue.isEmpty(); ++i) {
                long j = this.unloadQueue.popFirst();
                Chunk chunk = (Chunk) this.chunks.get(j);

                if (chunk != null) {
                    ChunkUnloadEvent chunkunloadevent = new ChunkUnloadEvent(chunk.bukkitChunk);

                    craftserver.getPluginManager().callEvent(chunkunloadevent);
                    if (!chunkunloadevent.isCancelled()) {
                        if (chunk != null) {
                            chunk.removeEntities();
                            this.saveChunk(chunk);
                            this.saveChunkNOP(chunk);
                            this.chunks.remove(j);
                        }

                        for (int k = -2; k < 3; ++k) {
                            for (int l = -2; l < 3; ++l) {
                                if (k != 0 || l != 0) {
                                    Chunk chunk1 = this.getChunkIfLoaded(chunk.locX + k, chunk.locZ + l);

                                    if (chunk1 != null) {
                                        chunk1.setNeighborUnloaded(-k, -l);
                                        chunk.setNeighborUnloaded(k, l);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (this.chunkLoader != null) {
                this.chunkLoader.a();
            }
        }

        return this.chunkProvider.unloadChunks();
    }

    public boolean canSave() {
        return !this.world.savingDisabled;
    }

    public String getName() {
        return "ServerChunkCache: " + this.chunks.size() + " Drop: " + this.unloadQueue.size();
    }

    public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        return this.chunkProvider.getMobsFor(enumcreaturetype, blockposition);
    }

    public BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockposition) {
        return this.chunkProvider.findNearestMapFeature(world, s, blockposition);
    }

    public int getLoadedChunks() {
        return this.chunks.size();
    }

    public void recreateStructures(Chunk chunk, int i, int j) {}

    public Chunk getChunkAt(BlockPosition blockposition) {
        return this.getOrCreateChunk(blockposition.getX() >> 4, blockposition.getZ() >> 4);
    }
}
