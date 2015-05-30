package net.minecraft.server.v1_8_R3;

import java.util.concurrent.Callable;

class MinecraftServer$4 implements Callable<String> {

    final MinecraftServer a;

    MinecraftServer$4(MinecraftServer minecraftserver) {
        this.a = minecraftserver;
    }

    public String a() {
        return MinecraftServer.a(this.a).getPlayerCount() + " / " + MinecraftServer.a(this.a).getMaxPlayers() + "; " + MinecraftServer.a(this.a).v();
    }

    public Object call() throws Exception {
        return this.a();
    }
}
