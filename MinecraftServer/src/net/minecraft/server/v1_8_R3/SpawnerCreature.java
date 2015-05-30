package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHashSet;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public final class SpawnerCreature {

    private static final int a = (int) Math.pow(17.0D, 2.0D);
    private final LongHashSet b = new LongHashSet();
    private static int[] $SWITCH_TABLE$net$minecraft$server$EnumCreatureType;

    public SpawnerCreature() {}

    private int getEntityCount(WorldServer worldserver, Class oclass) {
        int i = 0;
        Iterator iterator = this.b.iterator();

        while (iterator.hasNext()) {
            Long olong = (Long) iterator.next();
            int j = LongHash.msw(olong.longValue());
            int k = LongHash.lsw(olong.longValue());

            if (!worldserver.chunkProviderServer.unloadQueue.contains(olong.longValue()) && worldserver.isChunkLoaded(j, k, true)) {
                i += worldserver.getChunkAt(j, k).entityCount.get(oclass);
            }
        }

        return i;
    }

    public int a(WorldServer worldserver, boolean flag, boolean flag1, boolean flag2) {
        if (!flag && !flag1) {
            return 0;
        } else {
            this.b.clear();
            int i = 0;
            Iterator iterator = worldserver.players.iterator();

            int j;
            int k;

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (!entityhuman.isSpectator()) {
                    int l = MathHelper.floor(entityhuman.locX / 16.0D);

                    j = MathHelper.floor(entityhuman.locZ / 16.0D);
                    boolean flag3 = true;
                    byte b0 = worldserver.spigotConfig.mobSpawnRange;

                    b0 = b0 > worldserver.spigotConfig.viewDistance ? (byte) worldserver.spigotConfig.viewDistance : b0;
                    b0 = b0 > 8 ? 8 : b0;

                    for (k = -b0; k <= b0; ++k) {
                        for (int i1 = -b0; i1 <= b0; ++i1) {
                            boolean flag4 = k == -b0 || k == b0 || i1 == -b0 || i1 == b0;
                            long j1 = LongHash.toLong(k + l, i1 + j);

                            if (!this.b.contains(j1)) {
                                ++i;
                                if (!flag4 && worldserver.getWorldBorder().isInBounds(k + l, i1 + j)) {
                                    this.b.add(j1);
                                }
                            }
                        }
                    }
                }
            }

            int k1 = 0;
            BlockPosition blockposition = worldserver.getSpawn();
            EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();

            j = aenumcreaturetype.length;

            for (k = 0; k < j; ++k) {
                EnumCreatureType enumcreaturetype = aenumcreaturetype[k];
                int l1 = enumcreaturetype.b();

                switch ($SWITCH_TABLE$net$minecraft$server$EnumCreatureType()[enumcreaturetype.ordinal()]) {
                case 1:
                    l1 = worldserver.getWorld().getMonsterSpawnLimit();
                    break;

                case 2:
                    l1 = worldserver.getWorld().getAnimalSpawnLimit();
                    break;

                case 3:
                    l1 = worldserver.getWorld().getAmbientSpawnLimit();
                    break;

                case 4:
                    l1 = worldserver.getWorld().getWaterAnimalSpawnLimit();
                }

                if (l1 != 0) {
                    boolean flag5 = false;

                    if ((!enumcreaturetype.d() || flag1) && (enumcreaturetype.d() || flag) && (!enumcreaturetype.e() || flag2)) {
                        worldserver.a(enumcreaturetype.a());
                        int i2 = l1 * i / SpawnerCreature.a;
                        int j2;

                        if ((j2 = this.getEntityCount(worldserver, enumcreaturetype.a())) <= l1 * i / 256) {
                            Iterator iterator1 = this.b.iterator();
                            int k2 = l1 * i / 256 - j2 + 1;

                            label143:
                            while (iterator1.hasNext() && k2 > 0) {
                                long l2 = ((Long) iterator1.next()).longValue();
                                BlockPosition blockposition1 = getRandomPosition(worldserver, LongHash.msw(l2), LongHash.lsw(l2));
                                int i3 = blockposition1.getX();
                                int j3 = blockposition1.getY();
                                int k3 = blockposition1.getZ();
                                Block block = worldserver.getType(blockposition1).getBlock();

                                if (!block.isOccluding()) {
                                    int l3 = 0;

                                    for (int i4 = 0; i4 < 3; ++i4) {
                                        int j4 = i3;
                                        int k4 = j3;
                                        int l4 = k3;
                                        byte b1 = 6;
                                        BiomeBase.BiomeMeta biomebase_biomemeta = null;
                                        GroupDataEntity groupdataentity = null;

                                        for (int i5 = 0; i5 < 4; ++i5) {
                                            j4 += worldserver.random.nextInt(b1) - worldserver.random.nextInt(b1);
                                            k4 += worldserver.random.nextInt(1) - worldserver.random.nextInt(1);
                                            l4 += worldserver.random.nextInt(b1) - worldserver.random.nextInt(b1);
                                            BlockPosition blockposition2 = new BlockPosition(j4, k4, l4);
                                            float f = (float) j4 + 0.5F;
                                            float f1 = (float) l4 + 0.5F;

                                            if (!worldserver.isPlayerNearby((double) f, (double) k4, (double) f1, 24.0D) && blockposition.c((double) f, (double) k4, (double) f1) >= 576.0D) {
                                                if (biomebase_biomemeta == null) {
                                                    biomebase_biomemeta = worldserver.a(enumcreaturetype, blockposition2);
                                                    if (biomebase_biomemeta == null) {
                                                        break;
                                                    }
                                                }

                                                if (worldserver.a(enumcreaturetype, biomebase_biomemeta, blockposition2) && a(EntityPositionTypes.a(biomebase_biomemeta.b), worldserver, blockposition2)) {
                                                    EntityInsentient entityinsentient;

                                                    try {
                                                        entityinsentient = (EntityInsentient) biomebase_biomemeta.b.getConstructor(new Class[] { World.class}).newInstance(new Object[] { worldserver});
                                                    } catch (Exception exception) {
                                                        exception.printStackTrace();
                                                        return k1;
                                                    }

                                                    entityinsentient.setPositionRotation((double) f, (double) k4, (double) f1, worldserver.random.nextFloat() * 360.0F, 0.0F);
                                                    if (entityinsentient.bR() && entityinsentient.canSpawn()) {
                                                        groupdataentity = entityinsentient.prepare(worldserver.E(new BlockPosition(entityinsentient)), groupdataentity);
                                                        if (entityinsentient.canSpawn()) {
                                                            ++l3;
                                                            worldserver.addEntity(entityinsentient, SpawnReason.NATURAL);
                                                        }

                                                        --k2;
                                                        if (k2 <= 0 || l3 >= entityinsentient.bV()) {
                                                            continue label143;
                                                        }
                                                    }

                                                    k1 += l3;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return k1;
        }
    }

    protected static BlockPosition getRandomPosition(World world, int i, int j) {
        Chunk chunk = world.getChunkAt(i, j);
        int k = i * 16 + world.random.nextInt(16);
        int l = j * 16 + world.random.nextInt(16);
        int i1 = MathHelper.c(chunk.f(new BlockPosition(k, 0, l)) + 1, 16);
        int j1 = world.random.nextInt(i1 > 0 ? i1 : chunk.g() + 16 - 1);

        return new BlockPosition(k, j1, l);
    }

    public static boolean a(EntityInsentient.EnumEntityPositionType entityinsentient_enumentitypositiontype, World world, BlockPosition blockposition) {
        if (!world.getWorldBorder().a(blockposition)) {
            return false;
        } else {
            Block block = world.getType(blockposition).getBlock();

            if (entityinsentient_enumentitypositiontype == EntityInsentient.EnumEntityPositionType.IN_WATER) {
                return block.getMaterial().isLiquid() && world.getType(blockposition.down()).getBlock().getMaterial().isLiquid() && !world.getType(blockposition.up()).getBlock().isOccluding();
            } else {
                BlockPosition blockposition1 = blockposition.down();

                if (!World.a((IBlockAccess) world, blockposition1)) {
                    return false;
                } else {
                    Block block1 = world.getType(blockposition1).getBlock();
                    boolean flag = block1 != Blocks.BEDROCK && block1 != Blocks.BARRIER;

                    return flag && !block.isOccluding() && !block.getMaterial().isLiquid() && !world.getType(blockposition.up()).getBlock().isOccluding();
                }
            }
        }
    }

    public static void a(World world, BiomeBase biomebase, int i, int j, int k, int l, Random random) {
        List list = biomebase.getMobs(EnumCreatureType.CREATURE);

        if (!list.isEmpty()) {
            while (random.nextFloat() < biomebase.g()) {
                BiomeBase.BiomeMeta biomebase_biomemeta = (BiomeBase.BiomeMeta) WeightedRandom.a(world.random, list);
                int i1 = biomebase_biomemeta.c + random.nextInt(1 + biomebase_biomemeta.d - biomebase_biomemeta.c);
                GroupDataEntity groupdataentity = null;
                int j1 = i + random.nextInt(k);
                int k1 = j + random.nextInt(l);
                int l1 = j1;
                int i2 = k1;

                for (int j2 = 0; j2 < i1; ++j2) {
                    boolean flag = false;

                    for (int k2 = 0; !flag && k2 < 4; ++k2) {
                        BlockPosition blockposition = world.r(new BlockPosition(j1, 0, k1));

                        if (a(EntityInsentient.EnumEntityPositionType.ON_GROUND, world, blockposition)) {
                            EntityInsentient entityinsentient;

                            try {
                                entityinsentient = (EntityInsentient) biomebase_biomemeta.b.getConstructor(new Class[] { World.class}).newInstance(new Object[] { world});
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                continue;
                            }

                            entityinsentient.setPositionRotation((double) ((float) j1 + 0.5F), (double) blockposition.getY(), (double) ((float) k1 + 0.5F), random.nextFloat() * 360.0F, 0.0F);
                            groupdataentity = entityinsentient.prepare(world.E(new BlockPosition(entityinsentient)), groupdataentity);
                            world.addEntity(entityinsentient, SpawnReason.CHUNK_GEN);
                            flag = true;
                        }

                        j1 += random.nextInt(5) - random.nextInt(5);

                        for (k1 += random.nextInt(5) - random.nextInt(5); j1 < i || j1 >= i + k || k1 < j || k1 >= j + k; k1 = i2 + random.nextInt(5) - random.nextInt(5)) {
                            j1 = l1 + random.nextInt(5) - random.nextInt(5);
                        }
                    }
                }
            }
        }

    }

    static int[] $SWITCH_TABLE$net$minecraft$server$EnumCreatureType() {
        int[] aint = SpawnerCreature.$SWITCH_TABLE$net$minecraft$server$EnumCreatureType;

        if (SpawnerCreature.$SWITCH_TABLE$net$minecraft$server$EnumCreatureType != null) {
            return aint;
        } else {
            int[] aint1 = new int[EnumCreatureType.values().length];

            try {
                aint1[EnumCreatureType.AMBIENT.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[EnumCreatureType.CREATURE.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[EnumCreatureType.MONSTER.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[EnumCreatureType.WATER_CREATURE.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            SpawnerCreature.$SWITCH_TABLE$net$minecraft$server$EnumCreatureType = aint1;
            return aint1;
        }
    }
}
