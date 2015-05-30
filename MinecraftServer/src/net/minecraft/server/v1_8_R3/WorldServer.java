package net.minecraft.server.v1_8_R3;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import gnu.trove.iterator.TLongShortIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_8_R3.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_8_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_8_R3.generator.NetherChunkGenerator;
import org.bukkit.craftbukkit.v1_8_R3.generator.NormalChunkGenerator;
import org.bukkit.craftbukkit.v1_8_R3.generator.SkyLandsChunkGenerator;
import org.bukkit.craftbukkit.v1_8_R3.util.HashTreeSet;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.generator.ChunkGenerator;

public class WorldServer extends World implements IAsyncTaskHandler {

    private static final Logger a = LogManager.getLogger();
    private final MinecraftServer server;
    public EntityTracker tracker;
    private final PlayerChunkMap manager;
    private final HashTreeSet<NextTickListEntry> M = new HashTreeSet();
    private final Map<UUID, Entity> entitiesByUUID = Maps.newHashMap();
    public ChunkProviderServer chunkProviderServer;
    public boolean savingDisabled;
    private boolean O;
    private int emptyTime;
    private final PortalTravelAgent Q;
    private final SpawnerCreature R = new SpawnerCreature();
    protected final VillageSiege siegeManager = new VillageSiege(this);
    private WorldServer.BlockActionDataList[] S = new WorldServer.BlockActionDataList[] { new WorldServer.BlockActionDataList((Object) null), new WorldServer.BlockActionDataList((Object) null)};
    private int T;
    private static final List<StructurePieceTreasure> U = Lists.newArrayList(new StructurePieceTreasure[] { new StructurePieceTreasure(Items.STICK, 0, 1, 3, 10), new StructurePieceTreasure(Item.getItemOf(Blocks.PLANKS), 0, 1, 3, 10), new StructurePieceTreasure(Item.getItemOf(Blocks.LOG), 0, 1, 3, 10), new StructurePieceTreasure(Items.STONE_AXE, 0, 1, 1, 3), new StructurePieceTreasure(Items.WOODEN_AXE, 0, 1, 1, 5), new StructurePieceTreasure(Items.STONE_PICKAXE, 0, 1, 1, 3), new StructurePieceTreasure(Items.WOODEN_PICKAXE, 0, 1, 1, 5), new StructurePieceTreasure(Items.APPLE, 0, 2, 3, 5), new StructurePieceTreasure(Items.BREAD, 0, 2, 3, 3), new StructurePieceTreasure(Item.getItemOf(Blocks.LOG2), 0, 1, 3, 10)});
    private List<NextTickListEntry> V = Lists.newArrayList();
    public final int dimension;

    public WorldServer(MinecraftServer minecraftserver, IDataManager idatamanager, WorldData worlddata, int i, MethodProfiler methodprofiler, Environment environment, ChunkGenerator chunkgenerator) {
        super(idatamanager, worlddata, WorldProvider.byDimension(environment.getId()), methodprofiler, false, chunkgenerator, environment);
        this.dimension = i;
        this.pvpMode = minecraftserver.getPVP();
        worlddata.world = this;
        this.server = minecraftserver;
        this.tracker = new EntityTracker(this);
        this.manager = new PlayerChunkMap(this, this.spigotConfig.viewDistance);
        this.worldProvider.a(this);
        this.chunkProvider = this.k();
        this.Q = new CraftTravelAgent(this);
        this.B();
        this.C();
        this.getWorldBorder().a(minecraftserver.aI());
    }

    public World b() {
        this.worldMaps = new PersistentCollection(this.dataManager);
        String s = PersistentVillage.a(this.worldProvider);
        PersistentVillage persistentvillage = (PersistentVillage) this.worldMaps.get(PersistentVillage.class, s);

        if (persistentvillage == null) {
            this.villages = new PersistentVillage(this);
            this.worldMaps.a(s, this.villages);
        } else {
            this.villages = persistentvillage;
            this.villages.a((World) this);
        }

        if (this.getServer().getScoreboardManager() == null) {
            this.scoreboard = new ScoreboardServer(this.server);
            PersistentScoreboard persistentscoreboard = (PersistentScoreboard) this.worldMaps.get(PersistentScoreboard.class, "scoreboard");

            if (persistentscoreboard == null) {
                persistentscoreboard = new PersistentScoreboard();
                this.worldMaps.a("scoreboard", persistentscoreboard);
            }

            persistentscoreboard.a(this.scoreboard);
            ((ScoreboardServer) this.scoreboard).a(persistentscoreboard);
        } else {
            this.scoreboard = this.getServer().getScoreboardManager().getMainScoreboard().getHandle();
        }

        this.getWorldBorder().setCenter(this.worldData.C(), this.worldData.D());
        this.getWorldBorder().setDamageAmount(this.worldData.I());
        this.getWorldBorder().setDamageBuffer(this.worldData.H());
        this.getWorldBorder().setWarningDistance(this.worldData.J());
        this.getWorldBorder().setWarningTime(this.worldData.K());
        if (this.worldData.F() > 0L) {
            this.getWorldBorder().transitionSizeBetween(this.worldData.E(), this.worldData.G(), this.worldData.F());
        } else {
            this.getWorldBorder().setSize(this.worldData.E());
        }

        if (this.generator != null) {
            this.getWorld().getPopulators().addAll(this.generator.getDefaultPopulators(this.getWorld()));
        }

        return this;
    }

