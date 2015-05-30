package net.minecraft.server.v1_8_R3;

public interface PacketStatusInListener extends PacketListener {

    void a(PacketStatusInPing packetstatusinping);

    void a(PacketStatusInStart packetstatusinstart);
}
