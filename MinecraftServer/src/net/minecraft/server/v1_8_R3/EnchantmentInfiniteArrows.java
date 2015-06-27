package net.minecraft.server.v1_8_R3;

public class EnchantmentInfiniteArrows extends Enchantment {

    public EnchantmentInfiniteArrows(int i, MinecraftKey minecraftkey, int j) {
        super(i, minecraftkey, j, EnchantmentSlotType.BOW);
        this.c("arrowInfinite");
    }

    public int a(int i) {
        return 26;
    }

    public int b(int i) {
        return 80;
    }

    public int getMaxLevel() {
        return 1;
    }
}
