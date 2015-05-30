package net.minecraft.server.v1_8_R3;

import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

public interface IInventory extends INamableTileEntity {

    int MAX_STACK = 64;

    int getSize();

    ItemStack getItem(int i);

    ItemStack splitStack(int i, int j);

    ItemStack splitWithoutUpdate(int i);

    void setItem(int i, ItemStack itemstack);

    int getMaxStackSize();

    void update();

    boolean a(EntityHuman entityhuman);

    void startOpen(EntityHuman entityhuman);

    void closeContainer(EntityHuman entityhuman);

    boolean b(int i, ItemStack itemstack);

    int getProperty(int i);

    void b(int i, int j);

    int g();

    void l();

    ItemStack[] getContents();

    void onOpen(CraftHumanEntity crafthumanentity);

    void onClose(CraftHumanEntity crafthumanentity);

    List<HumanEntity> getViewers();

    InventoryHolder getOwner();

    void setMaxStackSize(int i);
}
