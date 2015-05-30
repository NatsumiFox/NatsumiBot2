package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;

public class BlockPressurePlateWeighted extends BlockPressurePlateAbstract {

    public static final BlockStateInteger POWER = BlockStateInteger.of("power", 0, 15);
    private final int b;

    protected BlockPressurePlateWeighted(Material material, int i) {
        this(material, i, material.r());
    }

    protected BlockPressurePlateWeighted(Material material, int i, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
        this.j(this.blockStateList.getBlockData().set(BlockPressurePlateWeighted.POWER, Integer.valueOf(0)));
        this.b = i;
    }

    protected int f(World world, BlockPosition blockposition) {
        int i = 0;
        Iterator iterator = world.a(Entity.class, this.a(blockposition)).iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            Object object;

            if (entity instanceof EntityHuman) {
                object = CraftEventFactory.callPlayerInteractEvent((EntityHuman) entity, Action.PHYSICAL, blockposition, (EnumDirection) null, (ItemStack) null);
            } else {
                object = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                world.getServer().getPluginManager().callEvent((EntityInteractEvent) object);
            }

            if (!((Cancellable) object).isCancelled()) {
                ++i;
            }
        }

        i = Math.min(i, this.b);
        if (i > 0) {
            float f = (float) Math.min(this.b, i) / (float) this.b;

            return MathHelper.f(f * 15.0F);
        } else {
            return 0;
        }
    }

    protected int e(IBlockData iblockdata) {
        return ((Integer) iblockdata.get(BlockPressurePlateWeighted.POWER)).intValue();
    }

    protected IBlockData a(IBlockData iblockdata, int i) {
        return iblockdata.set(BlockPressurePlateWeighted.POWER, Integer.valueOf(i));
    }

    public int a(World world) {
        return 10;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPressurePlateWeighted.POWER, Integer.valueOf(i));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((Integer) iblockdata.get(BlockPressurePlateWeighted.POWER)).intValue();
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPressurePlateWeighted.POWER});
    }
}
