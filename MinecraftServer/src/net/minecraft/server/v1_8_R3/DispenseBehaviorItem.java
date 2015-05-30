package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public class DispenseBehaviorItem implements IDispenseBehavior {

    public DispenseBehaviorItem() {}

    public final ItemStack a(ISourceBlock isourceblock, ItemStack itemstack) {
        ItemStack itemstack1 = this.b(isourceblock, itemstack);

        this.a(isourceblock);
        this.a(isourceblock, BlockDispenser.b(isourceblock.f()));
        return itemstack1;
    }

    protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());

        BlockDispenser.a(isourceblock);
        ItemStack itemstack1 = itemstack.a(1);

        if (!a(isourceblock.i(), itemstack1, 6, enumdirection, isourceblock)) {
            ++itemstack.count;
        }

        return itemstack;
    }

    public static boolean a(World world, ItemStack itemstack, int i, EnumDirection enumdirection, ISourceBlock isourceblock) {
        IPosition iposition = BlockDispenser.a(isourceblock);
        double d0 = iposition.getX();
        double d1 = iposition.getY();
        double d2 = iposition.getZ();

        if (enumdirection.k() == EnumDirection.EnumAxis.Y) {
            d1 -= 0.125D;
        } else {
            d1 -= 0.15625D;
        }

        EntityItem entityitem = new EntityItem(world, d0, d1, d2, itemstack);
        double d3 = world.random.nextDouble() * 0.1D + 0.2D;

        entityitem.motX = (double) enumdirection.getAdjacentX() * d3;
        entityitem.motY = 0.20000000298023224D;
        entityitem.motZ = (double) enumdirection.getAdjacentZ() * d3;
        entityitem.motX += world.random.nextGaussian() * 0.007499999832361937D * (double) i;
        entityitem.motY += world.random.nextGaussian() * 0.007499999832361937D * (double) i;
        entityitem.motZ += world.random.nextGaussian() * 0.007499999832361937D * (double) i;
        org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
        CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack);
        BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(entityitem.motX, entityitem.motY, entityitem.motZ));

        if (!BlockDispenser.eventFired) {
            world.getServer().getPluginManager().callEvent(blockdispenseevent);
        }

        if (blockdispenseevent.isCancelled()) {
            return false;
        } else {
            entityitem.setItemStack(CraftItemStack.asNMSCopy(blockdispenseevent.getItem()));
            entityitem.motX = blockdispenseevent.getVelocity().getX();
            entityitem.motY = blockdispenseevent.getVelocity().getY();
            entityitem.motZ = blockdispenseevent.getVelocity().getZ();
            if (blockdispenseevent.getItem().getType().equals(craftitemstack.getType())) {
                world.addEntity(entityitem);
                return true;
            } else {
                ItemStack itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack1.getItem());

                if (idispensebehavior != IDispenseBehavior.a && idispensebehavior.getClass() != DispenseBehaviorItem.class) {
                    idispensebehavior.a(isourceblock, itemstack1);
                } else {
                    world.addEntity(entityitem);
                }

                return false;
            }
        }
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
    }

    protected void a(ISourceBlock isourceblock, EnumDirection enumdirection) {
        isourceblock.i().triggerEffect(2000, isourceblock.getBlockPosition(), this.a(enumdirection));
    }

    private int a(EnumDirection enumdirection) {
        return enumdirection.getAdjacentX() + 1 + (enumdirection.getAdjacentZ() + 1) * 3;
    }
}
