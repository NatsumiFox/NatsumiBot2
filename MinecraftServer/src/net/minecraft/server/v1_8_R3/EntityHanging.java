package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Painting;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;

public abstract class EntityHanging extends Entity {

    private int c;
    public BlockPosition blockPosition;
    public EnumDirection direction;

    public EntityHanging(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
    }

    public EntityHanging(World world, BlockPosition blockposition) {
        this(world);
        this.blockPosition = blockposition;
    }

    protected void h() {}

    public void setDirection(EnumDirection enumdirection) {
        Validate.notNull(enumdirection);
        Validate.isTrue(enumdirection.k().c());
        this.direction = enumdirection;
        this.lastYaw = this.yaw = (float) (this.direction.b() * 90);
        this.updateBoundingBox();
    }

    public static AxisAlignedBB calculateBoundingBox(BlockPosition blockposition, EnumDirection enumdirection, int i, int j) {
        double d0 = (double) blockposition.getX() + 0.5D;
        double d1 = (double) blockposition.getY() + 0.5D;
        double d2 = (double) blockposition.getZ() + 0.5D;
        double d3 = i % 32 == 0 ? 0.5D : 0.0D;
        double d4 = j % 32 == 0 ? 0.5D : 0.0D;

        d0 -= (double) enumdirection.getAdjacentX() * 0.46875D;
        d2 -= (double) enumdirection.getAdjacentZ() * 0.46875D;
        d1 += d4;
        EnumDirection enumdirection1 = enumdirection.f();

        d0 += d3 * (double) enumdirection1.getAdjacentX();
        d2 += d3 * (double) enumdirection1.getAdjacentZ();
        double d5 = (double) i;
        double d6 = (double) j;
        double d7 = (double) i;

        if (enumdirection.k() == EnumDirection.EnumAxis.Z) {
            d7 = 1.0D;
        } else {
            d5 = 1.0D;
        }

        d5 /= 32.0D;
        d6 /= 32.0D;
        d7 /= 32.0D;
        return new AxisAlignedBB(d0 - d5, d1 - d6, d2 - d7, d0 + d5, d1 + d6, d2 + d7);
    }

    private void updateBoundingBox() {
        if (this.direction != null) {
            AxisAlignedBB axisalignedbb = calculateBoundingBox(this.blockPosition, this.direction, this.l(), this.m());

            this.locX = (axisalignedbb.a + axisalignedbb.d) / 2.0D;
            this.locY = (axisalignedbb.b + axisalignedbb.e) / 2.0D;
            this.locZ = (axisalignedbb.c + axisalignedbb.f) / 2.0D;
            this.a(axisalignedbb);
        }

    }

