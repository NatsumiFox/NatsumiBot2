package net.minecraft.server.v1_8_R3;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

class ServerConnection$7 implements GenericFutureListener<Future<? super Void>> {

    final NetworkManager a;
    final ChatComponentText b;
    final ServerConnection c;

    ServerConnection$7(ServerConnection serverconnection, NetworkManager networkmanager, ChatComponentText chatcomponenttext) {
        this.c = serverconnection;
        this.a = networkmanager;
        this.b = chatcomponenttext;
    }

    public void operationComplete(Future<? super Void> future) throws Exception {
        this.a.close(this.b);
    }
}
