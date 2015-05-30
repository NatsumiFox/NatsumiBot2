package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractManager {

    public World world;
    public EntityPlayer player;
    private WorldSettings.EnumGamemode gamemode;
    private boolean d;
    private int lastDigTick;
    private BlockPosition f;
    private int currentTick;
    private boolean h;
    private BlockPosition i;
    private int j;
    private int k;
    public boolean interactResult = false;
    public boolean firedInteract = false;

    public PlayerInteractManager(World world) {
        this.gamemode = WorldSettings.EnumGamemode.NOT_SET;
        this.f = BlockPosition.ZERO;
        this.i = BlockPosition.ZERO;
        this.k = -1;
        this.world = world;
    }

    public void setGameMode(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        this.gamemode = worldsettings_enumgamemode;
        worldsettings_enumgamemode.a(this.player.abilities);
        this.player.updateAbilities();
        this.player.server.getPlayerList().sendAll(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, new EntityPlayer[] { this.player}));
    }

    public WorldSettings.EnumGamemode getGameMode() {
        return this.gamemode;
    }

    public boolean c() {
        return this.gamemode.e();
    }

    public boolean isCreative() {
        return this.gamemode.d();
    }

    public void b(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        if (this.gamemode == WorldSettings.EnumGamemode.NOT_SET) {
            this.gamemode = worldsettings_enumgamemode;
        }

        this.setGameMode(this.gamemode);
    }

    public void a() {
        this.currentTick = MinecraftServer.currentTick;
        float f;
        int i;

        if (this.h) {
            int j = this.currentTick - this.j;
            Block block = this.world.getType(this.i).getBlock();

            if (block.getMaterial() == Material.AIR) {
                this.h = false;
            } else {
                f = block.getDamage(this.player, this.player.world, this.i) * (float) (j + 1);
                i = (int) (f * 10.0F);
                if (i != this.k) {
                    this.world.c(this.player.getId(), this.i, i);
                    this.k = i;
                }

                if (f >= 1.0F) {
                    this.h = false;
                    this.breakBlock(this.i);
                }
            }
        } else if (this.d) {
            Block block1 = this.world.getType(this.f).getBlock();

            if (block1.getMaterial() == Material.AIR) {
                this.world.c(this.player.getId(), this.f, -1);
                this.k = -1;
                this.d = false;
            } else {
                int k = this.currentTick - this.lastDigTick;

                f = block1.getDamage(this.player, this.player.world, this.i) * (float) (k + 1);
                i = (int) (f * 10.0F);
                if (i != this.k) {
                    this.world.c(this.player.getId(), this.f, i);
                    this.k = i;
                }
            }
        }

    }

    public void a(BlockPosition blockposition, EnumDirection enumdirection) {
        PlayerInteractEvent playerinteractevent = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.inventory.getItemInHand());

        if (playerinteractevent.isCancelled()) {
            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
            TileEntity tileentity = this.world.getTileEntity(blockposition);

            if (tileentity != null) {
                this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
            }

        } else {
            if (this.isCreative()) {
                if (!this.world.douseFire((EntityHuman) null, blockposition, enumdirection)) {
                    this.breakBlock(blockposition);
                }
            } else {
                Block block = this.world.getType(blockposition).getBlock();

                if (this.gamemode.c()) {
                    if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                        return;
                    }

                    if (!this.player.cn()) {
                        ItemStack itemstack = this.player.bZ();

                        if (itemstack == null) {
                            return;
                        }

                        if (!itemstack.c(block)) {
                            return;
                        }
                    }
                }

                this.lastDigTick = this.currentTick;
                float f = 1.0F;

                if (playerinteractevent.useInteractedBlock() == Result.DENY) {
                    IBlockData iblockdata = this.world.getType(blockposition);

                    if (block == Blocks.WOODEN_DOOR) {
                        boolean flag = iblockdata.get(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;

                        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, flag ? blockposition.up() : blockposition.down()));
                    } else if (block == Blocks.TRAPDOOR) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                    }
                } else if (block.getMaterial() != Material.AIR) {
                    block.attack(this.world, blockposition, this.player);
                    f = block.getDamage(this.player, this.player.world, blockposition);
                    this.world.douseFire((EntityHuman) null, blockposition, enumdirection);
                }

                if (playerinteractevent.useItemInHand() == Result.DENY) {
                    if (f > 1.0F) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                    }

                    return;
                }

                BlockDamageEvent blockdamageevent = CraftEventFactory.callBlockDamageEvent(this.player, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.player.inventory.getItemInHand(), f >= 1.0F);

                if (blockdamageevent.isCancelled()) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                    return;
                }

                if (blockdamageevent.getInstaBreak()) {
                    f = 2.0F;
                }

                if (block.getMaterial() != Material.AIR && f >= 1.0F) {
                    this.breakBlock(blockposition);
                } else {
                    this.d = true;
                    this.f = blockposition;
                    int i = (int) (f * 10.0F);

                    this.world.c(this.player.getId(), blockposition, i);
                    this.k = i;
                }
            }

            this.world.spigotConfig.antiXrayInstance.updateNearbyBlocks(this.world, blockposition);
        }
    }

    public void a(BlockPosition blockposition) {
        if (blockposition.equals(this.f)) {
            this.currentTick = MinecraftServer.currentTick;
            int i = this.currentTick - this.lastDigTick;
            Block block = this.world.getType(blockposition).getBlock();

            if (block.getMaterial() != Material.AIR) {
                float f = block.getDamage(this.player, this.player.world, blockposition) * (float) (i + 1);

                if (f >= 0.7F) {
                    this.d = false;
                    this.world.c(this.player.getId(), blockposition, -1);
                    this.breakBlock(blockposition);
                } else if (!this.h) {
                    this.d = false;
                    this.h = true;
                    this.i = blockposition;
                    this.j = this.lastDigTick;
                }
            }
        } else {
            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
        }

    }

    public void e() {
        this.d = false;
        this.world.c(this.player.getId(), this.f, -1);
    }

    private boolean c(BlockPosition blockposition) {
        IBlockData iblockdata = this.world.getType(blockposition);

        iblockdata.getBlock().a(this.world, blockposition, iblockdata, (EntityHuman) this.player);
        boolean flag = this.world.setAir(blockposition);

        if (flag) {
            iblockdata.getBlock().postBreak(this.world, blockposition, iblockdata);
        }

        return flag;
    }

    public boolean breakBlock(BlockPosition blockposition) {
        BlockBreakEvent blockbreakevent = null;

        if (this.player instanceof EntityPlayer) {
            org.bukkit.block.Block org_bukkit_block_block = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            boolean flag = this.gamemode.d() && this.player.bA() != null && this.player.bA().getItem() instanceof ItemSword;

            if (this.world.getTileEntity(blockposition) == null && !flag) {
                PacketPlayOutBlockChange packetplayoutblockchange = new PacketPlayOutBlockChange(this.world, blockposition);

                packetplayoutblockchange.block = Blocks.AIR.getBlockData();
                this.player.playerConnection.sendPacket(packetplayoutblockchange);
            }

            blockbreakevent = new BlockBreakEvent(org_bukkit_block_block, this.player.getBukkitEntity());
            blockbreakevent.setCancelled(flag);
            IBlockData iblockdata = this.world.getType(blockposition);
            Block block = iblockdata.getBlock();

            if (block != null && !blockbreakevent.isCancelled() && !this.isCreative() && this.player.b(block) && (!block.I() || !EnchantmentManager.hasSilkTouchEnchantment(this.player))) {
                org_bukkit_block_block.getData();
                int i = EnchantmentManager.getBonusBlockLootEnchantmentLevel(this.player);

                blockbreakevent.setExpToDrop(block.getExpDrop(this.world, iblockdata, i));
            }

            this.world.getServer().getPluginManager().callEvent(blockbreakevent);
            if (blockbreakevent.isCancelled()) {
                if (flag) {
                    return false;
                }

                this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                TileEntity tileentity = this.world.getTileEntity(blockposition);

                if (tileentity != null) {
                    this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
                }

                return false;
            }
        }

        IBlockData iblockdata1 = this.world.getType(blockposition);

        if (iblockdata1.getBlock() == Blocks.AIR) {
            return false;
        } else {
            TileEntity tileentity1 = this.world.getTileEntity(blockposition);

            if (iblockdata1.getBlock() == Blocks.SKULL && !this.isCreative()) {
                iblockdata1.getBlock().dropNaturally(this.world, blockposition, iblockdata1, 1.0F, 0);
                return this.c(blockposition);
            } else {
                if (this.gamemode.c()) {
                    if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                        return false;
                    }

                    if (!this.player.cn()) {
                        ItemStack itemstack = this.player.bZ();

                        if (itemstack == null) {
                            return false;
                        }

                        if (!itemstack.c(iblockdata1.getBlock())) {
                            return false;
                        }
                    }
                }

                this.world.a(this.player, 2001, blockposition, Block.getCombinedId(iblockdata1));
                boolean flag1 = this.c(blockposition);

                if (this.isCreative()) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                } else {
                    ItemStack itemstack1 = this.player.bZ();
                    boolean flag2 = this.player.b(iblockdata1.getBlock());

                    if (itemstack1 != null) {
                        itemstack1.a(this.world, iblockdata1.getBlock(), blockposition, this.player);
                        if (itemstack1.count == 0) {
                            this.player.ca();
                        }
                    }

                    if (flag1 && flag2) {
                        iblockdata1.getBlock().a(this.world, this.player, blockposition, iblockdata1, tileentity1);
                    }
                }

                if (flag1 && blockbreakevent != null) {
                    iblockdata1.getBlock().dropExperience(this.world, blockposition, blockbreakevent.getExpToDrop());
                }

                return flag1;
            }
        }
    }

    public boolean useItem(EntityHuman entityhuman, World world, ItemStack itemstack) {
        if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
            return false;
        } else {
            int i = itemstack.count;
            int j = itemstack.getData();
            ItemStack itemstack1 = itemstack.a(world, entityhuman);

            if (itemstack1 == itemstack && (itemstack1 == null || itemstack1.count == i && itemstack1.l() <= 0 && itemstack1.getData() == j)) {
                return false;
            } else {
                entityhuman.inventory.items[entityhuman.inventory.itemInHandIndex] = itemstack1;
                if (this.isCreative()) {
                    itemstack1.count = i;
                    if (itemstack1.e()) {
                        itemstack1.setData(j);
                    }
                }

                if (itemstack1.count == 0) {
                    entityhuman.inventory.items[entityhuman.inventory.itemInHandIndex] = null;
                }

                if (!entityhuman.bS()) {
                    ((EntityPlayer) entityhuman).updateInventory(entityhuman.defaultContainer);
                }

                return true;
            }
        }
    }

    public boolean interact(EntityHuman entityhuman, World world, ItemStack itemstack, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        IBlockData iblockdata = world.getType(blockposition);
        boolean flag = false;

        if (iblockdata.getBlock() != Blocks.AIR) {
            boolean flag1 = false;

            if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                TileEntity tileentity = world.getTileEntity(blockposition);

                flag1 = !(tileentity instanceof ITileInventory) && !(tileentity instanceof IInventory);
            }

            if (!entityhuman.getBukkitEntity().isOp() && itemstack != null && Block.asBlock(itemstack.getItem()) instanceof BlockCommand) {
                flag1 = true;
            }

            PlayerInteractEvent playerinteractevent = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, blockposition, enumdirection, itemstack, flag1);

            this.firedInteract = true;
            this.interactResult = playerinteractevent.useItemInHand() == Result.DENY;
            if (playerinteractevent.useInteractedBlock() == Result.DENY) {
                if (iblockdata.getBlock() instanceof BlockDoor) {
                    boolean flag2 = iblockdata.get(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;

                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutBlockChange(world, flag2 ? blockposition.up() : blockposition.down()));
                }

                flag = playerinteractevent.useItemInHand() != Result.ALLOW;
            } else {
                if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                    TileEntity tileentity1 = world.getTileEntity(blockposition);

                    if (tileentity1 instanceof ITileInventory) {
                        Block block = world.getType(blockposition).getBlock();
                        ITileInventory itileinventory = (ITileInventory) tileentity1;

                        if (itileinventory instanceof TileEntityChest && block instanceof BlockChest) {
                            itileinventory = ((BlockChest) block).f(world, blockposition);
                        }

                        if (itileinventory != null) {
                            entityhuman.openContainer(itileinventory);
                            return true;
                        }
                    } else if (tileentity1 instanceof IInventory) {
                        entityhuman.openContainer((IInventory) tileentity1);
                        return true;
                    }

                    return false;
                }

                if (!entityhuman.isSneaking() || itemstack == null) {
                    flag = iblockdata.getBlock().interact(world, blockposition, iblockdata, entityhuman, enumdirection, f, f1, f2);
                }
            }

            if (itemstack != null && !flag && !this.interactResult) {
                int i = itemstack.getData();
                int j = itemstack.count;

                flag = itemstack.placeItem(entityhuman, world, blockposition, enumdirection, f, f1, f2);
                if (this.isCreative()) {
                    itemstack.setData(i);
                    itemstack.count = j;
                }
            }
        }

        return flag;
    }

    public void a(WorldServer worldserver) {
        this.world = worldserver;
    }
}
