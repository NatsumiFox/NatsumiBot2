package net.minecraft.server.v1_8_R3;

public interface PacketLoginInListener extends PacketListener {

    void a(PacketLoginInStart packetlogininstart);

    void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin);
}
