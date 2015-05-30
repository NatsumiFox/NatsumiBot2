package net.minecraft.server.v1_8_R3;

import java.util.List;
import org.bukkit.inventory.Recipe;

public interface IRecipe {

    boolean a(InventoryCrafting inventorycrafting, World world);

    ItemStack a(InventoryCrafting inventorycrafting);

    int a();

    ItemStack b();

    ItemStack[] b(InventoryCrafting inventorycrafting);

    Recipe toBukkitRecipe();

    List<ItemStack> getIngredients();
}
