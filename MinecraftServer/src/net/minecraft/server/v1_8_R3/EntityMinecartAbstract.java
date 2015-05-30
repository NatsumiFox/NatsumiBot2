package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

public abstract class EntityMinecartAbstract extends Entity implements INamableTileEntity {

    private boolean a;
    private String b;
    private static final int[][][] matrix = new int[][][] { { { 0, 0, -1}, { 0, 0, 1}}, { { -1, 0, 0}, { 1, 0, 0}}, { { -1, -1, 0}, { 1, 0, 0}}, { { -1, 0, 0}, { 1, -1, 0}}, { { 0, 0, -1}, { 0, -1, 1}}, { { 0, -1, -1}, { 0, 0, 1}}, { { 0, 0, 1}, { 1, 0, 0}}, { { 0, 0, 1}, { -1, 0, 0}}, { { 0, 0, -1}, { -1, 0, 0}}, { { 0, 0, -1}, { 1, 0, 0}}};
    private int d;
    private double e;
    private double f;
    private double g;
    private double h;
    private double i;
    public boolean slowWhenEmpty;
    private double derailedX;
    private double derailedY;
    private double derailedZ;
    private double flyingX;
    private double flyingY;
    private double flyingZ;
    public double maxSpeed;

    public EntityMinecartAbstract(World world) {
        super(world);
        this.slowWhenEmpty = true;
        this.derailedX = 0.5D;
        this.derailedY = 0.5D;
        this.derailedZ = 0.5D;
        this.flyingX = 0.95D;
        this.flyingY = 0.95D;
        this.flyingZ = 0.95D;
        this.maxSpeed = 0.4D;
        this.k = true;
        this.setSize(0.98F, 0.7F);
    }

    public static EntityMinecartAbstract a(World world, double d0, double d1, double d2, EntityMinecartAbstract.EnumMinecartType entityminecartabstract_enumminecarttype) {
        switch (EntityMinecartAbstract.SyntheticClass_1.a[entityminecartabstract_enumminecarttype.ordinal()]) {
        case 1:
            return new EntityMinecartChest(world, d0, d1, d2);

        case 2:
            return new EntityMinecartFurnace(world, d0, d1, d2);

        case 3:
            return new EntityMinecartTNT(world, d0, d1, d2);

        case 4:
            return new EntityMinecartMobSpawner(world, d0, d1, d2);

        case 5:
            return new EntityMinecartHopper(world, d0, d1, d2);

        case 6:
            return new EntityMinecartCommandBlock(world, d0, d1, d2);

        default:
            return new EntityMinecartRideable(world, d0, d1, d2);
        }
    }

    protected boolean s_() {
        return false;
    }

    protected void h() {
        this.datawatcher.a(17, new Integer(0));
        this.datawatcher.a(18, new Integer(1));
        this.datawatcher.a(19, new Float(0.0F));
        this.datawatcher.a(20, new Integer(0));
        this.datawatcher.a(21, new Integer(6));
        this.datawatcher.a(22, Byte.valueOf((byte) 0));
    }

    public AxisAlignedBB j(Entity entity) {
        return entity.ae() ? entity.getBoundingBox() : null;
    }

    public AxisAlignedBB S() {
        return null;
    }

    public boolean ae() {
        return true;
    }

