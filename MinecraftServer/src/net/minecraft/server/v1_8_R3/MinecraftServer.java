package net.minecraft.server.v1_8_R3;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.ResourceLeakDetector;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World.Environment;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.SpigotTimings;
import org.bukkit.craftbukkit.v1_8_R3.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.v1_8_R3.util.ServerShutdownThread;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginLoadOrder;
import org.spigotmc.CustomTimingsHandler;
import org.spigotmc.SpigotConfig;
import org.spigotmc.WatchdogThread;

public abstract class MinecraftServer implements Runnable, ICommandListener, IAsyncTaskHandler, IMojangStatistics {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final File a = new File("usercache.json");
    private static MinecraftServer l;
    public Convertable convertable;
    private final MojangStatisticsGenerator n = new MojangStatisticsGenerator("server", this, az());
    public File universe;
    private final List<IUpdatePlayerListBox> p = Lists.newArrayList();
    public final ICommandHandler b;
    public final MethodProfiler methodProfiler = new MethodProfiler();
    private ServerConnection q;
    private final ServerPing r = new ServerPing();
    private final Random s = new Random();
    private String serverIp;
    private int u = -1;
    public WorldServer[] worldServer;
    public PlayerList v;
    private boolean isRunning = true;
    private boolean isStopped;
    private int ticks;
    protected final Proxy e;
    public String f;
    public int g;
    private boolean onlineMode;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    private String motd;
    private int F;
    private int G = 0;
    public final long[] h = new long[100];
    public long[][] i;
    private KeyPair H;
    private String I;
    private String J;
    private boolean demoMode;
    private boolean M;
    private boolean N;
    private String O = "";
    private String P = "";
    private boolean Q;
    private long R;
    private String S;
    private boolean T;
    private boolean U;
    private final YggdrasilAuthenticationService V;
    private final MinecraftSessionService W;
    private long X = 0L;
    private final GameProfileRepository Y;
    private final UserCache Z;
    protected final Queue<FutureTask<?>> j = new ConcurrentLinkedQueue();
    private Thread serverThread;
    private long ab = az();
    public List<WorldServer> worlds = new ArrayList();
    public CraftServer server;
    public OptionSet options;
    public ConsoleCommandSender console;
    public RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    public static int currentTick = (int) (System.currentTimeMillis() / 50L);
    public final Thread primaryThread;
    public Queue<Runnable> processQueue = new ConcurrentLinkedQueue();
    public int autosavePeriod;
    private static final int TPS = 20;
    private static final int TICK_TIME = 50000000;
    private static final int SAMPLE_INTERVAL = 100;
    public final double[] recentTps = new double[3];
    private boolean hasStopped = false;
    private final Object stopLock = new Object();