    public TileEntity getTileEntity(BlockPosition blockposition) {
        TileEntity tileentity = super.getTileEntity(blockposition);
        Block block = this.getType(blockposition).getBlock();

        if (block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST) {
            if (block == Blocks.FURNACE) {
                if (!(tileentity instanceof TileEntityFurnace)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (block == Blocks.DROPPER) {
                if (!(tileentity instanceof TileEntityDropper)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (block == Blocks.DISPENSER) {
                if (!(tileentity instanceof TileEntityDispenser)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (block == Blocks.JUKEBOX) {
                if (!(tileentity instanceof BlockJukeBox.TileEntityRecordPlayer)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (block == Blocks.NOTEBLOCK) {
                if (!(tileentity instanceof TileEntityNote)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (block == Blocks.MOB_SPAWNER) {
                if (!(tileentity instanceof TileEntityMobSpawner)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (block != Blocks.STANDING_SIGN && block != Blocks.WALL_SIGN) {
                if (block == Blocks.ENDER_CHEST) {
                    if (!(tileentity instanceof TileEntityEnderChest)) {
                        tileentity = this.fixTileEntity(blockposition, block, tileentity);
                    }
                } else if (block == Blocks.BREWING_STAND) {
                    if (!(tileentity instanceof TileEntityBrewingStand)) {
                        tileentity = this.fixTileEntity(blockposition, block, tileentity);
                    }
                } else if (block == Blocks.BEACON) {
                    if (!(tileentity instanceof TileEntityBeacon)) {
                        tileentity = this.fixTileEntity(blockposition, block, tileentity);
                    }
                } else if (block == Blocks.HOPPER && !(tileentity instanceof TileEntityHopper)) {
                    tileentity = this.fixTileEntity(blockposition, block, tileentity);
                }
            } else if (!(tileentity instanceof TileEntitySign)) {
                tileentity = this.fixTileEntity(blockposition, block, tileentity);
            }
        } else if (!(tileentity instanceof TileEntityChest)) {
            tileentity = this.fixTileEntity(blockposition, block, tileentity);
        }

        return tileentity;
    }

    private TileEntity fixTileEntity(BlockPosition blockposition, Block block, TileEntity tileentity) {
        this.getServer().getLogger().log(Level.SEVERE, "Block at {0},{1},{2} is {3} but has {4}. Bukkit will attempt to fix this, but there may be additional damage that we cannot recover.", new Object[] { Integer.valueOf(blockposition.getX()), Integer.valueOf(blockposition.getY()), Integer.valueOf(blockposition.getZ()), org.bukkit.Material.getMaterial(Block.getId(block)).toString(), tileentity});
        if (block instanceof IContainer) {
            TileEntity tileentity1 = ((IContainer) block).a(this, block.toLegacyData(this.getType(blockposition)));

            tileentity1.world = this;
            this.setTileEntity(blockposition, tileentity1);
            return tileentity1;
        } else {
            this.getServer().getLogger().severe("Don\'t know how to fix for this type... Can\'t do anything! :(");
            return tileentity;
        }
    }

    private boolean canSpawn(int i, int j) {
        return this.generator != null ? this.generator.canSpawn(this.getWorld(), i, j) : this.worldProvider.canSpawn(i, j);
    }

    public void doTick() {
        super.doTick();
        if (this.getWorldData().isHardcore() && this.getDifficulty() != EnumDifficulty.HARD) {
            this.getWorldData().setDifficulty(EnumDifficulty.HARD);
        }

        this.worldProvider.m().b();
        long i;

        if (this.everyoneDeeplySleeping()) {
            if (this.getGameRules().getBoolean("doDaylightCycle")) {
                i = this.worldData.getDayTime() + 24000L;
                this.worldData.setDayTime(i - i % 24000L);
            }

            this.e();
        }

        i = this.worldData.getTime();
        if (this.getGameRules().getBoolean("doMobSpawning") && this.worldData.getType() != WorldType.DEBUG_ALL_BLOCK_STATES && (this.allowMonsters || this.allowAnimals) && this instanceof WorldServer && this.players.size() > 0) {
            this.timings.mobSpawn.startTiming();
            this.R.a(this, this.allowMonsters && this.ticksPerMonsterSpawns != 0L && i % this.ticksPerMonsterSpawns == 0L, this.allowAnimals && this.ticksPerAnimalSpawns != 0L && i % this.ticksPerAnimalSpawns == 0L, this.worldData.getTime() % 400L == 0L);
            this.timings.mobSpawn.stopTiming();
        }

        this.timings.doChunkUnload.startTiming();
        this.methodProfiler.c("chunkSource");
        this.chunkProvider.unloadChunks();
        int j = this.a(1.0F);

        if (j != this.ab()) {
            this.c(j);
        }

        this.worldData.setTime(this.worldData.getTime() + 1L);
        if (this.getGameRules().getBoolean("doDaylightCycle")) {
            this.worldData.setDayTime(this.worldData.getDayTime() + 1L);
        }

        this.timings.doChunkUnload.stopTiming();
        this.methodProfiler.c("tickPending");
        this.timings.doTickPending.startTiming();
        this.a(false);
        this.timings.doTickPending.stopTiming();
        this.methodProfiler.c("tickBlocks");
        this.timings.doTickTiles.startTiming();
        this.h();
        this.timings.doTickTiles.stopTiming();
        this.methodProfiler.c("chunkMap");
        this.timings.doChunkMap.startTiming();
        this.manager.flush();
        this.timings.doChunkMap.stopTiming();
        this.methodProfiler.c("village");
        this.timings.doVillages.startTiming();
        this.villages.tick();
        this.siegeManager.a();
        this.timings.doVillages.stopTiming();
        this.methodProfiler.c("portalForcer");
        this.timings.doPortalForcer.startTiming();
        this.Q.a(this.getTime());
        this.timings.doPortalForcer.stopTiming();
        this.methodProfiler.b();
        this.timings.doSounds.startTiming();
        this.ak();
        this.getWorld().processChunkGC();
        this.timings.doChunkGC.stopTiming();
    }

    public BiomeBase.BiomeMeta a(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        List list = this.N().getMobsFor(enumcreaturetype, blockposition);

        return list != null && !list.isEmpty() ? (BiomeBase.BiomeMeta) WeightedRandom.a(this.random, list) : null;
    }

    public boolean a(EnumCreatureType enumcreaturetype, BiomeBase.BiomeMeta biomebase_biomemeta, BlockPosition blockposition) {
        List list = this.N().getMobsFor(enumcreaturetype, blockposition);

        return list != null && !list.isEmpty() ? list.contains(biomebase_biomemeta) : false;
    }

    public void everyoneSleeping() {
        this.O = false;
        if (!this.players.isEmpty()) {
            int i = 0;
            int j = 0;
            Iterator iterator = this.players.iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (entityhuman.isSpectator()) {
                    ++i;
                } else if (entityhuman.isSleeping() || entityhuman.fauxSleeping) {
                    ++j;
                }
            }

            this.O = j > 0 && j >= this.players.size() - i;
        }

    }

    protected void e() {
        this.O = false;
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            if (entityhuman.isSleeping()) {
                entityhuman.a(false, false, true);
            }
        }

        this.ag();
    }

    private void ag() {
        this.worldData.setStorm(false);
        if (!this.worldData.hasStorm()) {
            this.worldData.setWeatherDuration(0);
        }

        this.worldData.setThundering(false);
        if (!this.worldData.isThundering()) {
            this.worldData.setThunderDuration(0);
        }

    }

    public boolean everyoneDeeplySleeping() {
        if (this.O && !this.isClientSide) {
            Iterator iterator = this.players.iterator();
            boolean flag = false;

            EntityHuman entityhuman;

            do {
                if (!iterator.hasNext()) {
                    return flag;
                }

                entityhuman = (EntityHuman) iterator.next();
                if (entityhuman.isDeeplySleeping()) {
                    flag = true;
                }
            } while (!entityhuman.isSpectator() && (entityhuman.isDeeplySleeping() || entityhuman.fauxSleeping));

            return false;
        } else {
            return false;
        }
    }

    protected void h() {
        super.h();
        TLongShortIterator tlongshortiterator;
        long i;

        if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            tlongshortiterator = this.chunkTickList.iterator();

            while (tlongshortiterator.hasNext()) {
                tlongshortiterator.advance();
                i = tlongshortiterator.key();
                this.getChunkAt(World.keyToX(i), World.keyToZ(i)).b(false);
            }
        } else {
            tlongshortiterator = this.chunkTickList.iterator();

            while (tlongshortiterator.hasNext()) {
                tlongshortiterator.advance();
                i = tlongshortiterator.key();
                int j = World.keyToX(i);
                int k = World.keyToZ(i);

                if (this.chunkProvider.isChunkLoaded(j, k) && !this.chunkProviderServer.unloadQueue.contains(j, k)) {
                    int l = j * 16;
                    int i1 = k * 16;

                    this.methodProfiler.a("getChunk");
                    Chunk chunk = this.getChunkAt(j, k);

                    this.a(l, i1, chunk);
                    this.methodProfiler.c("tickChunk");
                    chunk.b(false);
                    this.methodProfiler.c("thunder");
                    int j1;
                    BlockPosition blockposition;

                    if (this.random.nextInt(100000) == 0 && this.S() && this.R()) {
                        this.m = this.m * 3 + 1013904223;
                        j1 = this.m >> 2;
                        blockposition = this.a(new BlockPosition(l + (j1 & 15), 0, i1 + (j1 >> 8 & 15)));
                        if (this.isRainingAt(blockposition)) {
                            this.strikeLightning(new EntityLightning(this, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ()));
                        }
                    }

                    this.methodProfiler.c("iceandsnow");
                    if (this.random.nextInt(16) == 0) {
                        this.m = this.m * 3 + 1013904223;
                        j1 = this.m >> 2;
                        blockposition = this.q(new BlockPosition(l + (j1 & 15), 0, i1 + (j1 >> 8 & 15)));
                        BlockPosition blockposition1 = blockposition.down();
                        org.bukkit.block.BlockState org_bukkit_block_blockstate;
                        BlockFormEvent blockformevent;

                        if (this.w(blockposition1)) {
                            org_bukkit_block_blockstate = this.getWorld().getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();
                            org_bukkit_block_blockstate.setTypeId(Block.getId(Blocks.ICE));
                            blockformevent = new BlockFormEvent(org_bukkit_block_blockstate.getBlock(), org_bukkit_block_blockstate);
                            this.getServer().getPluginManager().callEvent(blockformevent);
                            if (!blockformevent.isCancelled()) {
                                org_bukkit_block_blockstate.update(true);
                            }
                        }

                        if (this.S() && this.f(blockposition, true)) {
                            org_bukkit_block_blockstate = this.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()).getState();
                            org_bukkit_block_blockstate.setTypeId(Block.getId(Blocks.SNOW_LAYER));
                            blockformevent = new BlockFormEvent(org_bukkit_block_blockstate.getBlock(), org_bukkit_block_blockstate);
                            this.getServer().getPluginManager().callEvent(blockformevent);
                            if (!blockformevent.isCancelled()) {
                                org_bukkit_block_blockstate.update(true);
                            }
                        }

                        if (this.S() && this.getBiome(blockposition1).e()) {
                            this.getType(blockposition1).getBlock().k(this, blockposition1);
                        }
                    }

                    this.methodProfiler.c("tickBlocks");
                    j1 = this.getGameRules().c("randomTickSpeed");
                    if (j1 > 0) {
                        ChunkSection[] achunksection = chunk.getSections();
                        int k1 = achunksection.length;

                        for (int l1 = 0; l1 < k1; ++l1) {
                            ChunkSection chunksection = achunksection[l1];

                            if (chunksection != null && chunksection.shouldTick()) {
                                for (int i2 = 0; i2 < j1; ++i2) {
                                    this.m = this.m * 3 + 1013904223;
                                    int j2 = this.m >> 2;
                                    int k2 = j2 & 15;
                                    int l2 = j2 >> 8 & 15;
                                    int i3 = j2 >> 16 & 15;
                                    IBlockData iblockdata = chunksection.getType(k2, i3, l2);
                                    Block block = iblockdata.getBlock();

                                    if (block.isTicking()) {
                                        block.a((World) this, new BlockPosition(k2 + l, i3 + chunksection.getYPosition(), l2 + i1), iblockdata, this.random);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    tlongshortiterator.remove();
                }
            }
        }

        if (this.spigotConfig.clearChunksOnTick) {
            this.chunkTickList.clear();
        }

    }

    protected BlockPosition a(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.q(blockposition);
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition1, new BlockPosition(blockposition1.getX(), this.getHeight(), blockposition1.getZ()))).grow(3.0D, 3.0D, 3.0D);
        List list = this.a(EntityLiving.class, axisalignedbb, new Predicate() {
            public boolean a(EntityLiving entityliving) {
                return entityliving != null && entityliving.isAlive() && WorldServer.this.i(entityliving.getChunkCoordinates());
            }

            public boolean apply(Object object) {
                return this.a((EntityLiving) object);
            }
        });

        return !list.isEmpty() ? ((EntityLiving) list.get(this.random.nextInt(list.size()))).getChunkCoordinates() : blockposition1;
    }

    public boolean a(BlockPosition blockposition, Block block) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);

        return this.V.contains(nextticklistentry);
    }

    public void a(BlockPosition blockposition, Block block, int i) {
        this.a(blockposition, block, i, 0);
    }

    public void a(BlockPosition blockposition, Block block, int i, int j) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);
        byte b0 = 0;

        if (this.e && block.getMaterial() != Material.AIR) {
            if (block.N()) {
                b0 = 8;
                if (this.areChunksLoadedBetween(nextticklistentry.a.a(-b0, -b0, -b0), nextticklistentry.a.a(b0, b0, b0))) {
                    IBlockData iblockdata = this.getType(nextticklistentry.a);

                    if (iblockdata.getBlock().getMaterial() != Material.AIR && iblockdata.getBlock() == nextticklistentry.a()) {
                        iblockdata.getBlock().b((World) this, nextticklistentry.a, iblockdata, this.random);
                    }
                }

                return;
            }

            i = 1;
        }

        if (this.areChunksLoadedBetween(blockposition.a(-b0, -b0, -b0), blockposition.a(b0, b0, b0))) {
            if (block.getMaterial() != Material.AIR) {
                nextticklistentry.a((long) i + this.worldData.getTime());
                nextticklistentry.a(j);
            }

            if (!this.M.contains(nextticklistentry)) {
                this.M.add(nextticklistentry);
            }
        }

    }

    public void b(BlockPosition blockposition, Block block, int i, int j) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);

        nextticklistentry.a(j);
        if (block.getMaterial() != Material.AIR) {
            nextticklistentry.a((long) i + this.worldData.getTime());
        }

        if (!this.M.contains(nextticklistentry)) {
            this.M.add(nextticklistentry);
        }

    }

    public void tickEntities() {
        this.j();
        super.tickEntities();
        this.spigotConfig.currentPrimedTnt = 0;
    }

    public void j() {
        this.emptyTime = 0;
    }

    public boolean a(boolean flag) {
        if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            int i = this.M.size();

            if (i > 1000) {
                if (i > 20000) {
                    i /= 20;
                } else {
                    i = 1000;
                }
            }

            this.methodProfiler.a("cleaning");

            NextTickListEntry nextticklistentry;

            for (int j = 0; j < i; ++j) {
                nextticklistentry = (NextTickListEntry) this.M.first();
                if (!flag && nextticklistentry.b > this.worldData.getTime()) {
                    break;
                }

                this.M.remove(nextticklistentry);
                this.V.add(nextticklistentry);
            }

            this.methodProfiler.b();
            this.methodProfiler.a("ticking");
            Iterator iterator = this.V.iterator();

            while (iterator.hasNext()) {
                nextticklistentry = (NextTickListEntry) iterator.next();
                iterator.remove();
                byte b0 = 0;

                if (this.areChunksLoadedBetween(nextticklistentry.a.a(-b0, -b0, -b0), nextticklistentry.a.a(b0, b0, b0))) {
                    IBlockData iblockdata = this.getType(nextticklistentry.a);

                    if (iblockdata.getBlock().getMaterial() != Material.AIR && Block.a(iblockdata.getBlock(), nextticklistentry.a())) {
                        try {
                            iblockdata.getBlock().b((World) this, nextticklistentry.a, iblockdata, this.random);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.a(throwable, "Exception while ticking a block");
                            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being ticked");

                            CrashReportSystemDetails.a(crashreportsystemdetails, nextticklistentry.a, iblockdata);
                            throw new ReportedException(crashreport);
                        }
                    }
                } else {
                    this.a(nextticklistentry.a, nextticklistentry.a(), 0);
                }
            }

            this.methodProfiler.b();
            this.V.clear();
            return !this.M.isEmpty();
        }
    }

    public List<NextTickListEntry> a(Chunk chunk, boolean flag) {
        ChunkCoordIntPair chunkcoordintpair = chunk.j();
        int i = (chunkcoordintpair.x << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.z << 4) - 2;
        int l = k + 16 + 2;

        return this.a(new StructureBoundingBox(i, 0, k, j, 256, l), flag);
    }

    public List<NextTickListEntry> a(StructureBoundingBox structureboundingbox, boolean flag) {
        ArrayList arraylist = null;

        for (int i = 0; i < 2; ++i) {
            Iterator iterator;

            if (i == 0) {
                iterator = this.M.iterator();
            } else {
                iterator = this.V.iterator();
                if (!this.V.isEmpty()) {
                    WorldServer.a.debug("toBeTicked = " + this.V.size());
                }
            }

            while (iterator.hasNext()) {
                NextTickListEntry nextticklistentry = (NextTickListEntry) iterator.next();
                BlockPosition blockposition = nextticklistentry.a;

                if (blockposition.getX() >= structureboundingbox.a && blockposition.getX() < structureboundingbox.d && blockposition.getZ() >= structureboundingbox.c && blockposition.getZ() < structureboundingbox.f) {
                    if (flag) {
                        iterator.remove();
                    }

                    if (arraylist == null) {
                        arraylist = Lists.newArrayList();
                    }

                    arraylist.add(nextticklistentry);
                }
            }
        }

        return arraylist;
    }

    private boolean getSpawnNPCs() {
        return this.server.getSpawnNPCs();
    }

    private boolean getSpawnAnimals() {
        return this.server.getSpawnAnimals();
    }

    protected IChunkProvider k() {
        IChunkLoader ichunkloader = this.dataManager.createChunkLoader(this.worldProvider);
        Object object;

        if (this.generator != null) {
            object = new CustomChunkGenerator(this, this.getSeed(), this.generator);
        } else if (this.worldProvider instanceof WorldProviderHell) {
            object = new NetherChunkGenerator(this, this.getSeed());
        } else if (this.worldProvider instanceof WorldProviderTheEnd) {
            object = new SkyLandsChunkGenerator(this, this.getSeed());
        } else {
            object = new NormalChunkGenerator(this, this.getSeed());
        }

        this.chunkProviderServer = new ChunkProviderServer(this, ichunkloader, (IChunkProvider) object);
        return this.chunkProviderServer;
    }

    public List<TileEntity> getTileEntities(int i, int j, int k, int l, int i1, int j1) {
        ArrayList arraylist = Lists.newArrayList();

        for (int k1 = i >> 4; k1 <= l - 1 >> 4; ++k1) {
            for (int l1 = k >> 4; l1 <= j1 - 1 >> 4; ++l1) {
                Chunk chunk = this.getChunkAt(k1, l1);

                if (chunk != null) {
                    Iterator iterator = chunk.tileEntities.values().iterator();

                    while (iterator.hasNext()) {
                        Object object = iterator.next();
                        TileEntity tileentity = (TileEntity) object;

                        if (tileentity.position.getX() >= i && tileentity.position.getY() >= j && tileentity.position.getZ() >= k && tileentity.position.getX() < l && tileentity.position.getY() < i1 && tileentity.position.getZ() < j1) {
                            arraylist.add(tileentity);
                        }
                    }
                }
            }
        }

        return arraylist;
    }

    public boolean a(EntityHuman entityhuman, BlockPosition blockposition) {
        return !this.server.a(this, blockposition, entityhuman) && this.getWorldBorder().a(blockposition);
    }

    public void a(WorldSettings worldsettings) {
        if (!this.worldData.w()) {
            try {
                this.b(worldsettings);
                if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    this.aj();
                }

                super.a(worldsettings);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception initializing level");

                try {
                    this.a(crashreport);
                } catch (Throwable throwable1) {
                    ;
                }

                throw new ReportedException(crashreport);
            }

            this.worldData.d(true);
        }

    }

    private void aj() {
        this.worldData.f(false);
        this.worldData.c(true);
        this.worldData.setStorm(false);
        this.worldData.setThundering(false);
        this.worldData.i(1000000000);
        this.worldData.setDayTime(6000L);
        this.worldData.setGameType(WorldSettings.EnumGamemode.SPECTATOR);
        this.worldData.g(false);
        this.worldData.setDifficulty(EnumDifficulty.PEACEFUL);
        this.worldData.e(true);
        this.getGameRules().set("doDaylightCycle", "false");
    }

    private void b(WorldSettings worldsettings) {
        if (!this.worldProvider.e()) {
            this.worldData.setSpawn(BlockPosition.ZERO.up(this.worldProvider.getSeaLevel()));
        } else if (this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.worldData.setSpawn(BlockPosition.ZERO.up());
        } else {
            this.isLoading = true;
            WorldChunkManager worldchunkmanager = this.worldProvider.m();
            List list = worldchunkmanager.a();
            Random random = new Random(this.getSeed());
            BlockPosition blockposition = worldchunkmanager.a(0, 0, 256, list, random);
            int i = 0;
            int j = this.worldProvider.getSeaLevel();
            int k = 0;

            if (this.generator != null) {
                Random random1 = new Random(this.getSeed());
                Location location = this.generator.getFixedSpawnLocation(this.getWorld(), random1);

                if (location != null) {
                    if (location.getWorld() != this.getWorld()) {
                        throw new IllegalStateException("Cannot set spawn point for " + this.worldData.getName() + " to be in another world (" + location.getWorld().getName() + ")");
                    }

                    this.worldData.setSpawn(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                    this.isLoading = false;
                    return;
                }
            }

            if (blockposition != null) {
                i = blockposition.getX();
                k = blockposition.getZ();
            } else {
                WorldServer.a.warn("Unable to find spawn biome");
            }

            int l = 0;

            while (!this.canSpawn(i, k)) {
                i += random.nextInt(64) - random.nextInt(64);
                k += random.nextInt(64) - random.nextInt(64);
                ++l;
                if (l == 1000) {
                    break;
                }
            }

            this.worldData.setSpawn(new BlockPosition(i, j, k));
            this.isLoading = false;
            if (worldsettings.c()) {
                this.l();
            }
        }

    }

    protected void l() {
        WorldGenBonusChest worldgenbonuschest = new WorldGenBonusChest(WorldServer.U, 10);

        for (int i = 0; i < 10; ++i) {
            int j = this.worldData.c() + this.random.nextInt(6) - this.random.nextInt(6);
            int k = this.worldData.e() + this.random.nextInt(6) - this.random.nextInt(6);
            BlockPosition blockposition = this.r(new BlockPosition(j, 0, k)).up();

            if (worldgenbonuschest.generate(this, this.random, blockposition)) {
                break;
            }
        }

    }

    public BlockPosition getDimensionSpawn() {
        return this.worldProvider.h();
    }

    public void save(boolean flag, IProgressUpdate iprogressupdate) throws ExceptionWorldConflict {
        if (this.chunkProvider.canSave()) {
            Bukkit.getPluginManager().callEvent(new WorldSaveEvent(this.getWorld()));
            if (iprogressupdate != null) {
                iprogressupdate.a("Saving level");
            }

            this.a();
            if (iprogressupdate != null) {
                iprogressupdate.c("Saving chunks");
            }

            this.chunkProvider.saveChunks(flag, iprogressupdate);
            Collection collection = this.chunkProviderServer.a();
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                Chunk chunk = (Chunk) iterator.next();

                if (chunk != null && !this.manager.a(chunk.locX, chunk.locZ)) {
                    this.chunkProviderServer.queueUnload(chunk.locX, chunk.locZ);
                }
            }
        }

    }

    public void flushSave() {
        if (this.chunkProvider.canSave()) {
            this.chunkProvider.c();
        }

    }

    protected void a() throws ExceptionWorldConflict {
        this.checkSession();
        this.worldData.a(this.getWorldBorder().getSize());
        this.worldData.d(this.getWorldBorder().getCenterX());
        this.worldData.c(this.getWorldBorder().getCenterZ());
        this.worldData.e(this.getWorldBorder().getDamageBuffer());
        this.worldData.f(this.getWorldBorder().getDamageAmount());
        this.worldData.j(this.getWorldBorder().getWarningDistance());
        this.worldData.k(this.getWorldBorder().getWarningTime());
        this.worldData.b(this.getWorldBorder().j());
        this.worldData.e(this.getWorldBorder().i());
        if (!(this instanceof SecondaryWorldServer)) {
            this.worldMaps.a();
        }

        this.dataManager.saveWorldData(this.worldData, this.server.getPlayerList().t());
    }

    protected void a(Entity entity) {
        super.a(entity);
        this.entitiesById.a(entity.getId(), entity);
        this.entitiesByUUID.put(entity.getUniqueID(), entity);
        Entity[] aentity = entity.aB();

        if (aentity != null) {
            for (int i = 0; i < aentity.length; ++i) {
                this.entitiesById.a(aentity[i].getId(), aentity[i]);
            }
        }

    }

    protected void b(Entity entity) {
        super.b(entity);
        this.entitiesById.d(entity.getId());
        this.entitiesByUUID.remove(entity.getUniqueID());
        Entity[] aentity = entity.aB();

        if (aentity != null) {
            for (int i = 0; i < aentity.length; ++i) {
                this.entitiesById.d(aentity[i].getId());
            }
        }

    }

    public boolean strikeLightning(Entity entity) {
        LightningStrikeEvent lightningstrikeevent = new LightningStrikeEvent(this.getWorld(), (LightningStrike) entity.getBukkitEntity());

        this.getServer().getPluginManager().callEvent(lightningstrikeevent);
        if (lightningstrikeevent.isCancelled()) {
            return false;
        } else if (super.strikeLightning(entity)) {
            this.server.getPlayerList().sendPacketNearby(entity.locX, entity.locY, entity.locZ, 512.0D, this.dimension, new PacketPlayOutSpawnEntityWeather(entity));
            return true;
        } else {
            return false;
        }
    }

    public void broadcastEntityEffect(Entity entity, byte b0) {
        this.getTracker().sendPacketToEntity(entity, new PacketPlayOutEntityStatus(entity, b0));
    }

    public Explosion createExplosion(Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        Explosion explosion = super.createExplosion(entity, d0, d1, d2, f, flag, flag1);

        if (explosion.wasCanceled) {
            return explosion;
        } else {
            if (!flag1) {
                explosion.clearBlocks();
            }

            Iterator iterator = this.players.iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (entityhuman.e(d0, d1, d2) < 4096.0D) {
                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutExplosion(d0, d1, d2, f, explosion.getBlocks(), (Vec3D) explosion.b().get(entityhuman)));
                }
            }

            return explosion;
        }
    }

    public void playBlockAction(BlockPosition blockposition, Block block, int i, int j) {
        BlockActionData blockactiondata = new BlockActionData(blockposition, block, i, j);
        Iterator iterator = this.S[this.T].iterator();

        while (iterator.hasNext()) {
            BlockActionData blockactiondata1 = (BlockActionData) iterator.next();

            if (blockactiondata1.equals(blockactiondata)) {
                return;
            }
        }

        this.S[this.T].add(blockactiondata);
    }

    private void ak() {
        while (!this.S[this.T].isEmpty()) {
            int i = this.T;

            this.T ^= 1;
            Iterator iterator = this.S[i].iterator();

            while (iterator.hasNext()) {
                BlockActionData blockactiondata = (BlockActionData) iterator.next();

                if (this.a(blockactiondata)) {
                    this.server.getPlayerList().sendPacketNearby((double) blockactiondata.a().getX(), (double) blockactiondata.a().getY(), (double) blockactiondata.a().getZ(), 64.0D, this.dimension, new PacketPlayOutBlockAction(blockactiondata.a(), blockactiondata.d(), blockactiondata.b(), blockactiondata.c()));
                }
            }

            this.S[i].clear();
        }

    }

    private boolean a(BlockActionData blockactiondata) {
        IBlockData iblockdata = this.getType(blockactiondata.a());

        return iblockdata.getBlock() == blockactiondata.d() ? iblockdata.getBlock().a(this, blockactiondata.a(), iblockdata, blockactiondata.b(), blockactiondata.c()) : false;
    }

    public void saveLevel() {
        this.dataManager.a();
    }

    protected void p() {
        boolean flag = this.S();

        super.p();
        int i;

        if (flag != this.S()) {
            for (i = 0; i < this.players.size(); ++i) {
                if (((EntityPlayer) this.players.get(i)).world == this) {
                    ((EntityPlayer) this.players.get(i)).setPlayerWeather(!flag ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
                }
            }
        }

        for (i = 0; i < this.players.size(); ++i) {
            if (((EntityPlayer) this.players.get(i)).world == this) {
                ((EntityPlayer) this.players.get(i)).updateWeather(this.o, this.p, this.q, this.r);
            }
        }

    }

    protected int q() {
        return this.server.getPlayerList().s();
    }

    public MinecraftServer getMinecraftServer() {
        return this.server;
    }

    public EntityTracker getTracker() {
        return this.tracker;
    }

    public PlayerChunkMap getPlayerChunkMap() {
        return this.manager;
    }

    public PortalTravelAgent getTravelAgent() {
        return this.Q;
    }

    public void a(EnumParticle enumparticle, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        this.a(enumparticle, false, d0, d1, d2, i, d3, d4, d5, d6, aint);
    }

    public void a(EnumParticle enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        this.sendParticles((EntityPlayer) null, enumparticle, flag, d0, d1, d2, i, d3, d4, d5, d6, aint);
    }

    public void sendParticles(EntityPlayer entityplayer, EnumParticle enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
        PacketPlayOutWorldParticles packetplayoutworldparticles = new PacketPlayOutWorldParticles(enumparticle, flag, (float) d0, (float) d1, (float) d2, (float) d3, (float) d4, (float) d5, (float) d6, i, aint);

        for (int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.players.get(j);

            if (entityplayer == null || entityplayer1.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                BlockPosition blockposition = entityplayer1.getChunkCoordinates();
                double d7 = blockposition.c(d0, d1, d2);

                if (d7 <= 256.0D || flag && d7 <= 65536.0D) {
                    entityplayer1.playerConnection.sendPacket(packetplayoutworldparticles);
                }
            }
        }

    }

    public Entity getEntity(UUID uuid) {
        return (Entity) this.entitiesByUUID.get(uuid);
    }

    public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        return this.server.postToMainThread(runnable);
    }

    public boolean isMainThread() {
        return this.server.isMainThread();
    }

    static class BlockActionDataList extends ArrayList<BlockActionData> {

        private BlockActionDataList() {}

        BlockActionDataList(Object object) {
            this();
        }
    }
}
