package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class ContainerDispenser extends Container {

    public IInventory items;
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;

    public ContainerDispenser(IInventory iinventory, IInventory iinventory1) {
        this.items = iinventory1;
        this.player = (PlayerInventory) iinventory;

        int i;
        int j;

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.a(new Slot(iinventory1, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.a(new Slot(iinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.a(new Slot(iinventory, i, 8 + i * 18, 142));
        }

    }

    public boolean a(EntityHuman entityhuman) {
        return !this.checkReachable ? true : this.items.a(entityhuman);
    }

    public ItemStack b(EntityHuman entityhuman, int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i < 9) {
                if (!this.a(itemstack1, 9, 45, true)) {
                    return null;
                }
            } else if (!this.a(itemstack1, 0, 9, false)) {
                return null;
            }

            if (itemstack1.count == 0) {
                slot.set((ItemStack) null);
            } else {
                slot.f();
            }

            if (itemstack1.count == itemstack.count) {
                return null;
            }

            slot.a(entityhuman, itemstack1);
        }

        return itemstack;
    }

    public CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        } else {
            CraftInventory craftinventory = new CraftInventory(this.items);

            this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), craftinventory, this);
            return this.bukkitEntity;
        }
    }

    public InventoryView getBukkitView() {
        return this.getBukkitView();
    }
}
