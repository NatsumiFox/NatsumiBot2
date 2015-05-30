package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;

public class PortalTravelAgent {

    private final WorldServer a;
    private final Random b;
    private final LongHashMap<PortalTravelAgent.ChunkCoordinatesPortal> c = new LongHashMap();
    private final List<Long> d = Lists.newArrayList();

    public PortalTravelAgent(WorldServer worldserver) {
        this.a = worldserver;
        this.b = new Random(worldserver.getSeed());
    }

    public void a(Entity entity, float f) {
        if (this.a.worldProvider.getDimension() != 1) {
            if (!this.b(entity, f)) {
                this.a(entity);
                this.b(entity, f);
            }
        } else {
            MathHelper.floor(entity.locX);
            MathHelper.floor(entity.locY);
            MathHelper.floor(entity.locZ);
            BlockPosition blockposition = this.createEndPortal(entity.locX, entity.locY, entity.locZ);

            entity.setPositionRotation((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), entity.yaw, 0.0F);
            entity.motX = entity.motY = entity.motZ = 0.0D;
        }

    }

    private BlockPosition createEndPortal(double d0, double d1, double d2) {
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1) - 1;
        int k = MathHelper.floor(d2);
        byte b0 = 1;
        byte b1 = 0;

        for (int l = -2; l <= 2; ++l) {
            for (int i1 = -2; i1 <= 2; ++i1) {
                for (int j1 = -1; j1 < 3; ++j1) {
                    int k1 = i + i1 * b0 + l * b1;
                    int l1 = j + j1;
                    int i2 = k + i1 * b1 - l * b0;
                    boolean flag = j1 < 0;

                    this.a.setTypeUpdate(new BlockPosition(k1, l1, i2), flag ? Blocks.OBSIDIAN.getBlockData() : Blocks.AIR.getBlockData());
                }
            }
        }

