package net.minecraft.server.v1_8_R3;

class SecondaryWorldServer$1 implements IWorldBorderListener {

    final SecondaryWorldServer a;

    SecondaryWorldServer$1(SecondaryWorldServer secondaryworldserver) {
        this.a = secondaryworldserver;
    }

    public void a(WorldBorder worldborder, double d0) {
        this.a.getWorldBorder().setSize(d0);
    }

    public void a(WorldBorder worldborder, double d0, double d1, long i) {
        this.a.getWorldBorder().transitionSizeBetween(d0, d1, i);
    }

    public void a(WorldBorder worldborder, double d0, double d1) {
        this.a.getWorldBorder().setCenter(d0, d1);
    }

    public void a(WorldBorder worldborder, int i) {
        this.a.getWorldBorder().setWarningTime(i);
    }

    public void b(WorldBorder worldborder, int i) {
        this.a.getWorldBorder().setWarningDistance(i);
    }

    public void b(WorldBorder worldborder, double d0) {
        this.a.getWorldBorder().setDamageAmount(d0);
    }

    public void c(WorldBorder worldborder, double d0) {
        this.a.getWorldBorder().setDamageBuffer(d0);
    }
}
