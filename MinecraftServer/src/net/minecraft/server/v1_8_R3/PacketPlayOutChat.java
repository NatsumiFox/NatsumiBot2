package net.minecraft.server.v1_8_R3;

import java.io.IOException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class PacketPlayOutChat implements Packet<PacketListenerPlayOut> {

    private IChatBaseComponent a;
    public BaseComponent[] components;
    private byte b;

    public PacketPlayOutChat() {}

    public PacketPlayOutChat(IChatBaseComponent ichatbasecomponent) {
        this(ichatbasecomponent, (byte) 1);
    }

    public PacketPlayOutChat(IChatBaseComponent ichatbasecomponent, byte b0) {
        this.a = ichatbasecomponent;
        this.b = b0;
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.d();
        this.b = packetdataserializer.readByte();
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        if (this.components != null) {
            packetdataserializer.a(ComponentSerializer.toString(this.components));
        } else {
            packetdataserializer.a(this.a);
        }

        packetdataserializer.writeByte(this.b);
    }

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }

    public boolean b() {
        return this.b == 1 || this.b == 2;
    }

    public void a(PacketListener packetlistener) {
        this.a((PacketListenerPlayOut) packetlistener);
    }
}
