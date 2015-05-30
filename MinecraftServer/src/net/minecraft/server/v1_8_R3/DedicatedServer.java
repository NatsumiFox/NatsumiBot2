package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_8_R3.LoggerOutputStream;
import org.bukkit.craftbukkit.v1_8_R3.SpigotTimings;
import org.bukkit.craftbukkit.v1_8_R3.command.CraftRemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.util.ForwardLogHandler;
import org.bukkit.craftbukkit.v1_8_R3.util.TerminalConsoleWriterThread;
import org.bukkit.craftbukkit.v1_8_R3.util.Waitable;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.spigotmc.SpigotConfig;

public class DedicatedServer extends MinecraftServer implements IMinecraftServer {

    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Object> l = Collections.synchronizedList(Lists.newArrayList());
    private RemoteStatusListener m;
    private RemoteControlListener n;
    public PropertyManager propertyManager;
    private EULA p;
    private boolean generateStructures;
    private WorldSettings.EnumGamemode r;
    private boolean s;

    public DedicatedServer(OptionSet optionset) {
        super(optionset, Proxy.NO_PROXY, DedicatedServer.a);
        Thread thread = new Thread("Server Infinisleeper") {
            {
                this.setDaemon(true);
                this.start();
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException interruptedexception) {
                        ;
                    }
                }
            }
        };
    }

    protected boolean init() throws IOException {
        Thread thread = new Thread("Server console handler") {
            public void run() {
                if (Main.useConsole) {
                    ConsoleReader consolereader = DedicatedServer.this.reader;

                    try {
                        while (!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning()) {
                            String s;

                            if (Main.useJline) {
                                s = consolereader.readLine(">", (Character) null);
                            } else {
                                s = consolereader.readLine();
                            }

                            if (s != null && s.trim().length() > 0) {
                                DedicatedServer.this.issueCommand(s, DedicatedServer.this);
                            }
                        }
                    } catch (IOException ioexception) {
                        DedicatedServer.LOGGER.error("Exception handling console input", ioexception);
                    }

                }
            }
        };
        java.util.logging.Logger java_util_logging_logger = java.util.logging.Logger.getLogger("");

        java_util_logging_logger.setUseParentHandlers(false);
        Handler[] ahandler;
        int i = (ahandler = java_util_logging_logger.getHandlers()).length;

        for (int j = 0; j < i; ++j) {
            Handler handler = ahandler[j];

            java_util_logging_logger.removeHandler(handler);
        }

        java_util_logging_logger.addHandler(new ForwardLogHandler());
        org.apache.logging.log4j.core.Logger org_apache_logging_log4j_core_logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        Iterator iterator = org_apache_logging_log4j_core_logger.getAppenders().values().iterator();

        while (iterator.hasNext()) {
            Appender appender = (Appender) iterator.next();

            if (appender instanceof ConsoleAppender) {
                org_apache_logging_log4j_core_logger.removeAppender(appender);
            }
        }

        (new Thread(new TerminalConsoleWriterThread(System.out, this.reader))).start();
        System.setOut(new PrintStream(new LoggerOutputStream(org_apache_logging_log4j_core_logger, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(org_apache_logging_log4j_core_logger, Level.WARN), true));
        thread.setDaemon(true);
        thread.start();
        DedicatedServer.LOGGER.info("Starting minecraft server version 1.8.6");
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            DedicatedServer.LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        DedicatedServer.LOGGER.info("Loading properties");
        this.propertyManager = new PropertyManager(this.options);
        this.p = new EULA(new File("eula.txt"));
        boolean flag = Boolean.getBoolean("com.mojang.eula.agree");

        if (flag) {
            System.err.println("You have used the Spigot command line EULA agreement flag.");
            System.err.println("By using this setting you are indicating your agreement to Mojang\'s EULA (https://account.mojang.com/documents/minecraft_eula).");
            System.err.println("If you do not agree to the above EULA please stop your server and remove this flag immediately.");
        }

        if (!this.p.a() && !flag) {
            DedicatedServer.LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            this.p.b();
            return false;
        } else {
            if (this.T()) {
                this.c("127.0.0.1");
            } else {
                this.setOnlineMode(this.propertyManager.getBoolean("online-mode", true));
                this.c(this.propertyManager.getString("server-ip", ""));
            }

            this.setSpawnAnimals(this.propertyManager.getBoolean("spawn-animals", true));
            this.setSpawnNPCs(this.propertyManager.getBoolean("spawn-npcs", true));
            this.setPVP(this.propertyManager.getBoolean("pvp", true));
            this.setAllowFlight(this.propertyManager.getBoolean("allow-flight", false));
            this.setResourcePack(this.propertyManager.getString("resource-pack", ""), this.propertyManager.getString("resource-pack-hash", ""));
            this.setMotd(this.propertyManager.getString("motd", "A Minecraft Server"));
            this.setForceGamemode(this.propertyManager.getBoolean("force-gamemode", false));
            this.setIdleTimeout(this.propertyManager.getInt("player-idle-timeout", 0));
            if (this.propertyManager.getInt("difficulty", 1) < 0) {
                this.propertyManager.setProperty("difficulty", Integer.valueOf(0));
            } else if (this.propertyManager.getInt("difficulty", 1) > 3) {
                this.propertyManager.setProperty("difficulty", Integer.valueOf(3));
            }

            this.generateStructures = this.propertyManager.getBoolean("generate-structures", true);
            i = this.propertyManager.getInt("gamemode", WorldSettings.EnumGamemode.SURVIVAL.getId());
            this.r = WorldSettings.a(i);
            DedicatedServer.LOGGER.info("Default game type: " + this.r);
            InetAddress inetaddress = null;

            if (this.getServerIp().length() > 0) {
                inetaddress = InetAddress.getByName(this.getServerIp());
            }

            if (this.R() < 0) {
                this.setPort(this.propertyManager.getInt("server-port", 25565));
            }

            this.a((PlayerList) (new DedicatedPlayerList(this)));
            SpigotConfig.init((File) this.options.valueOf("spigot-settings"));
            SpigotConfig.registerCommands();
            DedicatedServer.LOGGER.info("Generating keypair");
            this.a(MinecraftEncryption.b());
            DedicatedServer.LOGGER.info("Starting Minecraft server on " + (this.getServerIp().length() == 0 ? "*" : this.getServerIp()) + ":" + this.R());
            if (!SpigotConfig.lateBind) {
                try {
                    this.aq().a(inetaddress, this.R());
                } catch (IOException ioexception) {
                    DedicatedServer.LOGGER.warn("**** FAILED TO BIND TO PORT!");
                    DedicatedServer.LOGGER.warn("The exception was: {}", new Object[] { ioexception.toString()});
                    DedicatedServer.LOGGER.warn("Perhaps a server is already running on that port?");
                    return false;
                }
            }

            this.server.loadPlugins();
            this.server.enablePlugins(PluginLoadOrder.STARTUP);
            if (!this.getOnlineMode()) {
                DedicatedServer.LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
                DedicatedServer.LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
                DedicatedServer.LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
                DedicatedServer.LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
            }

            if (this.aR()) {
                this.getUserCache().c();
            }

            if (!NameReferencingFileConverter.a(this.propertyManager)) {
                return false;
            } else {
                this.convertable = new WorldLoaderServer(this.server.getWorldContainer());
                long k = System.nanoTime();

                if (this.U() == null) {
                    this.setWorld(this.propertyManager.getString("level-name", "world"));
                }

                String s = this.propertyManager.getString("level-seed", "");
                String s1 = this.propertyManager.getString("level-type", "DEFAULT");
                String s2 = this.propertyManager.getString("generator-settings", "");
                long l = (new Random()).nextLong();

                if (s.length() > 0) {
                    try {
                        long i1 = Long.parseLong(s);

                        if (i1 != 0L) {
                            l = i1;
                        }
                    } catch (NumberFormatException numberformatexception) {
                        l = (long) s.hashCode();
                    }
                }

                WorldType worldtype = WorldType.getType(s1);

                if (worldtype == null) {
                    worldtype = WorldType.NORMAL;
                }

                this.aB();
                this.getEnableCommandBlock();
                this.p();
                this.getSnooperEnabled();
                this.aK();
                this.c(this.propertyManager.getInt("max-build-height", 256));
                this.c((this.getMaxBuildHeight() + 8) / 16 * 16);
                this.c(MathHelper.clamp(this.getMaxBuildHeight(), 64, 256));
                this.propertyManager.setProperty("max-build-height", Integer.valueOf(this.getMaxBuildHeight()));
                DedicatedServer.LOGGER.info("Preparing level \"" + this.U() + "\"");
                this.a(this.U(), this.U(), l, worldtype, s2);
                long j1 = System.nanoTime() - k;
                String s3 = String.format("%.3fs", new Object[] { Double.valueOf((double) j1 / 1.0E9D)});

                DedicatedServer.LOGGER.info("Done (" + s3 + ")! For help, type \"help\" or \"?\"");
                if (this.propertyManager.getBoolean("enable-query", false)) {
                    DedicatedServer.LOGGER.info("Starting GS4 status listener");
                    this.m = new RemoteStatusListener(this);
                    this.m.a();
                }

                if (this.propertyManager.getBoolean("enable-rcon", false)) {
                    DedicatedServer.LOGGER.info("Starting remote control listener");
                    this.n = new RemoteControlListener(this);
                    this.n.a();
                    this.remoteConsole = new CraftRemoteConsoleCommandSender();
                }

                if (this.server.getBukkitSpawnRadius() > -1) {
                    DedicatedServer.LOGGER.info("\'settings.spawn-radius\' in bukkit.yml has been moved to \'spawn-protection\' in server.properties. I will move your config for you.");
                    this.propertyManager.properties.remove("spawn-protection");
                    this.propertyManager.getInt("spawn-protection", this.server.getBukkitSpawnRadius());
                    this.server.removeBukkitSpawnRadius();
                    this.propertyManager.savePropertiesFile();
                }

                if (SpigotConfig.lateBind) {
                    try {
                        this.aq().a(inetaddress, this.R());
                    } catch (IOException ioexception1) {
                        DedicatedServer.LOGGER.warn("**** FAILED TO BIND TO PORT!");
                        DedicatedServer.LOGGER.warn("The exception was: {}", new Object[] { ioexception1.toString()});
                        DedicatedServer.LOGGER.warn("Perhaps a server is already running on that port?");
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public PropertyManager getPropertyManager() {
        return this.propertyManager;
    }

    public void setGamemode(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        super.setGamemode(worldsettings_enumgamemode);
        this.r = worldsettings_enumgamemode;
    }

    public boolean getGenerateStructures() {
        return this.generateStructures;
    }

    public WorldSettings.EnumGamemode getGamemode() {
        return this.r;
    }

    public EnumDifficulty getDifficulty() {
        return EnumDifficulty.getById(this.propertyManager.getInt("difficulty", 1));
    }

    public boolean isHardcore() {
        return this.propertyManager.getBoolean("hardcore", false);
    }

    protected void a(CrashReport crashreport) {}

    public CrashReport b(CrashReport crashreport) {
        crashreport = super.b(crashreport);
        crashreport.g().a("Is Modded", new Callable() {
            public String a() throws Exception {
                String s = DedicatedServer.this.getServerModName();

                return !s.equals("vanilla") ? "Definitely; Server brand changed to \'" + s + "\'" : "Unknown (can\'t tell)";
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreport.g().a("Type", new Callable() {
            public String a() throws Exception {
                return "Dedicated Server (map_server.txt)";
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        return crashreport;
    }

    protected void z() {
        System.exit(0);
    }

    public void B() {
        super.B();
        this.aO();
    }

    public boolean getAllowNether() {
        return this.propertyManager.getBoolean("allow-nether", true);
    }

    public boolean getSpawnMonsters() {
        return this.propertyManager.getBoolean("spawn-monsters", true);
    }

    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", Boolean.valueOf(this.aP().getHasWhitelist()));
        mojangstatisticsgenerator.a("whitelist_count", Integer.valueOf(this.aP().getWhitelisted().length));
        super.a(mojangstatisticsgenerator);
    }

    public boolean getSnooperEnabled() {
        return this.propertyManager.getBoolean("snooper-enabled", true);
    }

    public void issueCommand(String s, ICommandListener icommandlistener) {
        this.l.add(new ServerCommand(s, icommandlistener));
    }

    public void aO() {
        SpigotTimings.serverCommandTimer.startTiming();

        while (!this.l.isEmpty()) {
            ServerCommand servercommand = (ServerCommand) this.l.remove(0);
            ServerCommandEvent servercommandevent = new ServerCommandEvent(this.console, servercommand.command);

            this.server.getPluginManager().callEvent(servercommandevent);
            servercommand = new ServerCommand(servercommandevent.getCommand(), servercommand.source);
            this.server.dispatchServerCommand(this.console, servercommand);
        }

        SpigotTimings.serverCommandTimer.stopTiming();
    }

    public boolean ae() {
        return true;
    }

    public boolean ai() {
        return this.propertyManager.getBoolean("use-native-transport", true);
    }

    public DedicatedPlayerList aP() {
        return (DedicatedPlayerList) super.getPlayerList();
    }

    public int a(String s, int i) {
        return this.propertyManager.getInt(s, i);
    }

    public String a(String s, String s1) {
        return this.propertyManager.getString(s, s1);
    }

    public boolean a(String s, boolean flag) {
        return this.propertyManager.getBoolean(s, flag);
    }

    public void a(String s, Object object) {
        this.propertyManager.setProperty(s, object);
    }

    public void a() {
        this.propertyManager.savePropertiesFile();
    }

    public String b() {
        File file = this.propertyManager.c();

        return file != null ? file.getAbsolutePath() : "No settings file";
    }

    public void aQ() {
        ServerGUI.a(this);
        this.s = true;
    }

    public boolean as() {
        return this.s;
    }

    public String a(WorldSettings.EnumGamemode worldsettings_enumgamemode, boolean flag) {
        return "";
    }

    public boolean getEnableCommandBlock() {
        return this.propertyManager.getBoolean("enable-command-block", false);
    }

    public int getSpawnProtection() {
        return this.propertyManager.getInt("spawn-protection", super.getSpawnProtection());
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        if (world.worldProvider.getDimension() != 0) {
            return false;
        } else if (this.aP().getOPs().isEmpty()) {
            return false;
        } else if (this.aP().isOp(entityhuman.getProfile())) {
            return false;
        } else if (this.getSpawnProtection() <= 0) {
            return false;
        } else {
            BlockPosition blockposition1 = world.getSpawn();
            int i = MathHelper.a(blockposition.getX() - blockposition1.getX());
            int j = MathHelper.a(blockposition.getZ() - blockposition1.getZ());
            int k = Math.max(i, j);

            return k <= this.getSpawnProtection();
        }
    }

    public int p() {
        return this.propertyManager.getInt("op-permission-level", 4);
    }

    public void setIdleTimeout(int i) {
        super.setIdleTimeout(i);
        this.propertyManager.setProperty("player-idle-timeout", Integer.valueOf(i));
        this.a();
    }

    public boolean q() {
        return this.propertyManager.getBoolean("broadcast-rcon-to-ops", true);
    }

    public boolean r() {
        return this.propertyManager.getBoolean("broadcast-console-to-ops", true);
    }

    public boolean aB() {
        return this.propertyManager.getBoolean("announce-player-achievements", true);
    }

    public int aI() {
        int i = this.propertyManager.getInt("max-world-size", super.aI());

        if (i < 1) {
            i = 1;
        } else if (i > super.aI()) {
            i = super.aI();
        }

        return i;
    }

    public int aK() {
        return this.propertyManager.getInt("network-compression-threshold", super.aK());
    }

    protected boolean aR() {
        this.server.getLogger().info("**** Beginning UUID conversion, this may take A LONG time ****");
        boolean flag = false;

        int i;

        for (i = 0; !flag && i <= 2; ++i) {
            if (i > 0) {
                DedicatedServer.LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
                this.aU();
            }

            flag = NameReferencingFileConverter.a((MinecraftServer) this);
        }

        boolean flag1 = false;

        for (i = 0; !flag1 && i <= 2; ++i) {
            if (i > 0) {
                DedicatedServer.LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
                this.aU();
            }

            flag1 = NameReferencingFileConverter.b((MinecraftServer) this);
        }

        boolean flag2 = false;

        for (i = 0; !flag2 && i <= 2; ++i) {
            if (i > 0) {
                DedicatedServer.LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
                this.aU();
            }

            flag2 = NameReferencingFileConverter.c((MinecraftServer) this);
        }

        boolean flag3 = false;

        for (i = 0; !flag3 && i <= 2; ++i) {
            if (i > 0) {
                DedicatedServer.LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
                this.aU();
            }

            flag3 = NameReferencingFileConverter.d((MinecraftServer) this);
        }

        boolean flag4 = false;

        for (i = 0; !flag4 && i <= 2; ++i) {
            if (i > 0) {
                DedicatedServer.LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
                this.aU();
            }

            flag4 = NameReferencingFileConverter.a(this, this.propertyManager);
        }

        return flag || flag1 || flag2 || flag3 || flag4;
    }

    private void aU() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException interruptedexception) {
            ;
        }

    }

    public long aS() {
        return this.propertyManager.getLong("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
    }

    public String getPlugins() {
        StringBuilder stringbuilder = new StringBuilder();
        Plugin[] aplugin = this.server.getPluginManager().getPlugins();

        stringbuilder.append(this.server.getName());
        stringbuilder.append(" on Bukkit ");
        stringbuilder.append(this.server.getBukkitVersion());
        if (aplugin.length > 0 && this.server.getQueryPlugins()) {
            stringbuilder.append(": ");

            for (int i = 0; i < aplugin.length; ++i) {
                if (i > 0) {
                    stringbuilder.append("; ");
                }

                stringbuilder.append(aplugin[i].getDescription().getName());
                stringbuilder.append(" ");
                stringbuilder.append(aplugin[i].getDescription().getVersion().replaceAll(";", ","));
            }
        }

        return stringbuilder.toString();
    }

    public String executeRemoteCommand(final String s) {
        Waitable waitable = new Waitable() {
            protected String evaluate() {
                RemoteControlCommandListener.getInstance().i();
                RemoteServerCommandEvent remoteservercommandevent = new RemoteServerCommandEvent(DedicatedServer.this.remoteConsole, s);

                DedicatedServer.this.server.getPluginManager().callEvent(remoteservercommandevent);
                ServerCommand servercommand = new ServerCommand(remoteservercommandevent.getCommand(), RemoteControlCommandListener.getInstance());

                DedicatedServer.this.server.dispatchServerCommand(DedicatedServer.this.remoteConsole, servercommand);
                return RemoteControlCommandListener.getInstance().j();
            }
        };

        this.processQueue.add(waitable);

        try {
            return (String) waitable.get();
        } catch (ExecutionException executionexception) {
            throw new RuntimeException("Exception processing rcon command " + s, executionexception.getCause());
        } catch (InterruptedException interruptedexception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted processing rcon command " + s, interruptedexception);
        }
    }

    public PlayerList getPlayerList() {
        return this.aP();
    }
}
