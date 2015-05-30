package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;

public abstract class Container {

    public List<ItemStack> b = Lists.newArrayList();
    public List<Slot> c = Lists.newArrayList();
    public int windowId;
    private int dragType = -1;
    private int g;
    private final Set<Slot> h = Sets.newHashSet();
    protected List<ICrafting> listeners = Lists.newArrayList();
    private Set<EntityHuman> i = Sets.newHashSet();
    private int tickCount;
    public boolean checkReachable = true;

    public abstract InventoryView getBukkitView();

    public void transferTo(Container container, CraftHumanEntity crafthumanentity) {
        InventoryView inventoryview = this.getBukkitView();
        InventoryView inventoryview1 = container.getBukkitView();

        ((CraftInventory) inventoryview.getTopInventory()).getInventory().onClose(crafthumanentity);
        ((CraftInventory) inventoryview.getBottomInventory()).getInventory().onClose(crafthumanentity);
        ((CraftInventory) inventoryview1.getTopInventory()).getInventory().onOpen(crafthumanentity);
        ((CraftInventory) inventoryview1.getBottomInventory()).getInventory().onOpen(crafthumanentity);
    }

    public Container() {}

    protected Slot a(Slot slot) {
        slot.rawSlotIndex = this.c.size();
        this.c.add(slot);
        this.b.add((Object) null);
        return slot;
    }

    public void addSlotListener(ICrafting icrafting) {
        if (this.listeners.contains(icrafting)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.listeners.add(icrafting);
            icrafting.a(this, this.a());
            this.b();
        }
    }

    public List<ItemStack> a() {
        ArrayList arraylist = Lists.newArrayList();

        for (int i = 0; i < this.c.size(); ++i) {
            arraylist.add(((Slot) this.c.get(i)).getItem());
        }

        return arraylist;
    }

    public void b() {
        for (int i = 0; i < this.c.size(); ++i) {
            ItemStack itemstack = ((Slot) this.c.get(i)).getItem();
            ItemStack itemstack1 = (ItemStack) this.b.get(i);

            if (!ItemStack.fastMatches(itemstack1, itemstack) || this.tickCount % 20 == 0 && !ItemStack.matches(itemstack1, itemstack)) {
                itemstack1 = itemstack == null ? null : itemstack.cloneItemStack();
                this.b.set(i, itemstack1);

                for (int j = 0; j < this.listeners.size(); ++j) {
                    ((ICrafting) this.listeners.get(j)).a(this, i, itemstack1);
                }
            }
        }

        ++this.tickCount;
    }

    public boolean a(EntityHuman entityhuman, int i) {
        return false;
    }

    public Slot getSlot(IInventory iinventory, int i) {
        for (int j = 0; j < this.c.size(); ++j) {
            Slot slot = (Slot) this.c.get(j);

            if (slot.a(iinventory, i)) {
                return slot;
            }
        }

        return null;
    }

    public Slot getSlot(int i) {
        return (Slot) this.c.get(i);
    }

    public ItemStack b(EntityHuman entityhuman, int i) {
        Slot slot = (Slot) this.c.get(i);

        return slot != null ? slot.getItem() : null;
    }

