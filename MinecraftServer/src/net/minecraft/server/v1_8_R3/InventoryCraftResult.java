package net.minecraft.server.v1_8_R3;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryCraftResult implements IInventory {

    private ItemStack[] items = new ItemStack[1];
    private int maxStack = 64;

    public ItemStack[] getContents() {
        return this.items;
    }

    public InventoryHolder getOwner() {
        return null;
    }

    public void onOpen(CraftHumanEntity crafthumanentity) {}

    public void onClose(CraftHumanEntity crafthumanentity) {}

    public List<HumanEntity> getViewers() {
        return new ArrayList();
    }

    public void setMaxStackSize(int i) {
        this.maxStack = i;
    }

    public InventoryCraftResult() {}

    public int getSize() {
        return 1;
    }

    public ItemStack getItem(int i) {
        return this.items[0];
    }

    public String getName() {
        return "Result";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return (IChatBaseComponent) (this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatMessage(this.getName(), new Object[0]));
    }

    public ItemStack splitStack(int i, int j) {
        if (this.items[0] != null) {
            ItemStack itemstack = this.items[0];

            this.items[0] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public ItemStack splitWithoutUpdate(int i) {
        if (this.items[0] != null) {
            ItemStack itemstack = this.items[0];

            this.items[0] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setItem(int i, ItemStack itemstack) {
        this.items[0] = itemstack;
    }

    public int getMaxStackSize() {
        return this.maxStack;
    }

    public void update() {}

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
