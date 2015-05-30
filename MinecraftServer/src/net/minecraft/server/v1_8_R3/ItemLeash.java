package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class ItemLeash extends Item {

    public ItemLeash() {
        this.a(CreativeModeTab.i);
    }

    public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        Block block = world.getType(blockposition).getBlock();

        if (block instanceof BlockFence) {
            if (world.isClientSide) {
                return true;
            } else {
                a(entityhuman, world, blockposition);
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean a(EntityHuman entityhuman, World world, BlockPosition blockposition) {
        EntityLeash entityleash = EntityLeash.b(world, blockposition);
        boolean flag = false;
        double d0 = 7.0D;
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        List list = world.a(EntityInsentient.class, new AxisAlignedBB((double) i - d0, (double) j - d0, (double) k - d0, (double) i + d0, (double) j + d0, (double) k + d0));
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityInsentient entityinsentient = (EntityInsentient) iterator.next();

            if (entityinsentient.cc() && entityinsentient.getLeashHolder() == entityhuman) {
                if (entityleash == null) {
                    entityleash = EntityLeash.a(world, blockposition);
                    HangingPlaceEvent hangingplaceevent = new HangingPlaceEvent((Hanging) entityleash.getBukkitEntity(), entityhuman != null ? (Player) entityhuman.getBukkitEntity() : null, world.getWorld().getBlockAt(i, j, k), BlockFace.SELF);

                    world.getServer().getPluginManager().callEvent(hangingplaceevent);
                    if (hangingplaceevent.isCancelled()) {
                        entityleash.die();
                        return false;
                    }
                }

                if (!CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, entityleash, entityhuman).isCancelled()) {
                    entityinsentient.setLeashHolder(entityleash, true);
                    flag = true;
                }
            }
        }

        return flag;
    }
}
