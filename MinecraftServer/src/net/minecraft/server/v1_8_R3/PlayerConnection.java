package net.minecraft.server.v1_8_R3;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.SpigotTimings;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_8_R3.util.LazyPlayerSet;
import org.bukkit.craftbukkit.v1_8_R3.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.spigotmc.SpigotConfig;

public class PlayerConnection implements PacketListenerPlayIn, IUpdatePlayerListBox {

    private static final Logger c = LogManager.getLogger();
    public final NetworkManager networkManager;
    private final MinecraftServer minecraftServer;
    public EntityPlayer player;
    private int e;
    private int f;
    private int g;
    private boolean h;
    private int i;
    private long j;
    private long k;
    private volatile int chatThrottle;
    private static final AtomicIntegerFieldUpdater chatSpamField = AtomicIntegerFieldUpdater.newUpdater(PlayerConnection.class, "chatThrottle");
    private int m;
    private IntHashMap<Short> n = new IntHashMap();
    private double o;
    private double p;
    private double q;
    private boolean checkMovement = true;
    private boolean processedDisconnect;
    private final CraftServer server;
    private int lastTick;
    private int lastDropTick;
    private int dropCount;
    private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 36;
    private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 49;
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private float lastPitch;
    private float lastYaw;
    private boolean justTeleported;
    private boolean hasMoved;
    private static final HashSet<Integer> invalidItems = new HashSet(Arrays.asList(new Integer[] { Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(26), Integer.valueOf(34), Integer.valueOf(36), Integer.valueOf(43), Integer.valueOf(51), Integer.valueOf(52), Integer.valueOf(55), Integer.valueOf(59), Integer.valueOf(60), Integer.valueOf(62), Integer.valueOf(63), Integer.valueOf(64), Integer.valueOf(68), Integer.valueOf(71), Integer.valueOf(74), Integer.valueOf(75), Integer.valueOf(83), Integer.valueOf(90), Integer.valueOf(92), Integer.valueOf(93), Integer.valueOf(94), Integer.valueOf(104), Integer.valueOf(105), Integer.valueOf(115), Integer.valueOf(117), Integer.valueOf(118), Integer.valueOf(119), Integer.valueOf(125), Integer.valueOf(127), Integer.valueOf(132), Integer.valueOf(140), Integer.valueOf(141), Integer.valueOf(142), Integer.valueOf(144)}));
    private long lastPlace;
    private int packets;
    private static int[] $SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction;
    private static int[] $SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction;
    private static int[] $SWITCH_TABLE$org$bukkit$event$Event$Result;

