package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventorySubcontainer implements IInventory {

    private String a;
    private int b;
    public ItemStack[] items;
    private List<IInventoryListener> d;
    private boolean e;
    public List<HumanEntity> transaction;
    private int maxStack;
    protected InventoryHolder bukkitOwner;

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

    public InventoryHolder getOwner() {
        return this.bukkitOwner;
    }

    public InventorySubcontainer(String s, boolean flag, int i) {
        this(s, flag, i, (InventoryHolder) null);
    }

    public InventorySubcontainer(String s, boolean flag, int i, InventoryHolder inventoryholder) {
        this.transaction = new ArrayList();
        this.maxStack = 64;
        this.bukkitOwner = inventoryholder;
        this.a = s;
        this.e = flag;
        this.b = i;
        this.items = new ItemStack[i];
    }

    public void a(IInventoryListener iinventorylistener) {
        if (this.d == null) {
            this.d = Lists.newArrayList();
        }

        this.d.add(iinventorylistener);
    }

    public void b(IInventoryListener iinventorylistener) {
        this.d.remove(iinventorylistener);
    }

    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.length ? this.items[i] : null;
    }

    public ItemStack splitStack(int i, int j) {
        if (this.items[i] != null) {
            ItemStack itemstack;

            if (this.items[i].count <= j) {
                itemstack = this.items[i];
                this.items[i] = null;
                this.update();
                return itemstack;
            } else {
                itemstack = this.items[i].a(j);
                if (this.items[i].count == 0) {
                    this.items[i] = null;
                }

                this.update();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack a(ItemStack itemstack) {
        ItemStack itemstack1 = itemstack.cloneItemStack();

        for (int i = 0; i < this.b; ++i) {
            ItemStack itemstack2 = this.getItem(i);

            if (itemstack2 == null) {
                this.setItem(i, itemstack1);
                this.update();
                return null;
            }

            if (ItemStack.c(itemstack2, itemstack1)) {
                int j = Math.min(this.getMaxStackSize(), itemstack2.getMaxStackSize());
                int k = Math.min(itemstack1.count, j - itemstack2.count);

                if (k > 0) {
                    itemstack2.count += k;
                    itemstack1.count -= k;
                    if (itemstack1.count <= 0) {
                        this.update();
                        return null;
                    }
                }
            }
        }

        if (itemstack1.count != itemstack.count) {
            this.update();
        }

        return itemstack1;
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
        this.items[i] = itemstack;
        if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
            itemstack.count = this.getMaxStackSize();
        }

        this.update();
    }

    public int getSize() {
        return this.b;
    }

    public String getName() {
        return this.a;
    }

    public boolean hasCustomName() {
        return this.e;
    }

    public void a(String s) {
        this.e = true;
        this.a = s;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return (IChatBaseComponent) (this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatMessage(this.getName(), new Object[0]));
    }

    public int getMaxStackSize() {
        return 64;
    }

    public void update() {
        if (this.d != null) {
            for (int i = 0; i < this.d.size(); ++i) {
                ((IInventoryListener) this.d.get(i)).a(this);
            }
        }

    }

    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    public void startOpen(EntityHuman entityhuman) {}

    public void closeContainer(EntityHuman entityhuman) {}

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public int getProperty(int i) {
        return 0;
    }

    public void b(int i, int j) {}

    public int g() {
        return 0;
    }

    public void l() {
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = null;
        }

    }
}