    public ItemStack clickItem(int i, int j, int k, EntityHuman entityhuman) {
        ItemStack itemstack = null;
        PlayerInventory playerinventory = entityhuman.inventory;
        ItemStack itemstack1;
        int l;

        if (k == 5) {
            int i1 = this.g;

            this.g = c(j);
            if ((i1 != 1 || this.g != 2) && i1 != this.g) {
                this.d();
            } else if (playerinventory.getCarried() == null) {
                this.d();
            } else if (this.g == 0) {
                this.dragType = b(j);
                if (a(this.dragType, entityhuman)) {
                    this.g = 1;
                    this.h.clear();
                } else {
                    this.d();
                }
            } else if (this.g == 1) {
                Slot slot = (Slot) this.c.get(i);

                if (slot != null && a(slot, playerinventory.getCarried(), true) && slot.isAllowed(playerinventory.getCarried()) && playerinventory.getCarried().count > this.h.size() && this.b(slot)) {
                    this.h.add(slot);
                }
            } else if (this.g == 2) {
                if (!this.h.isEmpty()) {
                    itemstack1 = playerinventory.getCarried().cloneItemStack();
                    l = playerinventory.getCarried().count;
                    Iterator iterator = this.h.iterator();
                    HashMap hashmap = new HashMap();

                    while (iterator.hasNext()) {
                        Slot slot1 = (Slot) iterator.next();

                        if (slot1 != null && a(slot1, playerinventory.getCarried(), true) && slot1.isAllowed(playerinventory.getCarried()) && playerinventory.getCarried().count >= this.h.size() && this.b(slot1)) {
                            ItemStack itemstack2 = itemstack1.cloneItemStack();
                            int j1 = slot1.hasItem() ? slot1.getItem().count : 0;

                            a(this.h, this.dragType, itemstack2, j1);
                            if (itemstack2.count > itemstack2.getMaxStackSize()) {
                                itemstack2.count = itemstack2.getMaxStackSize();
                            }

                            if (itemstack2.count > slot1.getMaxStackSize(itemstack2)) {
                                itemstack2.count = slot1.getMaxStackSize(itemstack2);
                            }

                            l -= itemstack2.count - j1;
                            hashmap.put(Integer.valueOf(slot1.rawSlotIndex), itemstack2);
                        }
                    }

                    InventoryView inventoryview = this.getBukkitView();
                    CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(itemstack1);

                    craftitemstack.setAmount(l);
                    HashMap hashmap1 = new HashMap();
                    Iterator iterator1 = hashmap.entrySet().iterator();

                    while (iterator1.hasNext()) {
                        Entry entry = (Entry) iterator1.next();

                        hashmap1.put((Integer) entry.getKey(), CraftItemStack.asBukkitCopy((ItemStack) entry.getValue()));
                    }

                    ItemStack itemstack3 = playerinventory.getCarried();

                    playerinventory.setCarried(CraftItemStack.asNMSCopy(craftitemstack));
                    InventoryDragEvent inventorydragevent = new InventoryDragEvent(inventoryview, craftitemstack.getType() != org.bukkit.Material.AIR ? craftitemstack : null, CraftItemStack.asBukkitCopy(itemstack3), this.dragType == 1, hashmap1);

                    entityhuman.world.getServer().getPluginManager().callEvent(inventorydragevent);
                    boolean flag = inventorydragevent.getResult() != Result.DEFAULT;

                    if (inventorydragevent.getResult() != Result.DENY) {
                        Iterator iterator2 = hashmap.entrySet().iterator();

                        while (iterator2.hasNext()) {
                            Entry entry1 = (Entry) iterator2.next();

                            inventoryview.setItem(((Integer) entry1.getKey()).intValue(), CraftItemStack.asBukkitCopy((ItemStack) entry1.getValue()));
                        }

                        if (playerinventory.getCarried() != null) {
                            playerinventory.setCarried(CraftItemStack.asNMSCopy(inventorydragevent.getCursor()));
                            flag = true;
                        }
                    } else {
                        playerinventory.setCarried(itemstack3);
                    }

                    if (flag && entityhuman instanceof EntityPlayer) {
                        ((EntityPlayer) entityhuman).updateInventory(this);
                    }
                }

                this.d();
            } else {
                this.d();
            }
        } else if (this.g != 0) {
            this.d();
        } else {
            Slot slot2;
            int k1;
            ItemStack itemstack4;
            int l1;

            if ((k == 0 || k == 1) && (j == 0 || j == 1)) {
                ItemStack itemstack5;

                if (i == -999) {
                    if (playerinventory.getCarried() != null) {
                        if (j == 0) {
                            entityhuman.drop(playerinventory.getCarried(), true);
                            playerinventory.setCarried((ItemStack) null);
                        }

                        if (j == 1) {
                            itemstack5 = playerinventory.getCarried();
                            if (itemstack5.count > 0) {
                                entityhuman.drop(itemstack5.a(1), true);
                            }

                            if (itemstack5.count == 0) {
                                playerinventory.setCarried((ItemStack) null);
                            }
                        }
                    }
                } else if (k == 1) {
                    if (i < 0) {
                        return null;
                    }

                    slot2 = (Slot) this.c.get(i);
                    if (slot2 != null && slot2.isAllowed(entityhuman)) {
                        itemstack1 = this.b(entityhuman, i);
                        if (itemstack1 != null) {
                            Item item = itemstack1.getItem();

                            itemstack = itemstack1.cloneItemStack();
                            if (slot2.getItem() != null && slot2.getItem().getItem() == item) {
                                this.a(i, j, true, entityhuman);
                            }
                        }
                    }
                } else {
                    if (i < 0) {
                        return null;
                    }

                    slot2 = (Slot) this.c.get(i);
                    if (slot2 != null) {
                        itemstack1 = slot2.getItem();
                        itemstack5 = playerinventory.getCarried();
                        if (itemstack1 != null) {
                            itemstack = itemstack1.cloneItemStack();
                        }

                        if (itemstack1 == null) {
                            if (itemstack5 != null && slot2.isAllowed(itemstack5)) {
                                k1 = j == 0 ? itemstack5.count : 1;
                                if (k1 > slot2.getMaxStackSize(itemstack5)) {
                                    k1 = slot2.getMaxStackSize(itemstack5);
                                }

                                if (itemstack5.count >= k1) {
                                    slot2.set(itemstack5.a(k1));
                                }

                                if (itemstack5.count == 0) {
                                    playerinventory.setCarried((ItemStack) null);
                                } else if (entityhuman instanceof EntityPlayer) {
                                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityhuman.inventory.getCarried()));
                                }
                            }
                        } else if (slot2.isAllowed(entityhuman)) {
                            if (itemstack5 == null) {
                                k1 = j == 0 ? itemstack1.count : (itemstack1.count + 1) / 2;
                                itemstack4 = slot2.a(k1);
                                playerinventory.setCarried(itemstack4);
                                if (itemstack1.count == 0) {
                                    slot2.set((ItemStack) null);
                                }

                                slot2.a(entityhuman, playerinventory.getCarried());
                            } else if (slot2.isAllowed(itemstack5)) {
                                if (itemstack1.getItem() == itemstack5.getItem() && itemstack1.getData() == itemstack5.getData() && ItemStack.equals(itemstack1, itemstack5)) {
                                    k1 = j == 0 ? itemstack5.count : 1;
                                    if (k1 > slot2.getMaxStackSize(itemstack5) - itemstack1.count) {
                                        k1 = slot2.getMaxStackSize(itemstack5) - itemstack1.count;
                                    }

                                    if (k1 > itemstack5.getMaxStackSize() - itemstack1.count) {
                                        k1 = itemstack5.getMaxStackSize() - itemstack1.count;
                                    }

                                    itemstack5.a(k1);
                                    if (itemstack5.count == 0) {
                                        playerinventory.setCarried((ItemStack) null);
                                    } else if (entityhuman instanceof EntityPlayer) {
                                        ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityhuman.inventory.getCarried()));
                                    }

                                    itemstack1.count += k1;
                                } else if (itemstack5.count <= slot2.getMaxStackSize(itemstack5)) {
                                    slot2.set(itemstack5);
                                    playerinventory.setCarried(itemstack1);
                                }
                            } else if (itemstack1.getItem() == itemstack5.getItem() && itemstack5.getMaxStackSize() > 1 && (!itemstack1.usesData() || itemstack1.getData() == itemstack5.getData()) && ItemStack.equals(itemstack1, itemstack5)) {
                                k1 = itemstack1.count;
                                l1 = Math.min(itemstack5.getMaxStackSize(), slot2.getMaxStackSize());
                                if (k1 > 0 && k1 + itemstack5.count <= l1) {
                                    itemstack5.count += k1;
                                    itemstack1 = slot2.a(k1);
                                    if (itemstack1.count == 0) {
                                        slot2.set((ItemStack) null);
                                    }

                                    slot2.a(entityhuman, playerinventory.getCarried());
                                } else if (entityhuman instanceof EntityPlayer) {
                                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityhuman.inventory.getCarried()));
                                }
                            }
                        }

                        slot2.f();
                        if (entityhuman instanceof EntityPlayer && slot2.getMaxStackSize() != 64) {
                            ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(this.windowId, slot2.rawSlotIndex, slot2.getItem()));
                            if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
                                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(this.windowId, 0, this.getSlot(0).getItem()));
                            }
                        }
                    }
                }
            } else if (k == 2 && j >= 0 && j < 9) {
                slot2 = (Slot) this.c.get(i);
                if (slot2.isAllowed(entityhuman)) {
                    itemstack1 = playerinventory.getItem(j);
                    boolean flag1 = itemstack1 == null || slot2.inventory == playerinventory && slot2.isAllowed(itemstack1);

                    k1 = -1;
                    if (!flag1) {
                        k1 = playerinventory.getFirstEmptySlotIndex();
                        flag1 |= k1 > -1;
                    }

                    if (slot2.hasItem() && flag1) {
                        itemstack4 = slot2.getItem();
                        playerinventory.setItem(j, itemstack4.cloneItemStack());
                        if ((slot2.inventory != playerinventory || !slot2.isAllowed(itemstack1)) && itemstack1 != null) {
                            if (k1 > -1) {
                                playerinventory.pickup(itemstack1);
                                slot2.a(itemstack4.count);
                                slot2.set((ItemStack) null);
                                slot2.a(entityhuman, itemstack4);
                            }
                        } else {
                            slot2.a(itemstack4.count);
                            slot2.set(itemstack1);
                            slot2.a(entityhuman, itemstack4);
                        }
                    } else if (!slot2.hasItem() && itemstack1 != null && slot2.isAllowed(itemstack1)) {
                        playerinventory.setItem(j, (ItemStack) null);
                        slot2.set(itemstack1);
                    }
                }
            } else if (k == 3 && entityhuman.abilities.canInstantlyBuild && playerinventory.getCarried() == null && i >= 0) {
                slot2 = (Slot) this.c.get(i);
                if (slot2 != null && slot2.hasItem()) {
                    itemstack1 = slot2.getItem().cloneItemStack();
                    itemstack1.count = itemstack1.getMaxStackSize();
                    playerinventory.setCarried(itemstack1);
                }
            } else if (k == 4 && playerinventory.getCarried() == null && i >= 0) {
                slot2 = (Slot) this.c.get(i);
                if (slot2 != null && slot2.hasItem() && slot2.isAllowed(entityhuman)) {
                    itemstack1 = slot2.a(j == 0 ? 1 : slot2.getItem().count);
                    slot2.a(entityhuman, itemstack1);
                    entityhuman.drop(itemstack1, true);
                }
            } else if (k == 6 && i >= 0) {
                slot2 = (Slot) this.c.get(i);
                itemstack1 = playerinventory.getCarried();
                if (itemstack1 != null && (slot2 == null || !slot2.hasItem() || !slot2.isAllowed(entityhuman))) {
                    l = j == 0 ? 0 : this.c.size() - 1;
                    k1 = j == 0 ? 1 : -1;

                    for (int i2 = 0; i2 < 2; ++i2) {
                        for (l1 = l; l1 >= 0 && l1 < this.c.size() && itemstack1.count < itemstack1.getMaxStackSize(); l1 += k1) {
                            Slot slot3 = (Slot) this.c.get(l1);

                            if (slot3.hasItem() && a(slot3, itemstack1, true) && slot3.isAllowed(entityhuman) && this.a(itemstack1, slot3) && (i2 != 0 || slot3.getItem().count != slot3.getItem().getMaxStackSize())) {
                                int j2 = Math.min(itemstack1.getMaxStackSize() - itemstack1.count, slot3.getItem().count);
                                ItemStack itemstack6 = slot3.a(j2);

                                itemstack1.count += j2;
                                if (itemstack6.count <= 0) {
                                    slot3.set((ItemStack) null);
                                }

                                slot3.a(entityhuman, itemstack6);
                            }
                        }
                    }
                }

                this.b();
            }
        }

        return itemstack;
    }

    public boolean a(ItemStack itemstack, Slot slot) {
        return true;
    }

    protected void a(int i, int j, boolean flag, EntityHuman entityhuman) {
        this.clickItem(i, j, 1, entityhuman);
    }

    public void b(EntityHuman entityhuman) {
        PlayerInventory playerinventory = entityhuman.inventory;

        if (playerinventory.getCarried() != null) {
            entityhuman.drop(playerinventory.getCarried(), false);
            playerinventory.setCarried((ItemStack) null);
        }

    }

    public void a(IInventory iinventory) {
        this.b();
    }

    public void setItem(int i, ItemStack itemstack) {
        this.getSlot(i).set(itemstack);
    }

    public boolean c(EntityHuman entityhuman) {
        return !this.i.contains(entityhuman);
    }

    public void a(EntityHuman entityhuman, boolean flag) {
        if (flag) {
            this.i.remove(entityhuman);
        } else {
            this.i.add(entityhuman);
        }

    }

    public abstract boolean a(EntityHuman entityhuman);

    protected boolean a(ItemStack itemstack, int i, int j, boolean flag) {
        boolean flag1 = false;
        int k = i;

        if (flag) {
            k = j - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (itemstack.isStackable()) {
            while (itemstack.count > 0 && (!flag && k < j || flag && k >= i)) {
                slot = (Slot) this.c.get(k);
                itemstack1 = slot.getItem();
                if (itemstack1 != null && itemstack1.getItem() == itemstack.getItem() && (!itemstack.usesData() || itemstack.getData() == itemstack1.getData()) && ItemStack.equals(itemstack, itemstack1)) {
                    int l = itemstack1.count + itemstack.count;
                    int i1 = Math.min(itemstack.getMaxStackSize(), slot.getMaxStackSize());

                    if (l <= i1) {
                        itemstack.count = 0;
                        itemstack1.count = l;
                        slot.f();
                        flag1 = true;
                    } else if (itemstack1.count < i1) {
                        itemstack.count -= i1 - itemstack1.count;
                        itemstack1.count = i1;
                        slot.f();
                        flag1 = true;
                    }
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (itemstack.count > 0) {
            if (flag) {
                k = j - 1;
            } else {
                k = i;
            }

            while (!flag && k < j || flag && k >= i) {
                slot = (Slot) this.c.get(k);
                itemstack1 = slot.getItem();
                if (itemstack1 == null) {
                    slot.set(itemstack.cloneItemStack());
                    slot.f();
                    itemstack.count = 0;
                    flag1 = true;
                    break;
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        return flag1;
    }

    public static int b(int i) {
        return i >> 2 & 3;
    }

    public static int c(int i) {
        return i & 3;
    }

    public static boolean a(int i, EntityHuman entityhuman) {
        return i == 0 ? true : (i == 1 ? true : i == 2 && entityhuman.abilities.canInstantlyBuild);
    }

    protected void d() {
        this.g = 0;
        this.h.clear();
    }

    public static boolean a(Slot slot, ItemStack itemstack, boolean flag) {
        boolean flag1 = slot == null || !slot.hasItem();

        if (slot != null && slot.hasItem() && itemstack != null && itemstack.doMaterialsMatch(slot.getItem()) && ItemStack.equals(slot.getItem(), itemstack)) {
            flag1 |= slot.getItem().count + (flag ? 0 : itemstack.count) <= itemstack.getMaxStackSize();
        }

        return flag1;
    }

    public static void a(Set<Slot> set, int i, ItemStack itemstack, int j) {
        switch (i) {
        case 0:
            itemstack.count = MathHelper.d((float) itemstack.count / (float) set.size());
            break;

        case 1:
            itemstack.count = 1;
            break;

        case 2:
            itemstack.count = itemstack.getItem().getMaxStackSize();
        }

        itemstack.count += j;
    }

    public boolean b(Slot slot) {
        return true;
    }

    public static int a(TileEntity tileentity) {
        return tileentity instanceof IInventory ? b((IInventory) tileentity) : 0;
    }

    public static int b(IInventory iinventory) {
        if (iinventory == null) {
            return 0;
        } else {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < iinventory.getSize(); ++j) {
                ItemStack itemstack = iinventory.getItem(j);

                if (itemstack != null) {
                    f += (float) itemstack.count / (float) Math.min(iinventory.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float) iinventory.getSize();
            return MathHelper.d(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }
}
