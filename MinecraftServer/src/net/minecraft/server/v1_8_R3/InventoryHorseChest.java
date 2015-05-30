package net.minecraft.server.v1_8_R3;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryHorseChest extends InventorySubcontainer {

    public List<HumanEntity> transaction = new ArrayList();
    private EntityHorse horse;
    private int maxStack = 64;

    public InventoryHorseChest(String s, int i) {
        super(s, false, i);
    }

    public InventoryHorseChest(String s, int i, EntityHorse entityhorse) {
        super(s, false, i, (CraftHorse) entityhorse.getBukkitEntity());
        this.horse = entityhorse;
    }

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

    public InventoryHolder getOwner() {
        return (Horse) this.horse.getBukkitEntity();
    }

    public void setMaxStackSize(int i) {
        this.maxStack = i;
    }

    public int getMaxStackSize() {
        return this.maxStack;
    }
}
