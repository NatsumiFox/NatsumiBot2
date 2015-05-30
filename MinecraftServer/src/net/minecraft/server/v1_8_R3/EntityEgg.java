package net.minecraft.server.v1_8_R3;

import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class EntityEgg extends EntityProjectile {

    public EntityEgg(World world) {
        super(world);
    }

    public EntityEgg(World world, EntityLiving entityliving) {
        super(world, entityliving);
    }

    public EntityEgg(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null) {
            movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0.0F);
        }

        boolean flag = !this.world.isClientSide && this.random.nextInt(8) == 0;
        int i = this.random.nextInt(32) == 0 ? 4 : 1;

        if (!flag) {
            i = 0;
        }

        EntityType entitytype = EntityType.CHICKEN;
        EntityLiving entityliving = this.getShooter();

        if (entityliving instanceof EntityPlayer) {
            Player player = entityliving == null ? null : (Player) entityliving.getBukkitEntity();
            PlayerEggThrowEvent playereggthrowevent = new PlayerEggThrowEvent(player, (Egg) this.getBukkitEntity(), flag, (byte) i, entitytype);

            this.world.getServer().getPluginManager().callEvent(playereggthrowevent);
            flag = playereggthrowevent.isHatching();
            i = playereggthrowevent.getNumHatches();
            entitytype = playereggthrowevent.getHatchingType();
        }

        int j;

        if (flag) {
            for (j = 0; j < i; ++j) {
                org.bukkit.entity.Entity org_bukkit_entity_entity = this.world.getWorld().spawn(new Location(this.world.getWorld(), this.locX, this.locY, this.locZ, this.yaw, 0.0F), entitytype.getEntityClass(), SpawnReason.EGG);

                if (org_bukkit_entity_entity instanceof Ageable) {
                    ((Ageable) org_bukkit_entity_entity).setBaby();
                }
            }
        }

        for (j = 0; j < 8; ++j) {
            this.world.addParticle(EnumParticle.ITEM_CRACK, this.locX, this.locY, this.locZ, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, new int[] { Item.getId(Items.EGG)});
        }

        if (!this.world.isClientSide) {
            this.die();
        }

    }
}
