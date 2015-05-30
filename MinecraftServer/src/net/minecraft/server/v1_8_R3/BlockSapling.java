package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;

public class BlockSapling extends BlockPlant implements IBlockFragilePlantElement {

    public static final BlockStateEnum<BlockWood.EnumLogVariant> TYPE = BlockStateEnum.of("type", BlockWood.EnumLogVariant.class);
    public static final BlockStateInteger STAGE = BlockStateInteger.of("stage", 0, 1);
    public static TreeType treeType;

    protected BlockSapling() {
        this.j(this.blockStateList.getBlockData().set(BlockSapling.TYPE, BlockWood.EnumLogVariant.OAK).set(BlockSapling.STAGE, Integer.valueOf(0)));
        float f = 0.4F;

        this.a(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        this.a(CreativeModeTab.c);
    }

    public String getName() {
        return LocaleI18n.get(this.a() + "." + BlockWood.EnumLogVariant.OAK.d() + ".name");
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            super.b(world, blockposition, iblockdata, random);
            if (world.getLightLevel(blockposition.up()) >= 9 && random.nextInt(Math.max(2, (int) (world.growthOdds / (float) world.spigotConfig.saplingModifier * 7.0F + 0.5F))) == 0) {
                world.captureTreeGeneration = true;
                this.grow(world, blockposition, iblockdata, random);
                world.captureTreeGeneration = false;
                if (world.capturedBlockStates.size() > 0) {
                    TreeType treetype = BlockSapling.treeType;

                    BlockSapling.treeType = null;
                    Location location = new Location(world.getWorld(), (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
                    List list = (List) world.capturedBlockStates.clone();

                    world.capturedBlockStates.clear();
                    StructureGrowEvent structuregrowevent = null;

                    if (treetype != null) {
                        structuregrowevent = new StructureGrowEvent(location, treetype, false, (Player) null, list);
                        Bukkit.getPluginManager().callEvent(structuregrowevent);
                    }

                    if (structuregrowevent == null || !structuregrowevent.isCancelled()) {
                        Iterator iterator = list.iterator();

                        while (iterator.hasNext()) {
                            org.bukkit.block.BlockState org_bukkit_block_blockstate = (org.bukkit.block.BlockState) iterator.next();

                            org_bukkit_block_blockstate.update(true);
                        }
                    }
                }
            }
        }

    }

    public void grow(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (((Integer) iblockdata.get(BlockSapling.STAGE)).intValue() == 0) {
            world.setTypeAndData(blockposition, iblockdata.a(BlockSapling.STAGE), 4);
        } else {
            this.e(world, blockposition, iblockdata, random);
        }

    }

    public void e(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        Object object;

        if (random.nextInt(10) == 0) {
            BlockSapling.treeType = TreeType.BIG_TREE;
            object = new WorldGenBigTree(true);
        } else {
            BlockSapling.treeType = TreeType.TREE;
            object = new WorldGenTrees(true);
        }

        int i = 0;
        int j = 0;
        boolean flag = false;
        IBlockData iblockdata1;

        switch (BlockSapling.SyntheticClass_1.a[((BlockWood.EnumLogVariant) iblockdata.get(BlockSapling.TYPE)).ordinal()]) {
        case 1:
            label68:
            for (i = 0; i >= -1; --i) {
                for (j = 0; j >= -1; --j) {
                    if (this.a(world, blockposition, i, j, BlockWood.EnumLogVariant.SPRUCE)) {
                        BlockSapling.treeType = TreeType.MEGA_REDWOOD;
                        object = new WorldGenMegaTree(false, random.nextBoolean());
                        flag = true;
                        break label68;
                    }
                }
            }

            if (!flag) {
                j = 0;
                i = 0;
                BlockSapling.treeType = TreeType.REDWOOD;
                object = new WorldGenTaiga2(true);
            }
            break;

        case 2:
            BlockSapling.treeType = TreeType.BIRCH;
            object = new WorldGenForest(true, false);
            break;

        case 3:
            iblockdata1 = Blocks.LOG.getBlockData().set(BlockLog1.VARIANT, BlockWood.EnumLogVariant.JUNGLE);
            IBlockData iblockdata2 = Blocks.LEAVES.getBlockData().set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.JUNGLE).set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

            label82:
            for (i = 0; i >= -1; --i) {
                for (j = 0; j >= -1; --j) {
                    if (this.a(world, blockposition, i, j, BlockWood.EnumLogVariant.JUNGLE)) {
                        BlockSapling.treeType = TreeType.JUNGLE;
                        object = new WorldGenJungleTree(true, 10, 20, iblockdata1, iblockdata2);
                        flag = true;
                        break label82;
                    }
                }
            }

            if (!flag) {
                j = 0;
                i = 0;
                BlockSapling.treeType = TreeType.SMALL_JUNGLE;
                object = new WorldGenTrees(true, 4 + random.nextInt(7), iblockdata1, iblockdata2, false);
            }
            break;

        case 4:
            BlockSapling.treeType = TreeType.ACACIA;
            object = new WorldGenAcaciaTree(true);
            break;

        case 5:
            label96:
            for (i = 0; i >= -1; --i) {
                for (j = 0; j >= -1; --j) {
                    if (this.a(world, blockposition, i, j, BlockWood.EnumLogVariant.DARK_OAK)) {
                        BlockSapling.treeType = TreeType.DARK_OAK;
                        object = new WorldGenForestTree(true);
                        flag = true;
                        break label96;
                    }
                }
            }

            if (!flag) {
                return;
            }

        case 6:
        }

        iblockdata1 = Blocks.AIR.getBlockData();
        if (flag) {
            world.setTypeAndData(blockposition.a(i, 0, j), iblockdata1, 4);
            world.setTypeAndData(blockposition.a(i + 1, 0, j), iblockdata1, 4);
            world.setTypeAndData(blockposition.a(i, 0, j + 1), iblockdata1, 4);
            world.setTypeAndData(blockposition.a(i + 1, 0, j + 1), iblockdata1, 4);
        } else {
            world.setTypeAndData(blockposition, iblockdata1, 4);
        }

        if (!((WorldGenerator) object).generate(world, random, blockposition.a(i, 0, j))) {
            if (flag) {
                world.setTypeAndData(blockposition.a(i, 0, j), iblockdata, 4);
                world.setTypeAndData(blockposition.a(i + 1, 0, j), iblockdata, 4);
                world.setTypeAndData(blockposition.a(i, 0, j + 1), iblockdata, 4);
                world.setTypeAndData(blockposition.a(i + 1, 0, j + 1), iblockdata, 4);
            } else {
                world.setTypeAndData(blockposition, iblockdata, 4);
            }
        }

    }

    private boolean a(World world, BlockPosition blockposition, int i, int j, BlockWood.EnumLogVariant blockwood_enumlogvariant) {
        return this.a(world, blockposition.a(i, 0, j), blockwood_enumlogvariant) && this.a(world, blockposition.a(i + 1, 0, j), blockwood_enumlogvariant) && this.a(world, blockposition.a(i, 0, j + 1), blockwood_enumlogvariant) && this.a(world, blockposition.a(i + 1, 0, j + 1), blockwood_enumlogvariant);
    }

    public boolean a(World world, BlockPosition blockposition, BlockWood.EnumLogVariant blockwood_enumlogvariant) {
        IBlockData iblockdata = world.getType(blockposition);

        return iblockdata.getBlock() == this && iblockdata.get(BlockSapling.TYPE) == blockwood_enumlogvariant;
    }

    public int getDropData(IBlockData iblockdata) {
        return ((BlockWood.EnumLogVariant) iblockdata.get(BlockSapling.TYPE)).a();
    }

    public boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return true;
    }

