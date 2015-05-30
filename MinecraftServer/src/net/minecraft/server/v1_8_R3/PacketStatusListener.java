package net.minecraft.server.v1_8_R3;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftIconCache;
import org.bukkit.entity.Player;
import org.bukkit.util.CachedServerIcon;
import org.spigotmc.SpigotConfig;

public class PacketStatusListener implements PacketStatusInListener {

    private final MinecraftServer minecraftServer;
    private final NetworkManager networkManager;
    private static final int WAITING = 0;
    private static final int PING = 1;
    private static final int DONE = 2;
    private int state = 0;

    public PacketStatusListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
    }

    public void a(IChatBaseComponent ichatbasecomponent) {}

    public void a(PacketStatusInStart packetstatusinstart) {
        if (this.state != 0) {
            this.networkManager.close((IChatBaseComponent) null);
        } else {
            this.state = 1;
            final Object[] aobject = this.minecraftServer.getPlayerList().players.toArray();
            class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {

                CraftIconCache icon;

                ServerListPingEvent() {
                    super(((InetSocketAddress) PacketStatusListener.this.networkManager.getSocketAddress()).getAddress(), PacketStatusListener.this.minecraftServer.getMotd(), PacketStatusListener.this.minecraftServer.getPlayerList().getMaxPlayers());
                    this.icon = PacketStatusListener.this.minecraftServer.server.getServerIcon();
                }

                public void setServerIcon(CachedServerIcon cachedservericon) {
                    if (!(cachedservericon instanceof CraftIconCache)) {
                        throw new IllegalArgumentException(cachedservericon + " was not created by " + CraftServer.class);
                    } else {
                        this.icon = (CraftIconCache) cachedservericon;
                    }
                }

                public Iterator<Player> iterator() throws UnsupportedOperationException {
                    return new Iterator() {
                        int i;
                        int ret = Integer.MIN_VALUE;
                        EntityPlayer player;

                        public boolean hasNext() {
                            if (this.player != null) {
                                return true;
                            } else {
                                Object[] aobject = aobject1;
                                int i = aobject.length;

                                for (int j = this.i; j < i; ++j) {
                                    EntityPlayer entityplayer = (EntityPlayer) aobject[j];

                                    if (entityplayer != null) {
                                        this.i = j + 1;
                                        this.player = entityplayer;
                                        return true;
                                    }
                                }

                                return false;
                            }
                        }

                        public Player next() {
                            if (!this.hasNext()) {
                                throw new NoSuchElementException();
                            } else {
                                EntityPlayer entityplayer = this.player;

                                this.player = null;
                                this.ret = this.i - 1;
                                return entityplayer.getBukkitEntity();
                            }
                        }

                        public void remove() {
                            Object[] aobject = aobject1;
                            int i = this.ret;

                            if (i >= 0 && aobject[i] != null) {
                                aobject[i] = null;
                            } else {
                                throw new IllegalStateException();
                            }
                        }

                        public Object next() {
                            return this.next();
                        }
                    };
                }
            }

            ServerListPingEvent serverlistpingevent = new ServerListPingEvent();

            this.minecraftServer.server.getPluginManager().callEvent(serverlistpingevent);
            Object object = new ArrayList(aobject.length);
            Object[] aobject1 = aobject;
            int i = aobject.length;

            for (int j = 0; j < i; ++j) {
                Object object1 = aobject1[j];

                if (object1 != null) {
                    ((List) object).add(((EntityPlayer) object1).getProfile());
                }
            }

            ServerPing.ServerPingPlayerSample serverping_serverpingplayersample = new ServerPing.ServerPingPlayerSample(serverlistpingevent.getMaxPlayers(), ((List) object).size());

            if (!((List) object).isEmpty()) {
                Collections.shuffle((List) object);
                object = ((List) object).subList(0, Math.min(((List) object).size(), SpigotConfig.playerSample));
            }

            serverping_serverpingplayersample.a((GameProfile[]) ((List) object).toArray(new GameProfile[((List) object).size()]));
            ServerPing serverping = new ServerPing();

            serverping.setFavicon(serverlistpingevent.icon.value);
            serverping.setMOTD(new ChatComponentText(serverlistpingevent.getMotd()));
            serverping.setPlayerSample(serverping_serverpingplayersample);
            serverping.setServerInfo(new ServerPing.ServerData(this.minecraftServer.getServerModName() + " " + this.minecraftServer.getVersion(), 47));
            this.networkManager.handle(new PacketStatusOutServerInfo(serverping));
        }
    }

    public void a(PacketStatusInPing packetstatusinping) {
        if (this.state != 1) {
            this.networkManager.close((IChatBaseComponent) null);
        } else {
            this.state = 2;
            this.networkManager.a(new PacketStatusOutPong(packetstatusinping.a()), ChannelFutureListener.CLOSE, new GenericFutureListener[0]);
        }
    }
}
