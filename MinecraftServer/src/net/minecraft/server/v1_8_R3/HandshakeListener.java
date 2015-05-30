package net.minecraft.server.v1_8_R3;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.spigotmc.SpigotConfig;

public class HandshakeListener implements PacketHandshakingInListener {

    private static final Gson gson = new Gson();
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap();
    private static int throttleCounter = 0;
    private final MinecraftServer a;
    private final NetworkManager b;

    public HandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.a = minecraftserver;
        this.b = networkmanager;
    }

    public void a(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        switch (HandshakeListener.SyntheticClass_1.a[packethandshakinginsetprotocol.a().ordinal()]) {
        case 1:
            this.b.a(EnumProtocol.LOGIN);

            ChatComponentText chatcomponenttext;

            try {
                long i = System.currentTimeMillis();
                long j = MinecraftServer.getServer().server.getConnectionThrottle();
                InetAddress inetaddress = ((InetSocketAddress) this.b.getSocketAddress()).getAddress();
                HashMap hashmap = HandshakeListener.throttleTracker;

                synchronized (HandshakeListener.throttleTracker) {
                    if (HandshakeListener.throttleTracker.containsKey(inetaddress) && !"127.0.0.1".equals(inetaddress.getHostAddress()) && i - ((Long) HandshakeListener.throttleTracker.get(inetaddress)).longValue() < j) {
                        HandshakeListener.throttleTracker.put(inetaddress, Long.valueOf(i));
                        chatcomponenttext = new ChatComponentText("Connection throttled! Please wait before reconnecting.");
                        this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                        this.b.close(chatcomponenttext);
                        return;
                    }

                    HandshakeListener.throttleTracker.put(inetaddress, Long.valueOf(i));
                    ++HandshakeListener.throttleCounter;
                    if (HandshakeListener.throttleCounter > 200) {
                        HandshakeListener.throttleCounter = 0;
                        Iterator iterator = HandshakeListener.throttleTracker.entrySet().iterator();

                        while (iterator.hasNext()) {
                            Entry entry = (Entry) iterator.next();

                            if (((Long) entry.getValue()).longValue() > j) {
                                iterator.remove();
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                LogManager.getLogger().debug("Failed to check connection throttle", throwable);
            }

            if (packethandshakinginsetprotocol.b() > 47) {
                chatcomponenttext = new ChatComponentText(MessageFormat.format(SpigotConfig.outdatedServerMessage, new Object[] { "1.8.6"}));
                this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                this.b.close(chatcomponenttext);
            } else if (packethandshakinginsetprotocol.b() < 47) {
                chatcomponenttext = new ChatComponentText(MessageFormat.format(SpigotConfig.outdatedClientMessage, new Object[] { "1.8.6"}));
                this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                this.b.close(chatcomponenttext);
            } else {
                this.b.a((PacketListener) (new LoginListener(this.a, this.b)));
                if (SpigotConfig.bungee) {
                    String[] astring = packethandshakinginsetprotocol.b.split("\u0000");

                    if (astring.length != 3 && astring.length != 4) {
                        chatcomponenttext = new ChatComponentText("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                        this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                        this.b.close(chatcomponenttext);
                        return;
                    }

                    packethandshakinginsetprotocol.b = astring[0];
                    this.b.l = new InetSocketAddress(astring[1], ((InetSocketAddress) this.b.getSocketAddress()).getPort());
                    this.b.spoofedUUID = UUIDTypeAdapter.fromString(astring[2]);
                    if (astring.length == 4) {
                        this.b.spoofedProfile = (Property[]) HandshakeListener.gson.fromJson(astring[3], Property[].class);
                    }
                }

                ((LoginListener) this.b.getPacketListener()).hostname = packethandshakinginsetprotocol.b + ":" + packethandshakinginsetprotocol.c;
            }
            break;

        case 2:
            this.b.a(EnumProtocol.STATUS);
            this.b.a((PacketListener) (new PacketStatusListener(this.a, this.b)));
            break;

        default:
            throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.a());
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {}

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumProtocol.values().length];

        static {
            try {
                HandshakeListener.SyntheticClass_1.a[EnumProtocol.LOGIN.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                HandshakeListener.SyntheticClass_1.a[EnumProtocol.STATUS.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

        }

        SyntheticClass_1() {}
    }
}