    public MinecraftServer(OptionSet optionset, Proxy proxy, File file) {
        ResourceLeakDetector.setEnabled(false);
        this.e = proxy;
        MinecraftServer.l = this;
        this.Z = new UserCache(this, file);
        this.b = this.h();
        this.V = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        this.W = this.V.createMinecraftSessionService();
        this.Y = this.V.createProfileRepository();
        this.options = optionset;
        if (System.console() == null) {
            System.setProperty("org.bukkit.craftbukkit.libs.jline.terminal", "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal");
            Main.useJline = false;
        }

        try {
            this.reader = new ConsoleReader(System.in, System.out);
            this.reader.setExpandEvents(false);
        } catch (Throwable throwable) {
            try {
                System.setProperty("org.bukkit.craftbukkit.libs.jline.terminal", "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");
                Main.useJline = false;
                this.reader = new ConsoleReader(System.in, System.out);
                this.reader.setExpandEvents(false);
            } catch (IOException ioexception) {
                MinecraftServer.LOGGER.warn((String) null, ioexception);
            }
        }

        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));
        this.serverThread = this.primaryThread = new Thread(this, "Server thread");
    }

    public abstract PropertyManager getPropertyManager();

    protected CommandDispatcher h() {
        return new CommandDispatcher();
    }

    protected abstract boolean init() throws IOException;

    protected void a(String s) {
        if (this.getConvertable().isConvertable(s)) {
            MinecraftServer.LOGGER.info("Converting map!");
            this.b("menu.convertingLevel");
            this.getConvertable().convert(s, new IProgressUpdate() {
                private long b = System.currentTimeMillis();

                public void a(String s) {}

                public void a(int i) {
                    if (System.currentTimeMillis() - this.b >= 1000L) {
                        this.b = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... " + i + "%");
                    }

                }

                public void c(String s) {}
            });
        }

    }

    protected synchronized void b(String s) {
        this.S = s;
    }

    protected void a(String s, String s1, long i, WorldType worldtype, String s2) {
        this.a(s);
        this.b("menu.loadingLevel");
        this.worldServer = new WorldServer[3];
        byte b0 = 3;

        for (int j = 0; j < b0; ++j) {
            byte b1 = 0;

            if (j == 1) {
                if (!this.getAllowNether()) {
                    continue;
                }

                b1 = -1;
            }

            if (j == 2) {
                if (!this.server.getAllowEnd()) {
                    continue;
                }

                b1 = 1;
            }

            String s3 = Environment.getEnvironment(b1).toString().toLowerCase();
            String s4 = b1 == 0 ? s : s + "_" + s3;
            ChunkGenerator chunkgenerator = this.server.getGenerator(s4);
            WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);

            worldsettings.setGeneratorSettings(s2);
            WorldServer worldserver;

            if (j == 0) {
                ServerNBTManager servernbtmanager = new ServerNBTManager(this.server.getWorldContainer(), s1, true);
                WorldData worlddata = servernbtmanager.getWorldData();

                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, s1);
                }

                worlddata.checkName(s1);
                if (this.X()) {
                    worldserver = (WorldServer) (new DemoWorldServer(this, servernbtmanager, worlddata, b1, this.methodProfiler)).b();
                } else {
                    worldserver = (WorldServer) (new WorldServer(this, servernbtmanager, worlddata, b1, this.methodProfiler, Environment.getEnvironment(b1), chunkgenerator)).b();
                }

                worldserver.a(worldsettings);
                this.server.scoreboardManager = new CraftScoreboardManager(this, worldserver.getScoreboard());
            } else {
                String s5 = "DIM" + b1;
                File file = new File(new File(s4), s5);
                File file1 = new File(new File(s), s5);

                if (!file.isDirectory() && file1.isDirectory()) {
                    MinecraftServer.LOGGER.info("---- Migration of old " + s3 + " folder required ----");
                    MinecraftServer.LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + s3 + " folder to a new location in order to operate correctly.");
                    MinecraftServer.LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    MinecraftServer.LOGGER.info("Attempting to move " + file1 + " to " + file + "...");
                    if (file.exists()) {
                        MinecraftServer.LOGGER.warn("A file or folder already exists at " + file + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + s3 + " folder failed ----");
                    } else if (file.getParentFile().mkdirs()) {
                        if (file1.renameTo(file)) {
                            MinecraftServer.LOGGER.info("Success! To restore " + s3 + " in the future, simply move " + file + " to " + file1);

                            try {
                                Files.copy(new File(new File(s), "level.dat"), new File(new File(s4), "level.dat"));
                            } catch (IOException ioexception) {
                                MinecraftServer.LOGGER.warn("Unable to migrate world data.");
                            }

                            MinecraftServer.LOGGER.info("---- Migration of old " + s3 + " folder complete ----");
                        } else {
                            MinecraftServer.LOGGER.warn("Could not move folder " + file1 + " to " + file + "!");
                            MinecraftServer.LOGGER.info("---- Migration of old " + s3 + " folder failed ----");
                        }
                    } else {
                        MinecraftServer.LOGGER.warn("Could not create path for " + file + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + s3 + " folder failed ----");
                    }
                }

                ServerNBTManager servernbtmanager1 = new ServerNBTManager(this.server.getWorldContainer(), s4, true);
                WorldData worlddata1 = servernbtmanager1.getWorldData();

                if (worlddata1 == null) {
                    worlddata1 = new WorldData(worldsettings, s4);
                }

                worlddata1.checkName(s4);
                worldserver = (WorldServer) (new SecondaryWorldServer(this, servernbtmanager1, b1, (WorldServer) this.worlds.get(0), this.methodProfiler, worlddata1, Environment.getEnvironment(b1), chunkgenerator)).b();
            }

            this.server.getPluginManager().callEvent(new WorldInitEvent(worldserver.getWorld()));
            worldserver.addIWorldAccess(new WorldManager(this, worldserver));
            if (!this.T()) {
                worldserver.getWorldData().setGameType(this.getGamemode());
            }

            this.worlds.add(worldserver);
            this.getPlayerList().setPlayerFileData((WorldServer[]) this.worlds.toArray(new WorldServer[this.worlds.size()]));
        }

        this.a(this.getDifficulty());
        this.k();
    }

    protected void k() {
        boolean flag = false;

        this.b("menu.generatingTerrain");

        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = (WorldServer) this.worlds.get(i);

            MinecraftServer.LOGGER.info("Preparing start region for level " + i + " (Seed: " + worldserver.getSeed() + ")");
            if (worldserver.getWorld().getKeepSpawnInMemory()) {
                BlockPosition blockposition = worldserver.getSpawn();
                long j = az();
                int k = 0;

                for (int l = -192; l <= 192 && this.isRunning(); l += 16) {
                    for (int i1 = -192; i1 <= 192 && this.isRunning(); i1 += 16) {
                        long j1 = az();

                        if (j1 - j > 1000L) {
                            this.a_("Preparing spawn area", k * 100 / 625);
                            j = j1;
                        }

                        ++k;
                        worldserver.chunkProviderServer.getChunkAt(blockposition.getX() + l >> 4, blockposition.getZ() + i1 >> 4);
                    }
                }
            }
        }

        Iterator iterator = this.worlds.iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver1 = (WorldServer) iterator.next();

            this.server.getPluginManager().callEvent(new WorldLoadEvent(worldserver1.getWorld()));
        }

        this.s();
    }

    protected void a(String s, IDataManager idatamanager) {
        File file = new File(idatamanager.getDirectory(), "resources.zip");

        if (file.isFile()) {
            this.setResourcePack("level://" + s + "/" + file.getName(), "");
        }

    }

    public abstract boolean getGenerateStructures();

    public abstract WorldSettings.EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int p();

    public abstract boolean q();

    public abstract boolean r();

    protected void a_(String s, int i) {
        this.f = s;
        this.g = i;
        MinecraftServer.LOGGER.info(s + ": " + i + "%");
    }

    protected void s() {
        this.f = null;
        this.g = 0;
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    protected void saveChunks(boolean flag) throws ExceptionWorldConflict {
        if (!this.N) {
            WorldServer[] aworldserver = this.worldServer;
            int i = aworldserver.length;

            for (int j = 0; j < this.worlds.size(); ++j) {
                WorldServer worldserver = (WorldServer) this.worlds.get(j);

                if (worldserver != null) {
                    if (!flag) {
                        MinecraftServer.LOGGER.info("Saving chunks for level \'" + worldserver.getWorldData().getName() + "\'/" + worldserver.worldProvider.getName());
                    }

                    try {
                        worldserver.save(true, (IProgressUpdate) null);
                        worldserver.saveLevel();
                    } catch (ExceptionWorldConflict exceptionworldconflict) {
                        MinecraftServer.LOGGER.warn(exceptionworldconflict.getMessage());
                    }
                }
            }
        }

    }

    public void stop() throws ExceptionWorldConflict {
        Object object = this.stopLock;

        synchronized (this.stopLock) {
            if (this.hasStopped) {
                return;
            }

            this.hasStopped = true;
        }

        if (!this.N) {
            MinecraftServer.LOGGER.info("Stopping server");
            if (this.server != null) {
                this.server.disablePlugins();
            }

            if (this.aq() != null) {
                this.aq().b();
            }

            if (this.v != null) {
                MinecraftServer.LOGGER.info("Saving players");
                this.v.savePlayers();
                this.v.u();
            }

            if (this.worldServer != null) {
                MinecraftServer.LOGGER.info("Saving worlds");
                this.saveChunks(false);
            }

            if (this.n.d()) {
                this.n.e();
            }

            if (SpigotConfig.saveUserCacheOnStopOnly) {
                MinecraftServer.LOGGER.info("Saving usercache.json");
                this.Z.c();
            }
        }

    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void c(String s) {
        this.serverIp = s;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void safeShutdown() {
        this.isRunning = false;
    }

    private static double calcTps(double d0, double d1, double d2) {
        return d0 * d1 + d2 * (1.0D - d1);
    }

    public void run() {
        try {
            if (this.init()) {
                this.ab = az();
                this.r.setMOTD(new ChatComponentText(this.motd));
                this.r.setServerInfo(new ServerPing.ServerData("1.8.6", 47));
                this.a(this.r);
                Arrays.fill(this.recentTps, 20.0D);
                long i = System.nanoTime();
                long j = 0L;
                long k = i;

                while (this.isRunning) {
                    long l = System.nanoTime();
                    long i1 = 50000000L - (l - i) - j;

                    if (i1 > 0L) {
                        Thread.sleep(i1 / 1000000L);
                        j = 0L;
                    } else {
                        j = Math.min(1000000000L, Math.abs(i1));
                        if (MinecraftServer.currentTick++ % 100 == 0) {
                            double d0 = 1.0E9D / (double) (l - k) * 100.0D;

                            this.recentTps[0] = calcTps(this.recentTps[0], 0.92D, d0);
                            this.recentTps[1] = calcTps(this.recentTps[1], 0.9835D, d0);
                            this.recentTps[2] = calcTps(this.recentTps[2], 0.9945D, d0);
                            k = l;
                        }

                        i = l;
                        this.A();
                        this.Q = true;
                    }
                }
            } else {
                this.a((CrashReport) null);
            }
        } catch (Throwable throwable) {
            MinecraftServer.LOGGER.error("Encountered an unexpected exception", throwable);
            if (throwable.getCause() != null) {
                MinecraftServer.LOGGER.error("\tCause of unexpected exception was", throwable.getCause());
            }

            CrashReport crashreport = null;

            if (throwable instanceof ReportedException) {
                crashreport = this.b(((ReportedException) throwable).a());
            } else {
                crashreport = this.b(new CrashReport("Exception in server tick loop", throwable));
            }

            File file = new File(new File(this.y(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.a(file)) {
                MinecraftServer.LOGGER.error("This crash report has been saved to: " + file.getAbsolutePath());
            } else {
                MinecraftServer.LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.a(crashreport);
        } finally {
            try {
                WatchdogThread.doStop();
                this.isStopped = true;
                this.stop();
            } catch (Throwable throwable1) {
                MinecraftServer.LOGGER.error("Exception stopping the server", throwable1);
            } finally {
                try {
                    this.reader.getTerminal().restore();
                } catch (Exception exception) {
                    ;
                }

                this.z();
            }

        }

    }

    private void a(ServerPing serverping) {
        File file = this.d("server-icon.png");

        if (file.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file);

                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);

                serverping.setFavicon("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
            } catch (Exception exception) {
                MinecraftServer.LOGGER.error("Couldn\'t load server icon", exception);
            } finally {
                bytebuf.release();
            }
        }

    }

    public File y() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {}

    protected void z() {}

    protected void A() throws ExceptionWorldConflict {
        SpigotTimings.serverTickTimer.startTiming();
        long i = System.nanoTime();

        ++this.ticks;
        if (this.T) {
            this.T = false;
            this.methodProfiler.a = true;
            this.methodProfiler.a();
        }

        this.methodProfiler.a("root");
        this.B();
        if (i - this.X >= 5000000000L) {
            this.X = i;
            this.r.setPlayerSample(new ServerPing.ServerPingPlayerSample(this.J(), this.I()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.I(), 12)];
            int j = MathHelper.nextInt(this.s, 0, this.I() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = ((EntityPlayer) this.v.v().get(j + k)).getProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.r.b().a(agameprofile);
        }

        if (this.autosavePeriod > 0 && this.ticks % this.autosavePeriod == 0) {
            SpigotTimings.worldSaveTimer.startTiming();
            this.methodProfiler.a("save");
            this.v.savePlayers();
            this.server.playerCommandState = true;
            Iterator iterator = this.worlds.iterator();

            while (iterator.hasNext()) {
                World world = (World) iterator.next();

                world.getWorld().save(false);
            }

            this.server.playerCommandState = false;
            this.methodProfiler.b();
            SpigotTimings.worldSaveTimer.stopTiming();
        }

        this.methodProfiler.a("tallying");
        this.h[this.ticks % 100] = System.nanoTime() - i;
        this.methodProfiler.b();
        this.methodProfiler.a("snooper");
        if (this.getSnooperEnabled() && !this.n.d() && this.ticks > 100) {
            this.n.a();
        }

        if (this.getSnooperEnabled() && this.ticks % 6000 == 0) {
            this.n.b();
        }

        this.methodProfiler.b();
        this.methodProfiler.b();
        WatchdogThread.tick();
        SpigotTimings.serverTickTimer.stopTiming();
        CustomTimingsHandler.tick();
    }

    public void B() {
        this.methodProfiler.a("jobs");
        int i = this.j.size();

        FutureTask futuretask;

        while (i-- > 0 && (futuretask = (FutureTask) this.j.poll()) != null) {
            SystemUtils.a(futuretask, MinecraftServer.LOGGER);
        }

        this.methodProfiler.c("levels");
        SpigotTimings.schedulerTimer.startTiming();
        this.server.getScheduler().mainThreadHeartbeat(this.ticks);
        SpigotTimings.schedulerTimer.stopTiming();
        SpigotTimings.processQueueTimer.startTiming();

        while (!this.processQueue.isEmpty()) {
            ((Runnable) this.processQueue.remove()).run();
        }

        SpigotTimings.processQueueTimer.stopTiming();
        SpigotTimings.chunkIOTickTimer.startTiming();
        ChunkIOExecutor.tick();
        SpigotTimings.chunkIOTickTimer.stopTiming();
        SpigotTimings.timeUpdateTimer.startTiming();
        int j;

        if (this.ticks % 20 == 0) {
            for (j = 0; j < this.getPlayerList().players.size(); ++j) {
                EntityPlayer entityplayer = (EntityPlayer) this.getPlayerList().players.get(j);

                entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(entityplayer.world.getTime(), entityplayer.getPlayerTime(), entityplayer.world.getGameRules().getBoolean("doDaylightCycle")));
            }
        }

        SpigotTimings.timeUpdateTimer.stopTiming();

        for (j = 0; j < this.worlds.size(); ++j) {
            System.nanoTime();
            WorldServer worldserver = (WorldServer) this.worlds.get(j);

            this.methodProfiler.a(worldserver.getWorldData().getName());
            this.methodProfiler.a("tick");

            Throwable throwable;
            CrashReport crashreport;

            try {
                worldserver.timings.doTick.startTiming();
                worldserver.doTick();
                worldserver.timings.doTick.stopTiming();
            } catch (Throwable throwable1) {
                throwable = throwable1;

                try {
                    crashreport = CrashReport.a(throwable, "Exception ticking world");
                } catch (Throwable throwable2) {
                    throw new RuntimeException("Error generating crash report", throwable2);
                }

                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            try {
                worldserver.timings.tickEntities.startTiming();
                worldserver.tickEntities();
                worldserver.timings.tickEntities.stopTiming();
            } catch (Throwable throwable3) {
                throwable = throwable3;

                try {
                    crashreport = CrashReport.a(throwable, "Exception ticking world entities");
                } catch (Throwable throwable4) {
                    throw new RuntimeException("Error generating crash report", throwable4);
                }

                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            this.methodProfiler.b();
            this.methodProfiler.a("tracker");
            worldserver.timings.tracker.startTiming();
            worldserver.getTracker().updatePlayers();
            worldserver.timings.tracker.stopTiming();
            this.methodProfiler.b();
            this.methodProfiler.b();
        }

        this.methodProfiler.c("connection");
        SpigotTimings.connectionTimer.startTiming();
        this.aq().c();
        SpigotTimings.connectionTimer.stopTiming();
        this.methodProfiler.c("players");
        SpigotTimings.playerListTimer.startTiming();
        this.v.tick();
        SpigotTimings.playerListTimer.stopTiming();
        this.methodProfiler.c("tickables");
        SpigotTimings.tickablesTimer.startTiming();

        for (j = 0; j < this.p.size(); ++j) {
            ((IUpdatePlayerListBox) this.p.get(j)).c();
        }

        SpigotTimings.tickablesTimer.stopTiming();
        this.methodProfiler.b();
    }

    public boolean getAllowNether() {
        return true;
    }

    public void a(IUpdatePlayerListBox iupdateplayerlistbox) {
        this.p.add(iupdateplayerlistbox);
    }

    public static void main(OptionSet optionset) {
        DispenserRegistry.c();

        try {
            DedicatedServer dedicatedserver = new DedicatedServer(optionset);

            if (optionset.has("port")) {
                int i = ((Integer) optionset.valueOf("port")).intValue();

                if (i > 0) {
                    dedicatedserver.setPort(i);
                }
            }

            if (optionset.has("universe")) {
                dedicatedserver.universe = (File) optionset.valueOf("universe");
            }

            if (optionset.has("world")) {
                dedicatedserver.setWorld((String) optionset.valueOf("world"));
            }

            dedicatedserver.primaryThread.start();
        } catch (Exception exception) {
            MinecraftServer.LOGGER.fatal("Failed to start the minecraft server", exception);
        }

    }

    public void C() {}

    public File d(String s) {
        return new File(this.y(), s);
    }

    public void info(String s) {
        MinecraftServer.LOGGER.info(s);
    }

    public void warning(String s) {
        MinecraftServer.LOGGER.warn(s);
    }

    public WorldServer getWorldServer(int i) {
        Iterator iterator = this.worlds.iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (worldserver.dimension == i) {
                return worldserver;
            }
        }

        return (WorldServer) this.worlds.get(0);
    }

    public String E() {
        return this.serverIp;
    }

    public int F() {
        return this.u;
    }

    public String G() {
        return this.motd;
    }

    public String getVersion() {
        return "1.8.6";
    }

    public int I() {
        return this.v.getPlayerCount();
    }

    public int J() {
        return this.v.getMaxPlayers();
    }

    public String[] getPlayers() {
        return this.v.f();
    }

    public GameProfile[] L() {
        return this.v.g();
    }

    public boolean isDebugging() {
        return this.getPropertyManager().getBoolean("debug", false);
    }

    public void g(String s) {
        MinecraftServer.LOGGER.error(s);
    }

    public void h(String s) {
        if (this.isDebugging()) {
            MinecraftServer.LOGGER.info(s);
        }

    }

    public String getServerModName() {
        return "Spigot";
    }

    public CrashReport b(CrashReport crashreport) {
        crashreport.g().a("Profiler Position", new Callable() {
            public String a() throws Exception {
                return MinecraftServer.this.methodProfiler.a ? MinecraftServer.this.methodProfiler.c() : "N/A (disabled)";
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        if (this.v != null) {
            crashreport.g().a("Player Count", new Callable() {
                public String a() {
                    return MinecraftServer.this.v.getPlayerCount() + " / " + MinecraftServer.this.v.getMaxPlayers() + "; " + MinecraftServer.this.v.v();
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
        }

        return crashreport;
    }

    public List<String> tabCompleteCommand(ICommandListener icommandlistener, String s, BlockPosition blockposition) {
        return this.server.tabComplete(icommandlistener, s);
    }

    public static MinecraftServer getServer() {
        return MinecraftServer.l;
    }

    public boolean O() {
        return true;
    }

    public String getName() {
        return "Server";
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        MinecraftServer.LOGGER.info(ichatbasecomponent.c());
    }

    public boolean a(int i, String s) {
        return true;
    }

    public ICommandHandler getCommandHandler() {
        return this.b;
    }

    public KeyPair Q() {
        return this.H;
    }

    public int R() {
        return this.u;
    }

    public void setPort(int i) {
        this.u = i;
    }

    public String S() {
        return this.I;
    }

    public void i(String s) {
        this.I = s;
    }

    public boolean T() {
        return this.I != null;
    }

    public String U() {
        return this.J;
    }

    public void setWorld(String s) {
        this.J = s;
    }

    public void a(KeyPair keypair) {
        this.H = keypair;
    }

    public void a(EnumDifficulty enumdifficulty) {
        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = (WorldServer) this.worlds.get(i);

            if (worldserver != null) {
                if (worldserver.getWorldData().isHardcore()) {
                    worldserver.getWorldData().setDifficulty(EnumDifficulty.HARD);
                    worldserver.setSpawnFlags(true, true);
                } else if (this.T()) {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
                }
            }
        }

    }

    protected boolean getSpawnMonsters() {
        return true;
    }

    public boolean X() {
        return this.demoMode;
    }

    public void b(boolean flag) {
        this.demoMode = flag;
    }

    public void c(boolean flag) {
        this.M = flag;
    }

    public Convertable getConvertable() {
        return this.convertable;
    }

    public void aa() {
        this.N = true;
        this.getConvertable().d();

        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = (WorldServer) this.worlds.get(i);

            if (worldserver != null) {
                worldserver.saveLevel();
            }
        }

        this.getConvertable().e(((WorldServer) this.worlds.get(0)).getDataManager().g());
        this.safeShutdown();
    }

    public String getResourcePack() {
        return this.O;
    }

    public String getResourcePackHash() {
        return this.P;
    }

    public void setResourcePack(String s, String s1) {
        this.O = s;
        this.P = s1;
    }

    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", Boolean.valueOf(false));
        mojangstatisticsgenerator.a("whitelist_count", Integer.valueOf(0));
        if (this.v != null) {
            mojangstatisticsgenerator.a("players_current", Integer.valueOf(this.I()));
            mojangstatisticsgenerator.a("players_max", Integer.valueOf(this.J()));
            mojangstatisticsgenerator.a("players_seen", Integer.valueOf(this.v.getSeenPlayers().length));
        }

        mojangstatisticsgenerator.a("uses_auth", Boolean.valueOf(this.onlineMode));
        mojangstatisticsgenerator.a("gui_state", this.as() ? "enabled" : "disabled");
        mojangstatisticsgenerator.a("run_time", Long.valueOf((az() - mojangstatisticsgenerator.g()) / 60L * 1000L));
        mojangstatisticsgenerator.a("avg_tick_ms", Integer.valueOf((int) (MathHelper.a(this.h) * 1.0E-6D)));
        int i = 0;

        if (this.worldServer != null) {
            for (int j = 0; j < this.worlds.size(); ++j) {
                WorldServer worldserver = (WorldServer) this.worlds.get(j);

                if (worldserver != null) {
                    WorldData worlddata = worldserver.getWorldData();

                    mojangstatisticsgenerator.a("world[" + i + "][dimension]", Integer.valueOf(worldserver.worldProvider.getDimension()));
                    mojangstatisticsgenerator.a("world[" + i + "][mode]", worlddata.getGameType());
                    mojangstatisticsgenerator.a("world[" + i + "][difficulty]", worldserver.getDifficulty());
                    mojangstatisticsgenerator.a("world[" + i + "][hardcore]", Boolean.valueOf(worlddata.isHardcore()));
                    mojangstatisticsgenerator.a("world[" + i + "][generator_name]", worlddata.getType().name());
                    mojangstatisticsgenerator.a("world[" + i + "][generator_version]", Integer.valueOf(worlddata.getType().getVersion()));
                    mojangstatisticsgenerator.a("world[" + i + "][height]", Integer.valueOf(this.F));
                    mojangstatisticsgenerator.a("world[" + i + "][chunks_loaded]", Integer.valueOf(worldserver.N().getLoadedChunks()));
                    ++i;
                }
            }
        }

        mojangstatisticsgenerator.a("worlds", Integer.valueOf(i));
    }

    public void b(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.b("singleplayer", Boolean.valueOf(this.T()));
        mojangstatisticsgenerator.b("server_brand", this.getServerModName());
        mojangstatisticsgenerator.b("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        mojangstatisticsgenerator.b("dedicated", Boolean.valueOf(this.ae()));
    }

    public boolean getSnooperEnabled() {
        return true;
    }

    public abstract boolean ae();

    public boolean getOnlineMode() {
        return this.server.getOnlineMode();
    }

    public void setOnlineMode(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean getSpawnAnimals() {
        return this.spawnAnimals;
    }

    public void setSpawnAnimals(boolean flag) {
        this.spawnAnimals = flag;
    }

    public boolean getSpawnNPCs() {
        return this.spawnNPCs;
    }

    public abstract boolean ai();

    public void setSpawnNPCs(boolean flag) {
        this.spawnNPCs = flag;
    }

    public boolean getPVP() {
        return this.pvpMode;
    }

    public void setPVP(boolean flag) {
        this.pvpMode = flag;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public int getMaxBuildHeight() {
        return this.F;
    }

    public void c(int i) {
        this.F = i;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public PlayerList getPlayerList() {
        return this.v;
    }

    public void a(PlayerList playerlist) {
        this.v = playerlist;
    }

    public void setGamemode(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        for (int i = 0; i < this.worlds.size(); ++i) {
            ((WorldServer) getServer().worlds.get(i)).getWorldData().setGameType(worldsettings_enumgamemode);
        }

    }

    public ServerConnection getServerConnection() {
        return this.q;
    }

    public ServerConnection aq() {
        return this.q == null ? (this.q = new ServerConnection(this)) : this.q;
    }

    public boolean as() {
        return false;
    }

    public abstract String a(WorldSettings.EnumGamemode worldsettings_enumgamemode, boolean flag);

    public int at() {
        return this.ticks;
    }

    public void au() {
        this.T = true;
    }

    public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

    public Vec3D d() {
        return new Vec3D(0.0D, 0.0D, 0.0D);
    }

    public World getWorld() {
        return (World) this.worlds.get(0);
    }

    public Entity f() {
        return null;
    }

    public int getSpawnProtection() {
        return 16;
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return false;
    }

    public void setForceGamemode(boolean flag) {
        this.U = flag;
    }

    public boolean getForceGamemode() {
        return this.U;
    }

    public Proxy ay() {
        return this.e;
    }

    public static long az() {
        return System.currentTimeMillis();
    }

    public int getIdleTimeout() {
        return this.G;
    }

    public void setIdleTimeout(int i) {
        this.G = i;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

    public boolean aB() {
        return true;
    }

    public MinecraftSessionService aD() {
        return this.W;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.Y;
    }

    public UserCache getUserCache() {
        return this.Z;
    }

    public ServerPing aG() {
        return this.r;
    }

    public void aH() {
        this.X = 0L;
    }

    public Entity a(UUID uuid) {
        WorldServer[] aworldserver = this.worldServer;
        int i = aworldserver.length;

        for (int j = 0; j < this.worlds.size(); ++j) {
            WorldServer worldserver = (WorldServer) this.worlds.get(j);

            if (worldserver != null) {
                Entity entity = worldserver.getEntity(uuid);

                if (entity != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    public boolean getSendCommandFeedback() {
        return ((WorldServer) getServer().worlds.get(0)).getGameRules().getBoolean("sendCommandFeedback");
    }

    public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {}

    public int aI() {
        return 29999984;
    }

    public <V> ListenableFuture<V> a(Callable<V> callable) {
        Validate.notNull(callable);
        if (!this.isMainThread()) {
            ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(callable);

            this.j.add(listenablefuturetask);
            return listenablefuturetask;
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        Validate.notNull(runnable);
        return this.a(Executors.callable(runnable));
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.serverThread;
    }

    public int aK() {
        return 256;
    }

    public long aL() {
        return this.ab;
    }

    public Thread aM() {
        return this.serverThread;
    }
}
