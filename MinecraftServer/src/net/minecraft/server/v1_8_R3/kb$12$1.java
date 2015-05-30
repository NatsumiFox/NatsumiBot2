package net.minecraft.server.v1_8_R3;

class kb$12$1 extends DispenseBehaviorProjectile {

    final ItemStack b;
    final Object c;

    kb$12$1(Object object, ItemStack itemstack) {
        this.c = object;
        this.b = itemstack;
    }

    protected IProjectile a(World world, IPosition iposition) {
        return new EntityPotion(world, iposition.getX(), iposition.getY(), iposition.getZ(), this.b.cloneItemStack());
    }

    protected float a() {
        return super.a() * 0.5F;
    }

    protected float b() {
        return super.b() * 1.25F;
    }
}
