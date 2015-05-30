package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class ItemFlintAndSteel extends Item {

    public ItemFlintAndSteel() {
        this.maxStackSize = 1;
        this.setMaxDurability(64);
        this.a(CreativeModeTab.i);
    }

    public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        BlockPosition blockposition1 = blockposition;

        blockposition = blockposition.shift(enumdirection);
        if (!entityhuman.a(blockposition, enumdirection, itemstack)) {
            return false;
        } else {
            if (world.getType(blockposition).getBlock().getMaterial() == Material.AIR) {
                if (CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), IgniteCause.FLINT_AND_STEEL, entityhuman).isCancelled()) {
                    itemstack.damage(1, entityhuman);
                    return false;
                }

                CraftBlockState craftblockstate = CraftBlockState.getBlockState(world, blockposition.getX(), blockposition.getY(), blockposition.getZ());

                world.makeSound((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, "fire.ignite", 1.0F, ItemFlintAndSteel.g.nextFloat() * 0.4F + 0.8F);
                world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                BlockPlaceEvent blockplaceevent = CraftEventFactory.callBlockPlaceEvent(world, entityhuman, craftblockstate, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());

                if (blockplaceevent.isCancelled() || !blockplaceevent.canBuild()) {
                    blockplaceevent.getBlockPlaced().setTypeIdAndData(0, (byte) 0, false);
                    return false;
                }
            }

            itemstack.damage(1, entityhuman);
            return true;
        }
    }
}
