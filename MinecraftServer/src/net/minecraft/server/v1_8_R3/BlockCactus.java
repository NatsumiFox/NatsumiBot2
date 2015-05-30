package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.Random;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;

public class BlockCactus extends Block {

    public static final BlockStateInteger AGE = BlockStateInteger.of("age", 0, 15);

    protected BlockCactus() {
        super(Material.CACTUS);
        this.j(this.blockStateList.getBlockData().set(BlockCactus.AGE, Integer.valueOf(0)));
        this.a(true);
        this.a(CreativeModeTab.c);
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        BlockPosition blockposition1 = blockposition.up();

        if (world.isEmpty(blockposition1)) {
            int i;

            for (i = 1; world.getType(blockposition.down(i)).getBlock() == this; ++i) {
                ;
            }

            if (i < 3) {
                int j = ((Integer) iblockdata.get(BlockCactus.AGE)).intValue();

                if (j >= (byte) ((int) range(3.0F, world.growthOdds / (float) world.spigotConfig.cactusModifier * 15.0F + 0.5F, 15.0F))) {
                    IBlockData iblockdata1 = iblockdata.set(BlockCactus.AGE, Integer.valueOf(0));

                    CraftEventFactory.handleBlockGrowEvent(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), this, 0);
                    world.setTypeAndData(blockposition, iblockdata1, 4);
                    this.doPhysics(world, blockposition1, iblockdata1, this);
                } else {
                    world.setTypeAndData(blockposition, iblockdata.set(BlockCactus.AGE, Integer.valueOf(j + 1)), 4);
                }
            }
        }

    }

    public AxisAlignedBB a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        float f = 0.0625F;

        return new AxisAlignedBB((double) ((float) blockposition.getX() + f), (double) blockposition.getY(), (double) ((float) blockposition.getZ() + f), (double) ((float) (blockposition.getX() + 1) - f), (double) ((float) (blockposition.getY() + 1) - f), (double) ((float) (blockposition.getZ() + 1) - f));
    }

    public boolean d() {
        return false;
    }

    public boolean c() {
        return false;
    }

    public boolean canPlace(World world, BlockPosition blockposition) {
        return super.canPlace(world, blockposition) ? this.e(world, blockposition) : false;
    }

    public void doPhysics(World world, BlockPosition blockposition, IBlockData iblockdata, Block block) {
        if (!this.e(world, blockposition)) {
            world.setAir(blockposition, true);
        }

    }

    public boolean e(World world, BlockPosition blockposition) {
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();

            if (world.getType(blockposition.shift(enumdirection)).getBlock().getMaterial().isBuildable()) {
                return false;
            }
        }

        Block block = world.getType(blockposition.down()).getBlock();

        return block == Blocks.CACTUS || block == Blocks.SAND;
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        CraftEventFactory.blockDamage = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        entity.damageEntity(DamageSource.CACTUS, 1.0F);
        CraftEventFactory.blockDamage = null;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockCactus.AGE, Integer.valueOf(i));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((Integer) iblockdata.get(BlockCactus.AGE)).intValue();
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockCactus.AGE});
    }
}
