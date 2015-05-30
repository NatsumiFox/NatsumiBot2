package net.minecraft.server.v1_8_R3;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketPlayInChat implements Packet<PacketListenerPlayIn> {

    private String a;
    private static final ExecutorService executors = Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("Async Chat Thread - #%d").build());

    public PacketPlayInChat() {}

    public PacketPlayInChat(String s) {
        if (s.length() > 100) {
            s = s.substring(0, 100);
        }

        this.a = s;
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.c(100);
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.a(this.a);
    }

    public void a(final PacketListenerPlayIn packetlistenerplayin) {
        if (!this.a.startsWith("/")) {
            PacketPlayInChat.executors.submit(new Runnable() {
                public void run() {
                    packetlistenerplayin.a(PacketPlayInChat.this);
                }
            });
        } else {
            packetlistenerplayin.a(this);
        }
    }

    public String a() {
        return this.a;
    }

    public void a(PacketListener packetlistener) {
        this.a((PacketListenerPlayIn) packetlistener);
    }
}
