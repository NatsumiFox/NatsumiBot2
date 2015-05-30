package net.minecraft.server.v1_8_R3;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

public class EntityBoat extends Entity {

    private boolean a;
    private double b;
    private int c;
    private double d;
    private double e;
    private double f;
    private double g;
    private double h;
    public double maxSpeed;
    public double occupiedDeceleration;
    public double unoccupiedDeceleration;
    public boolean landBoats;

    public void collide(Entity entity) {
        CraftEntity craftentity = entity == null ? null : entity.getBukkitEntity();
        VehicleEntityCollisionEvent vehicleentitycollisionevent = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), craftentity);

        this.world.getServer().getPluginManager().callEvent(vehicleentitycollisionevent);
        if (!vehicleentitycollisionevent.isCancelled()) {
            super.collide(entity);
        }
    }

    public EntityBoat(World world) {
        super(world);
        this.maxSpeed = 0.4D;
        this.occupiedDeceleration = 0.2D;
        this.unoccupiedDeceleration = -1.0D;
        this.landBoats = false;
        this.a = true;
        this.b = 0.07D;
        this.k = true;
        this.setSize(1.5F, 0.6F);
    }

    protected boolean s_() {
        return false;
    }

    protected void h() {
        this.datawatcher.a(17, new Integer(0));
        this.datawatcher.a(18, new Integer(1));
        this.datawatcher.a(19, new Float(0.0F));
    }

    public AxisAlignedBB j(Entity entity) {
        return entity.getBoundingBox();
    }

    public AxisAlignedBB S() {
        return this.getBoundingBox();
    }

    public boolean ae() {
        return true;
    }

    public EntityBoat(World world, double d0, double d1, double d2) {
        this(world);
        this.setPosition(d0, d1, d2);
        this.motX = 0.0D;
        this.motY = 0.0D;
        this.motZ = 0.0D;
        this.lastX = d0;
        this.lastY = d1;
        this.lastZ = d2;
        this.world.getServer().getPluginManager().callEvent(new VehicleCreateEvent((Vehicle) this.getBukkitEntity()));
    }

    public double an() {
        return -0.3D;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (!this.world.isClientSide && !this.dead) {
            if (this.passenger != null && this.passenger == damagesource.getEntity() && damagesource instanceof EntityDamageSourceIndirect) {
                return false;
            } else {
                Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                CraftEntity craftentity = damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity();
                VehicleDamageEvent vehicledamageevent = new VehicleDamageEvent(vehicle, craftentity, (double) f);

                this.world.getServer().getPluginManager().callEvent(vehicledamageevent);
                if (vehicledamageevent.isCancelled()) {
                    return true;
                } else {
                    this.b(-this.m());
                    this.a(10);
                    this.setDamage(this.j() + f * 10.0F);
                    this.ac();
                    boolean flag = damagesource.getEntity() instanceof EntityHuman && ((EntityHuman) damagesource.getEntity()).abilities.canInstantlyBuild;

                    if (flag || this.j() > 40.0F) {
                        VehicleDestroyEvent vehicledestroyevent = new VehicleDestroyEvent(vehicle, craftentity);

                        this.world.getServer().getPluginManager().callEvent(vehicledestroyevent);
                        if (vehicledestroyevent.isCancelled()) {
                            this.setDamage(40.0F);
                            return true;
                        }

                        if (this.passenger != null) {
                            this.passenger.mount(this);
                        }

                        if (!flag && this.world.getGameRules().getBoolean("doEntityDrops")) {
                            this.a(Items.BOAT, 1, 0.0F);
                        }

                        this.die();
                    }

                    return true;
                }
            }
        } else {
            return true;
        }
    }

    public boolean ad() {
        return !this.dead;
    }

    public void t_() {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;
        float f = this.yaw;
        float f1 = this.pitch;

        super.t_();
        if (this.l() > 0) {
            this.a(this.l() - 1);
        }

        if (this.j() > 0.0F) {
            this.setDamage(this.j() - 1.0F);
        }

        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        byte b0 = 5;
        double d3 = 0.0D;

        for (int i = 0; i < b0; ++i) {
            double d4 = this.getBoundingBox().b + (this.getBoundingBox().e - this.getBoundingBox().b) * (double) (i + 0) / (double) b0 - 0.125D;
            double d5 = this.getBoundingBox().b + (this.getBoundingBox().e - this.getBoundingBox().b) * (double) (i + 1) / (double) b0 - 0.125D;
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(this.getBoundingBox().a, d4, this.getBoundingBox().c, this.getBoundingBox().d, d5, this.getBoundingBox().f);

            if (this.world.b(axisalignedbb, Material.WATER)) {
                d3 += 1.0D / (double) b0;
            }
        }

        double d6 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
        double d7;
        double d8;
        int j;
        double d9;
        double d10;

        if (d6 > 0.2975D) {
            d7 = Math.cos((double) this.yaw * 3.141592653589793D / 180.0D);
            d8 = Math.sin((double) this.yaw * 3.141592653589793D / 180.0D);

            for (j = 0; (double) j < 1.0D + d6 * 60.0D; ++j) {
                d9 = (double) (this.random.nextFloat() * 2.0F - 1.0F);
                d10 = (double) (this.random.nextInt(2) * 2 - 1) * 0.7D;
                double d11;
                double d12;

                if (this.random.nextBoolean()) {
                    d11 = this.locX - d7 * d9 * 0.8D + d8 * d10;
                    d12 = this.locZ - d8 * d9 * 0.8D - d7 * d10;
                    this.world.addParticle(EnumParticle.WATER_SPLASH, d11, this.locY - 0.125D, d12, this.motX, this.motY, this.motZ, new int[0]);
                } else {
                    d11 = this.locX + d7 + d8 * d9 * 0.7D;
                    d12 = this.locZ + d8 - d7 * d9 * 0.7D;
                    this.world.addParticle(EnumParticle.WATER_SPLASH, d11, this.locY - 0.125D, d12, this.motX, this.motY, this.motZ, new int[0]);
                }
            }
        }

        if (this.world.isClientSide && this.a) {
            if (this.c > 0) {
                d7 = this.locX + (this.d - this.locX) / (double) this.c;
                d8 = this.locY + (this.e - this.locY) / (double) this.c;
                d9 = this.locZ + (this.f - this.locZ) / (double) this.c;
                d10 = MathHelper.g(this.g - (double) this.yaw);
                this.yaw = (float) ((double) this.yaw + d10 / (double) this.c);
                this.pitch = (float) ((double) this.pitch + (this.h - (double) this.pitch) / (double) this.c);
                --this.c;
                this.setPosition(d7, d8, d9);
                this.setYawPitch(this.yaw, this.pitch);
            } else {
                d7 = this.locX + this.motX;
                d8 = this.locY + this.motY;
                d9 = this.locZ + this.motZ;
                this.setPosition(d7, d8, d9);
                if (this.onGround) {
                    this.motX *= 0.5D;
                    this.motY *= 0.5D;
                    this.motZ *= 0.5D;
                }

                this.motX *= 0.9900000095367432D;
                this.motY *= 0.949999988079071D;
                this.motZ *= 0.9900000095367432D;
            }
        } else {
            if (d3 < 1.0D) {
                d7 = d3 * 2.0D - 1.0D;
                this.motY += 0.03999999910593033D * d7;
            } else {
                if (this.motY < 0.0D) {
                    this.motY /= 2.0D;
                }

                this.motY += 0.007000000216066837D;
            }

            if (this.passenger instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) this.passenger;
                float f2 = this.passenger.yaw + -entityliving.aZ * 90.0F;

                this.motX += -Math.sin((double) (f2 * 3.1415927F / 180.0F)) * this.b * (double) entityliving.ba * 0.05000000074505806D;
                this.motZ += Math.cos((double) (f2 * 3.1415927F / 180.0F)) * this.b * (double) entityliving.ba * 0.05000000074505806D;
            } else if (this.unoccupiedDeceleration >= 0.0D) {
                this.motX *= this.unoccupiedDeceleration;
                this.motZ *= this.unoccupiedDeceleration;
                if (this.motX <= 1.0E-5D) {
                    this.motX = 0.0D;
                }

                if (this.motZ <= 1.0E-5D) {
                    this.motZ = 0.0D;
                }
            }

            d7 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            if (d7 > 0.35D) {
                d8 = 0.35D / d7;
                this.motX *= d8;
                this.motZ *= d8;
                d7 = 0.35D;
            }

            if (d7 > d6 && this.b < 0.35D) {
                this.b += (0.35D - this.b) / 35.0D;
                if (this.b > 0.35D) {
                    this.b = 0.35D;
                }
            } else {
                this.b -= (this.b - 0.07D) / 35.0D;
                if (this.b < 0.07D) {
                    this.b = 0.07D;
                }
            }

            int k;

            for (k = 0; k < 4; ++k) {
                int l = MathHelper.floor(this.locX + ((double) (k % 2) - 0.5D) * 0.8D);

                j = MathHelper.floor(this.locZ + ((double) (k / 2) - 0.5D) * 0.8D);

                for (int i1 = 0; i1 < 2; ++i1) {
                    int j1 = MathHelper.floor(this.locY) + i1;
                    BlockPosition blockposition = new BlockPosition(l, j1, j);
                    Block block = this.world.getType(blockposition).getBlock();

                    if (block == Blocks.SNOW_LAYER) {
                        if (!CraftEventFactory.callEntityChangeBlockEvent(this, l, j1, j, Blocks.AIR, 0).isCancelled()) {
                            this.world.setAir(blockposition);
                            this.positionChanged = false;
                        }
                    } else if (block == Blocks.WATERLILY && !CraftEventFactory.callEntityChangeBlockEvent(this, l, j1, j, Blocks.AIR, 0).isCancelled()) {
                        this.world.setAir(blockposition, true);
                        this.positionChanged = false;
                    }
                }
            }

            if (this.onGround && !this.landBoats) {
                this.motX *= 0.5D;
                this.motY *= 0.5D;
                this.motZ *= 0.5D;
            }

            this.move(this.motX, this.motY, this.motZ);
            if (this.positionChanged && d6 > 0.2975D) {
                if (!this.world.isClientSide && !this.dead) {
                    Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                    VehicleDestroyEvent vehicledestroyevent = new VehicleDestroyEvent(vehicle, (org.bukkit.entity.Entity) null);

                    this.world.getServer().getPluginManager().callEvent(vehicledestroyevent);
                    if (!vehicledestroyevent.isCancelled()) {
                        this.die();
                        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                            for (k = 0; k < 3; ++k) {
                                this.a(Item.getItemOf(Blocks.PLANKS), 1, 0.0F);
                            }

                            for (k = 0; k < 2; ++k) {
                                this.a(Items.STICK, 1, 0.0F);
                            }
                        }
                    }
                }
            } else {
                this.motX *= 0.9900000095367432D;
                this.motY *= 0.949999988079071D;
                this.motZ *= 0.9900000095367432D;
            }

            this.pitch = 0.0F;
            d8 = (double) this.yaw;
            d9 = this.lastX - this.locX;
            d10 = this.lastZ - this.locZ;
            if (d9 * d9 + d10 * d10 > 0.001D) {
                d8 = (double) ((float) (MathHelper.b(d10, d9) * 180.0D / 3.141592653589793D));
            }

            double d13 = MathHelper.g(d8 - (double) this.yaw);

            if (d13 > 20.0D) {
                d13 = 20.0D;
            }

            if (d13 < -20.0D) {
                d13 = -20.0D;
            }

            this.yaw = (float) ((double) this.yaw + d13);
            this.setYawPitch(this.yaw, this.pitch);
            CraftServer craftserver = this.world.getServer();
            CraftWorld craftworld = this.world.getWorld();
            Location location = new Location(craftworld, d0, d1, d2, f, f1);
            Location location1 = new Location(craftworld, this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            Vehicle vehicle1 = (Vehicle) this.getBukkitEntity();

            craftserver.getPluginManager().callEvent(new VehicleUpdateEvent(vehicle1));
            if (!location.equals(location1)) {
                VehicleMoveEvent vehiclemoveevent = new VehicleMoveEvent(vehicle1, location, location1);

                craftserver.getPluginManager().callEvent(vehiclemoveevent);
            }

            if (!this.world.isClientSide) {
                List list = this.world.getEntities(this, this.getBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D));

                if (list != null && !list.isEmpty()) {
                    for (int k1 = 0; k1 < list.size(); ++k1) {
                        Entity entity = (Entity) list.get(k1);

                        if (entity != this.passenger && entity.ae() && entity instanceof EntityBoat) {
                            entity.collide(this);
                        }
                    }
                }

                if (this.passenger != null && this.passenger.dead) {
                    this.passenger.vehicle = null;
                    this.passenger = null;
                }
            }
        }

    }

    public void al() {
        if (this.passenger != null) {
            double d0 = Math.cos((double) this.yaw * 3.141592653589793D / 180.0D) * 0.4D;
            double d1 = Math.sin((double) this.yaw * 3.141592653589793D / 180.0D) * 0.4D;

            this.passenger.setPosition(this.locX + d0, this.locY + this.an() + this.passenger.am(), this.locZ + d1);
        }

    }

    protected void b(NBTTagCompound nbttagcompound) {}

    protected void a(NBTTagCompound nbttagcompound) {}

    public boolean e(EntityHuman entityhuman) {
        if (this.passenger != null && this.passenger instanceof EntityHuman && this.passenger != entityhuman) {
            return true;
        } else {
            if (!this.world.isClientSide) {
                entityhuman.mount(this);
            }

            return true;
        }
    }

    protected void a(double d0, boolean flag, Block block, BlockPosition blockposition) {
        if (flag) {
            if (this.fallDistance > 3.0F) {
                this.e(this.fallDistance, 1.0F);
                if (!this.world.isClientSide && !this.dead) {
                    Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                    VehicleDestroyEvent vehicledestroyevent = new VehicleDestroyEvent(vehicle, (org.bukkit.entity.Entity) null);

                    this.world.getServer().getPluginManager().callEvent(vehicledestroyevent);
                    if (!vehicledestroyevent.isCancelled()) {
                        this.die();
                        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                            int i;

                            for (i = 0; i < 3; ++i) {
                                this.a(Item.getItemOf(Blocks.PLANKS), 1, 0.0F);
                            }

                            for (i = 0; i < 2; ++i) {
                                this.a(Items.STICK, 1, 0.0F);
                            }
                        }
                    }
                }

                this.fallDistance = 0.0F;
            }
        } else if (this.world.getType((new BlockPosition(this)).down()).getBlock().getMaterial() != Material.WATER && d0 < 0.0D) {
            this.fallDistance = (float) ((double) this.fallDistance - d0);
        }

    }

    public void setDamage(float f) {
        this.datawatcher.watch(19, Float.valueOf(f));
    }

    public float j() {
        return this.datawatcher.getFloat(19);
    }

    public void a(int i) {
        this.datawatcher.watch(17, Integer.valueOf(i));
    }

    public int l() {
        return this.datawatcher.getInt(17);
    }

    public void b(int i) {
        this.datawatcher.watch(18, Integer.valueOf(i));
    }

    public int m() {
        return this.datawatcher.getInt(18);
    }
}