        return new BlockPosition(i, k, k);
    }

    private BlockPosition findEndPortal(BlockPosition blockposition) {
        int i = blockposition.getX();
        int j = blockposition.getY() - 1;
        int k = blockposition.getZ();
        byte b0 = 1;
        byte b1 = 0;

        for (int l = -2; l <= 2; ++l) {
            for (int i1 = -2; i1 <= 2; ++i1) {
                for (int j1 = -1; j1 < 3; ++j1) {
                    int k1 = i + i1 * b0 + l * b1;
                    int l1 = j + j1;
                    int i2 = k + i1 * b1 - l * b0;
                    boolean flag = j1 < 0;

                    if (this.a.getType(new BlockPosition(k1, l1, i2)).getBlock() != (flag ? Blocks.OBSIDIAN : Blocks.AIR)) {
                        return null;
                    }
                }
            }
        }

        return new BlockPosition(i, j, k);
    }

    public boolean b(Entity entity, float f) {
        BlockPosition blockposition = this.findPortal(entity.locX, entity.locY, entity.locZ, 128);

        if (blockposition == null) {
            return false;
        } else {
            Location location = new Location(this.a.getWorld(), (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), f, entity.pitch);
            Vector vector = entity.getBukkitEntity().getVelocity();

            this.adjustExit(entity, location, vector);
            entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            if (entity.motX != vector.getX() || entity.motY != vector.getY() || entity.motZ != vector.getZ()) {
                entity.getBukkitEntity().setVelocity(vector);
            }

            return true;
        }
    }

    public BlockPosition findPortal(double d0, double d1, double d2, int i) {
        if (this.a.getWorld().getEnvironment() == Environment.THE_END) {
            return this.findEndPortal(this.a.worldProvider.h());
        } else {
            double d3 = -1.0D;
            int j = MathHelper.floor(d0);
            int k = MathHelper.floor(d2);
            boolean flag = true;
            Object object = BlockPosition.ZERO;
            long l = ChunkCoordIntPair.a(j, k);

            if (this.c.contains(l)) {
                PortalTravelAgent.ChunkCoordinatesPortal portaltravelagent_chunkcoordinatesportal = (PortalTravelAgent.ChunkCoordinatesPortal) this.c.getEntry(l);

                d3 = 0.0D;
                object = portaltravelagent_chunkcoordinatesportal;
                portaltravelagent_chunkcoordinatesportal.c = this.a.getTime();
                flag = false;
            } else {
                BlockPosition blockposition = new BlockPosition(d0, d1, d2);

                for (int i1 = -128; i1 <= 128; ++i1) {
                    BlockPosition blockposition1;

                    for (int j1 = -128; j1 <= 128; ++j1) {
                        for (BlockPosition blockposition2 = blockposition.a(i1, this.a.V() - 1 - blockposition.getY(), j1); blockposition2.getY() >= 0; blockposition2 = blockposition1) {
                            blockposition1 = blockposition2.down();
                            if (this.a.getType(blockposition2).getBlock() == Blocks.PORTAL) {
                                while (this.a.getType(blockposition1 = blockposition2.down()).getBlock() == Blocks.PORTAL) {
                                    blockposition2 = blockposition1;
                                }

                                double d4 = blockposition2.i(blockposition);

                                if (d3 < 0.0D || d4 < d3) {
                                    d3 = d4;
                                    object = blockposition2;
                                }
                            }
                        }
                    }
                }
            }

            if (d3 >= 0.0D) {
                if (flag) {
                    this.c.put(l, new PortalTravelAgent.ChunkCoordinatesPortal((BlockPosition) object, this.a.getTime()));
                    this.d.add(Long.valueOf(l));
                }

                return (BlockPosition) object;
            } else {
                return null;
            }
        }
    }

    public void adjustExit(Entity entity, Location location, Vector vector) {
        Location location1 = location.clone();
        Vector vector1 = vector.clone();
        BlockPosition blockposition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        float f = location.getYaw();

        if (this.a.getWorld().getEnvironment() != Environment.THE_END && entity.getBukkitEntity().getWorld().getEnvironment() != Environment.THE_END && entity.aG() != null) {
            double d0 = (double) blockposition.getX() + 0.5D;
            double d1 = (double) blockposition.getY() + 0.5D;
            double d2 = (double) blockposition.getZ() + 0.5D;
            ShapeDetector.ShapeDetectorCollection shapedetector_shapedetectorcollection = Blocks.PORTAL.f(this.a, blockposition);
            boolean flag = shapedetector_shapedetectorcollection.b().e().c() == EnumDirection.EnumAxisDirection.NEGATIVE;
            double d3 = shapedetector_shapedetectorcollection.b().k() == EnumDirection.EnumAxis.X ? (double) shapedetector_shapedetectorcollection.a().getZ() : (double) shapedetector_shapedetectorcollection.a().getX();

            d1 = (double) (shapedetector_shapedetectorcollection.a().getY() + 1) - entity.aG().b * (double) shapedetector_shapedetectorcollection.e();
            if (flag) {
                ++d3;
            }

            if (shapedetector_shapedetectorcollection.b().k() == EnumDirection.EnumAxis.X) {
                d2 = d3 + (1.0D - entity.aG().a) * (double) shapedetector_shapedetectorcollection.d() * (double) shapedetector_shapedetectorcollection.b().e().c().a();
            } else {
                d0 = d3 + (1.0D - entity.aG().a) * (double) shapedetector_shapedetectorcollection.d() * (double) shapedetector_shapedetectorcollection.b().e().c().a();
            }

            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;

            if (shapedetector_shapedetectorcollection.b().opposite() == entity.aH()) {
                f1 = 1.0F;
                f2 = 1.0F;
            } else if (shapedetector_shapedetectorcollection.b().opposite() == entity.aH().opposite()) {
                f1 = -1.0F;
                f2 = -1.0F;
            } else if (shapedetector_shapedetectorcollection.b().opposite() == entity.aH().e()) {
                f3 = 1.0F;
                f4 = -1.0F;
            } else {
                f3 = -1.0F;
                f4 = 1.0F;
            }

            double d4 = vector.getX();
            double d5 = vector.getZ();

            vector.setX(d4 * (double) f1 + d5 * (double) f4);
            vector.setZ(d4 * (double) f3 + d5 * (double) f2);
            f = f - (float) (entity.aH().opposite().b() * 90) + (float) (shapedetector_shapedetectorcollection.b().b() * 90);
            location.setX(d0);
            location.setY(d1);
            location.setZ(d2);
            location.setYaw(f);
        } else {
            location.setPitch(0.0F);
            vector.setX(0);
            vector.setY(0);
            vector.setZ(0);
        }

        EntityPortalExitEvent entityportalexitevent = new EntityPortalExitEvent(entity.getBukkitEntity(), location1, location, vector1, vector);

        this.a.getServer().getPluginManager().callEvent(entityportalexitevent);
        Location location2 = entityportalexitevent.getTo();

        if (!entityportalexitevent.isCancelled() && location2 != null && entity.isAlive()) {
            location.setX(location2.getX());
            location.setY(location2.getY());
            location.setZ(location2.getZ());
            location.setYaw(location2.getYaw());
            location.setPitch(location2.getPitch());
            vector.copy(entityportalexitevent.getAfter());
        } else {
            location.setX(location1.getX());
            location.setY(location1.getY());
            location.setZ(location1.getZ());
            location.setYaw(location1.getYaw());
            location.setPitch(location1.getPitch());
            vector.copy(vector1);
        }

    }

    public boolean a(Entity entity) {
        return this.createPortal(entity.locX, entity.locY, entity.locZ, 16);
    }

    public boolean createPortal(double d0, double d1, double d2, int i) {
        if (this.a.getWorld().getEnvironment() == Environment.THE_END) {
            this.createEndPortal(d0, d1, d2);
            return true;
        } else {
            double d3 = -1.0D;
            int j = MathHelper.floor(d0);
            int k = MathHelper.floor(d1);
            int l = MathHelper.floor(d2);
            int i1 = j;
            int j1 = k;
            int k1 = l;
            int l1 = 0;
            int i2 = this.b.nextInt(4);
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

            int j2;
            double d4;
            int k2;
            double d5;
            int l2;
            int i3;
            int j3;
            int k3;
            int l3;
            int i4;
            int j4;
            int k4;
            int l4;
            int i5;
            double d6;
            double d7;

            for (j2 = j - i; j2 <= j + i; ++j2) {
                d4 = (double) j2 + 0.5D - d0;

                for (k2 = l - i; k2 <= l + i; ++k2) {
                    d5 = (double) k2 + 0.5D - d2;

                    label280:
                    for (l2 = this.a.V() - 1; l2 >= 0; --l2) {
                        if (this.a.isEmpty(blockposition_mutableblockposition.c(j2, l2, k2))) {
                            while (l2 > 0 && this.a.isEmpty(blockposition_mutableblockposition.c(j2, l2 - 1, k2))) {
                                --l2;
                            }

                            for (i3 = i2; i3 < i2 + 4; ++i3) {
                                j3 = i3 % 2;
                                k3 = 1 - j3;
                                if (i3 % 4 >= 2) {
                                    j3 = -j3;
                                    k3 = -k3;
                                }

                                for (l3 = 0; l3 < 3; ++l3) {
                                    for (i4 = 0; i4 < 4; ++i4) {
                                        for (j4 = -1; j4 < 4; ++j4) {
                                            k4 = j2 + (i4 - 1) * j3 + l3 * k3;
                                            l4 = l2 + j4;
                                            i5 = k2 + (i4 - 1) * k3 - l3 * j3;
                                            blockposition_mutableblockposition.c(k4, l4, i5);
                                            if (j4 < 0 && !this.a.getType(blockposition_mutableblockposition).getBlock().getMaterial().isBuildable() || j4 >= 0 && !this.a.isEmpty(blockposition_mutableblockposition)) {
                                                continue label280;
                                            }
                                        }
                                    }
                                }

                                d6 = (double) l2 + 0.5D - d1;
                                d7 = d4 * d4 + d6 * d6 + d5 * d5;
                                if (d3 < 0.0D || d7 < d3) {
                                    d3 = d7;
                                    i1 = j2;
                                    j1 = l2;
                                    k1 = k2;
                                    l1 = i3 % 4;
                                }
                            }
                        }
                    }
                }
            }

            if (d3 < 0.0D) {
                for (j2 = j - i; j2 <= j + i; ++j2) {
                    d4 = (double) j2 + 0.5D - d0;

                    for (k2 = l - i; k2 <= l + i; ++k2) {
                        d5 = (double) k2 + 0.5D - d2;

                        label224:
                        for (l2 = this.a.V() - 1; l2 >= 0; --l2) {
                            if (this.a.isEmpty(blockposition_mutableblockposition.c(j2, l2, k2))) {
                                while (l2 > 0 && this.a.isEmpty(blockposition_mutableblockposition.c(j2, l2 - 1, k2))) {
                                    --l2;
                                }

                                for (i3 = i2; i3 < i2 + 2; ++i3) {
                                    j3 = i3 % 2;
                                    k3 = 1 - j3;

                                    for (l3 = 0; l3 < 4; ++l3) {
                                        for (i4 = -1; i4 < 4; ++i4) {
                                            j4 = j2 + (l3 - 1) * j3;
                                            k4 = l2 + i4;
                                            l4 = k2 + (l3 - 1) * k3;
                                            blockposition_mutableblockposition.c(j4, k4, l4);
                                            if (i4 < 0 && !this.a.getType(blockposition_mutableblockposition).getBlock().getMaterial().isBuildable() || i4 >= 0 && !this.a.isEmpty(blockposition_mutableblockposition)) {
                                                continue label224;
                                            }
                                        }
                                    }

                                    d6 = (double) l2 + 0.5D - d1;
                                    d7 = d4 * d4 + d6 * d6 + d5 * d5;
                                    if (d3 < 0.0D || d7 < d3) {
                                        d3 = d7;
                                        i1 = j2;
                                        j1 = l2;
                                        k1 = k2;
                                        l1 = i3 % 2;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            i5 = i1;
            int j5 = j1;

            k2 = k1;
            int k5 = l1 % 2;
            int l5 = 1 - k5;

            if (l1 % 4 >= 2) {
                k5 = -k5;
                l5 = -l5;
            }

            if (d3 < 0.0D) {
                j1 = MathHelper.clamp(j1, 70, this.a.V() - 10);
                j5 = j1;

                for (l2 = -1; l2 <= 1; ++l2) {
                    for (i3 = 1; i3 < 3; ++i3) {
                        for (j3 = -1; j3 < 3; ++j3) {
                            k3 = i5 + (i3 - 1) * k5 + l2 * l5;
                            l3 = j5 + j3;
                            i4 = k2 + (i3 - 1) * l5 - l2 * k5;
                            boolean flag = j3 < 0;

                            this.a.setTypeUpdate(new BlockPosition(k3, l3, i4), flag ? Blocks.OBSIDIAN.getBlockData() : Blocks.AIR.getBlockData());
                        }
                    }
                }
            }

            IBlockData iblockdata = Blocks.PORTAL.getBlockData().set(BlockPortal.AXIS, k5 != 0 ? EnumDirection.EnumAxis.X : EnumDirection.EnumAxis.Z);

            for (i3 = 0; i3 < 4; ++i3) {
                for (j3 = 0; j3 < 4; ++j3) {
                    for (k3 = -1; k3 < 4; ++k3) {
                        l3 = i5 + (j3 - 1) * k5;
                        i4 = j5 + k3;
                        j4 = k2 + (j3 - 1) * l5;
                        boolean flag1 = j3 == 0 || j3 == 3 || k3 == -1 || k3 == 3;

                        this.a.setTypeAndData(new BlockPosition(l3, i4, j4), flag1 ? Blocks.OBSIDIAN.getBlockData() : iblockdata, 2);
                    }
                }

                for (j3 = 0; j3 < 4; ++j3) {
                    for (k3 = -1; k3 < 4; ++k3) {
                        l3 = i5 + (j3 - 1) * k5;
                        i4 = j5 + k3;
                        j4 = k2 + (j3 - 1) * l5;
                        BlockPosition blockposition = new BlockPosition(l3, i4, j4);

                        this.a.applyPhysics(blockposition, this.a.getType(blockposition).getBlock());
                    }
                }
            }

            return true;
        }
    }

    public void a(long i) {
        if (i % 100L == 0L) {
            Iterator iterator = this.d.iterator();
            long j = i - 300L;

            while (iterator.hasNext()) {
                Long olong = (Long) iterator.next();
                PortalTravelAgent.ChunkCoordinatesPortal portaltravelagent_chunkcoordinatesportal = (PortalTravelAgent.ChunkCoordinatesPortal) this.c.getEntry(olong.longValue());

                if (portaltravelagent_chunkcoordinatesportal == null || portaltravelagent_chunkcoordinatesportal.c < j) {
                    iterator.remove();
                    this.c.remove(olong.longValue());
                }
            }
        }

    }

    public class ChunkCoordinatesPortal extends BlockPosition {

        public long c;

        public ChunkCoordinatesPortal(BlockPosition blockposition, long i) {
            super(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            this.c = i;
        }
    }
}
