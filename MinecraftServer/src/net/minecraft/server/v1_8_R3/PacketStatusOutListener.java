package net.minecraft.server.v1_8_R3;

public interface PacketStatusOutListener extends PacketListener {

    void a(PacketStatusOutServerInfo packetstatusoutserverinfo);

    void a(PacketStatusOutPong packetstatusoutpong);
}
