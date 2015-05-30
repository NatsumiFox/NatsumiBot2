package net.minecraft.server.v1_8_R3;

import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockBloodStone extends Block {

    public BlockBloodStone() {
        super(Material.STONE);
        this.a(CreativeModeTab.b);
    }

    public MaterialMapColor g(IBlockData iblockdata) {
        return MaterialMapColor.K;
    }

    public void doPhysics(World world, BlockPosition blockposition, IBlockData iblockdata, Block block) {
        if (block != null && block.isPowerSource()) {
            org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            int i = org_bukkit_block_block.getBlockPower();
            BlockRedstoneEvent blockredstoneevent = new BlockRedstoneEvent(org_bukkit_block_block, i, i);

            world.getServer().getPluginManager().callEvent(blockredstoneevent);
        }

    }
}
