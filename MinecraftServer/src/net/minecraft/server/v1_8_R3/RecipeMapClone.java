package net.minecraft.server.v1_8_R3;

import java.util.Arrays;

public class RecipeMapClone extends ShapelessRecipes implements IRecipe {

    public RecipeMapClone() {
        super(new ItemStack(Items.MAP, 0, -1), Arrays.asList(new ItemStack[] { new ItemStack(Items.MAP, 0, 0)}));
    }

    public boolean a(InventoryCrafting inventorycrafting, World world) {
        int i = 0;
        ItemStack itemstack = null;

        for (int j = 0; j < inventorycrafting.getSize(); ++j) {
            ItemStack itemstack1 = inventorycrafting.getItem(j);

            if (itemstack1 != null) {
                if (itemstack1.getItem() == Items.FILLED_MAP) {
                    if (itemstack != null) {
                        return false;
                    }

                    itemstack = itemstack1;
                } else {
                    if (itemstack1.getItem() != Items.MAP) {
                        return false;
                    }

                    ++i;
                }
            }
        }

        if (itemstack != null && i > 0) {
            return true;
        } else {
            return false;
        }
    }

    public ItemStack a(InventoryCrafting inventorycrafting) {
        int i = 0;
        ItemStack itemstack = null;

        for (int j = 0; j < inventorycrafting.getSize(); ++j) {
            ItemStack itemstack1 = inventorycrafting.getItem(j);

            if (itemstack1 != null) {
                if (itemstack1.getItem() == Items.FILLED_MAP) {
                    if (itemstack != null) {
                        return null;
                    }

                    itemstack = itemstack1;
                } else {
                    if (itemstack1.getItem() != Items.MAP) {
                        return null;
                    }

                    ++i;
                }
            }
        }

        if (itemstack != null && i >= 1) {
            ItemStack itemstack2 = new ItemStack(Items.FILLED_MAP, i + 1, itemstack.getData());

            if (itemstack.hasName()) {
                itemstack2.c(itemstack.getName());
            }

            return itemstack2;
        } else {
            return null;
        }
    }

    public int a() {
        return 9;
    }

    public ItemStack b() {
        return null;
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
}
