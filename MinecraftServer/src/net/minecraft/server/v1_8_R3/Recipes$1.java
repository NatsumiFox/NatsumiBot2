package net.minecraft.server.v1_8_R3;

import java.util.Comparator;

class Recipes$1 implements Comparator<IRecipe> {

    final CraftingManager a;

    Recipes$1(CraftingManager craftingmanager) {
        this.a = craftingmanager;
    }

    public int a(IRecipe irecipe, IRecipe irecipe1) {
        return irecipe instanceof ShapelessRecipes && irecipe1 instanceof ShapedRecipes ? 1 : (irecipe1 instanceof ShapelessRecipes && irecipe instanceof ShapedRecipes ? -1 : (irecipe1.a() < irecipe.a() ? -1 : (irecipe1.a() > irecipe.a() ? 1 : 0)));
    }

    public int compare(Object object, Object object1) {
        return this.a((IRecipe) object, (IRecipe) object1);
    }
}
