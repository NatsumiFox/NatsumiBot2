package net.minecraft.server.v1_8_R3;

import java.io.IOException;

public interface Packet<T extends PacketListener> {

    void a(PacketDataSerializer packetdataserializer) throws IOException;

    void b(PacketDataSerializer packetdataserializer) throws IOException;

    void a(T t0);
}
