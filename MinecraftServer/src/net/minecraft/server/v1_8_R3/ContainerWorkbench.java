package net.minecraft.server.v1_8_R3;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class ContainerWorkbench extends Container {

    public InventoryCrafting craftInventory;
    public IInventory resultInventory = new InventoryCraftResult();
    private World g;
    private BlockPosition h;
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;

    public ContainerWorkbench(PlayerInventory playerinventory, World world, BlockPosition blockposition) {
        this.craftInventory = new InventoryCrafting(this, 3, 3, playerinventory.player);
        this.craftInventory.resultInventory = this.resultInventory;
        this.player = playerinventory;
        this.g = world;
        this.h = blockposition;
        this.a((Slot) (new SlotResult(playerinventory.player, this.craftInventory, this.resultInventory, 0, 124, 35)));

        int i;
        int j;

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.a(new Slot(this.craftInventory, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.a(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.a(new Slot(playerinventory, i, 8 + i * 18, 142));
        }

        this.a((IInventory) this.craftInventory);
    }

    public void a(IInventory iinventory) {
        CraftingManager.getInstance().lastCraftView = this.getBukkitView();
        ItemStack itemstack = CraftingManager.getInstance().craft(this.craftInventory, this.g);

        this.resultInventory.setItem(0, itemstack);
        if (super.listeners.size() >= 1) {
            if (itemstack == null || itemstack.getItem() != Items.FILLED_MAP) {
                EntityPlayer entityplayer = (EntityPlayer) super.listeners.get(0);

                entityplayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(entityplayer.activeContainer.windowId, 0, itemstack));
            }
        }
    }

    public void b(EntityHuman entityhuman) {
        super.b(entityhuman);
        if (!this.g.isClientSide) {
            for (int i = 0; i < 9; ++i) {
                ItemStack itemstack = this.craftInventory.splitWithoutUpdate(i);

                if (itemstack != null) {
                    entityhuman.drop(itemstack, false);
                }
            }
        }

    }

    public boolean a(EntityHuman entityhuman) {
        return !this.checkReachable ? true : (this.g.getType(this.h).getBlock() != Blocks.CRAFTING_TABLE ? false : entityhuman.e((double) this.h.getX() + 0.5D, (double) this.h.getY() + 0.5D, (double) this.h.getZ() + 0.5D) <= 64.0D);
    }

    public ItemStack b(EntityHuman entityhuman, int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i == 0) {
                if (!this.a(itemstack1, 10, 46, true)) {
                    return null;
                }

                slot.a(itemstack1, itemstack);
            } else if (i >= 10 && i < 37) {
                if (!this.a(itemstack1, 37, 46, false)) {
                    return null;
                }
            } else if (i >= 37 && i < 46) {
                if (!this.a(itemstack1, 10, 37, false)) {
                    return null;
                }
            } else if (!this.a(itemstack1, 10, 46, false)) {
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

    public boolean a(ItemStack itemstack, Slot slot) {
        return slot.inventory != this.resultInventory && super.a(itemstack, slot);
    }

    public CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        } else {
            CraftInventoryCrafting craftinventorycrafting = new CraftInventoryCrafting(this.craftInventory, this.resultInventory);

            this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), craftinventorycrafting, this);
            return this.bukkitEntity;
        }
    }

    public InventoryView getBukkitView() {
        return this.getBukkitView();
    }
}
