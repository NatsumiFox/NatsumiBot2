package net.minecraft.server.v1_8_R3;

import java.util.Random;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockMycel extends Block {

    public static final BlockStateBoolean SNOWY = BlockStateBoolean.of("snowy");

    protected BlockMycel() {
        super(Material.GRASS, MaterialMapColor.z);
        this.j(this.blockStateList.getBlockData().set(BlockMycel.SNOWY, Boolean.valueOf(false)));
        this.a(true);
        this.a(CreativeModeTab.b);
    }

    public IBlockData updateState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        Block block = iblockaccess.getType(blockposition.up()).getBlock();

        return iblockdata.set(BlockMycel.SNOWY, Boolean.valueOf(block == Blocks.SNOW || block == Blocks.SNOW_LAYER));
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            if (world.getLightLevel(blockposition.up()) < 4 && world.getType(blockposition.up()).getBlock().p() > 2) {
                CraftWorld craftworld = world.getWorld();
                org.bukkit.block.BlockState org_bukkit_block_blockstate = craftworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()).getState();

                org_bukkit_block_blockstate.setType(CraftMagicNumbers.getMaterial(Blocks.DIRT));
                BlockFadeEvent blockfadeevent = new BlockFadeEvent(org_bukkit_block_blockstate.getBlock(), org_bukkit_block_blockstate);

                world.getServer().getPluginManager().callEvent(blockfadeevent);
                if (!blockfadeevent.isCancelled()) {
                    org_bukkit_block_blockstate.update(true);
                }
            } else if (world.getLightLevel(blockposition.up()) >= 9) {
                for (int i = 0; i < Math.min(4, Math.max(20, (int) (400.0F / world.growthOdds))); ++i) {
                    BlockPosition blockposition1 = blockposition.a(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    IBlockData iblockdata1 = world.getType(blockposition1);
                    Block block = world.getType(blockposition1.up()).getBlock();

                    if (iblockdata1.getBlock() == Blocks.DIRT && iblockdata1.get(BlockDirt.VARIANT) == BlockDirt.EnumDirtVariant.DIRT && world.getLightLevel(blockposition1.up()) >= 4 && block.p() <= 2) {
                        CraftWorld craftworld1 = world.getWorld();
                        org.bukkit.block.BlockState org_bukkit_block_blockstate1 = craftworld1.getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();

                        org_bukkit_block_blockstate1.setType(CraftMagicNumbers.getMaterial(this));
                        BlockSpreadEvent blockspreadevent = new BlockSpreadEvent(org_bukkit_block_blockstate1.getBlock(), craftworld1.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), org_bukkit_block_blockstate1);

                        world.getServer().getPluginManager().callEvent(blockspreadevent);
                        if (!blockspreadevent.isCancelled()) {
                            org_bukkit_block_blockstate1.update(true);
                        }
                    }
                }
            }
        }

    }

    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Blocks.DIRT.getDropType(Blocks.DIRT.getBlockData().set(BlockDirt.VARIANT, BlockDirt.EnumDirtVariant.DIRT), random, i);
    }

    public int toLegacyData(IBlockData iblockdata) {
        return 0;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockMycel.SNOWY});
    }
}
