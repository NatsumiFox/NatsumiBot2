package net.minecraft.server.v1_8_R3;

import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

public class ItemHanging extends Item {

    private final Class<? extends EntityHanging> a;

    public ItemHanging(Class<? extends EntityHanging> oclass) {
        this.a = oclass;
        this.a(CreativeModeTab.c);
    }

    public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        if (enumdirection == EnumDirection.DOWN) {
            return false;
        } else if (enumdirection == EnumDirection.UP) {
            return false;
        } else {
            BlockPosition blockposition1 = blockposition.shift(enumdirection);

            if (!entityhuman.a(blockposition1, enumdirection, itemstack)) {
                return false;
            } else {
                EntityHanging entityhanging = this.a(world, blockposition1, enumdirection);

                if (entityhanging != null && entityhanging.survives()) {
                    if (!world.isClientSide) {
                        Player player = entityhuman == null ? null : (Player) entityhuman.getBukkitEntity();
                        org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                        BlockFace blockface = CraftBlock.notchToBlockFace(enumdirection);
                        HangingPlaceEvent hangingplaceevent = new HangingPlaceEvent((Hanging) entityhanging.getBukkitEntity(), player, org_bukkit_block_block, blockface);

                        world.getServer().getPluginManager().callEvent(hangingplaceevent);
                        PaintingPlaceEvent paintingplaceevent = null;

                        if (entityhanging instanceof EntityPainting) {
                            paintingplaceevent = new PaintingPlaceEvent((Painting) entityhanging.getBukkitEntity(), player, org_bukkit_block_block, blockface);
                            paintingplaceevent.setCancelled(hangingplaceevent.isCancelled());
                            world.getServer().getPluginManager().callEvent(paintingplaceevent);
                        }

                        if (hangingplaceevent.isCancelled() || paintingplaceevent != null && paintingplaceevent.isCancelled()) {
                            return false;
                        }

                        world.addEntity(entityhanging);
                    }

                    --itemstack.count;
                }

                return true;
            }
        }
    }

    private EntityHanging a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        return (EntityHanging) (this.a == EntityPainting.class ? new EntityPainting(world, blockposition, enumdirection) : (this.a == EntityItemFrame.class ? new EntityItemFrame(world, blockposition, enumdirection) : null));
    }
}
