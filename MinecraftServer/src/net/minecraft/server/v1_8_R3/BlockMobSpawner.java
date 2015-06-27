package net.minecraft.server.v1_8_R3;

import java.util.Random;

public class BlockMobSpawner extends BlockContainer {

	private boolean st3;

	protected BlockMobSpawner() {
        super(Material.STONE);
    }

    public TileEntity a(World world, int i) {
        return new TileEntityMobSpawner();
    }

    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return st3 ? super.getDropType(iblockdata, random, i) : null;
    }

    public int a(Random random) {
        return 0;
    }

    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        super.dropNaturally(world, blockposition, iblockdata, f, i);
    }

    public int getExpDrop(World world, IBlockData iblockdata, int i) {
        int j = 15 + world.random.nextInt(15) + world.random.nextInt(15);

        return j;
    }

    public boolean c() {
        return st3;
    }

    public int b() {
        return 3;
    }

	public void a(World world, EntityHuman entityhuman, BlockPosition blockposition, IBlockData iblockdata, TileEntity tileentity) {
		entityhuman.b(StatisticList.MINE_BLOCK_COUNT[getId(this)]);
		entityhuman.applyExhaustion(0.025F);
		st3 = EnchantmentManager.hasSilkTouchEnchantment3(entityhuman);
		if (this.I() && st3) {
			ItemStack itemstack = this.i(iblockdata);

			if (itemstack != null) {
				a(world, blockposition, itemstack);
			}
		} else {
			int i = EnchantmentManager.getBonusBlockLootEnchantmentLevel(entityhuman);

			this.b(world, blockposition, iblockdata, i);
		}
	}
}
