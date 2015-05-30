package net.minecraft.server.v1_8_R3;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryLargeChest implements ITileInventory {

    private String a;
    public ITileInventory left;
    public ITileInventory right;
    public List<HumanEntity> transaction = new ArrayList();

    public ItemStack[] getContents() {
        ItemStack[] aitemstack = new ItemStack[this.getSize()];

        for (int i = 0; i < aitemstack.length; ++i) {
            aitemstack[i] = this.getItem(i);
        }

        return aitemstack;
    }

    public void onOpen(CraftHumanEntity crafthumanentity) {
        this.left.onOpen(crafthumanentity);
        this.right.onOpen(crafthumanentity);
        this.transaction.add(crafthumanentity);
    }

    public void onClose(CraftHumanEntity crafthumanentity) {
        this.left.onClose(crafthumanentity);
        this.right.onClose(crafthumanentity);
        this.transaction.remove(crafthumanentity);
    }

    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    public InventoryHolder getOwner() {
        return null;
    }

    public void setMaxStackSize(int i) {
        this.left.setMaxStackSize(i);
        this.right.setMaxStackSize(i);
    }

    public InventoryLargeChest(String s, ITileInventory itileinventory, ITileInventory itileinventory1) {
        this.a = s;
        if (itileinventory == null) {
            itileinventory = itileinventory1;
        }

        if (itileinventory1 == null) {
            itileinventory1 = itileinventory;
        }

        this.left = itileinventory;
        this.right = itileinventory1;
        if (itileinventory.r_()) {
            itileinventory1.a(itileinventory.i());
        } else if (itileinventory1.r_()) {
            itileinventory.a(itileinventory1.i());
        }

    }

    public int getSize() {
        return this.left.getSize() + this.right.getSize();
    }

    public boolean a(IInventory iinventory) {
        return this.left == iinventory || this.right == iinventory;
    }

    public String getName() {
        return this.left.hasCustomName() ? this.left.getName() : (this.right.hasCustomName() ? this.right.getName() : this.a);
    }

    public boolean hasCustomName() {
        return this.left.hasCustomName() || this.right.hasCustomName();
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return (IChatBaseComponent) (this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatMessage(this.getName(), new Object[0]));
    }

    public ItemStack getItem(int i) {
        return i >= this.left.getSize() ? this.right.getItem(i - this.left.getSize()) : this.left.getItem(i);
    }

    public ItemStack splitStack(int i, int j) {
        return i >= this.left.getSize() ? this.right.splitStack(i - this.left.getSize(), j) : this.left.splitStack(i, j);
    }

    public ItemStack splitWithoutUpdate(int i) {
        return i >= this.left.getSize() ? this.right.splitWithoutUpdate(i - this.left.getSize()) : this.left.splitWithoutUpdate(i);
    }

    public void setItem(int i, ItemStack itemstack) {
        if (i >= this.left.getSize()) {
            this.right.setItem(i - this.left.getSize(), itemstack);
        } else {
            this.left.setItem(i, itemstack);
        }

    }

    public int getMaxStackSize() {
        return Math.min(this.left.getMaxStackSize(), this.right.getMaxStackSize());
    }

    public void update() {
        this.left.update();
        this.right.update();
    }

    public boolean a(EntityHuman entityhuman) {
        return this.left.a(entityhuman) && this.right.a(entityhuman);
    }

    public void startOpen(EntityHuman entityhuman) {
        this.left.startOpen(entityhuman);
        this.right.startOpen(entityhuman);
    }

    public void closeContainer(EntityHuman entityhuman) {
        this.left.closeContainer(entityhuman);
        this.right.closeContainer(entityhuman);
    }

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

    public boolean r_() {
        return this.left.r_() || this.right.r_();
    }

    public void a(ChestLock chestlock) {
        this.left.a(chestlock);
        this.right.a(chestlock);
    }

    public ChestLock i() {
        return this.left.i();
    }

    public String getContainerName() {
        return this.left.getContainerName();
    }

    public Container createContainer(PlayerInventory playerinventory, EntityHuman entityhuman) {
        return new ContainerChest(playerinventory, this, entityhuman);
    }

    public void l() {
        this.left.l();
        this.right.l();
    }
}