    private double a(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    public void t_() {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        if (this.c++ == this.world.spigotConfig.hangingTickFrequency && !this.world.isClientSide) {
            this.c = 0;
            if (!this.dead && !this.survives()) {
                Material material = this.world.getType(new BlockPosition(this)).getBlock().getMaterial();
                RemoveCause removecause;

                if (!material.equals(Material.AIR)) {
                    removecause = RemoveCause.OBSTRUCTION;
                } else {
                    removecause = RemoveCause.PHYSICS;
                }

                HangingBreakEvent hangingbreakevent = new HangingBreakEvent((Hanging) this.getBukkitEntity(), removecause);

                this.world.getServer().getPluginManager().callEvent(hangingbreakevent);
                PaintingBreakEvent paintingbreakevent = null;

                if (this instanceof EntityPainting) {
                    paintingbreakevent = new PaintingBreakEvent((Painting) this.getBukkitEntity(), org.bukkit.event.painting.PaintingBreakEvent.RemoveCause.valueOf(removecause.name()));
                    paintingbreakevent.setCancelled(hangingbreakevent.isCancelled());
                    this.world.getServer().getPluginManager().callEvent(paintingbreakevent);
                }

                if (this.dead || hangingbreakevent.isCancelled() || paintingbreakevent != null && paintingbreakevent.isCancelled()) {
                    return;
                }

                this.die();
                this.b((Entity) null);
            }
        }

    }

    public boolean survives() {
        if (!this.world.getCubes(this, this.getBoundingBox()).isEmpty()) {
            return false;
        } else {
            int i = Math.max(1, this.l() / 16);
            int j = Math.max(1, this.m() / 16);
            BlockPosition blockposition = this.blockPosition.shift(this.direction.opposite());
            EnumDirection enumdirection = this.direction.f();

            for (int k = 0; k < i; ++k) {
                for (int l = 0; l < j; ++l) {
                    BlockPosition blockposition1 = blockposition.shift(enumdirection, k).up(l);
                    Block block = this.world.getType(blockposition1).getBlock();

                    if (!block.getMaterial().isBuildable() && !BlockDiodeAbstract.d(block)) {
                        return false;
                    }
                }
            }

            List list = this.world.getEntities(this, this.getBoundingBox());
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                if (entity instanceof EntityHanging) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean ad() {
        return true;
    }

    public boolean l(Entity entity) {
        return entity instanceof EntityHuman ? this.damageEntity(DamageSource.playerAttack((EntityHuman) entity), 0.0F) : false;
    }

    public EnumDirection getDirection() {
        return this.direction;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            if (!this.dead && !this.world.isClientSide) {
                Object object = new HangingBreakEvent((Hanging) this.getBukkitEntity(), RemoveCause.DEFAULT);
                PaintingBreakByEntityEvent paintingbreakbyentityevent = null;

                if (damagesource.getEntity() != null) {
                    object = new HangingBreakByEntityEvent((Hanging) this.getBukkitEntity(), damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity());
                    if (this instanceof EntityPainting) {
                        paintingbreakbyentityevent = new PaintingBreakByEntityEvent((Painting) this.getBukkitEntity(), damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity());
                    }
                } else if (damagesource.isExplosion()) {
                    object = new HangingBreakEvent((Hanging) this.getBukkitEntity(), RemoveCause.EXPLOSION);
                }

                this.world.getServer().getPluginManager().callEvent((Event) object);
                if (paintingbreakbyentityevent != null) {
                    paintingbreakbyentityevent.setCancelled(((HangingBreakEvent) object).isCancelled());
                    this.world.getServer().getPluginManager().callEvent(paintingbreakbyentityevent);
                }

                if (this.dead || ((HangingBreakEvent) object).isCancelled() || paintingbreakbyentityevent != null && paintingbreakbyentityevent.isCancelled()) {
                    return true;
                }

                this.die();
                this.ac();
                this.b(damagesource.getEntity());
            }

            return true;
        }
    }

    public void move(double d0, double d1, double d2) {
        if (!this.world.isClientSide && !this.dead && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) {
            if (this.dead) {
                return;
            }

            HangingBreakEvent hangingbreakevent = new HangingBreakEvent((Hanging) this.getBukkitEntity(), RemoveCause.PHYSICS);

            this.world.getServer().getPluginManager().callEvent(hangingbreakevent);
            if (this.dead || hangingbreakevent.isCancelled()) {
                return;
            }

            this.die();
            this.b((Entity) null);
        }

    }

    public void g(double d0, double d1, double d2) {}

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("Facing", (byte) this.direction.b());
        nbttagcompound.setInt("TileX", this.getBlockPosition().getX());
        nbttagcompound.setInt("TileY", this.getBlockPosition().getY());
        nbttagcompound.setInt("TileZ", this.getBlockPosition().getZ());
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.blockPosition = new BlockPosition(nbttagcompound.getInt("TileX"), nbttagcompound.getInt("TileY"), nbttagcompound.getInt("TileZ"));
        EnumDirection enumdirection;

        if (nbttagcompound.hasKeyOfType("Direction", 99)) {
            enumdirection = EnumDirection.fromType2(nbttagcompound.getByte("Direction"));
            this.blockPosition = this.blockPosition.shift(enumdirection);
        } else if (nbttagcompound.hasKeyOfType("Facing", 99)) {
            enumdirection = EnumDirection.fromType2(nbttagcompound.getByte("Facing"));
        } else {
            enumdirection = EnumDirection.fromType2(nbttagcompound.getByte("Dir"));
        }

        this.setDirection(enumdirection);
    }

    public abstract int l();

    public abstract int m();

    public abstract void b(Entity entity);

    protected boolean af() {
        return false;
    }

    public void setPosition(double d0, double d1, double d2) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        BlockPosition blockposition = this.blockPosition;

        this.blockPosition = new BlockPosition(d0, d1, d2);
        if (!this.blockPosition.equals(blockposition)) {
            this.updateBoundingBox();
            this.ai = true;
        }

    }

    public BlockPosition getBlockPosition() {
        return this.blockPosition;
    }
}
