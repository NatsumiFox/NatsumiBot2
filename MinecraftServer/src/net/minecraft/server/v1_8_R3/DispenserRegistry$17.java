package net.minecraft.server.v1_8_R3;

final class DispenserRegistry$17 extends DispenseBehaviorProjectile {

    DispenserRegistry$17() {}

    protected IProjectile a(World world, IPosition iposition) {
        return new EntityEgg(world, iposition.getX(), iposition.getY(), iposition.getZ());
    }
}
