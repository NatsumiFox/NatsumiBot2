package net.minecraft.server.v1_8_R3;

import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class ItemFishingRod extends Item {

    public ItemFishingRod() {
        this.setMaxDurability(64);
        this.c(1);
        this.a(CreativeModeTab.i);
    }

    public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
        if (entityhuman.hookedFish != null) {
            int i = entityhuman.hookedFish.l();

            itemstack.damage(i, entityhuman);
            entityhuman.bw();
        } else {
            EntityFishingHook entityfishinghook = new EntityFishingHook(world, entityhuman);
            PlayerFishEvent playerfishevent = new PlayerFishEvent((Player) entityhuman.getBukkitEntity(), (org.bukkit.entity.Entity) null, (Fish) entityfishinghook.getBukkitEntity(), State.FISHING);

            world.getServer().getPluginManager().callEvent(playerfishevent);
            if (playerfishevent.isCancelled()) {
                entityhuman.hookedFish = null;
                return itemstack;
            }

            world.makeSound(entityhuman, "random.bow", 0.5F, 0.4F / (ItemFishingRod.g.nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                world.addEntity(entityfishinghook);
            }

            entityhuman.bw();
            entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
        }

        return itemstack;
    }

    public boolean f_(ItemStack itemstack) {
        return super.f_(itemstack);
    }

    public int b() {
        return 1;
    }
}
