package net.minecraft.server.v1_8_R3;

public interface PacketLoginOutListener extends PacketListener {

    void a(PacketLoginOutEncryptionBegin packetloginoutencryptionbegin);

    void a(PacketLoginOutSuccess packetloginoutsuccess);

    void a(PacketLoginOutDisconnect packetloginoutdisconnect);

    void a(PacketLoginOutSetCompression packetloginoutsetcompression);
}
