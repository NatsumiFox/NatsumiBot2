package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class ItemMinecart extends Item {

    private static final IDispenseBehavior a = new DispenseBehaviorItem() {
        private final DispenseBehaviorItem b = new DispenseBehaviorItem();

        public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
            EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
            World world = isourceblock.i();
            double d0 = isourceblock.getX() + (double) enumdirection.getAdjacentX() * 1.125D;
            double d1 = Math.floor(isourceblock.getY()) + (double) enumdirection.getAdjacentY();
            double d2 = isourceblock.getZ() + (double) enumdirection.getAdjacentZ() * 1.125D;
            BlockPosition blockposition = isourceblock.getBlockPosition().shift(enumdirection);
            IBlockData iblockdata = world.getType(blockposition);
            BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition = iblockdata.getBlock() instanceof BlockMinecartTrackAbstract ? (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(((BlockMinecartTrackAbstract) iblockdata.getBlock()).n()) : BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH;
            double d3;

            if (BlockMinecartTrackAbstract.d(iblockdata)) {
                if (blockminecarttrackabstract_enumtrackposition.c()) {
                    d3 = 0.6D;
                } else {
                    d3 = 0.1D;
                }
            } else {
                if (iblockdata.getBlock().getMaterial() != Material.AIR || !BlockMinecartTrackAbstract.d(world.getType(blockposition.down()))) {
                    return this.b.a(isourceblock, itemstack);
                }

                IBlockData iblockdata1 = world.getType(blockposition.down());
                BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition1 = iblockdata1.getBlock() instanceof BlockMinecartTrackAbstract ? (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata1.get(((BlockMinecartTrackAbstract) iblockdata1.getBlock()).n()) : BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH;

                if (enumdirection != EnumDirection.DOWN && blockminecarttrackabstract_enumtrackposition1.c()) {
                    d3 = -0.4D;
                } else {
                    d3 = -0.9D;
                }
            }

            ItemStack itemstack1 = itemstack.a(1);
            org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
            CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
            BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(d0, d1 + d3, d2));

            if (!BlockDispenser.eventFired) {
                world.getServer().getPluginManager().callEvent(blockdispenseevent);
            }

            if (blockdispenseevent.isCancelled()) {
                ++itemstack.count;
                return itemstack;
            } else {
                if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                    ++itemstack.count;
                    ItemStack itemstack2 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                    IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack2.getItem());

                    if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                        idispensebehavior.a(isourceblock, itemstack2);
                        return itemstack;
                    }
                }

                itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                EntityMinecartAbstract entityminecartabstract = EntityMinecartAbstract.a(world, blockdispenseevent.getVelocity().getX(), blockdispenseevent.getVelocity().getY(), blockdispenseevent.getVelocity().getZ(), ((ItemMinecart) itemstack1.getItem()).b);

                if (itemstack.hasName()) {
                    entityminecartabstract.setCustomName(itemstack.getName());
                }

                world.addEntity(entityminecartabstract);
                return itemstack;
            }
        }

        protected void a(ISourceBlock isourceblock) {
            isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
        }
    };
    private final EntityMinecartAbstract.EnumMinecartType b;

    public ItemMinecart(EntityMinecartAbstract.EnumMinecartType entityminecartabstract_enumminecarttype) {
        this.maxStackSize = 1;
        this.b = entityminecartabstract_enumminecarttype;
        this.a(CreativeModeTab.e);
        BlockDispenser.N.a(this, ItemMinecart.a);
    }

    public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        IBlockData iblockdata = world.getType(blockposition);

        if (BlockMinecartTrackAbstract.d(iblockdata)) {
            if (!world.isClientSide) {
                BlockMinecartTrackAbstract.EnumTrackPosition blockminecarttrackabstract_enumtrackposition = iblockdata.getBlock() instanceof BlockMinecartTrackAbstract ? (BlockMinecartTrackAbstract.EnumTrackPosition) iblockdata.get(((BlockMinecartTrackAbstract) iblockdata.getBlock()).n()) : BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH;
                double d0 = 0.0D;

                if (blockminecarttrackabstract_enumtrackposition.c()) {
                    d0 = 0.5D;
                }

                PlayerInteractEvent playerinteractevent = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, blockposition, enumdirection, itemstack);

                if (playerinteractevent.isCancelled()) {
                    return false;
                }

                EntityMinecartAbstract entityminecartabstract = EntityMinecartAbstract.a(world, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.0625D + d0, (double) blockposition.getZ() + 0.5D, this.b);

                if (itemstack.hasName()) {
                    entityminecartabstract.setCustomName(itemstack.getName());
                }

                world.addEntity(entityminecartabstract);
            }

            --itemstack.count;
            return true;
        } else {
            return false;
        }
    }
}
