package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class ShapelessRecipes implements IRecipe {

    public final ItemStack result;
    private final List<ItemStack> ingredients;

    public ShapelessRecipes(ItemStack itemstack, List<ItemStack> list) {
        this.result = itemstack;
        this.ingredients = list;
    }

    public ShapelessRecipe toBukkitRecipe() {
        CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(this.result);
        CraftShapelessRecipe craftshapelessrecipe = new CraftShapelessRecipe(craftitemstack, this);
        Iterator iterator = this.ingredients.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (itemstack != null) {
                craftshapelessrecipe.addIngredient(CraftMagicNumbers.getMaterial(itemstack.getItem()), itemstack.getData());
            }
        }

        return craftshapelessrecipe;
    }

    public ItemStack b() {
        return this.result;
    }

    public ItemStack[] b(InventoryCrafting inventorycrafting) {
        ItemStack[] aitemstack = new ItemStack[inventorycrafting.getSize()];

        for (int i = 0; i < aitemstack.length; ++i) {
            ItemStack itemstack = inventorycrafting.getItem(i);

            if (itemstack != null && itemstack.getItem().r()) {
                aitemstack[i] = new ItemStack(itemstack.getItem().q());
            }
        }

        return aitemstack;
    }

    public boolean a(InventoryCrafting inventorycrafting, World world) {
        ArrayList arraylist = Lists.newArrayList(this.ingredients);

        for (int i = 0; i < inventorycrafting.h(); ++i) {
            for (int j = 0; j < inventorycrafting.i(); ++j) {
                ItemStack itemstack = inventorycrafting.c(j, i);

                if (itemstack != null) {
                    boolean flag = false;
                    Iterator iterator = arraylist.iterator();

                    while (iterator.hasNext()) {
                        ItemStack itemstack1 = (ItemStack) iterator.next();

                        if (itemstack.getItem() == itemstack1.getItem() && (itemstack1.getData() == 32767 || itemstack.getData() == itemstack1.getData())) {
                            flag = true;
                            arraylist.remove(itemstack1);
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }
            }
        }

        return arraylist.isEmpty();
    }

    public ItemStack a(InventoryCrafting inventorycrafting) {
        return this.result.cloneItemStack();
    }

    public int a() {
        return this.ingredients.size();
    }

    public List<ItemStack> getIngredients() {
        return Collections.unmodifiableList(this.ingredients);
    }

    public Recipe toBukkitRecipe() {
        return this.toBukkitRecipe();
    }
}
