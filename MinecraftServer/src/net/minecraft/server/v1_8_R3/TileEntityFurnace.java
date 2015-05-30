package net.minecraft.server.v1_8_R3;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

public class TileEntityFurnace extends TileEntityContainer implements IUpdatePlayerListBox, IWorldInventory {

    private static final int[] a = new int[1];
    private static final int[] f = new int[] { 2, 1};
    private static final int[] g = new int[] { 1};
    private ItemStack[] items = new ItemStack[3];
    public int burnTime;
    private int ticksForCurrentFuel;
    public int cookTime;
    private int cookTimeTotal;
    private String m;
    private int lastTick;
    private int maxStack;
    public List<HumanEntity> transaction;

    public ItemStack[] getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity crafthumanentity) {
        this.transaction.add(crafthumanentity);
    }

    public void onClose(CraftHumanEntity crafthumanentity) {
        this.transaction.remove(crafthumanentity);
    }

    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    public void setMaxStackSize(int i) {
        this.maxStack = i;
    }

    public TileEntityFurnace() {
        this.lastTick = MinecraftServer.currentTick;
        this.maxStack = 64;
        this.transaction = new ArrayList();
    }

    public int getSize() {
        return this.items.length;
    }

    public ItemStack getItem(int i) {
        return this.items[i];
    }

    public ItemStack splitStack(int i, int j) {
        if (this.items[i] != null) {
            ItemStack itemstack;

            if (this.items[i].count <= j) {
                itemstack = this.items[i];
                this.items[i] = null;
                return itemstack;
            } else {
                itemstack = this.items[i].a(j);
                if (this.items[i].count == 0) {
                    this.items[i] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack splitWithoutUpdate(int i) {
        if (this.items[i] != null) {
            ItemStack itemstack = this.items[i];

            this.items[i] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setItem(int i, ItemStack itemstack) {
        boolean flag = itemstack != null && itemstack.doMaterialsMatch(this.items[i]) && ItemStack.equals(itemstack, this.items[i]);

        this.items[i] = itemstack;
        if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
            itemstack.count = this.getMaxStackSize();
        }

        if (i == 0 && !flag) {
            this.cookTimeTotal = this.a(itemstack);
            this.cookTime = 0;
            this.update();
        }

    }

    public String getName() {
        return this.hasCustomName() ? this.m : "container.furnace";
    }

    public boolean hasCustomName() {
        return this.m != null && this.m.length() > 0;
    }

    public void a(String s) {
        this.m = s;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);

        this.items = new ItemStack[this.getSize()];

        for (int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < this.items.length) {
                this.items[b0] = ItemStack.createStack(nbttagcompound1);
            }
        }

        this.burnTime = nbttagcompound.getShort("BurnTime");
        this.cookTime = nbttagcompound.getShort("CookTime");
        this.cookTimeTotal = nbttagcompound.getShort("CookTimeTotal");
        this.ticksForCurrentFuel = fuelTime(this.items[1]);
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.m = nbttagcompound.getString("CustomName");
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setShort("BurnTime", (short) this.burnTime);
        nbttagcompound.setShort("CookTime", (short) this.cookTime);
        nbttagcompound.setShort("CookTimeTotal", (short) this.cookTimeTotal);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                nbttagcompound1.setByte("Slot", (byte) i);
                this.items[i].save(nbttagcompound1);
                nbttaglist.add(nbttagcompound1);
            }
        }

        nbttagcompound.set("Items", nbttaglist);
        if (this.hasCustomName()) {
            nbttagcompound.setString("CustomName", this.m);
        }

    }

    public int getMaxStackSize() {
        return this.maxStack;
    }

    public boolean isBurning() {
        return this.burnTime > 0;
    }

    public void c() {
        boolean flag = this.w() == Blocks.LIT_FURNACE;
        boolean flag1 = false;
        int i = MinecraftServer.currentTick - this.lastTick;

        this.lastTick = MinecraftServer.currentTick;
        if (this.isBurning() && this.canBurn()) {
            this.cookTime += i;
            if (this.cookTime >= this.cookTimeTotal) {
                this.cookTime = 0;
                this.cookTimeTotal = this.a(this.items[0]);
                this.burn();
                flag1 = true;
            }
        } else {
            this.cookTime = 0;
        }

        if (this.isBurning()) {
            this.burnTime -= i;
        }

        if (!this.world.isClientSide) {
            if (!this.isBurning() && (this.items[1] == null || this.items[0] == null)) {
                if (!this.isBurning() && this.cookTime > 0) {
                    this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.cookTimeTotal);
                }
            } else if (this.burnTime <= 0 && this.canBurn()) {
                CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(this.items[1]);
                FurnaceBurnEvent furnaceburnevent = new FurnaceBurnEvent(this.world.getWorld().getBlockAt(this.position.getX(), this.position.getY(), this.position.getZ()), craftitemstack, fuelTime(this.items[1]));

                this.world.getServer().getPluginManager().callEvent(furnaceburnevent);
                if (furnaceburnevent.isCancelled()) {
                    return;
                }

                this.ticksForCurrentFuel = furnaceburnevent.getBurnTime();
                this.burnTime += this.ticksForCurrentFuel;
                if (this.burnTime > 0 && furnaceburnevent.isBurning()) {
                    flag1 = true;
                    if (this.items[1] != null) {
                        --this.items[1].count;
                        if (this.items[1].count == 0) {
                            Item item = this.items[1].getItem().q();

                            this.items[1] = item != null ? new ItemStack(item) : null;
                        }
                    }
                }
            }

            if (flag != this.isBurning()) {
                flag1 = true;
                BlockFurnace.a(this.isBurning(), this.world, this.position);
                this.E();
            }
        }

        if (flag1) {
            this.update();
        }

    }

    public int a(ItemStack itemstack) {
        return 200;
    }

    private boolean canBurn() {
        if (this.items[0] == null) {
            return false;
        } else {
            ItemStack itemstack = RecipesFurnace.getInstance().getResult(this.items[0]);

            return itemstack == null ? false : (this.items[2] == null ? true : (!this.items[2].doMaterialsMatch(itemstack) ? false : (this.items[2].count + itemstack.count <= this.getMaxStackSize() && this.items[2].count < this.items[2].getMaxStackSize() ? true : this.items[2].count + itemstack.count <= itemstack.getMaxStackSize())));
        }
    }

    public void burn() {
        if (this.canBurn()) {
            ItemStack itemstack = RecipesFurnace.getInstance().getResult(this.items[0]);
            CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(this.items[0]);
            org.bukkit.inventory.ItemStack org_bukkit_inventory_itemstack = CraftItemStack.asBukkitCopy(itemstack);
            FurnaceSmeltEvent furnacesmeltevent = new FurnaceSmeltEvent(this.world.getWorld().getBlockAt(this.position.getX(), this.position.getY(), this.position.getZ()), craftitemstack, org_bukkit_inventory_itemstack);

            this.world.getServer().getPluginManager().callEvent(furnacesmeltevent);
            if (furnacesmeltevent.isCancelled()) {
                return;
            }

            org_bukkit_inventory_itemstack = furnacesmeltevent.getResult();
            itemstack = CraftItemStack.asNMSCopy(org_bukkit_inventory_itemstack);
            if (itemstack != null) {
                if (this.items[2] == null) {
                    this.items[2] = itemstack;
                } else {
                    if (!CraftItemStack.asCraftMirror(this.items[2]).isSimilar(org_bukkit_inventory_itemstack)) {
                        return;
                    }

                    this.items[2].count += itemstack.count;
                }
            }

            if (this.items[0].getItem() == Item.getItemOf(Blocks.SPONGE) && this.items[0].getData() == 1 && this.items[1] != null && this.items[1].getItem() == Items.BUCKET) {
                this.items[1] = new ItemStack(Items.WATER_BUCKET);
            }

            --this.items[0].count;
            if (this.items[0].count <= 0) {
                this.items[0] = null;
            }
        }

    }

    public static int fuelTime(ItemStack itemstack) {
        if (itemstack == null) {
            return 0;
        } else {
            Item item = itemstack.getItem();

            if (item instanceof ItemBlock && Block.asBlock(item) != Blocks.AIR) {
                Block block = Block.asBlock(item);

                if (block == Blocks.WOODEN_SLAB) {
                    return 150;
                }

                if (block.getMaterial() == Material.WOOD) {
                    return 300;
                }

                if (block == Blocks.COAL_BLOCK) {
                    return 16000;
                }
            }

            return item instanceof ItemTool && ((ItemTool) item).h().equals("WOOD") ? 200 : (item instanceof ItemSword && ((ItemSword) item).h().equals("WOOD") ? 200 : (item instanceof ItemHoe && ((ItemHoe) item).g().equals("WOOD") ? 200 : (item == Items.STICK ? 100 : (item == Items.COAL ? 1600 : (item == Items.LAVA_BUCKET ? 20000 : (item == Item.getItemOf(Blocks.SAPLING) ? 100 : (item == Items.BLAZE_ROD ? 2400 : 0)))))));
        }
    }

    public static boolean isFuel(ItemStack itemstack) {
        return fuelTime(itemstack) > 0;
    }

    public boolean a(EntityHuman entityhuman) {
        return this.world.getTileEntity(this.position) != this ? false : entityhuman.e((double) this.position.getX() + 0.5D, (double) this.position.getY() + 0.5D, (double) this.position.getZ() + 0.5D) <= 64.0D;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        return i == 2 ? false : (i != 1 ? true : isFuel(itemstack) || SlotFurnaceFuel.c_(itemstack));
    }

    public int[] getSlotsForFace(EnumDirection enumdirection) {
        return enumdirection == EnumDirection.DOWN ? TileEntityFurnace.f : (enumdirection == EnumDirection.UP ? TileEntityFurnace.a : TileEntityFurnace.g);
    }

    public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection) {
        return this.b(i, itemstack);
    }

    public boolean canTakeItemThroughFace(int i, ItemStack itemstack, EnumDirection enumdirection) {
        if (enumdirection == EnumDirection.DOWN && i == 1) {
            Item item = itemstack.getItem();

            if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
                return false;
            }
        }

        return true;
    }

    public String getContainerName() {
        return "minecraft:furnace";
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        return new ContainerFurnace(playerinventory, this);
    }

    public int getProperty(int i) {
        switch (i) {
        case 0:
            return this.burnTime;

        case 1:
            return this.ticksForCurrentFuel;

        case 2:
            return this.cookTime;

        case 3:
            return this.cookTimeTotal;

        default:
            return 0;
        }
    }

    public void b(int i, int j) {
        switch (i) {
        case 0:
            this.burnTime = j;
            break;

        case 1:
            this.ticksForCurrentFuel = j;
            break;

        case 2:
            this.cookTime = j;
            break;

        case 3:
            this.cookTimeTotal = j;
        }

    }

    public int g() {
        return 4;
    }

    public void l() {
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = null;
        }

    }
}