    public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        this.lastTick = MinecraftServer.currentTick;
        this.lastDropTick = MinecraftServer.currentTick;
        this.dropCount = 0;
        this.lastPosX = Double.MAX_VALUE;
        this.lastPosY = Double.MAX_VALUE;
        this.lastPosZ = Double.MAX_VALUE;
        this.lastPitch = Float.MAX_VALUE;
        this.lastYaw = Float.MAX_VALUE;
        this.justTeleported = false;
        this.lastPlace = -1L;
        this.packets = 0;
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
        networkmanager.a((PacketListener) this);
        this.player = entityplayer;
        entityplayer.playerConnection = this;
        this.server = minecraftserver.server;
    }

    public CraftPlayer getPlayer() {
        return this.player == null ? null : this.player.getBukkitEntity();
    }

    public void c() {
        this.h = false;
        ++this.e;
        this.minecraftServer.methodProfiler.a("keepAlive");
        if ((long) this.e - this.k > 40L) {
            this.k = (long) this.e;
            this.j = this.d();
            this.i = (int) this.j;
            this.sendPacket(new PacketPlayOutKeepAlive(this.i));
        }

        this.minecraftServer.methodProfiler.b();

        int i;

        do {
            i = this.chatThrottle;
        } while (this.chatThrottle > 0 && !PlayerConnection.chatSpamField.compareAndSet(this, i, i - 1));

        if (this.m > 0) {
            --this.m;
        }

        if (this.player.D() > 0L && this.minecraftServer.getIdleTimeout() > 0 && MinecraftServer.az() - this.player.D() > (long) (this.minecraftServer.getIdleTimeout() * 1000 * 60)) {
            this.player.resetIdleTimer();
            this.disconnect("You have been idle for too long!");
        }

    }

    public NetworkManager a() {
        return this.networkManager;
    }

    public void disconnect(String s) {
        String s1 = EnumChatFormat.YELLOW + this.player.getName() + " left the game.";
        PlayerKickEvent playerkickevent = new PlayerKickEvent(this.server.getPlayer(this.player), s, s1);

        if (this.server.getServer().isRunning()) {
            this.server.getPluginManager().callEvent(playerkickevent);
        }

        if (!playerkickevent.isCancelled()) {
            s = playerkickevent.getReason();
            final ChatComponentText chatcomponenttext = new ChatComponentText(s);

            this.networkManager.a(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener() {
                public void operationComplete(Future future) throws Exception {
                    PlayerConnection.this.networkManager.close(chatcomponenttext);
                }
            }, new GenericFutureListener[0]);
            this.a((IChatBaseComponent) chatcomponenttext);
            this.networkManager.k();
            this.minecraftServer.postToMainThread(new Runnable() {
                public void run() {
                    PlayerConnection.this.networkManager.l();
                }
            });
        }
    }

    public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {
        PlayerConnectionUtils.ensureMainThread(packetplayinsteervehicle, this, this.player.u());
        this.player.a(packetplayinsteervehicle.a(), packetplayinsteervehicle.b(), packetplayinsteervehicle.c(), packetplayinsteervehicle.d());
    }

    private boolean b(PacketPlayInFlying packetplayinflying) {
        return !Doubles.isFinite(packetplayinflying.a()) || !Doubles.isFinite(packetplayinflying.b()) || !Doubles.isFinite(packetplayinflying.c()) || !Floats.isFinite(packetplayinflying.e()) || !Floats.isFinite(packetplayinflying.d());
    }

    public void a(PacketPlayInFlying packetplayinflying) {
        PlayerConnectionUtils.ensureMainThread(packetplayinflying, this, this.player.u());
        if (this.b(packetplayinflying)) {
            this.disconnect("Invalid move packet received");
        } else {
            WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

            this.h = true;
            if (!this.player.viewingCredits) {
                double d0 = this.player.locX;
                double d1 = this.player.locY;
                double d2 = this.player.locZ;
                double d3 = 0.0D;
                double d4 = packetplayinflying.a() - this.o;
                double d5 = packetplayinflying.b() - this.p;
                double d6 = packetplayinflying.c() - this.q;

                if (packetplayinflying.g()) {
                    d3 = d4 * d4 + d5 * d5 + d6 * d6;
                    if (!this.checkMovement && d3 < 0.25D) {
                        this.checkMovement = true;
                    }
                }

                CraftPlayer craftplayer = this.getPlayer();
                Location location;

                if (!this.hasMoved) {
                    location = craftplayer.getLocation();
                    this.lastPosX = location.getX();
                    this.lastPosY = location.getY();
                    this.lastPosZ = location.getZ();
                    this.lastYaw = location.getYaw();
                    this.lastPitch = location.getPitch();
                    this.hasMoved = true;
                }

                location = new Location(craftplayer.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                Location location1 = craftplayer.getLocation().clone();

                if (packetplayinflying.hasPos && (!packetplayinflying.hasPos || packetplayinflying.y != -999.0D)) {
                    location1.setX(packetplayinflying.x);
                    location1.setY(packetplayinflying.y);
                    location1.setZ(packetplayinflying.z);
                }

                if (packetplayinflying.hasLook) {
                    location1.setYaw(packetplayinflying.yaw);
                    location1.setPitch(packetplayinflying.pitch);
                }

                double d7 = Math.pow(this.lastPosX - location1.getX(), 2.0D) + Math.pow(this.lastPosY - location1.getY(), 2.0D) + Math.pow(this.lastPosZ - location1.getZ(), 2.0D);
                float f = Math.abs(this.lastYaw - location1.getYaw()) + Math.abs(this.lastPitch - location1.getPitch());

                if ((d7 > 0.00390625D || f > 10.0F) && this.checkMovement && !this.player.dead) {
                    this.lastPosX = location1.getX();
                    this.lastPosY = location1.getY();
                    this.lastPosZ = location1.getZ();
                    this.lastYaw = location1.getYaw();
                    this.lastPitch = location1.getPitch();
                    Location location2 = location1.clone();
                    PlayerMoveEvent playermoveevent = new PlayerMoveEvent(craftplayer, location, location1);

                    this.server.getPluginManager().callEvent(playermoveevent);
                    if (playermoveevent.isCancelled()) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), Collections.emptySet()));
                        return;
                    }

                    if (!location2.equals(playermoveevent.getTo()) && !playermoveevent.isCancelled()) {
                        this.player.getBukkitEntity().teleport(playermoveevent.getTo(), TeleportCause.UNKNOWN);
                        return;
                    }

                    if (!location.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                        this.justTeleported = false;
                        return;
                    }
                }

                if (this.checkMovement && !this.player.dead) {
                    this.f = this.e;
                    double d8;
                    double d9;
                    double d10;

                    if (this.player.vehicle != null) {
                        float f1 = this.player.yaw;
                        float f2 = this.player.pitch;

                        this.player.vehicle.al();
                        d8 = this.player.locX;
                        d9 = this.player.locY;
                        d10 = this.player.locZ;
                        if (packetplayinflying.h()) {
                            f1 = packetplayinflying.d();
                            f2 = packetplayinflying.e();
                        }

                        this.player.onGround = packetplayinflying.f();
                        this.player.l();
                        this.player.setLocation(d8, d9, d10, f1, f2);
                        if (this.player.vehicle != null) {
                            this.player.vehicle.al();
                        }

                        this.minecraftServer.getPlayerList().d(this.player);
                        if (this.player.vehicle != null) {
                            this.player.vehicle.ai = true;
                            if (d3 > 4.0D) {
                                Entity entity = this.player.vehicle;

                                this.player.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(entity));
                                this.a(this.player.locX, this.player.locY, this.player.locZ, this.player.yaw, this.player.pitch);
                            }
                        }

                        if (this.checkMovement) {
                            this.o = this.player.locX;
                            this.p = this.player.locY;
                            this.q = this.player.locZ;
                        }

                        worldserver.g(this.player);
                        return;
                    }

                    if (this.player.isSleeping()) {
                        this.player.l();
                        this.player.setLocation(this.o, this.p, this.q, this.player.yaw, this.player.pitch);
                        worldserver.g(this.player);
                        return;
                    }

                    double d11 = this.player.locY;

                    this.o = this.player.locX;
                    this.p = this.player.locY;
                    this.q = this.player.locZ;
                    d8 = this.player.locX;
                    d9 = this.player.locY;
                    d10 = this.player.locZ;
                    float f3 = this.player.yaw;
                    float f4 = this.player.pitch;

                    if (packetplayinflying.g() && packetplayinflying.b() == -999.0D) {
                        packetplayinflying.a(false);
                    }

                    if (packetplayinflying.g()) {
                        d8 = packetplayinflying.a();
                        d9 = packetplayinflying.b();
                        d10 = packetplayinflying.c();
                        if (Math.abs(packetplayinflying.a()) > 3.0E7D || Math.abs(packetplayinflying.c()) > 3.0E7D) {
                            this.disconnect("Illegal position");
                            return;
                        }
                    }

                    if (packetplayinflying.h()) {
                        f3 = packetplayinflying.d();
                        f4 = packetplayinflying.e();
                    }

                    this.player.l();
                    this.player.setLocation(this.o, this.p, this.q, f3, f4);
                    if (!this.checkMovement) {
                        return;
                    }

                    double d12 = d8 - this.player.locX;
                    double d13 = d9 - this.player.locY;
                    double d14 = d10 - this.player.locZ;
                    double d15 = this.player.motX * this.player.motX + this.player.motY * this.player.motY + this.player.motZ * this.player.motZ;
                    double d16 = d12 * d12 + d13 * d13 + d14 * d14;

                    if (d16 - d15 > SpigotConfig.movedTooQuicklyThreshold && this.checkMovement && (!this.minecraftServer.T() || !this.minecraftServer.S().equals(this.player.getName()))) {
                        PlayerConnection.c.warn(this.player.getName() + " moved too quickly! " + d12 + "," + d13 + "," + d14 + " (" + d12 + ", " + d13 + ", " + d14 + ")");
                        this.a(this.o, this.p, this.q, this.player.yaw, this.player.pitch);
                        return;
                    }

                    float f5 = 0.0625F;
                    boolean flag = worldserver.getCubes(this.player, this.player.getBoundingBox().shrink((double) f5, (double) f5, (double) f5)).isEmpty();

                    if (this.player.onGround && !packetplayinflying.f() && d13 > 0.0D) {
                        this.player.bF();
                    }

                    this.player.move(d12, d13, d14);
                    this.player.onGround = packetplayinflying.f();
                    double d17 = d13;

                    d12 = d8 - this.player.locX;
                    d13 = d9 - this.player.locY;
                    if (d13 > -0.5D || d13 < 0.5D) {
                        d13 = 0.0D;
                    }

                    d14 = d10 - this.player.locZ;
                    d16 = d12 * d12 + d13 * d13 + d14 * d14;
                    boolean flag1 = false;

                    if (d16 > SpigotConfig.movedWronglyThreshold && !this.player.isSleeping() && !this.player.playerInteractManager.isCreative()) {
                        flag1 = true;
                        PlayerConnection.c.warn(this.player.getName() + " moved wrongly!");
                    }

                    this.player.setLocation(d8, d9, d10, f3, f4);
                    this.player.checkMovement(this.player.locX - d0, this.player.locY - d1, this.player.locZ - d2);
                    if (!this.player.noclip) {
                        boolean flag2 = worldserver.getCubes(this.player, this.player.getBoundingBox().shrink((double) f5, (double) f5, (double) f5)).isEmpty();

                        if (flag && (flag1 || !flag2) && !this.player.isSleeping()) {
                            this.a(this.o, this.p, this.q, f3, f4);
                            return;
                        }
                    }

                    AxisAlignedBB axisalignedbb = this.player.getBoundingBox().grow((double) f5, (double) f5, (double) f5).a(0.0D, -0.55D, 0.0D);

                    if (!this.minecraftServer.getAllowFlight() && !this.player.abilities.canFly && !worldserver.c(axisalignedbb)) {
                        if (d17 >= -0.03125D) {
                            ++this.g;
                            if (this.g > 80) {
                                PlayerConnection.c.warn(this.player.getName() + " was kicked for floating too long!");
                                this.disconnect("Flying is not enabled on this server");
                                return;
                            }
                        }
                    } else {
                        this.g = 0;
                    }

                    this.player.onGround = packetplayinflying.f();
                    this.minecraftServer.getPlayerList().d(this.player);
                    this.player.a(this.player.locY - d11, packetplayinflying.f());
                } else if (this.e - this.f > 20) {
                    this.a(this.o, this.p, this.q, this.player.yaw, this.player.pitch);
                }
            }
        }

    }

    public void a(double d0, double d1, double d2, float f, float f1) {
        this.a(d0, d1, d2, f, f1, Collections.emptySet());
    }

    public void a(double d0, double d1, double d2, float f, float f1, Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> set) {
        CraftPlayer craftplayer = this.getPlayer();
        Location location = craftplayer.getLocation();
        Location location1 = new Location(this.getPlayer().getWorld(), d0, d1, d2, f, f1);
        PlayerTeleportEvent playerteleportevent = new PlayerTeleportEvent(craftplayer, location, location1, TeleportCause.UNKNOWN);

        this.server.getPluginManager().callEvent(playerteleportevent);
        location = playerteleportevent.getFrom();
        location1 = playerteleportevent.isCancelled() ? location : playerteleportevent.getTo();
        this.teleport(location1, set);
    }

    public void teleport(Location location) {
        this.teleport(location, Collections.emptySet());
    }

    public void teleport(Location location, Set set) {
        double d0 = location.getX();
        double d1 = location.getY();
        double d2 = location.getZ();
        float f = location.getYaw();
        float f1 = location.getPitch();

        if (Float.isNaN(f)) {
            f = 0.0F;
        }

        if (Float.isNaN(f1)) {
            f1 = 0.0F;
        }

        this.lastPosX = d0;
        this.lastPosY = d1;
        this.lastPosZ = d2;
        this.lastYaw = f;
        this.lastPitch = f1;
        this.justTeleported = true;
        this.checkMovement = false;
        this.o = d0;
        this.p = d1;
        this.q = d2;
        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X)) {
            this.o += this.player.locX;
        }

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y)) {
            this.p += this.player.locY;
        }

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Z)) {
            this.q += this.player.locZ;
        }

        float f2 = f;
        float f3 = f1;

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT)) {
            f2 = f + this.player.yaw;
        }

        if (set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT)) {
            f3 = f1 + this.player.pitch;
        }

        this.player.setLocation(this.o, this.p, this.q, f2, f3);
        this.player.playerConnection.sendPacket(new PacketPlayOutPosition(d0, d1, d2, f, f1, set));
    }

    public void a(PacketPlayInBlockDig packetplayinblockdig) {
        PlayerConnectionUtils.ensureMainThread(packetplayinblockdig, this, this.player.u());
        if (!this.player.dead) {
            WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
            BlockPosition blockposition = packetplayinblockdig.a();

            this.player.resetIdleTimer();
            switch (PlayerConnection.SyntheticClass_1.a[packetplayinblockdig.c().ordinal()]) {
            case 1:
                if (!this.player.isSpectator()) {
                    if (this.lastDropTick != MinecraftServer.currentTick) {
                        this.dropCount = 0;
                        this.lastDropTick = MinecraftServer.currentTick;
                    } else {
                        ++this.dropCount;
                        if (this.dropCount >= 20) {
                            PlayerConnection.c.warn(this.player.getName() + " dropped their items too quickly!");
                            this.disconnect("You dropped your items too quickly (Hacking?)");
                            return;
                        }
                    }

                    this.player.a(false);
                }

                return;

            case 2:
                if (!this.player.isSpectator()) {
                    this.player.a(true);
                }

                return;

            case 3:
                this.player.bU();
                return;

            case 4:
            case 5:
            case 6:
                double d0 = this.player.locX - ((double) blockposition.getX() + 0.5D);
                double d1 = this.player.locY - ((double) blockposition.getY() + 0.5D) + 1.5D;
                double d2 = this.player.locZ - ((double) blockposition.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > 36.0D) {
                    return;
                } else if (blockposition.getY() >= this.minecraftServer.getMaxBuildHeight()) {
                    return;
                } else {
                    if (packetplayinblockdig.c() == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                        if (!this.minecraftServer.a(worldserver, blockposition, this.player) && worldserver.getWorldBorder().a(blockposition)) {
                            this.player.playerInteractManager.a(blockposition, packetplayinblockdig.b());
                        } else {
                            CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, packetplayinblockdig.b(), this.player.inventory.getItemInHand());
                            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
                            TileEntity tileentity = worldserver.getTileEntity(blockposition);

                            if (tileentity != null) {
                                this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
                            }
                        }
                    } else {
                        if (packetplayinblockdig.c() == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                            this.player.playerInteractManager.a(blockposition);
                        } else if (packetplayinblockdig.c() == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                            this.player.playerInteractManager.e();
                        }

                        if (worldserver.getType(blockposition).getBlock().getMaterial() != Material.AIR) {
                            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
            }
        }
    }

    public void a(PacketPlayInBlockPlace packetplayinblockplace) {
        PlayerConnectionUtils.ensureMainThread(packetplayinblockplace, this, this.player.u());
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        boolean flag = false;

        if (this.lastPlace != -1L && packetplayinblockplace.timestamp - this.lastPlace < 30L && this.packets++ >= 4) {
            flag = true;
        } else if (packetplayinblockplace.timestamp - this.lastPlace >= 30L || this.lastPlace == -1L) {
            this.lastPlace = packetplayinblockplace.timestamp;
            this.packets = 0;
        }

        if (!this.player.dead) {
            boolean flag1 = false;
            ItemStack itemstack = this.player.inventory.getItemInHand();
            boolean flag2 = false;
            BlockPosition blockposition = packetplayinblockplace.a();
            EnumDirection enumdirection = EnumDirection.fromType1(packetplayinblockplace.getFace());

            this.player.resetIdleTimer();
            if (packetplayinblockplace.getFace() == 255) {
                if (itemstack == null) {
                    return;
                }

                int i = itemstack.count;

                if (!flag) {
                    float f = this.player.pitch;
                    float f1 = this.player.yaw;
                    double d0 = this.player.locX;
                    double d1 = this.player.locY + (double) this.player.getHeadHeight();
                    double d2 = this.player.locZ;
                    Vec3D vec3d = new Vec3D(d0, d1, d2);
                    float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
                    float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
                    float f4 = -MathHelper.cos(-f * 0.017453292F);
                    float f5 = MathHelper.sin(-f * 0.017453292F);
                    float f6 = f3 * f4;
                    float f7 = f2 * f4;
                    double d3 = this.player.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.CREATIVE ? 5.0D : 4.5D;
                    Vec3D vec3d1 = vec3d.add((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
                    MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, false);
                    boolean flag3 = false;
                    PlayerInteractEvent playerinteractevent;

                    if (movingobjectposition != null && movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                        if (this.player.playerInteractManager.firedInteract) {
                            this.player.playerInteractManager.firedInteract = false;
                            flag3 = this.player.playerInteractManager.interactResult;
                        } else {
                            playerinteractevent = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_BLOCK, movingobjectposition.a(), movingobjectposition.direction, itemstack, true);
                            flag3 = playerinteractevent.useItemInHand() == Result.DENY;
                        }
                    } else {
                        playerinteractevent = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack);
                        flag3 = playerinteractevent.useItemInHand() == Result.DENY;
                    }

                    if (!flag3) {
                        this.player.playerInteractManager.useItem(this.player, this.player.world, itemstack);
                    }
                }

                flag1 = itemstack.count != i || itemstack.getItem() == Item.getItemOf(Blocks.WATERLILY);
            } else if (blockposition.getY() >= this.minecraftServer.getMaxBuildHeight() - 1 && (enumdirection == EnumDirection.UP || blockposition.getY() >= this.minecraftServer.getMaxBuildHeight())) {
                ChatMessage chatmessage = new ChatMessage("build.tooHigh", new Object[] { Integer.valueOf(this.minecraftServer.getMaxBuildHeight())});

                chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                this.player.playerConnection.sendPacket(new PacketPlayOutChat(chatmessage));
                flag2 = true;
            } else {
                Location location = this.getPlayer().getEyeLocation();
                double d4 = NumberConversions.square(location.getX() - (double) blockposition.getX()) + NumberConversions.square(location.getY() - (double) blockposition.getY()) + NumberConversions.square(location.getZ() - (double) blockposition.getZ());

                if (d4 > (double) (this.getPlayer().getGameMode() == GameMode.CREATIVE ? 49 : 36)) {
                    return;
                }

                if (!worldserver.getWorldBorder().a(blockposition)) {
                    return;
                }

                if (this.checkMovement && this.player.e((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D) < 64.0D && !this.minecraftServer.a(worldserver, blockposition, this.player) && worldserver.getWorldBorder().a(blockposition)) {
                    flag1 = flag || !this.player.playerInteractManager.interact(this.player, worldserver, itemstack, blockposition, enumdirection, packetplayinblockplace.d(), packetplayinblockplace.e(), packetplayinblockplace.f());
                }

                flag2 = true;
            }

            if (flag2) {
                this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
                this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition.shift(enumdirection)));
            }

            itemstack = this.player.inventory.getItemInHand();
            if (itemstack != null && itemstack.count == 0) {
                this.player.inventory.items[this.player.inventory.itemInHandIndex] = null;
                itemstack = null;
            }

            if (itemstack == null || itemstack.l() == 0) {
                this.player.g = true;
                this.player.inventory.items[this.player.inventory.itemInHandIndex] = ItemStack.b(this.player.inventory.items[this.player.inventory.itemInHandIndex]);
                Slot slot = this.player.activeContainer.getSlot(this.player.inventory, this.player.inventory.itemInHandIndex);

                this.player.activeContainer.b();
                this.player.g = false;
                if (!ItemStack.matches(this.player.inventory.getItemInHand(), packetplayinblockplace.getItemStack()) || flag1) {
                    this.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, slot.rawSlotIndex, this.player.inventory.getItemInHand()));
                }
            }

        }
    }

    public void a(PacketPlayInSpectate packetplayinspectate) {
        PlayerConnectionUtils.ensureMainThread(packetplayinspectate, this, this.player.u());
        if (this.player.isSpectator()) {
            Entity entity = null;
            WorldServer[] aworldserver = this.minecraftServer.worldServer;
            int i = aworldserver.length;
            Iterator iterator = this.minecraftServer.worlds.iterator();

            while (iterator.hasNext()) {
                WorldServer worldserver = (WorldServer) iterator.next();

                if (worldserver != null) {
                    entity = packetplayinspectate.a(worldserver);
                    if (entity != null) {
                        break;
                    }
                }
            }

            if (entity != null) {
                this.player.setSpectatorTarget(this.player);
                this.player.mount((Entity) null);
                this.player.getBukkitEntity().teleport(entity.getBukkitEntity(), TeleportCause.SPECTATE);
            }
        }

    }

    public void a(PacketPlayInResourcePackStatus packetplayinresourcepackstatus) {}

    public void a(IChatBaseComponent ichatbasecomponent) {
        if (!this.processedDisconnect) {
            this.processedDisconnect = true;
            PlayerConnection.c.info(this.player.getName() + " lost connection: " + ichatbasecomponent.c());
            this.player.q();
            String s = this.minecraftServer.getPlayerList().disconnect(this.player);

            if (s != null && s.length() > 0) {
                this.minecraftServer.getPlayerList().sendMessage(CraftChatMessage.fromString(s));
            }

            if (this.minecraftServer.T() && this.player.getName().equals(this.minecraftServer.S())) {
                PlayerConnection.c.info("Stopping singleplayer server as player logged out");
                this.minecraftServer.safeShutdown();
            }

        }
    }

    public void sendPacket(final Packet packet) {
        if (packet instanceof PacketPlayOutChat) {
            PacketPlayOutChat packetplayoutchat = (PacketPlayOutChat) packet;
            EntityHuman.EnumChatVisibility entityhuman_enumchatvisibility = this.player.getChatFlags();

            if (entityhuman_enumchatvisibility == EntityHuman.EnumChatVisibility.HIDDEN) {
                return;
            }

            if (entityhuman_enumchatvisibility == EntityHuman.EnumChatVisibility.SYSTEM && !packetplayoutchat.b()) {
                return;
            }
        }

        if (packet != null) {
            if (packet instanceof PacketPlayOutSpawnPosition) {
                PacketPlayOutSpawnPosition packetplayoutspawnposition = (PacketPlayOutSpawnPosition) packet;

                this.player.compassTarget = new Location(this.getPlayer().getWorld(), (double) packetplayoutspawnposition.position.getX(), (double) packetplayoutspawnposition.position.getY(), (double) packetplayoutspawnposition.position.getZ());
            }

            try {
                this.networkManager.handle(packet);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Sending packet");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Packet being sent");

                crashreportsystemdetails.a("Packet class", new Callable() {
                    public String a() throws Exception {
                        return packet.getClass().getCanonicalName();
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
    }

    public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayinhelditemslot, this, this.player.u());
            if (packetplayinhelditemslot.a() >= 0 && packetplayinhelditemslot.a() < PlayerInventory.getHotbarSize()) {
                PlayerItemHeldEvent playeritemheldevent = new PlayerItemHeldEvent(this.getPlayer(), this.player.inventory.itemInHandIndex, packetplayinhelditemslot.a());

                this.server.getPluginManager().callEvent(playeritemheldevent);
                if (playeritemheldevent.isCancelled()) {
                    this.sendPacket(new PacketPlayOutHeldItemSlot(this.player.inventory.itemInHandIndex));
                    this.player.resetIdleTimer();
                    return;
                }

                this.player.inventory.itemInHandIndex = packetplayinhelditemslot.a();
                this.player.resetIdleTimer();
            } else {
                PlayerConnection.c.warn(this.player.getName() + " tried to set an invalid carried item");
                this.disconnect("Invalid hotbar selection (Hacking?)");
            }

        }
    }

    public void a(PacketPlayInChat packetplayinchat) {
        boolean flag = packetplayinchat.a().startsWith("/");

        if (packetplayinchat.a().startsWith("/")) {
            PlayerConnectionUtils.ensureMainThread(packetplayinchat, this, this.player.u());
        }

        if (!this.player.dead && this.player.getChatFlags() != EntityHuman.EnumChatVisibility.HIDDEN) {
            this.player.resetIdleTimer();
            final String s = packetplayinchat.a();

            s = StringUtils.normalizeSpace(s);

            Waitable waitable;

            for (int i = 0; i < s.length(); ++i) {
                if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
                    if (!flag) {
                        waitable = new Waitable() {
                            protected Object evaluate() {
                                PlayerConnection.this.disconnect("Illegal characters in chat");
                                return null;
                            }
                        };
                        this.minecraftServer.processQueue.add(waitable);

                        try {
                            waitable.get();
                        } catch (InterruptedException interruptedexception) {
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException executionexception) {
                            throw new RuntimeException(executionexception);
                        }
                    } else {
                        this.disconnect("Illegal characters in chat");
                    }

                    return;
                }
            }

            if (flag) {
                try {
                    this.minecraftServer.server.playerCommandState = true;
                    this.handleCommand(s);
                } finally {
                    this.minecraftServer.server.playerCommandState = false;
                }
            } else if (s.isEmpty()) {
                PlayerConnection.c.warn(this.player.getName() + " tried to send an empty message");
            } else if (this.getPlayer().isConversing()) {
                this.minecraftServer.processQueue.add(new Waitable() {
                    protected Object evaluate() {
                        PlayerConnection.this.getPlayer().acceptConversationInput(s);
                        return null;
                    }
                });
            } else if (this.player.getChatFlags() == EntityHuman.EnumChatVisibility.SYSTEM) {
                ChatMessage chatmessage = new ChatMessage("chat.cannotSend", new Object[0]);

                chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                this.sendPacket(new PacketPlayOutChat(chatmessage));
            } else {
                this.chat(s, true);
            }

            boolean flag1 = true;
            Iterator iterator = SpigotConfig.spamExclusions.iterator();

            while (iterator.hasNext()) {
                String s1 = (String) iterator.next();

                if (s1 != null && s.startsWith(s1)) {
                    flag1 = false;
                    break;
                }
            }

            if (flag1 && PlayerConnection.chatSpamField.addAndGet(this, 20) > 200 && !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) {
                if (!flag) {
                    waitable = new Waitable() {
                        protected Object evaluate() {
                            PlayerConnection.this.disconnect("disconnect.spam");
                            return null;
                        }
                    };
                    this.minecraftServer.processQueue.add(waitable);

                    try {
                        waitable.get();
                    } catch (InterruptedException interruptedexception1) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException executionexception1) {
                        throw new RuntimeException(executionexception1);
                    }
                } else {
                    this.disconnect("disconnect.spam");
                }
            }
        } else {
            ChatMessage chatmessage1 = new ChatMessage("chat.cannotSend", new Object[0]);

            chatmessage1.getChatModifier().setColor(EnumChatFormat.RED);
            this.sendPacket(new PacketPlayOutChat(chatmessage1));
        }

    }

    public void chat(String s, boolean flag) {
        if (!s.isEmpty() && this.player.getChatFlags() != EntityHuman.EnumChatVisibility.HIDDEN) {
            if (!flag && s.startsWith("/")) {
                this.handleCommand(s);
            } else if (this.player.getChatFlags() != EntityHuman.EnumChatVisibility.SYSTEM) {
                CraftPlayer craftplayer = this.getPlayer();
                AsyncPlayerChatEvent asyncplayerchatevent = new AsyncPlayerChatEvent(flag, craftplayer, s, new LazyPlayerSet());

                this.server.getPluginManager().callEvent(asyncplayerchatevent);
                if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                    final PlayerChatEvent playerchatevent = new PlayerChatEvent(craftplayer, asyncplayerchatevent.getMessage(), asyncplayerchatevent.getFormat(), asyncplayerchatevent.getRecipients());

                    playerchatevent.setCancelled(asyncplayerchatevent.isCancelled());
                    Waitable waitable = new Waitable() {
                        protected Object evaluate() {
                            Bukkit.getPluginManager().callEvent(playerchatevent);
                            if (playerchatevent.isCancelled()) {
                                return null;
                            } else {
                                String s = String.format(playerchatevent.getFormat(), new Object[] { playerchatevent.getPlayer().getDisplayName(), playerchatevent.getMessage()});

                                PlayerConnection.this.minecraftServer.console.sendMessage(s);
                                Iterator iterator;

                                if (((LazyPlayerSet) playerchatevent.getRecipients()).isLazy()) {
                                    iterator = PlayerConnection.this.minecraftServer.getPlayerList().players.iterator();

                                    while (iterator.hasNext()) {
                                        Object object = iterator.next();

                                        ((EntityPlayer) object).sendMessage(CraftChatMessage.fromString(s));
                                    }
                                } else {
                                    iterator = playerchatevent.getRecipients().iterator();

                                    while (iterator.hasNext()) {
                                        Player player = (Player) iterator.next();

                                        player.sendMessage(s);
                                    }
                                }

                                return null;
                            }
                        }
                    };

                    if (flag) {
                        this.minecraftServer.processQueue.add(waitable);
                    } else {
                        waitable.run();
                    }

                    try {
                        waitable.get();
                    } catch (InterruptedException interruptedexception) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException executionexception) {
                        throw new RuntimeException("Exception processing chat event", executionexception.getCause());
                    }
                } else {
                    if (asyncplayerchatevent.isCancelled()) {
                        return;
                    }

                    s = String.format(asyncplayerchatevent.getFormat(), new Object[] { asyncplayerchatevent.getPlayer().getDisplayName(), asyncplayerchatevent.getMessage()});
                    this.minecraftServer.console.sendMessage(s);
                    Iterator iterator;

                    if (((LazyPlayerSet) asyncplayerchatevent.getRecipients()).isLazy()) {
                        iterator = this.minecraftServer.getPlayerList().players.iterator();

                        while (iterator.hasNext()) {
                            Object object = iterator.next();

                            ((EntityPlayer) object).sendMessage(CraftChatMessage.fromString(s));
                        }
                    } else {
                        iterator = asyncplayerchatevent.getRecipients().iterator();

                        while (iterator.hasNext()) {
                            Player player = (Player) iterator.next();

                            player.sendMessage(s);
                        }
                    }
                }
            }

        }
    }

    private void handleCommand(String s) {
        SpigotTimings.playerCommandTimer.startTiming();
        if (SpigotConfig.logCommands) {
            PlayerConnection.c.info(this.player.getName() + " issued server command: " + s);
        }

        CraftPlayer craftplayer = this.getPlayer();
        PlayerCommandPreprocessEvent playercommandpreprocessevent = new PlayerCommandPreprocessEvent(craftplayer, s, new LazyPlayerSet());

        this.server.getPluginManager().callEvent(playercommandpreprocessevent);
        if (playercommandpreprocessevent.isCancelled()) {
            SpigotTimings.playerCommandTimer.stopTiming();
        } else {
            try {
                if (this.server.dispatchCommand(playercommandpreprocessevent.getPlayer(), playercommandpreprocessevent.getMessage().substring(1))) {
                    SpigotTimings.playerCommandTimer.stopTiming();
                    return;
                }
            } catch (org.bukkit.command.CommandException org_bukkit_command_commandexception) {
                craftplayer.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
                java.util.logging.Logger.getLogger(PlayerConnection.class.getName()).log(Level.SEVERE, (String) null, org_bukkit_command_commandexception);
                SpigotTimings.playerCommandTimer.stopTiming();
                return;
            }

            SpigotTimings.playerCommandTimer.stopTiming();
        }
    }

    public void a(PacketPlayInArmAnimation packetplayinarmanimation) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayinarmanimation, this, this.player.u());
            this.player.resetIdleTimer();
            float f = this.player.pitch;
            float f1 = this.player.yaw;
            double d0 = this.player.locX;
            double d1 = this.player.locY + (double) this.player.getHeadHeight();
            double d2 = this.player.locZ;
            Vec3D vec3d = new Vec3D(d0, d1, d2);
            float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
            float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
            float f4 = -MathHelper.cos(-f * 0.017453292F);
            float f5 = MathHelper.sin(-f * 0.017453292F);
            float f6 = f3 * f4;
            float f7 = f2 * f4;
            double d3 = this.player.playerInteractManager.getGameMode() == WorldSettings.EnumGamemode.CREATIVE ? 5.0D : 4.5D;
            Vec3D vec3d1 = vec3d.add((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
            MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, false);

            if (movingobjectposition == null || movingobjectposition.type != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getItemInHand());
            }

            PlayerAnimationEvent playeranimationevent = new PlayerAnimationEvent(this.getPlayer());

            this.server.getPluginManager().callEvent(playeranimationevent);
            if (!playeranimationevent.isCancelled()) {
                this.player.bw();
            }
        }
    }

    public void a(PacketPlayInEntityAction packetplayinentityaction) {
        PlayerConnectionUtils.ensureMainThread(packetplayinentityaction, this, this.player.u());
        if (!this.player.dead) {
            switch ($SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction()[packetplayinentityaction.b().ordinal()]) {
            case 1:
            case 2:
                PlayerToggleSneakEvent playertogglesneakevent = new PlayerToggleSneakEvent(this.getPlayer(), packetplayinentityaction.b() == PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING);

                this.server.getPluginManager().callEvent(playertogglesneakevent);
                if (playertogglesneakevent.isCancelled()) {
                    return;
                }

            case 3:
            default:
                break;

            case 4:
            case 5:
                PlayerToggleSprintEvent playertogglesprintevent = new PlayerToggleSprintEvent(this.getPlayer(), packetplayinentityaction.b() == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING);

                this.server.getPluginManager().callEvent(playertogglesprintevent);
                if (playertogglesprintevent.isCancelled()) {
                    return;
                }
            }

            this.player.resetIdleTimer();
            switch (PlayerConnection.SyntheticClass_1.b[packetplayinentityaction.b().ordinal()]) {
            case 1:
                this.player.setSneaking(true);
                break;

            case 2:
                this.player.setSneaking(false);
                break;

            case 3:
                this.player.setSprinting(true);
                break;

            case 4:
                this.player.setSprinting(false);
                break;

            case 5:
                this.player.a(false, true, true);
                break;

            case 6:
                if (this.player.vehicle instanceof EntityHorse) {
                    ((EntityHorse) this.player.vehicle).v(packetplayinentityaction.c());
                }
                break;

            case 7:
                if (this.player.vehicle instanceof EntityHorse) {
                    ((EntityHorse) this.player.vehicle).g((EntityHuman) this.player);
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid client command!");
            }

        }
    }

    public void a(PacketPlayInUseEntity packetplayinuseentity) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayinuseentity, this, this.player.u());
            WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
            Entity entity = packetplayinuseentity.a((World) worldserver);

            if (entity == this.player && !this.player.isSpectator()) {
                this.disconnect("Cannot interact with self!");
            } else {
                this.player.resetIdleTimer();
                if (entity != null) {
                    boolean flag = this.player.hasLineOfSight(entity);
                    double d0 = 36.0D;

                    if (!flag) {
                        d0 = 9.0D;
                    }

                    if (this.player.h(entity) < d0) {
                        ItemStack itemstack = this.player.inventory.getItemInHand();

                        if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT || packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
                            boolean flag1 = itemstack != null && itemstack.getItem() == Items.LEAD && entity instanceof EntityInsentient;
                            Item item = this.player.inventory.getItemInHand() == null ? null : this.player.inventory.getItemInHand().getItem();
                            Object object;

                            if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) {
                                object = new PlayerInteractEntityEvent(this.getPlayer(), entity.getBukkitEntity());
                            } else {
                                Vec3D vec3d = packetplayinuseentity.b();

                                object = new PlayerInteractAtEntityEvent(this.getPlayer(), entity.getBukkitEntity(), new Vector(vec3d.a, vec3d.b, vec3d.c));
                            }

                            this.server.getPluginManager().callEvent((Event) object);
                            if (flag1 && (((PlayerInteractEntityEvent) object).isCancelled() || this.player.inventory.getItemInHand() == null || this.player.inventory.getItemInHand().getItem() != Items.LEAD)) {
                                this.sendPacket(new PacketPlayOutAttachEntity(1, entity, ((EntityInsentient) entity).getLeashHolder()));
                            }

                            if (((PlayerInteractEntityEvent) object).isCancelled() || this.player.inventory.getItemInHand() == null || this.player.inventory.getItemInHand().getItem() != item) {
                                this.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.datawatcher, true));
                            }

                            if (((PlayerInteractEntityEvent) object).isCancelled()) {
                                return;
                            }
                        }

                        if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) {
                            this.player.u(entity);
                            if (itemstack != null && itemstack.count <= -1) {
                                this.player.updateInventory(this.player.activeContainer);
                            }
                        } else if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
                            entity.a((EntityHuman) this.player, packetplayinuseentity.b());
                            if (itemstack != null && itemstack.count <= -1) {
                                this.player.updateInventory(this.player.activeContainer);
                            }
                        } else if (packetplayinuseentity.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
                            if (entity instanceof EntityItem || entity instanceof EntityExperienceOrb || entity instanceof EntityArrow || entity == this.player && !this.player.isSpectator()) {
                                this.disconnect("Attempting to attack an invalid entity");
                                this.minecraftServer.warning("Player " + this.player.getName() + " tried to attack an invalid entity");
                                return;
                            }

                            this.player.attack(entity);
                            if (itemstack != null && itemstack.count <= -1) {
                                this.player.updateInventory(this.player.activeContainer);
                            }
                        }
                    }
                }

            }
        }
    }

    public void a(PacketPlayInClientCommand packetplayinclientcommand) {
        PlayerConnectionUtils.ensureMainThread(packetplayinclientcommand, this, this.player.u());
        this.player.resetIdleTimer();
        PacketPlayInClientCommand.EnumClientCommand packetplayinclientcommand_enumclientcommand = packetplayinclientcommand.a();

        switch (PlayerConnection.SyntheticClass_1.c[packetplayinclientcommand_enumclientcommand.ordinal()]) {
        case 1:
            if (this.player.viewingCredits) {
                this.minecraftServer.getPlayerList().changeDimension(this.player, 0, TeleportCause.END_PORTAL);
            } else if (this.player.u().getWorldData().isHardcore()) {
                if (this.minecraftServer.T() && this.player.getName().equals(this.minecraftServer.S())) {
                    this.player.playerConnection.disconnect("You have died. Game over, man, it\'s game over!");
                    this.minecraftServer.aa();
                } else {
                    GameProfileBanEntry gameprofilebanentry = new GameProfileBanEntry(this.player.getProfile(), (Date) null, "(You just lost the game)", (Date) null, "Death in Hardcore");

                    this.minecraftServer.getPlayerList().getProfileBans().add(gameprofilebanentry);
                    this.player.playerConnection.disconnect("You have died. Game over, man, it\'s game over!");
                }
            } else {
                if (this.player.getHealth() > 0.0F) {
                    return;
                }

                this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, 0, false);
            }
            break;

        case 2:
            this.player.getStatisticManager().a(this.player);
            break;

        case 3:
            this.player.b((Statistic) AchievementList.f);
        }

    }

    public void a(PacketPlayInCloseWindow packetplayinclosewindow) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayinclosewindow, this, this.player.u());
            CraftEventFactory.handleInventoryCloseEvent(this.player);
            this.player.p();
        }
    }

    public void a(PacketPlayInWindowClick packetplayinwindowclick) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayinwindowclick, this, this.player.u());
            this.player.resetIdleTimer();
            if (this.player.activeContainer.windowId == packetplayinwindowclick.a() && this.player.activeContainer.c(this.player)) {
                boolean flag = this.player.isSpectator();

                if (packetplayinwindowclick.b() < -1 && packetplayinwindowclick.b() != -999) {
                    return;
                }

                InventoryView inventoryview = this.player.activeContainer.getBukkitView();
                SlotType slottype = CraftInventoryView.getSlotType(inventoryview, packetplayinwindowclick.b());
                Object object = null;
                ClickType clicktype = ClickType.UNKNOWN;
                InventoryAction inventoryaction = InventoryAction.UNKNOWN;
                ItemStack itemstack = null;

                if (packetplayinwindowclick.b() == -1) {
                    slottype = SlotType.OUTSIDE;
                    clicktype = packetplayinwindowclick.c() == 0 ? ClickType.WINDOW_BORDER_LEFT : ClickType.WINDOW_BORDER_RIGHT;
                    inventoryaction = InventoryAction.NOTHING;
                } else {
                    Slot slot;
                    ItemStack itemstack1;
                    int i;

                    if (packetplayinwindowclick.f() == 0) {
                        if (packetplayinwindowclick.c() == 0) {
                            clicktype = ClickType.LEFT;
                        } else if (packetplayinwindowclick.c() == 1) {
                            clicktype = ClickType.RIGHT;
                        }

                        if (packetplayinwindowclick.c() == 0 || packetplayinwindowclick.c() == 1) {
                            inventoryaction = InventoryAction.NOTHING;
                            if (packetplayinwindowclick.b() == -999) {
                                if (this.player.inventory.getCarried() != null) {
                                    inventoryaction = packetplayinwindowclick.c() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                                }
                            } else {
                                slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null) {
                                    itemstack1 = slot.getItem();
                                    ItemStack itemstack2 = this.player.inventory.getCarried();

                                    if (itemstack1 == null) {
                                        if (itemstack2 != null) {
                                            inventoryaction = packetplayinwindowclick.c() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                        }
                                    } else if (slot.isAllowed((EntityHuman) this.player)) {
                                        if (itemstack2 == null) {
                                            inventoryaction = packetplayinwindowclick.c() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                        } else if (slot.isAllowed(itemstack2)) {
                                            if (itemstack1.doMaterialsMatch(itemstack2) && ItemStack.equals(itemstack1, itemstack2)) {
                                                i = packetplayinwindowclick.c() == 0 ? itemstack2.count : 1;
                                                i = Math.min(i, itemstack1.getMaxStackSize() - itemstack1.count);
                                                i = Math.min(i, slot.inventory.getMaxStackSize() - itemstack1.count);
                                                if (i == 1) {
                                                    inventoryaction = InventoryAction.PLACE_ONE;
                                                } else if (i == itemstack2.count) {
                                                    inventoryaction = InventoryAction.PLACE_ALL;
                                                } else if (i < 0) {
                                                    inventoryaction = i != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                                                } else if (i != 0) {
                                                    inventoryaction = InventoryAction.PLACE_SOME;
                                                }
                                            } else if (itemstack2.count <= slot.getMaxStackSize()) {
                                                inventoryaction = InventoryAction.SWAP_WITH_CURSOR;
                                            }
                                        } else if (itemstack2.getItem() == itemstack1.getItem() && (!itemstack2.usesData() || itemstack2.getData() == itemstack1.getData()) && ItemStack.equals(itemstack2, itemstack1) && itemstack1.count >= 0 && itemstack1.count + itemstack2.count <= itemstack2.getMaxStackSize()) {
                                            inventoryaction = InventoryAction.PICKUP_ALL;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (packetplayinwindowclick.f() == 1) {
                        if (packetplayinwindowclick.c() == 0) {
                            clicktype = ClickType.SHIFT_LEFT;
                        } else if (packetplayinwindowclick.c() == 1) {
                            clicktype = ClickType.SHIFT_RIGHT;
                        }

                        if (packetplayinwindowclick.c() == 0 || packetplayinwindowclick.c() == 1) {
                            if (packetplayinwindowclick.b() < 0) {
                                inventoryaction = InventoryAction.NOTHING;
                            } else {
                                slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.isAllowed((EntityHuman) this.player) && slot.hasItem()) {
                                    inventoryaction = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                                } else {
                                    inventoryaction = InventoryAction.NOTHING;
                                }
                            }
                        }
                    } else if (packetplayinwindowclick.f() == 2) {
                        if (packetplayinwindowclick.c() >= 0 && packetplayinwindowclick.c() < 9) {
                            clicktype = ClickType.NUMBER_KEY;
                            slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                            if (slot.isAllowed((EntityHuman) this.player)) {
                                itemstack1 = this.player.inventory.getItem(packetplayinwindowclick.c());
                                boolean flag1 = itemstack1 == null || slot.inventory == this.player.inventory && slot.isAllowed(itemstack1);

                                if (slot.hasItem()) {
                                    if (flag1) {
                                        inventoryaction = InventoryAction.HOTBAR_SWAP;
                                    } else {
                                        i = this.player.inventory.getFirstEmptySlotIndex();
                                        if (i > -1) {
                                            inventoryaction = InventoryAction.HOTBAR_MOVE_AND_READD;
                                        } else {
                                            inventoryaction = InventoryAction.NOTHING;
                                        }
                                    }
                                } else if (!slot.hasItem() && itemstack1 != null && slot.isAllowed(itemstack1)) {
                                    inventoryaction = InventoryAction.HOTBAR_SWAP;
                                } else {
                                    inventoryaction = InventoryAction.NOTHING;
                                }
                            } else {
                                inventoryaction = InventoryAction.NOTHING;
                            }

                            new InventoryClickEvent(inventoryview, slottype, packetplayinwindowclick.b(), clicktype, inventoryaction, packetplayinwindowclick.c());
                        }
                    } else if (packetplayinwindowclick.f() == 3) {
                        if (packetplayinwindowclick.c() == 2) {
                            clicktype = ClickType.MIDDLE;
                            if (packetplayinwindowclick.b() == -999) {
                                inventoryaction = InventoryAction.NOTHING;
                            } else {
                                slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.hasItem() && this.player.abilities.canInstantlyBuild && this.player.inventory.getCarried() == null) {
                                    inventoryaction = InventoryAction.CLONE_STACK;
                                } else {
                                    inventoryaction = InventoryAction.NOTHING;
                                }
                            }
                        } else {
                            clicktype = ClickType.UNKNOWN;
                            inventoryaction = InventoryAction.UNKNOWN;
                        }
                    } else if (packetplayinwindowclick.f() == 4) {
                        if (packetplayinwindowclick.b() >= 0) {
                            if (packetplayinwindowclick.c() == 0) {
                                clicktype = ClickType.DROP;
                                slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.hasItem() && slot.isAllowed((EntityHuman) this.player) && slot.getItem() != null && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                                    inventoryaction = InventoryAction.DROP_ONE_SLOT;
                                } else {
                                    inventoryaction = InventoryAction.NOTHING;
                                }
                            } else if (packetplayinwindowclick.c() == 1) {
                                clicktype = ClickType.CONTROL_DROP;
                                slot = this.player.activeContainer.getSlot(packetplayinwindowclick.b());
                                if (slot != null && slot.hasItem() && slot.isAllowed((EntityHuman) this.player) && slot.getItem() != null && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                                    inventoryaction = InventoryAction.DROP_ALL_SLOT;
                                } else {
                                    inventoryaction = InventoryAction.NOTHING;
                                }
                            }
                        } else {
                            clicktype = ClickType.LEFT;
                            if (packetplayinwindowclick.c() == 1) {
                                clicktype = ClickType.RIGHT;
                            }

                            inventoryaction = InventoryAction.NOTHING;
                        }
                    } else if (packetplayinwindowclick.f() == 5) {
                        itemstack = this.player.activeContainer.clickItem(packetplayinwindowclick.b(), packetplayinwindowclick.c(), 5, this.player);
                    } else if (packetplayinwindowclick.f() == 6) {
                        clicktype = ClickType.DOUBLE_CLICK;
                        inventoryaction = InventoryAction.NOTHING;
                        if (packetplayinwindowclick.b() >= 0 && this.player.inventory.getCarried() != null) {
                            ItemStack itemstack3 = this.player.inventory.getCarried();

                            inventoryaction = InventoryAction.NOTHING;
                            if (inventoryview.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getId(itemstack3.getItem()))) || inventoryview.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getId(itemstack3.getItem())))) {
                                inventoryaction = InventoryAction.COLLECT_TO_CURSOR;
                            }
                        }
                    }
                }

                if (packetplayinwindowclick.f() != 5) {
                    if (clicktype == ClickType.NUMBER_KEY) {
                        object = new InventoryClickEvent(inventoryview, slottype, packetplayinwindowclick.b(), clicktype, inventoryaction, packetplayinwindowclick.c());
                    } else {
                        object = new InventoryClickEvent(inventoryview, slottype, packetplayinwindowclick.b(), clicktype, inventoryaction);
                    }

                    Inventory inventory = inventoryview.getTopInventory();

                    if (packetplayinwindowclick.b() == 0 && inventory instanceof CraftingInventory) {
                        Recipe recipe = ((CraftingInventory) inventory).getRecipe();

                        if (recipe != null) {
                            if (clicktype == ClickType.NUMBER_KEY) {
                                object = new CraftItemEvent(recipe, inventoryview, slottype, packetplayinwindowclick.b(), clicktype, inventoryaction, packetplayinwindowclick.c());
                            } else {
                                object = new CraftItemEvent(recipe, inventoryview, slottype, packetplayinwindowclick.b(), clicktype, inventoryaction);
                            }
                        }
                    }

                    ((InventoryClickEvent) object).setCancelled(flag);
                    this.server.getPluginManager().callEvent((Event) object);
                    switch ($SWITCH_TABLE$org$bukkit$event$Event$Result()[((InventoryClickEvent) object).getResult().ordinal()]) {
                    case 1:
                        switch ($SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction()[inventoryaction.ordinal()]) {
                        case 1:
                        default:
                            break;

                        case 2:
                        case 14:
                        case 15:
                        case 16:
                        case 18:
                        case 19:
                            this.player.updateInventory(this.player.activeContainer);
                            break;

                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                            this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
                            this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, packetplayinwindowclick.b(), this.player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                            break;

                        case 10:
                        case 11:
                        case 17:
                            this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
                            break;

                        case 12:
                        case 13:
                            this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, packetplayinwindowclick.b(), this.player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                        }

                        return;

                    case 2:
                    case 3:
                        itemstack = this.player.activeContainer.clickItem(packetplayinwindowclick.b(), packetplayinwindowclick.c(), packetplayinwindowclick.f(), this.player);

                    default:
                        if (object instanceof CraftItemEvent) {
                            this.player.updateInventory(this.player.activeContainer);
                        }
                    }
                }

                if (ItemStack.matches(packetplayinwindowclick.e(), itemstack)) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), true));
                    this.player.g = true;
                    this.player.activeContainer.b();
                    this.player.broadcastCarriedItem();
                    this.player.g = false;
                } else {
                    this.n.a(this.player.activeContainer.windowId, Short.valueOf(packetplayinwindowclick.d()));
                    this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), false));
                    this.player.activeContainer.a(this.player, false);
                    ArrayList arraylist = Lists.newArrayList();

                    for (int j = 0; j < this.player.activeContainer.c.size(); ++j) {
                        arraylist.add(((Slot) this.player.activeContainer.c.get(j)).getItem());
                    }

                    this.player.a(this.player.activeContainer, (List) arraylist);
                }
            }

        }
    }

    public void a(PacketPlayInEnchantItem packetplayinenchantitem) {
        PlayerConnectionUtils.ensureMainThread(packetplayinenchantitem, this, this.player.u());
        this.player.resetIdleTimer();
        if (this.player.activeContainer.windowId == packetplayinenchantitem.a() && this.player.activeContainer.c(this.player) && !this.player.isSpectator()) {
            this.player.activeContainer.a(this.player, packetplayinenchantitem.b());
            this.player.activeContainer.b();
        }

    }

    public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
        PlayerConnectionUtils.ensureMainThread(packetplayinsetcreativeslot, this, this.player.u());
        if (this.player.playerInteractManager.isCreative()) {
            boolean flag = packetplayinsetcreativeslot.a() < 0;
            ItemStack itemstack = packetplayinsetcreativeslot.getItemStack();

            if (itemstack != null && itemstack.hasTag() && itemstack.getTag().hasKeyOfType("BlockEntityTag", 10)) {
                NBTTagCompound nbttagcompound = itemstack.getTag().getCompound("BlockEntityTag");

                if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
                    BlockPosition blockposition = new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
                    TileEntity tileentity = this.player.world.getTileEntity(blockposition);

                    if (tileentity != null) {
                        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                        tileentity.b(nbttagcompound1);
                        nbttagcompound1.remove("x");
                        nbttagcompound1.remove("y");
                        nbttagcompound1.remove("z");
                        itemstack.a("BlockEntityTag", (NBTBase) nbttagcompound1);
                    }
                }
            }

            boolean flag1 = packetplayinsetcreativeslot.a() >= 1 && packetplayinsetcreativeslot.a() < 36 + PlayerInventory.getHotbarSize();
            boolean flag2 = itemstack == null || itemstack.getItem() != null && (!PlayerConnection.invalidItems.contains(Integer.valueOf(Item.getId(itemstack.getItem()))) || !SpigotConfig.filterCreativeItems);
            boolean flag3 = itemstack == null || itemstack.getData() >= 0 && itemstack.count <= 64 && itemstack.count > 0;

            if (flag || flag1 && !ItemStack.matches(this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.a()).getItem(), packetplayinsetcreativeslot.getItemStack())) {
                CraftPlayer craftplayer = this.player.getBukkitEntity();
                CraftInventoryView craftinventoryview = new CraftInventoryView(craftplayer, craftplayer.getInventory(), this.player.defaultContainer);
                org.bukkit.inventory.ItemStack org_bukkit_inventory_itemstack = CraftItemStack.asBukkitCopy(packetplayinsetcreativeslot.getItemStack());
                SlotType slottype = SlotType.QUICKBAR;

                if (flag) {
                    slottype = SlotType.OUTSIDE;
                } else if (packetplayinsetcreativeslot.a() < 36) {
                    if (packetplayinsetcreativeslot.a() >= 5 && packetplayinsetcreativeslot.a() < 9) {
                        slottype = SlotType.ARMOR;
                    } else {
                        slottype = SlotType.CONTAINER;
                    }
                }

                InventoryCreativeEvent inventorycreativeevent = new InventoryCreativeEvent(craftinventoryview, slottype, flag ? -999 : packetplayinsetcreativeslot.a(), org_bukkit_inventory_itemstack);

                this.server.getPluginManager().callEvent(inventorycreativeevent);
                itemstack = CraftItemStack.asNMSCopy(inventorycreativeevent.getCursor());
                switch ($SWITCH_TABLE$org$bukkit$event$Event$Result()[inventorycreativeevent.getResult().ordinal()]) {
                case 1:
                    if (packetplayinsetcreativeslot.a() >= 0) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.defaultContainer.windowId, packetplayinsetcreativeslot.a(), this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.a()).getItem()));
                        this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, (ItemStack) null));
                    }

                    return;

                case 2:
                default:
                    break;

                case 3:
                    flag3 = true;
                    flag2 = true;
                }
            }

            if (flag1 && flag2 && flag3) {
                if (itemstack == null) {
                    this.player.defaultContainer.setItem(packetplayinsetcreativeslot.a(), (ItemStack) null);
                } else {
                    this.player.defaultContainer.setItem(packetplayinsetcreativeslot.a(), itemstack);
                }

                this.player.defaultContainer.a(this.player, true);
            } else if (flag && flag2 && flag3 && this.m < 200) {
                this.m += 20;
                EntityItem entityitem = this.player.drop(itemstack, true);

                if (entityitem != null) {
                    entityitem.j();
                }
            }
        }

    }

    public void a(PacketPlayInTransaction packetplayintransaction) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayintransaction, this, this.player.u());
            Short oshort = (Short) this.n.get(this.player.activeContainer.windowId);

            if (oshort != null && packetplayintransaction.b() == oshort.shortValue() && this.player.activeContainer.windowId == packetplayintransaction.a() && !this.player.activeContainer.c(this.player) && !this.player.isSpectator()) {
                this.player.activeContainer.a(this.player, true);
            }

        }
    }

    public void a(PacketPlayInUpdateSign packetplayinupdatesign) {
        if (!this.player.dead) {
            PlayerConnectionUtils.ensureMainThread(packetplayinupdatesign, this, this.player.u());
            this.player.resetIdleTimer();
            WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
            BlockPosition blockposition = packetplayinupdatesign.a();

            if (worldserver.isLoaded(blockposition)) {
                TileEntity tileentity = worldserver.getTileEntity(blockposition);

                if (!(tileentity instanceof TileEntitySign)) {
                    return;
                }

                TileEntitySign tileentitysign = (TileEntitySign) tileentity;

                if (!tileentitysign.b() || tileentitysign.c() != this.player) {
                    this.minecraftServer.warning("Player " + this.player.getName() + " just tried to change non-editable sign");
                    this.sendPacket(new PacketPlayOutUpdateSign(tileentity.world, packetplayinupdatesign.a(), tileentitysign.lines));
                    return;
                }

                IChatBaseComponent[] aichatbasecomponent = packetplayinupdatesign.b();
                Player player = this.server.getPlayer(this.player);
                int i = packetplayinupdatesign.a().getX();
                int j = packetplayinupdatesign.a().getY();
                int k = packetplayinupdatesign.a().getZ();
                String[] astring = new String[4];

                for (int l = 0; l < aichatbasecomponent.length; ++l) {
                    astring[l] = aichatbasecomponent[l].c();
                }

                SignChangeEvent signchangeevent = new SignChangeEvent((CraftBlock) player.getWorld().getBlockAt(i, j, k), this.server.getPlayer(this.player), astring);

                this.server.getPluginManager().callEvent(signchangeevent);
                if (!signchangeevent.isCancelled()) {
                    System.arraycopy(CraftSign.sanitizeLines(signchangeevent.getLines()), 0, tileentitysign.lines, 0, 4);
                    tileentitysign.isEditable = false;
                }

                tileentitysign.update();
                worldserver.notify(blockposition);
            }

        }
    }

    public void a(PacketPlayInKeepAlive packetplayinkeepalive) {
        if (packetplayinkeepalive.a() == this.i) {
            int i = (int) (this.d() - this.j);

            this.player.ping = (this.player.ping * 3 + i) / 4;
        }

    }

    private long d() {
        return System.nanoTime() / 1000000L;
    }

    public void a(PacketPlayInAbilities packetplayinabilities) {
        PlayerConnectionUtils.ensureMainThread(packetplayinabilities, this, this.player.u());
        if (this.player.abilities.canFly && this.player.abilities.isFlying != packetplayinabilities.isFlying()) {
            PlayerToggleFlightEvent playertoggleflightevent = new PlayerToggleFlightEvent(this.server.getPlayer(this.player), packetplayinabilities.isFlying());

            this.server.getPluginManager().callEvent(playertoggleflightevent);
            if (!playertoggleflightevent.isCancelled()) {
                this.player.abilities.isFlying = packetplayinabilities.isFlying();
            } else {
                this.player.updateAbilities();
            }
        }

    }

    public void a(PacketPlayInTabComplete packetplayintabcomplete) {
        PlayerConnectionUtils.ensureMainThread(packetplayintabcomplete, this, this.player.u());
        if (PlayerConnection.chatSpamField.addAndGet(this, 10) > 500 && !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) {
            this.disconnect("disconnect.spam");
        } else {
            ArrayList arraylist = Lists.newArrayList();
            Iterator iterator = this.minecraftServer.tabCompleteCommand(this.player, packetplayintabcomplete.a(), packetplayintabcomplete.b()).iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();

                arraylist.add(s);
            }

            this.player.playerConnection.sendPacket(new PacketPlayOutTabComplete((String[]) arraylist.toArray(new String[arraylist.size()])));
        }
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
        PlayerConnectionUtils.ensureMainThread(packetplayinsettings, this, this.player.u());
        this.player.a(packetplayinsettings);
    }

    public void a(PacketPlayInCustomPayload packetplayincustompayload) {
        PlayerConnectionUtils.ensureMainThread(packetplayincustompayload, this, this.player.u());
        PacketDataSerializer packetdataserializer;
        ItemStack itemstack;
        ItemStack itemstack1;

        if ("MC|BEdit".equals(packetplayincustompayload.a())) {
            packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.b()));

            try {
                itemstack = packetdataserializer.i();
                if (itemstack == null) {
                    return;
                }

                if (!ItemBookAndQuill.b(itemstack.getTag())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.player.inventory.getItemInHand();
                if (itemstack1 == null) {
                    return;
                }

                if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack.getItem() == itemstack1.getItem()) {
                    itemstack1 = new ItemStack(Items.WRITABLE_BOOK);
                    itemstack1.a("pages", (NBTBase) itemstack.getTag().getList("pages", 8));
                    CraftEventFactory.handleEditBookEvent(this.player, itemstack1);
                }
            } catch (Exception exception) {
                PlayerConnection.c.error("Couldn\'t handle book info", exception);
                this.disconnect("Invalid book data!");
                return;
            } finally {
                packetdataserializer.release();
            }

        } else if ("MC|BSign".equals(packetplayincustompayload.a())) {
            packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.b()));

            try {
                itemstack = packetdataserializer.i();
                if (itemstack == null) {
                    return;
                }

                if (!ItemWrittenBook.b(itemstack.getTag())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.player.inventory.getItemInHand();
                if (itemstack1 == null) {
                    return;
                }

                if (itemstack.getItem() == Items.WRITTEN_BOOK && itemstack1.getItem() == Items.WRITABLE_BOOK) {
                    itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
                    itemstack1.a("author", (NBTBase) (new NBTTagString(this.player.getName())));
                    itemstack1.a("title", (NBTBase) (new NBTTagString(itemstack.getTag().getString("title"))));
                    itemstack1.a("pages", (NBTBase) itemstack.getTag().getList("pages", 8));
                    itemstack1.setItem(Items.WRITTEN_BOOK);
                    CraftEventFactory.handleEditBookEvent(this.player, itemstack1);
                }
            } catch (Exception exception1) {
                PlayerConnection.c.error("Couldn\'t sign book", exception1);
                this.disconnect("Invalid book data!");
                return;
            } finally {
                packetdataserializer.release();
            }

        } else {
            int i;

            if ("MC|TrSel".equals(packetplayincustompayload.a())) {
                try {
                    i = packetplayincustompayload.b().readInt();
                    Container container = this.player.activeContainer;

                    if (container instanceof ContainerMerchant) {
                        ((ContainerMerchant) container).d(i);
                    }
                } catch (Exception exception2) {
                    PlayerConnection.c.error("Couldn\'t select trade", exception2);
                    this.disconnect("Invalid trade data!");
                }
            } else if ("MC|AdvCdm".equals(packetplayincustompayload.a())) {
                if (!this.minecraftServer.getEnableCommandBlock()) {
                    this.player.sendMessage((IChatBaseComponent) (new ChatMessage("advMode.notEnabled", new Object[0])));
                } else if (this.player.getBukkitEntity().isOp() && this.player.abilities.canInstantlyBuild) {
                    packetdataserializer = packetplayincustompayload.b();

                    try {
                        byte b0 = packetdataserializer.readByte();
                        CommandBlockListenerAbstract commandblocklistenerabstract = null;

                        if (b0 == 0) {
                            TileEntity tileentity = this.player.world.getTileEntity(new BlockPosition(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt()));

                            if (tileentity instanceof TileEntityCommand) {
                                commandblocklistenerabstract = ((TileEntityCommand) tileentity).getCommandBlock();
                            }
                        } else if (b0 == 1) {
                            Entity entity = this.player.world.a(packetdataserializer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock) {
                                commandblocklistenerabstract = ((EntityMinecartCommandBlock) entity).getCommandBlock();
                            }
                        }

                        String s = packetdataserializer.c(packetdataserializer.readableBytes());
                        boolean flag = packetdataserializer.readBoolean();

                        if (commandblocklistenerabstract != null) {
                            commandblocklistenerabstract.setCommand(s);
                            commandblocklistenerabstract.a(flag);
                            if (!flag) {
                                commandblocklistenerabstract.b((IChatBaseComponent) null);
                            }

                            commandblocklistenerabstract.h();
                            this.player.sendMessage((IChatBaseComponent) (new ChatMessage("advMode.setCommand.success", new Object[] { s})));
                        }
                    } catch (Exception exception3) {
                        PlayerConnection.c.error("Couldn\'t set command block", exception3);
                        this.disconnect("Invalid CommandBlock data!");
                    } finally {
                        packetdataserializer.release();
                    }
                } else {
                    this.player.sendMessage((IChatBaseComponent) (new ChatMessage("advMode.notAllowed", new Object[0])));
                }
            } else if ("MC|Beacon".equals(packetplayincustompayload.a())) {
                if (this.player.activeContainer instanceof ContainerBeacon) {
                    try {
                        packetdataserializer = packetplayincustompayload.b();
                        i = packetdataserializer.readInt();
                        int j = packetdataserializer.readInt();
                        ContainerBeacon containerbeacon = (ContainerBeacon) this.player.activeContainer;
                        Slot slot = containerbeacon.getSlot(0);

                        if (slot.hasItem()) {
                            slot.a(1);
                            IInventory iinventory = containerbeacon.e();

                            iinventory.b(1, i);
                            iinventory.b(2, j);
                            iinventory.update();
                        }
                    } catch (Exception exception4) {
                        PlayerConnection.c.error("Couldn\'t set beacon", exception4);
                        this.disconnect("Invalid beacon data!");
                    }
                }
            } else {
                String s1;

                if ("MC|ItemName".equals(packetplayincustompayload.a()) && this.player.activeContainer instanceof ContainerAnvil) {
                    ContainerAnvil containeranvil = (ContainerAnvil) this.player.activeContainer;

                    if (packetplayincustompayload.b() != null && packetplayincustompayload.b().readableBytes() >= 1) {
                        s1 = SharedConstants.a(packetplayincustompayload.b().c(32767));
                        if (s1.length() <= 30) {
                            containeranvil.a(s1);
                        }
                    } else {
                        containeranvil.a("");
                    }
                } else {
                    String s2;
                    int k;
                    int l;
                    String[] astring;

                    if (packetplayincustompayload.a().equals("REGISTER")) {
                        s2 = packetplayincustompayload.b().toString(Charsets.UTF_8);
                        l = (astring = s2.split("\u0000")).length;

                        for (k = 0; k < l; ++k) {
                            s1 = astring[k];
                            this.getPlayer().addChannel(s1);
                        }
                    } else if (packetplayincustompayload.a().equals("UNREGISTER")) {
                        s2 = packetplayincustompayload.b().toString(Charsets.UTF_8);
                        l = (astring = s2.split("\u0000")).length;

                        for (k = 0; k < l; ++k) {
                            s1 = astring[k];
                            this.getPlayer().removeChannel(s1);
                        }
                    } else {
                        byte[] abyte = new byte[packetplayincustompayload.b().readableBytes()];

                        packetplayincustompayload.b().readBytes(abyte);
                        this.server.getMessenger().dispatchIncomingMessage(this.player.getBukkitEntity(), packetplayincustompayload.a(), abyte);
                    }
                }
            }

        }
    }

    public boolean isDisconnected() {
        return !this.player.joining && !this.networkManager.channel.config().isAutoRead();
    }

    static int[] $SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction() {
        int[] aint = PlayerConnection.$SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction;

        if (PlayerConnection.$SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction != null) {
            return aint;
        } else {
            int[] aint1 = new int[PacketPlayInEntityAction.EnumPlayerAction.values().length];

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.OPEN_INVENTORY.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.RIDING_JUMP.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.STOP_SLEEPING.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.STOP_SNEAKING.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                aint1[PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            PlayerConnection.$SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction = aint1;
            return aint1;
        }
    }

    static int[] $SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction() {
        int[] aint = PlayerConnection.$SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction;

        if (PlayerConnection.$SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction != null) {
            return aint;
        } else {
            int[] aint1 = new int[InventoryAction.values().length];

            try {
                aint1[InventoryAction.CLONE_STACK.ordinal()] = 17;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[InventoryAction.COLLECT_TO_CURSOR.ordinal()] = 18;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[InventoryAction.DROP_ALL_CURSOR.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[InventoryAction.DROP_ALL_SLOT.ordinal()] = 12;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[InventoryAction.DROP_ONE_CURSOR.ordinal()] = 11;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                aint1[InventoryAction.DROP_ONE_SLOT.ordinal()] = 13;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                aint1[InventoryAction.HOTBAR_MOVE_AND_READD.ordinal()] = 15;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                aint1[InventoryAction.HOTBAR_SWAP.ordinal()] = 16;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                aint1[InventoryAction.MOVE_TO_OTHER_INVENTORY.ordinal()] = 14;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                aint1[InventoryAction.NOTHING.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                aint1[InventoryAction.PICKUP_ALL.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                aint1[InventoryAction.PICKUP_HALF.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                aint1[InventoryAction.PICKUP_ONE.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                aint1[InventoryAction.PICKUP_SOME.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                aint1[InventoryAction.PLACE_ALL.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                aint1[InventoryAction.PLACE_ONE.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

            try {
                aint1[InventoryAction.PLACE_SOME.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror16) {
                ;
            }

            try {
                aint1[InventoryAction.SWAP_WITH_CURSOR.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror17) {
                ;
            }

            try {
                aint1[InventoryAction.UNKNOWN.ordinal()] = 19;
            } catch (NoSuchFieldError nosuchfielderror18) {
                ;
            }

            PlayerConnection.$SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction = aint1;
            return aint1;
        }
    }

    static int[] $SWITCH_TABLE$org$bukkit$event$Event$Result() {
        int[] aint = PlayerConnection.$SWITCH_TABLE$org$bukkit$event$Event$Result;

        if (PlayerConnection.$SWITCH_TABLE$org$bukkit$event$Event$Result != null) {
            return aint;
        } else {
            int[] aint1 = new int[Result.values().length];

            try {
                aint1[Result.ALLOW.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[Result.DEFAULT.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[Result.DENY.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            PlayerConnection.$SWITCH_TABLE$org$bukkit$event$Event$Result = aint1;
            return aint1;
        }
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b;
        static final int[] c = new int[PacketPlayInClientCommand.EnumClientCommand.values().length];

        static {
            try {
                PlayerConnection.SyntheticClass_1.c[PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.c[PacketPlayInClientCommand.EnumClientCommand.REQUEST_STATS.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.c[PacketPlayInClientCommand.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            b = new int[PacketPlayInEntityAction.EnumPlayerAction.values().length];

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.START_SNEAKING.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_SNEAKING.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_SPRINTING.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.STOP_SLEEPING.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.RIDING_JUMP.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.b[PacketPlayInEntityAction.EnumPlayerAction.OPEN_INVENTORY.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            a = new int[PacketPlayInBlockDig.EnumPlayerDigType.values().length];

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.DROP_ITEM.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.DROP_ALL_ITEMS.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.RELEASE_USE_ITEM.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                PlayerConnection.SyntheticClass_1.a[PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

        }

        SyntheticClass_1() {}
    }
}
