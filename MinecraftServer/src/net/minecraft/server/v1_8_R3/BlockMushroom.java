package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.Random;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockMushroom extends BlockPlant implements IBlockFragilePlantElement {

    protected BlockMushroom() {
        float f = 0.2F;

        this.a(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        this.a(true);
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();

        if (random.nextInt(Math.max(1, (int) world.growthOdds / world.spigotConfig.mushroomModifier * 25)) == 0) {
            int l = 5;
            Iterator iterator = BlockPosition.b(blockposition.a(-4, -1, -4), blockposition.a(4, 1, 4)).iterator();

            BlockPosition blockposition1;

            while (iterator.hasNext()) {
                blockposition1 = (BlockPosition) iterator.next();
                if (world.getType(blockposition1).getBlock() == this) {
                    --l;
                    if (l <= 0) {
                        return;
                    }
                }
            }

            blockposition1 = blockposition.a(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

            for (int i1 = 0; i1 < 4; ++i1) {
                if (world.isEmpty(blockposition1) && this.f(world, blockposition1, this.getBlockData())) {
                    blockposition = blockposition1;
                }

                blockposition1 = blockposition.a(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            }

            if (world.isEmpty(blockposition1) && this.f(world, blockposition1, this.getBlockData())) {
                CraftWorld craftworld = world.getWorld();
                org.bukkit.block.BlockState org_bukkit_block_blockstate = craftworld.getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();

                org_bukkit_block_blockstate.setType(CraftMagicNumbers.getMaterial(this));
                BlockSpreadEvent blockspreadevent = new BlockSpreadEvent(org_bukkit_block_blockstate.getBlock(), craftworld.getBlockAt(i, j, k), org_bukkit_block_blockstate);

                world.getServer().getPluginManager().callEvent(blockspreadevent);
                if (!blockspreadevent.isCancelled()) {
                    org_bukkit_block_blockstate.update(true);
                }
            }
        }

    }

    public boolean canPlace(World world, BlockPosition blockposition) {
        return super.canPlace(world, blockposition) && this.f(world, blockposition, this.getBlockData());
    }

    protected boolean c(Block block) {
        return block.o();
    }

    public boolean f(World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (blockposition.getY() >= 0 && blockposition.getY() < 256) {
            IBlockData iblockdata1 = world.getType(blockposition.down());

            return iblockdata1.getBlock() == Blocks.MYCELIUM ? true : (iblockdata1.getBlock() == Blocks.DIRT && iblockdata1.get(BlockDirt.VARIANT) == BlockDirt.EnumDirtVariant.PODZOL ? true : world.k(blockposition) < 13 && this.c(iblockdata1.getBlock()));
        } else {
            return false;
        }
    }

    public boolean d(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        world.setAir(blockposition);
        WorldGenHugeMushroom worldgenhugemushroom = null;

        if (this == Blocks.BROWN_MUSHROOM) {
            BlockSapling.treeType = TreeType.BROWN_MUSHROOM;
            worldgenhugemushroom = new WorldGenHugeMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
        } else if (this == Blocks.RED_MUSHROOM) {
            BlockSapling.treeType = TreeType.RED_MUSHROOM;
            worldgenhugemushroom = new WorldGenHugeMushroom(Blocks.RED_MUSHROOM_BLOCK);
        }

        if (worldgenhugemushroom != null && worldgenhugemushroom.generate(world, random, blockposition)) {
            return true;
        } else {
            world.setTypeAndData(blockposition, iblockdata, 3);
            return false;
        }
    }

    public boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return true;
    }

    public boolean a(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        return (double) random.nextFloat() < 0.4D;
    }

    public void b(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        this.d(world, blockposition, iblockdata, random);
    }
}