    public EntityMinecartAbstract(World world, double d0, double d1, double d2) {
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
        return 0.0D;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!this.world.isClientSide && !this.dead) {
            if (this.isInvulnerable(damagesource)) {
                return false;
            } else {
                Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                CraftEntity craftentity = damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity();
                VehicleDamageEvent vehicledamageevent = new VehicleDamageEvent(vehicle, craftentity, (double) f);

                this.world.getServer().getPluginManager().callEvent(vehicledamageevent);
                if (vehicledamageevent.isCancelled()) {
                    return true;
                } else {
                    f = (float) vehicledamageevent.getDamage();
                    this.k(-this.r());
                    this.j(10);
                    this.ac();
                    this.setDamage(this.getDamage() + f * 10.0F);
                    boolean flag = damagesource.getEntity() instanceof EntityHuman && ((EntityHuman) damagesource.getEntity()).abilities.canInstantlyBuild;

                    if (flag || this.getDamage() > 40.0F) {
                        VehicleDestroyEvent vehicledestroyevent = new VehicleDestroyEvent(vehicle, craftentity);

                        this.world.getServer().getPluginManager().callEvent(vehicledestroyevent);
                        if (vehicledestroyevent.isCancelled()) {
                            this.setDamage(40.0F);
                            return true;
                        }

                        if (this.passenger != null) {
                            this.passenger.mount((Entity) null);
                        }

                        if (flag && !this.hasCustomName()) {
                            this.die();
                        } else {
                            this.a(damagesource);
                        }
                    }

                    return true;
                }
            }
        } else {
            return true;
        }
    }

    public void a(DamageSource damagesource) {
        this.die();
        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
            ItemStack itemstack = new ItemStack(Items.MINECART, 1);

            if (this.b != null) {
                itemstack.c(this.b);
            }

            this.a(itemstack, 0.0F);
        }

    }

    public boolean ad() {
        return !this.dead;
    }

    public void die() {
        super.die();
    }

    public void t_() {
        double d0 = this.locX;
        double d1 = this.locY;
        double d2 = this.locZ;
        float f = this.yaw;
        float f1 = this.pitch;

        if (this.getType() > 0) {
            this.j(this.getType() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.locY < -64.0D) {
            this.O();
        }

        int i;

        if (!this.world.isClientSide && this.world instanceof WorldServer) {
            this.world.methodProfiler.a("portal");
            MinecraftServer minecraftserver = ((WorldServer) this.world).getMinecraftServer();

            i = this.L();
            if (this.ak) {
                if (this.vehicle == null && this.al++ >= i) {
                    this.al = i;
                    this.portalCooldown = this.aq();
                    byte b0;

                    if (this.world.worldProvider.getDimension() == -1) {
                        b0 = 0;
                    } else {
                        b0 = -1;
                    }

                    this.c(b0);
                }

                this.ak = false;
            } else {
                if (this.al > 0) {
                    this.al -= 4;
                }

                if (this.al < 0) {
                    this.al = 0;
                }
            }

            if (this.portalCooldown > 0) {
                --this.portalCooldown;
            }

            this.world.methodProfiler.b();
        }

        double d3;
        double d4;

        if (this.world.isClientSide) {
            if (this.d > 0) {
                double d5 = this.locX + (this.e - this.locX) / (double) this.d;
                double d6 = this.locY + (this.f - this.locY) / (double) this.d;

                d3 = this.locZ + (this.g - this.locZ) / (double) this.d;
                d4 = MathHelper.g(this.h - (double) this.yaw);
                this.yaw = (float) ((double) this.yaw + d4 / (double) this.d);
                this.pitch = (float) ((double) this.pitch + (this.i - (double) this.pitch) / (double) this.d);
                --this.d;
                this.setPosition(d5, d6, d3);
                this.setYawPitch(this.yaw, this.pitch);
            } else {
                this.setPosition(this.locX, this.locY, this.locZ);
                this.setYawPitch(this.yaw, this.pitch);
            }
        } else {
            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            this.motY -= 0.03999999910593033D;
            int j = MathHelper.floor(this.locX);

            i = MathHelper.floor(this.locY);
            int k = MathHelper.floor(this.locZ);

            if (BlockMinecartTrackAbstract.e(this.world, new BlockPosition(j, i - 1, k))) {
                --i;
            }

            BlockPosition blockposition = new BlockPosition(j, i, k);
            IBlockData iblockdata = this.world.getType(blockposition);

            if (BlockMinecartTrackAbstract.d(iblockdata)) {
                this.a(blockposition, iblockdata);
                if (iblockdata.getBlock() == Blocks.ACTIVATOR_RAIL) {
                    this.a(j, i, k, ((Boolean) iblockdata.get(BlockPoweredRail.POWERED)).booleanValue());
                }
            } else {
                this.n();
            }

            this.checkBlockCollisions();
            this.pitch = 0.0F;
            d3 = this.lastX - this.locX;
            d4 = this.lastZ - this.locZ;
            if (d3 * d3 + d4 * d4 > 0.001D) {
                this.yaw = (float) (MathHelper.b(d4, d3) * 180.0D / 3.141592653589793D);
                if (this.a) {
                    this.yaw += 180.0F;
                }
            }

            double d7 = (double) MathHelper.g(this.yaw - this.lastYaw);

            if (d7 < -170.0D || d7 >= 170.0D) {
                this.yaw += 180.0F;
                this.a = !this.a;
            }

            this.setYawPitch(this.yaw, this.pitch);
            CraftWorld craftworld = this.world.getWorld();
            Location location = new Location(craftworld, d0, d1, d2, f, f1);
            Location location1 = new Location(craftworld, this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            Vehicle vehicle = (Vehicle) this.getBukkitEntity();

            this.world.getServer().getPluginManager().callEvent(new VehicleUpdateEvent(vehicle));
            if (!location.equals(location1)) {
                this.world.getServer().getPluginManager().callEvent(new VehicleMoveEvent(vehicle, location, location1));
            }

            Iterator iterator = this.world.getEntities(this, this.getBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D)).iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                if (entity != this.passenger && entity.ae() && entity instanceof EntityMinecartAbstract) {
                    entity.collide(this);
                }
            }

            if (this.passenger != null && this.passenger.dead) {
                if (this.passenger.vehicle == this) {
                    this.passenger.vehicle = null;
                }

                this.passenger = null;
            }

            this.W();
        }

    }

    protected double m() {
        return this.maxSpeed;
    }

    public void a(int i, int j, int k, boolean flag) {}

    protected void n() {
        double d0 = this.m();

        this.motX = MathHelper.a(this.motX, -d0, d0);
        this.motZ = MathHelper.a(this.motZ, -d0, d0);
        if (this.onGround) {
            this.motX *= this.derailedX;
            this.motY *= this.derailedY;
            this.motZ *= this.derailedZ;
        }

        this.move(this.motX, this.motY, this.motZ);
        if (!this.onGround) {
            this.motX *= this.flyingX;
            this.motY *= this.flyingY;
            this.motZ *= this.flyingZ;
        }

    }

    protected void a(BlockPosition blockposition, IBlockData iblockdata) {
        this.fallDistance = 0.0F;
        Vec3D vec3d = this.k(this.locX, this.locY, this.locZ);

        this.locY = (double) blockposition.getY();
        boolean flag = false;
        boolean flag1 = false;
        BlockMinecartTrackAbstract blockminecarttrackabstract = (BlockMinecartTrackAbstract) iblockdata.getBlock();

        if (blockminecarttrackabstract == Blocks.GOLDEN_RAIL) {
            flag = ((Boolean) iblockdata.get(BlockPoweredRail.POWERED)).booleanValue();
            flag1 = !flag;
        }

        BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition = (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(blockminecarttrackabstract.n());

        switch (EntityMinecartAbstract.SyntheticClass_1.b[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
        case 1:
            this.motX -= 0.0078125D;
            ++this.locY;
            break;

        case 2:
            this.motX += 0.0078125D;
            ++this.locY;
            break;

        case 3:
            this.motZ += 0.0078125D;
            ++this.locY;
            break;

        case 4:
            this.motZ -= 0.0078125D;
            ++this.locY;
        }

        int[][] aint = EntityMinecartAbstract.matrix[blockminecarttrackabstract_enumtrackposition.a()];
        double d0 = (double) (aint[1][0] - aint[0][0]);
        double d1 = (double) (aint[1][2] - aint[0][2]);
        double d2 = Math.sqrt(d0 * d0 + d1 * d1);
        double d3 = this.motX * d0 + this.motZ * d1;

        if (d3 < 0.0D) {
            d0 = -d0;
            d1 = -d1;
        }

        double d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);

        if (d4 > 2.0D) {
            d4 = 2.0D;
        }

        this.motX = d4 * d0 / d2;
        this.motZ = d4 * d1 / d2;
        double d5;
        double d6;
        double d7;
        double d8;

        if (this.passenger instanceof EntityLiving) {
            d5 = (double) ((EntityLiving) this.passenger).ba;
            if (d5 > 0.0D) {
                d6 = -Math.sin((double) (this.passenger.yaw * 3.1415927F / 180.0F));
                d7 = Math.cos((double) (this.passenger.yaw * 3.1415927F / 180.0F));
                d8 = this.motX * this.motX + this.motZ * this.motZ;
                if (d8 < 0.01D) {
                    this.motX += d6 * 0.1D;
                    this.motZ += d7 * 0.1D;
                    flag1 = false;
                }
            }
        }

        if (flag1) {
            d5 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            if (d5 < 0.03D) {
                this.motX *= 0.0D;
                this.motY *= 0.0D;
                this.motZ *= 0.0D;
            } else {
                this.motX *= 0.5D;
                this.motY *= 0.0D;
                this.motZ *= 0.5D;
            }
        }

        d5 = 0.0D;
        d6 = (double) blockposition.getX() + 0.5D + (double) aint[0][0] * 0.5D;
        d7 = (double) blockposition.getZ() + 0.5D + (double) aint[0][2] * 0.5D;
        d8 = (double) blockposition.getX() + 0.5D + (double) aint[1][0] * 0.5D;
        double d9 = (double) blockposition.getZ() + 0.5D + (double) aint[1][2] * 0.5D;

        d0 = d8 - d6;
        d1 = d9 - d7;
        double d10;
        double d11;

        if (d0 == 0.0D) {
            this.locX = (double) blockposition.getX() + 0.5D;
            d5 = this.locZ - (double) blockposition.getZ();
        } else if (d1 == 0.0D) {
            this.locZ = (double) blockposition.getZ() + 0.5D;
            d5 = this.locX - (double) blockposition.getX();
        } else {
            d10 = this.locX - d6;
            d11 = this.locZ - d7;
            d5 = (d10 * d0 + d11 * d1) * 2.0D;
        }

        this.locX = d6 + d0 * d5;
        this.locZ = d7 + d1 * d5;
        this.setPosition(this.locX, this.locY, this.locZ);
        d10 = this.motX;
        d11 = this.motZ;
        if (this.passenger != null) {
            d10 *= 0.75D;
            d11 *= 0.75D;
        }

        double d12 = this.m();

        d10 = MathHelper.a(d10, -d12, d12);
        d11 = MathHelper.a(d11, -d12, d12);
        this.move(d10, 0.0D, d11);
        if (aint[0][1] != 0 && MathHelper.floor(this.locX) - blockposition.getX() == aint[0][0] && MathHelper.floor(this.locZ) - blockposition.getZ() == aint[0][2]) {
            this.setPosition(this.locX, this.locY + (double) aint[0][1], this.locZ);
        } else if (aint[1][1] != 0 && MathHelper.floor(this.locX) - blockposition.getX() == aint[1][0] && MathHelper.floor(this.locZ) - blockposition.getZ() == aint[1][2]) {
            this.setPosition(this.locX, this.locY + (double) aint[1][1], this.locZ);
        }

        this.o();
        Vec3D vec3d1 = this.k(this.locX, this.locY, this.locZ);

        if (vec3d1 != null && vec3d != null) {
            double d13 = (vec3d.b - vec3d1.b) * 0.05D;

            d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            if (d4 > 0.0D) {
                this.motX = this.motX / d4 * (d4 + d13);
                this.motZ = this.motZ / d4 * (d4 + d13);
            }

            this.setPosition(this.locX, vec3d1.b, this.locZ);
        }

        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.locZ);

        if (i != blockposition.getX() || j != blockposition.getZ()) {
            d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            this.motX = d4 * (double) (i - blockposition.getX());
            this.motZ = d4 * (double) (j - blockposition.getZ());
        }

        if (flag) {
            double d14 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);

            if (d14 > 0.01D) {
                double d15 = 0.06D;

                this.motX += this.motX / d14 * d15;
                this.motZ += this.motZ / d14 * d15;
            } else if (blockminecarttrackabstract_enumtrackposition == BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST) {
                if (this.world.getType(blockposition.west()).getBlock().isOccluding()) {
                    this.motX = 0.02D;
                } else if (this.world.getType(blockposition.east()).getBlock().isOccluding()) {
                    this.motX = -0.02D;
                }
            } else if (blockminecarttrackabstract_enumtrackposition == BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH) {
                if (this.world.getType(blockposition.north()).getBlock().isOccluding()) {
                    this.motZ = 0.02D;
                } else if (this.world.getType(blockposition.south()).getBlock().isOccluding()) {
                    this.motZ = -0.02D;
                }
            }
        }

    }

    protected void o() {
        if (this.passenger == null && this.slowWhenEmpty) {
            this.motX *= 0.9599999785423279D;
            this.motY *= 0.0D;
            this.motZ *= 0.9599999785423279D;
        } else {
            this.motX *= 0.996999979019165D;
            this.motY *= 0.0D;
            this.motZ *= 0.996999979019165D;
        }

    }

    public void setPosition(double d0, double d1, double d2) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        float f = this.width / 2.0F;
        float f1 = this.length;

        this.a(new AxisAlignedBB(d0 - (double) f, d1, d2 - (double) f, d0 + (double) f, d1 + (double) f1, d2 + (double) f));
    }

    public Vec3D k(double d0, double d1, double d2) {
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1);
        int k = MathHelper.floor(d2);

        if (BlockMinecartTrackAbstract.e(this.world, new BlockPosition(i, j - 1, k))) {
            --j;
        }

        IBlockData iblockdata = this.world.getType(new BlockPosition(i, j, k));

        if (BlockMinecartTrackAbstract.d(iblockdata)) {
            BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition = (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(((BlockMinecartTrackAbstract) iblockdata.getBlock()).n());
            int[][] aint = EntityMinecartAbstract.matrix[blockminecarttrackabstract_enumtrackposition.a()];
            double d3 = 0.0D;
            double d4 = (double) i + 0.5D + (double) aint[0][0] * 0.5D;
            double d5 = (double) j + 0.0625D + (double) aint[0][1] * 0.5D;
            double d6 = (double) k + 0.5D + (double) aint[0][2] * 0.5D;
            double d7 = (double) i + 0.5D + (double) aint[1][0] * 0.5D;
            double d8 = (double) j + 0.0625D + (double) aint[1][1] * 0.5D;
            double d9 = (double) k + 0.5D + (double) aint[1][2] * 0.5D;
            double d10 = d7 - d4;
            double d11 = (d8 - d5) * 2.0D;
            double d12 = d9 - d6;

            if (d10 == 0.0D) {
                d0 = (double) i + 0.5D;
                d3 = d2 - (double) k;
            } else if (d12 == 0.0D) {
                d2 = (double) k + 0.5D;
                d3 = d0 - (double) i;
            } else {
                double d13 = d0 - d4;
                double d14 = d2 - d6;

                d3 = (d13 * d10 + d14 * d12) * 2.0D;
            }

            d0 = d4 + d10 * d3;
            d1 = d5 + d11 * d3;
            d2 = d6 + d12 * d3;
            if (d11 < 0.0D) {
                ++d1;
            }

            if (d11 > 0.0D) {
                d1 += 0.5D;
            }

            return new Vec3D(d0, d1, d2);
        } else {
            return null;
        }
    }

    protected void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.getBoolean("CustomDisplayTile")) {
            int i = nbttagcompound.getInt("DisplayData");
            Block block;

            if (nbttagcompound.hasKeyOfType("DisplayTile", 8)) {
                block = Block.getByName(nbttagcompound.getString("DisplayTile"));
                if (block == null) {
                    this.setDisplayBlock(Blocks.AIR.getBlockData());
                } else {
                    this.setDisplayBlock(block.fromLegacyData(i));
                }
            } else {
                block = Block.getById(nbttagcompound.getInt("DisplayTile"));
                if (block == null) {
                    this.setDisplayBlock(Blocks.AIR.getBlockData());
                } else {
                    this.setDisplayBlock(block.fromLegacyData(i));
                }
            }

            this.SetDisplayBlockOffset(nbttagcompound.getInt("DisplayOffset"));
        }

        if (nbttagcompound.hasKeyOfType("CustomName", 8) && nbttagcompound.getString("CustomName").length() > 0) {
            this.b = nbttagcompound.getString("CustomName");
        }

    }

    protected void b(NBTTagCompound nbttagcompound) {
        if (this.x()) {
            nbttagcompound.setBoolean("CustomDisplayTile", true);
            IBlockData iblockdata = this.getDisplayBlock();
            MinecraftKey minecraftkey = (MinecraftKey) Block.REGISTRY.c(iblockdata.getBlock());

            nbttagcompound.setString("DisplayTile", minecraftkey == null ? "" : minecraftkey.toString());
            nbttagcompound.setInt("DisplayData", iblockdata.getBlock().toLegacyData(iblockdata));
            nbttagcompound.setInt("DisplayOffset", this.getDisplayBlockOffset());
        }

        if (this.b != null && this.b.length() > 0) {
            nbttagcompound.setString("CustomName", this.b);
        }

    }

    public void collide(Entity entity) {
        if (!this.world.isClientSide && !entity.noclip && !this.noclip && entity != this.passenger) {
            Vehicle vehicle = (Vehicle) this.getBukkitEntity();
            CraftEntity craftentity = entity == null ? null : entity.getBukkitEntity();
            VehicleEntityCollisionEvent vehicleentitycollisionevent = new VehicleEntityCollisionEvent(vehicle, craftentity);

            this.world.getServer().getPluginManager().callEvent(vehicleentitycollisionevent);
            if (vehicleentitycollisionevent.isCancelled()) {
                return;
            }

            if (entity instanceof EntityLiving && !(entity instanceof EntityHuman) && !(entity instanceof EntityIronGolem) && this.s() == EntityMinecartAbstract.EnumMinecartType.RIDEABLE && this.motX * this.motX + this.motZ * this.motZ > 0.01D && this.passenger == null && entity.vehicle == null) {
                entity.mount(this);
            }

            double d0 = entity.locX - this.locX;
            double d1 = entity.locZ - this.locZ;
            double d2 = d0 * d0 + d1 * d1;

            if (d2 >= 9.999999747378752E-5D && !vehicleentitycollisionevent.isCollisionCancelled()) {
                d2 = (double) MathHelper.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;

                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.10000000149011612D;
                d1 *= 0.10000000149011612D;
                d0 *= (double) (1.0F - this.U);
                d1 *= (double) (1.0F - this.U);
                d0 *= 0.5D;
                d1 *= 0.5D;
                if (entity instanceof EntityMinecartAbstract) {
                    double d4 = entity.locX - this.locX;
                    double d5 = entity.locZ - this.locZ;
                    Vec3D vec3d = (new Vec3D(d4, 0.0D, d5)).a();
                    Vec3D vec3d1 = (new Vec3D((double) MathHelper.cos(this.yaw * 3.1415927F / 180.0F), 0.0D, (double) MathHelper.sin(this.yaw * 3.1415927F / 180.0F))).a();
                    double d6 = Math.abs(vec3d.b(vec3d1));

                    if (d6 < 0.800000011920929D) {
                        return;
                    }

                    double d7 = entity.motX + this.motX;
                    double d8 = entity.motZ + this.motZ;

                    if (((EntityMinecartAbstract) entity).s() == EntityMinecartAbstract.EnumMinecartType.FURNACE && this.s() != EntityMinecartAbstract.EnumMinecartType.FURNACE) {
                        this.motX *= 0.20000000298023224D;
                        this.motZ *= 0.20000000298023224D;
                        this.g(entity.motX - d0, 0.0D, entity.motZ - d1);
                        entity.motX *= 0.949999988079071D;
                        entity.motZ *= 0.949999988079071D;
                    } else if (((EntityMinecartAbstract) entity).s() != EntityMinecartAbstract.EnumMinecartType.FURNACE && this.s() == EntityMinecartAbstract.EnumMinecartType.FURNACE) {
                        entity.motX *= 0.20000000298023224D;
                        entity.motZ *= 0.20000000298023224D;
                        entity.g(this.motX + d0, 0.0D, this.motZ + d1);
                        this.motX *= 0.949999988079071D;
                        this.motZ *= 0.949999988079071D;
                    } else {
                        d7 /= 2.0D;
                        d8 /= 2.0D;
                        this.motX *= 0.20000000298023224D;
                        this.motZ *= 0.20000000298023224D;
                        this.g(d7 - d0, 0.0D, d8 - d1);
                        entity.motX *= 0.20000000298023224D;
                        entity.motZ *= 0.20000000298023224D;
                        entity.g(d7 + d0, 0.0D, d8 + d1);
                    }
                } else {
                    this.g(-d0, 0.0D, -d1);
                    entity.g(d0 / 4.0D, 0.0D, d1 / 4.0D);
                }
            }
        }

    }

    public void setDamage(float f) {
        this.datawatcher.watch(19, Float.valueOf(f));
    }

    public float getDamage() {
        return this.datawatcher.getFloat(19);
    }

    public void j(int i) {
        this.datawatcher.watch(17, Integer.valueOf(i));
    }

    public int getType() {
        return this.datawatcher.getInt(17);
    }

    public void k(int i) {
        this.datawatcher.watch(18, Integer.valueOf(i));
    }

    public int r() {
        return this.datawatcher.getInt(18);
    }

    public abstract EntityMinecartAbstract.EnumMinecartType s();

    public IBlockData getDisplayBlock() {
        return !this.x() ? this.u() : Block.getByCombinedId(this.getDataWatcher().getInt(20));
    }

    public IBlockData u() {
        return Blocks.AIR.getBlockData();
    }

    public int getDisplayBlockOffset() {
        return !this.x() ? this.w() : this.getDataWatcher().getInt(21);
    }

    public int w() {
        return 6;
    }

    public void setDisplayBlock(IBlockData iblockdata) {
        this.getDataWatcher().watch(20, Integer.valueOf(Block.getCombinedId(iblockdata)));
        this.a(true);
    }

    public void SetDisplayBlockOffset(int i) {
        this.getDataWatcher().watch(21, Integer.valueOf(i));
        this.a(true);
    }

    public boolean x() {
        return this.getDataWatcher().getByte(22) == 1;
    }

    public void a(boolean flag) {
        this.getDataWatcher().watch(22, Byte.valueOf((byte) (flag ? 1 : 0)));
    }

    public void setCustomName(String s) {
        this.b = s;
    }

    public String getName() {
        return this.b != null ? this.b : super.getName();
    }

    public boolean hasCustomName() {
        return this.b != null;
    }

    public String getCustomName() {
        return this.b;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        if (this.hasCustomName()) {
            ChatComponentText chatcomponenttext = new ChatComponentText(this.b);

            chatcomponenttext.getChatModifier().setChatHoverable(this.aQ());
            chatcomponenttext.getChatModifier().setInsertion(this.getUniqueID().toString());
            return chatcomponenttext;
        } else {
            ChatMessage chatmessage = new ChatMessage(this.getName(), new Object[0]);

            chatmessage.getChatModifier().setChatHoverable(this.aQ());
            chatmessage.getChatModifier().setInsertion(this.getUniqueID().toString());
            return chatmessage;
        }
    }

    public Vector getFlyingVelocityMod() {
        return new Vector(this.flyingX, this.flyingY, this.flyingZ);
    }

    public void setFlyingVelocityMod(Vector vector) {
        this.flyingX = vector.getX();
        this.flyingY = vector.getY();
        this.flyingZ = vector.getZ();
    }

    public Vector getDerailedVelocityMod() {
        return new Vector(this.derailedX, this.derailedY, this.derailedZ);
    }

    public void setDerailedVelocityMod(Vector vector) {
        this.derailedX = vector.getX();
        this.derailedY = vector.getY();
        this.derailedZ = vector.getZ();
    }

    public static enum EnumMinecartType {

        RIDEABLE(0, "MinecartRideable"), CHEST(1, "MinecartChest"), FURNACE(2, "MinecartFurnace"), TNT(3, "MinecartTNT"), SPAWNER(4, "MinecartSpawner"), HOPPER(5, "MinecartHopper"), COMMAND_BLOCK(6, "MinecartCommandBlock");

        private static final Map<Integer, EntityMinecartAbstract.EnumMinecartType> h = Maps.newHashMap();
        private final int i;
        private final String j;

        static {
            EntityMinecartAbstract.EnumMinecartType[] aentityminecartabstract_enumminecarttype = values();
            int i = aentityminecartabstract_enumminecarttype.length;

            for (int j = 0; j < i; ++j) {
                EntityMinecartAbstract.EnumMinecartType entityminecartabstract_enumminecarttype = aentityminecartabstract_enumminecarttype[j];

                EntityMinecartAbstract.EnumMinecartType.h.put(Integer.valueOf(entityminecartabstract_enumminecarttype.a()), entityminecartabstract_enumminecarttype);
            }

        }

        private EnumMinecartType(int i, String s) {
            this.i = i;
            this.j = s;
        }

        public int a() {
            return this.i;
        }

        public String b() {
            return this.j;
        }

        public static EntityMinecartAbstract.EnumMinecartType a(int i) {
            EntityMinecartAbstract.EnumMinecartType entityminecartabstract_enumminecarttype = (EntityMinecartAbstract.EnumMinecartType) EntityMinecartAbstract.EnumMinecartType.h.get(Integer.valueOf(i));

            return entityminecartabstract_enumminecarttype == null ? EntityMinecartAbstract.EnumMinecartType.RIDEABLE : entityminecartabstract_enumminecarttype;
        }
    }

    static class SyntheticClass_1 {

        static final int[] a;
        static final int[] b = new int[BlockMinecartTrackAbstract.EnumTrackPosition.values().length];

        static {
            try {
                EntityMinecartAbstract.SyntheticClass_1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            a = new int[EntityMinecartAbstract.EnumMinecartType.values().length];

            try {
                EntityMinecartAbstract.SyntheticClass_1.a[EntityMinecartAbstract.EnumMinecartType.CHEST.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.a[EntityMinecartAbstract.EnumMinecartType.FURNACE.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.a[EntityMinecartAbstract.EnumMinecartType.TNT.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.a[EntityMinecartAbstract.EnumMinecartType.SPAWNER.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.a[EntityMinecartAbstract.EnumMinecartType.HOPPER.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                EntityMinecartAbstract.SyntheticClass_1.a[EntityMinecartAbstract.EnumMinecartType.COMMAND_BLOCK.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

        }

        SyntheticClass_1() {}
    }
}
