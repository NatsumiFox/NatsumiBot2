package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.projectiles.CraftBlockProjectileSource;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public abstract class DispenseBehaviorProjectile extends DispenseBehaviorItem {

    public DispenseBehaviorProjectile() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        World world = isourceblock.i();
        IPosition iposition = BlockDispenser.a(isourceblock);
        EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
        IProjectile iprojectile = this.a(world, iposition);
        ItemStack itemstack1 = itemstack.a(1);
        org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
        CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
        BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector((double) enumdirection.getAdjacentX(), (double) ((float) enumdirection.getAdjacentY() + 0.1F), (double) enumdirection.getAdjacentZ()));

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

            iprojectile.shoot(blockdispenseevent.getVelocity().getX(), blockdispenseevent.getVelocity().getY(), blockdispenseevent.getVelocity().getZ(), this.b(), this.a());
            ((Entity) iprojectile).projectileSource = new CraftBlockProjectileSource((TileEntityDispenser) isourceblock.getTileEntity());
            world.addEntity((Entity) iprojectile);
            return itemstack;
        }
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.i().triggerEffect(1002, isourceblock.getBlockPosition(), 0);
    }

    protected abstract IProjectile a(World world, IPosition iposition);

    protected float a() {
        return 6.0F;
    }

    protected float b() {
        return 1.1F;
    }
}
