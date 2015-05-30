package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.PluginManager;

public class BlockPressurePlateBinary extends BlockPressurePlateAbstract {

    public static final BlockStateBoolean POWERED = BlockStateBoolean.of("powered");
    private final BlockPressurePlateBinary.EnumMobType b;

    protected BlockPressurePlateBinary(Material material, BlockPressurePlateBinary.EnumMobType blockpressureplatebinary_enummobtype) {
        super(material);
        this.j(this.blockStateList.getBlockData().set(BlockPressurePlateBinary.POWERED, Boolean.valueOf(false)));
        this.b = blockpressureplatebinary_enummobtype;
    }

    protected int e(IBlockData iblockdata) {
        return ((Boolean) iblockdata.get(BlockPressurePlateBinary.POWERED)).booleanValue() ? 15 : 0;
    }

    protected IBlockData a(IBlockData iblockdata, int i) {
        return iblockdata.set(BlockPressurePlateBinary.POWERED, Boolean.valueOf(i > 0));
    }

    protected int f(World world, BlockPosition blockposition) {
        AxisAlignedBB axisalignedbb = this.a(blockposition);
        List list;

        switch (BlockPressurePlateBinary.SyntheticClass_1.a[this.b.ordinal()]) {
        case 1:
            list = world.getEntities((Entity) null, axisalignedbb);
            break;

        case 2:
            list = world.a(EntityLiving.class, axisalignedbb);
            break;

        default:
            return 0;
        }

        if (!list.isEmpty()) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                if (this.e(world.getType(blockposition)) == 0) {
                    CraftWorld craftworld = world.getWorld();
                    PluginManager pluginmanager = world.getServer().getPluginManager();
                    Object object;

                    if (entity instanceof EntityHuman) {
                        object = CraftEventFactory.callPlayerInteractEvent((EntityHuman) entity, Action.PHYSICAL, blockposition, (EnumDirection) null, (ItemStack) null);
                    } else {
                        object = new EntityInteractEvent(entity.getBukkitEntity(), craftworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                        pluginmanager.callEvent((EntityInteractEvent) object);
                    }

                    if (((Cancellable) object).isCancelled()) {
                        continue;
                    }
                }

                if (!entity.aI()) {
                    return 15;
                }
            }
        }

        return 0;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPressurePlateBinary.POWERED, Boolean.valueOf(i == 1));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((Boolean) iblockdata.get(BlockPressurePlateBinary.POWERED)).booleanValue() ? 1 : 0;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPressurePlateBinary.POWERED});
    }

    public static enum EnumMobType {

        EVERYTHING, MOBS;

        private EnumMobType() {}
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[BlockPressurePlateBinary.EnumMobType.values().length];

        static {
            try {
                BlockPressurePlateBinary.SyntheticClass_1.a[BlockPressurePlateBinary.EnumMobType.EVERYTHING.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                BlockPressurePlateBinary.SyntheticClass_1.a[BlockPressurePlateBinary.EnumMobType.MOBS.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

        }

        SyntheticClass_1() {}
    }
}