    public boolean a(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        return (double) world.random.nextFloat() < 0.45D;
    }

    public void b(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        this.grow(world, blockposition, iblockdata, random);
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockSapling.TYPE, BlockWood.EnumLogVariant.a(i & 7)).set(BlockSapling.STAGE, Integer.valueOf((i & 8) >> 3));
    }

    public int toLegacyData(IBlockData iblockdata) {
        byte b0 = 0;
        int i = b0 | ((BlockWood.EnumLogVariant) iblockdata.get(BlockSapling.TYPE)).a();

        i |= ((Integer) iblockdata.get(BlockSapling.STAGE)).intValue() << 3;
        return i;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockSapling.TYPE, BlockSapling.STAGE});
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[BlockWood.EnumLogVariant.values().length];

        static {
            try {
                BlockSapling.SyntheticClass_1.a[BlockWood.EnumLogVariant.SPRUCE.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockSapling.SyntheticClass_1.a[BlockWood.EnumLogVariant.BIRCH.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                BlockSapling.SyntheticClass_1.a[BlockWood.EnumLogVariant.JUNGLE.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                BlockSapling.SyntheticClass_1.a[BlockWood.EnumLogVariant.ACACIA.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                BlockSapling.SyntheticClass_1.a[BlockWood.EnumLogVariant.DARK_OAK.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                BlockSapling.SyntheticClass_1.a[BlockWood.EnumLogVariant.OAK.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

        }

        SyntheticClass_1() {}
    }
}
