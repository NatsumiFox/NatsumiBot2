package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class ContainerChest extends Container {

    private IInventory container;
    private int f;
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;

    public CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        } else {
            Object object;

            if (this.container instanceof PlayerInventory) {
                object = new CraftInventoryPlayer((PlayerInventory) this.container);
            } else if (this.container instanceof InventoryLargeChest) {
                object = new CraftInventoryDoubleChest((InventoryLargeChest) this.container);
            } else {
                object = new CraftInventory(this.container);
            }

            this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), (Inventory) object, this);
            return this.bukkitEntity;
        }
    }

    public ContainerChest(IInventory iinventory, IInventory iinventory1, EntityHuman entityhuman) {
        this.container = iinventory1;
        this.f = iinventory1.getSize() / 9;
        iinventory1.startOpen(entityhuman);
        int i = (this.f - 4) * 18;

        this.player = (PlayerInventory) iinventory;

        int j;
        int k;

        for (j = 0; j < this.f; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new Slot(iinventory1, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new Slot(iinventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.a(new Slot(iinventory, j, 8 + j * 18, 161 + i));
        }

    }

    public boolean a(EntityHuman entityhuman) {
        return !this.checkReachable ? true : this.container.a(entityhuman);
    }

    public ItemStack b(EntityHuman entityhuman, int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i < this.f * 9) {
                if (!this.a(itemstack1, this.f * 9, this.c.size(), true)) {
                    return null;
                }
            } else if (!this.a(itemstack1, 0, this.f * 9, false)) {
                return null;
            }

            if (itemstack1.count == 0) {
                slot.set((ItemStack) null);
            } else {
                slot.f();
            }
        }

        return itemstack;
    }

    public void b(EntityHuman entityhuman) {
        super.b(entityhuman);
        this.container.closeContainer(entityhuman);
    }

    public IInventory e() {
        return this.container;
    }

    public InventoryView getBukkitView() {
        return this.getBukkitView();
    }
}
