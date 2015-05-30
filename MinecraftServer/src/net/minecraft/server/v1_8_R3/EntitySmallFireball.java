package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

public class EntitySmallFireball extends EntityFireball {

    public EntitySmallFireball(World world) {
        super(world);
        this.setSize(0.3125F, 0.3125F);
    }

    public EntitySmallFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
        this.setSize(0.3125F, 0.3125F);
    }

    public EntitySmallFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
        super(world, d0, d1, d2, d3, d4, d5);
        this.setSize(0.3125F, 0.3125F);
    }

    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isClientSide) {
            boolean flag;

            if (movingobjectposition.entity != null) {
                flag = movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), 5.0F);
                if (flag) {
                    this.a(this.shooter, movingobjectposition.entity);
                    if (!movingobjectposition.entity.isFireProof()) {
                        EntityCombustByEntityEvent entitycombustbyentityevent = new EntityCombustByEntityEvent((Projectile) this.getBukkitEntity(), movingobjectposition.entity.getBukkitEntity(), 5);

                        movingobjectposition.entity.world.getServer().getPluginManager().callEvent(entitycombustbyentityevent);
                        if (!entitycombustbyentityevent.isCancelled()) {
                            movingobjectposition.entity.setOnFire(entitycombustbyentityevent.getDuration());
                        }
                    }
                }
            } else {
                flag = true;
                if (this.shooter != null && this.shooter instanceof EntityInsentient) {
                    flag = this.world.getGameRules().getBoolean("mobGriefing");
                }

                if (flag) {
                    BlockPosition blockposition = movingobjectposition.a().shift(movingobjectposition.direction);

                    if (this.world.isEmpty(blockposition) && this.isIncendiary && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
                        this.world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                    }
                }
            }

            this.die();
        }

    }

    public boolean ad() {
        return false;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }
}