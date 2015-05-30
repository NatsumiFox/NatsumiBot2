package net.minecraft.server.v1_8_R3;

public interface EntityOwnable {

    String getOwnerUUID();

    Entity getOwner();
}
