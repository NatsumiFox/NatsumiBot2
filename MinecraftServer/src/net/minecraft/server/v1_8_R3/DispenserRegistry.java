package net.minecraft.server.v1_8_R3;

import com.mojang.authlib.GameProfile;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.projectiles.CraftBlockProjectileSource;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.util.Vector;

public class DispenserRegistry {

    private static final PrintStream a = System.out;
    private static boolean b = false;
    private static final Logger c = LogManager.getLogger();

    public DispenserRegistry() {}

    public static boolean a() {
        return DispenserRegistry.b;
    }

    static void b() {
        BlockDispenser.N.a(Items.ARROW, new DispenseBehaviorProjectile() {
            protected IProjectile a(World world, IPosition iposition) {
                EntityArrow entityarrow = new EntityArrow(world, iposition.getX(), iposition.getY(), iposition.getZ());

                entityarrow.fromPlayer = 1;
                return entityarrow;
            }
        });
        BlockDispenser.N.a(Items.EGG, new DispenseBehaviorProjectile() {
            protected IProjectile a(World world, IPosition iposition) {
                return new EntityEgg(world, iposition.getX(), iposition.getY(), iposition.getZ());
            }
        });
        BlockDispenser.N.a(Items.SNOWBALL, new DispenseBehaviorProjectile() {
            protected IProjectile a(World world, IPosition iposition) {
                return new EntitySnowball(world, iposition.getX(), iposition.getY(), iposition.getZ());
            }
        });
        BlockDispenser.N.a(Items.EXPERIENCE_BOTTLE, new DispenseBehaviorProjectile() {
            protected IProjectile a(World world, IPosition iposition) {
                return new EntityThrownExpBottle(world, iposition.getX(), iposition.getY(), iposition.getZ());
            }

            protected float a() {
                return super.a() * 0.5F;
            }

            protected float b() {
                return super.b() * 1.25F;
            }
        });
        BlockDispenser.N.a(Items.POTION, new IDispenseBehavior() {
            private final DispenseBehaviorItem b = new DispenseBehaviorItem();

            public ItemStack a(ISourceBlock isourceblock, final ItemStack itemstack) {
                return ItemPotion.f(itemstack.getData()) ? (new DispenseBehaviorProjectile() {
                    protected IProjectile a(World world, IPosition iposition) {
                        return new EntityPotion(world, iposition.getX(), iposition.getY(), iposition.getZ(), itemstack.cloneItemStack());
                    }

                    protected float a() {
                        return super.a() * 0.5F;
                    }

                    protected float b() {
                        return super.b() * 1.25F;
                    }
                }).a(isourceblock, itemstack) : this.b.a(isourceblock, itemstack);
            }
        });
        BlockDispenser.N.a(Items.SPAWN_EGG, new DispenseBehaviorItem() {
            public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
                double d0 = isourceblock.getX() + (double) enumdirection.getAdjacentX();
                double d1 = (double) ((float) isourceblock.getBlockPosition().getY() + 0.2F);
                double d2 = isourceblock.getZ() + (double) enumdirection.getAdjacentZ();
                World world = isourceblock.i();
                ItemStack itemstack1 = itemstack.a(1);
                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(d0, d1, d2));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    ++itemstack.count;
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ++itemstack.count;
                        ItemStack itemstack2 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack2.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack2);
                            return itemstack;
                        }
                    }

                    itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                    Entity entity = ItemMonsterEgg.spawnCreature(isourceblock.i(), itemstack.getData(), blockdispenseevent.getVelocity().getX(), blockdispenseevent.getVelocity().getY(), blockdispenseevent.getVelocity().getZ(), SpawnReason.DISPENSE_EGG);

                    if (entity instanceof EntityLiving && itemstack.hasName()) {
                        ((EntityInsentient) entity).setCustomName(itemstack.getName());
                    }

                    return itemstack;
                }
            }
        });
        BlockDispenser.N.a(Items.FIREWORKS, new DispenseBehaviorItem() {
            public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
                double d0 = isourceblock.getX() + (double) enumdirection.getAdjacentX();
                double d1 = (double) ((float) isourceblock.getBlockPosition().getY() + 0.2F);
                double d2 = isourceblock.getZ() + (double) enumdirection.getAdjacentZ();
                World world = isourceblock.i();
                ItemStack itemstack1 = itemstack.a(1);
                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(d0, d1, d2));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    ++itemstack.count;
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ++itemstack.count;
                        ItemStack itemstack2 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack2.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack2);
                            return itemstack;
                        }
                    }

                    EntityFireworks entityfireworks = new EntityFireworks(isourceblock.i(), d0, d1, d2, itemstack);

                    isourceblock.i().addEntity(entityfireworks);
                    return itemstack;
                }
            }

            protected void a(ISourceBlock isourceblock) {
                isourceblock.i().triggerEffect(1002, isourceblock.getBlockPosition(), 0);
            }
        });
        BlockDispenser.N.a(Items.FIRE_CHARGE, new DispenseBehaviorItem() {
            public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
                IPosition iposition = BlockDispenser.a(isourceblock);
                double d0 = iposition.getX() + (double) ((float) enumdirection.getAdjacentX() * 0.3F);
                double d1 = iposition.getY() + (double) ((float) enumdirection.getAdjacentY() * 0.3F);
                double d2 = iposition.getZ() + (double) ((float) enumdirection.getAdjacentZ() * 0.3F);
                World world = isourceblock.i();
                Random random = world.random;
                double d3 = random.nextGaussian() * 0.05D + (double) enumdirection.getAdjacentX();
                double d4 = random.nextGaussian() * 0.05D + (double) enumdirection.getAdjacentY();
                double d5 = random.nextGaussian() * 0.05D + (double) enumdirection.getAdjacentZ();
                ItemStack itemstack1 = itemstack.a(1);
                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(d3, d4, d5));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    ++itemstack.count;
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ++itemstack.count;
                        ItemStack itemstack2 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack2.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack2);
                            return itemstack;
                        }
                    }

                    EntitySmallFireball entitysmallfireball = new EntitySmallFireball(world, d0, d1, d2, blockdispenseevent.getVelocity().getX(), blockdispenseevent.getVelocity().getY(), blockdispenseevent.getVelocity().getZ());

                    entitysmallfireball.projectileSource = new CraftBlockProjectileSource((TileEntityDispenser) isourceblock.getTileEntity());
                    world.addEntity(entitysmallfireball);
                    return itemstack;
                }
            }

            protected void a(ISourceBlock isourceblock) {
                isourceblock.i().triggerEffect(1009, isourceblock.getBlockPosition(), 0);
            }
        });
        BlockDispenser.N.a(Items.BOAT, new DispenseBehaviorItem() {
            private final DispenseBehaviorItem b = new DispenseBehaviorItem();

            public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
                World world = isourceblock.i();
                double d0 = isourceblock.getX() + (double) ((float) enumdirection.getAdjacentX() * 1.125F);
                double d1 = isourceblock.getY() + (double) ((float) enumdirection.getAdjacentY() * 1.125F);
                double d2 = isourceblock.getZ() + (double) ((float) enumdirection.getAdjacentZ() * 1.125F);
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(enumdirection);
                Material material = world.getType(blockposition).getBlock().getMaterial();
                double d3;

                if (Material.WATER.equals(material)) {
                    d3 = 1.0D;
                } else {
                    if (!Material.AIR.equals(material) || !Material.WATER.equals(world.getType(blockposition.down()).getBlock().getMaterial())) {
                        return this.b.a(isourceblock, itemstack);
                    }

                    d3 = 0.0D;
                }

                ItemStack itemstack1 = itemstack.a(1);
                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(d0, d1 + d3, d2));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    ++itemstack.count;
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ++itemstack.count;
                        ItemStack itemstack2 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack2.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack2);
                            return itemstack;
                        }
                    }

                    EntityBoat entityboat = new EntityBoat(world, blockdispenseevent.getVelocity().getX(), blockdispenseevent.getVelocity().getY(), blockdispenseevent.getVelocity().getZ());

                    world.addEntity(entityboat);
                    return itemstack;
                }
            }

            protected void a(ISourceBlock isourceblock) {
                isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
            }
        });
        DispenseBehaviorItem dispensebehavioritem = new DispenseBehaviorItem() {
            private final DispenseBehaviorItem b = new DispenseBehaviorItem();

            public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                ItemBucket itembucket = (ItemBucket) itemstack.getItem();
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(BlockDispenser.b(isourceblock.f()));
                World world = isourceblock.i();
                int i = blockposition.getX();
                int j = blockposition.getY();
                int k = blockposition.getZ();

                if (world.isEmpty(blockposition) || !world.getType(blockposition).getBlock().getMaterial().isBuildable()) {
                    org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                    CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack);
                    BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(i, j, k));

                    if (!BlockDispenser.eventFired) {
                        world.getServer().getPluginManager().callEvent(blockdispenseevent);
                    }

                    if (blockdispenseevent.isCancelled()) {
                        return itemstack;
                    }

                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ItemStack itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack1.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack1);
                            return itemstack;
                        }
                    }

                    itembucket = (ItemBucket) CraftItemStack.asNMSCopy(blockdispenseevent.getItem()).getItem();
                }

                if (itembucket.a(isourceblock.i(), blockposition)) {
                    Item item = Items.BUCKET;

                    if (--itemstack.count == 0) {
                        itemstack.setItem(Items.BUCKET);
                        itemstack.count = 1;
                    } else if (((TileEntityDispenser) isourceblock.getTileEntity()).addItem(new ItemStack(item)) < 0) {
                        this.b.a(isourceblock, new ItemStack(item));
                    }

                    return itemstack;
                } else {
                    return this.b.a(isourceblock, itemstack);
                }
            }
        };

        BlockDispenser.N.a(Items.LAVA_BUCKET, dispensebehavioritem);
        BlockDispenser.N.a(Items.WATER_BUCKET, dispensebehavioritem);
        BlockDispenser.N.a(Items.BUCKET, new DispenseBehaviorItem() {
            private final DispenseBehaviorItem b = new DispenseBehaviorItem();

            public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                World world = isourceblock.i();
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(BlockDispenser.b(isourceblock.f()));
                IBlockData iblockdata = world.getType(blockposition);
                Block block = iblockdata.getBlock();
                Material material = block.getMaterial();
                Item item;

                if (Material.WATER.equals(material) && block instanceof BlockFluids && ((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue() == 0) {
                    item = Items.WATER_BUCKET;
                } else {
                    if (!Material.LAVA.equals(material) || !(block instanceof BlockFluids) || ((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue() != 0) {
                        return super.b(isourceblock, itemstack);
                    }

                    item = Items.LAVA_BUCKET;
                }

                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(blockposition.getX(), blockposition.getY(), blockposition.getZ()));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ItemStack itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack1.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack1);
                            return itemstack;
                        }
                    }

                    world.setAir(blockposition);
                    if (--itemstack.count == 0) {
                        itemstack.setItem(item);
                        itemstack.count = 1;
                    } else if (((TileEntityDispenser) isourceblock.getTileEntity()).addItem(new ItemStack(item)) < 0) {
                        this.b.a(isourceblock, new ItemStack(item));
                    }

                    return itemstack;
                }
            }
        });
        BlockDispenser.N.a(Items.FLINT_AND_STEEL, new DispenseBehaviorItem() {
            private boolean b = true;

            protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                World world = isourceblock.i();
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(BlockDispenser.b(isourceblock.f()));
                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(0, 0, 0));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ItemStack itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack1.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack1);
                            return itemstack;
                        }
                    }

                    if (world.isEmpty(blockposition)) {
                        if (!CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ()).isCancelled()) {
                            world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                            if (itemstack.isDamaged(1, world.random)) {
                                itemstack.count = 0;
                            }
                        }
                    } else if (world.getType(blockposition).getBlock() == Blocks.TNT) {
                        Blocks.TNT.postBreak(world, blockposition, Blocks.TNT.getBlockData().set(BlockTNT.EXPLODE, Boolean.valueOf(true)));
                        world.setAir(blockposition);
                    } else {
                        this.b = false;
                    }

                    return itemstack;
                }
            }

            protected void a(ISourceBlock isourceblock) {
                if (this.b) {
                    isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
                } else {
                    isourceblock.i().triggerEffect(1001, isourceblock.getBlockPosition(), 0);
                }

            }
        });
        BlockDispenser.N.a(Items.DYE, new DispenseBehaviorItem() {
            private boolean b = true;

            protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                if (EnumColor.WHITE == EnumColor.fromInvColorIndex(itemstack.getData())) {
                    World world = isourceblock.i();
                    BlockPosition blockposition = isourceblock.getBlockPosition().shift(BlockDispenser.b(isourceblock.f()));
                    org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                    CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack);
                    BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector(0, 0, 0));

                    if (!BlockDispenser.eventFired) {
                        world.getServer().getPluginManager().callEvent(blockdispenseevent);
                    }

                    if (blockdispenseevent.isCancelled()) {
                        return itemstack;
                    } else {
                        if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                            ItemStack itemstack1 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                            IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack1.getItem());

                            if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                                idispensebehavior.a(isourceblock, itemstack1);
                                return itemstack;
                            }
                        }

                        if (ItemDye.a(itemstack, world, blockposition)) {
                            if (!world.isClientSide) {
                                world.triggerEffect(2005, blockposition, 0);
                            }
                        } else {
                            this.b = false;
                        }

                        return itemstack;
                    }
                } else {
                    return super.b(isourceblock, itemstack);
                }
            }

            protected void a(ISourceBlock isourceblock) {
                if (this.b) {
                    isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
                } else {
                    isourceblock.i().triggerEffect(1001, isourceblock.getBlockPosition(), 0);
                }

            }
        });
        BlockDispenser.N.a(Item.getItemOf(Blocks.TNT), new DispenseBehaviorItem() {
            protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                World world = isourceblock.i();
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(BlockDispenser.b(isourceblock.f()));
                ItemStack itemstack1 = itemstack.a(1);
                org.bukkit.block.Block org_bukkit_block_block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);
                BlockDispenseEvent blockdispenseevent = new BlockDispenseEvent(org_bukkit_block_block, craftitemstack.clone(), new Vector((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D));

                if (!BlockDispenser.eventFired) {
                    world.getServer().getPluginManager().callEvent(blockdispenseevent);
                }

                if (blockdispenseevent.isCancelled()) {
                    ++itemstack.count;
                    return itemstack;
                } else {
                    if (!blockdispenseevent.getItem().equals(craftitemstack)) {
                        ++itemstack.count;
                        ItemStack itemstack2 = CraftItemStack.asNMSCopy(blockdispenseevent.getItem());
                        IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.N.get(itemstack2.getItem());

                        if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                            idispensebehavior.a(isourceblock, itemstack2);
                            return itemstack;
                        }
                    }

                    EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, blockdispenseevent.getVelocity().getX(), blockdispenseevent.getVelocity().getY(), blockdispenseevent.getVelocity().getZ(), (EntityLiving) null);

                    world.addEntity(entitytntprimed);
                    world.makeSound(entitytntprimed, "game.tnt.primed", 1.0F, 1.0F);
                    return itemstack;
                }
            }
        });
        BlockDispenser.N.a(Items.SKULL, new DispenseBehaviorItem() {
            private boolean b = true;

            protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                World world = isourceblock.i();
                EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(enumdirection);
                BlockSkull blockskull = Blocks.SKULL;

                if (world.isEmpty(blockposition) && blockskull.b(world, blockposition, itemstack)) {
                    if (!world.isClientSide) {
                        world.setTypeAndData(blockposition, blockskull.getBlockData().set(BlockSkull.FACING, EnumDirection.UP), 3);
                        TileEntity tileentity = world.getTileEntity(blockposition);

                        if (tileentity instanceof TileEntitySkull) {
                            if (itemstack.getData() == 3) {
                                GameProfile gameprofile = null;

                                if (itemstack.hasTag()) {
                                    NBTTagCompound nbttagcompound = itemstack.getTag();

                                    if (nbttagcompound.hasKeyOfType("SkullOwner", 10)) {
                                        gameprofile = GameProfileSerializer.deserialize(nbttagcompound.getCompound("SkullOwner"));
                                    } else if (nbttagcompound.hasKeyOfType("SkullOwner", 8)) {
                                        gameprofile = new GameProfile((UUID) null, nbttagcompound.getString("SkullOwner"));
                                    }
                                }

                                ((TileEntitySkull) tileentity).setGameProfile(gameprofile);
                            } else {
                                ((TileEntitySkull) tileentity).setSkullType(itemstack.getData());
                            }

                            ((TileEntitySkull) tileentity).setRotation(enumdirection.opposite().b() * 4);
                            Blocks.SKULL.a(world, blockposition, (TileEntitySkull) tileentity);
                        }

                        --itemstack.count;
                    }
                } else {
                    this.b = false;
                }

                return itemstack;
            }

            protected void a(ISourceBlock isourceblock) {
                if (this.b) {
                    isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
                } else {
                    isourceblock.i().triggerEffect(1001, isourceblock.getBlockPosition(), 0);
                }

            }
        });
        BlockDispenser.N.a(Item.getItemOf(Blocks.PUMPKIN), new DispenseBehaviorItem() {
            private boolean b = true;

            protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
                World world = isourceblock.i();
                BlockPosition blockposition = isourceblock.getBlockPosition().shift(BlockDispenser.b(isourceblock.f()));
                BlockPumpkin blockpumpkin = (BlockPumpkin) Blocks.PUMPKIN;

                if (world.isEmpty(blockposition) && blockpumpkin.e(world, blockposition)) {
                    if (!world.isClientSide) {
                        world.setTypeAndData(blockposition, blockpumpkin.getBlockData(), 3);
                    }

                    --itemstack.count;
                } else {
                    this.b = false;
                }

                return itemstack;
            }

            protected void a(ISourceBlock isourceblock) {
                if (this.b) {
                    isourceblock.i().triggerEffect(1000, isourceblock.getBlockPosition(), 0);
                } else {
                    isourceblock.i().triggerEffect(1001, isourceblock.getBlockPosition(), 0);
                }

            }
        });
    }

    public static void c() {
        if (!DispenserRegistry.b) {
            DispenserRegistry.b = true;
            if (DispenserRegistry.c.isDebugEnabled()) {
                d();
            }

            Block.S();
            BlockFire.l();
            Item.t();
            StatisticList.a();
            b();
        }

    }

    private static void d() {
        System.setErr(new RedirectStream("STDERR", System.err));
        System.setOut(new RedirectStream("STDOUT", DispenserRegistry.a));
    }
}
