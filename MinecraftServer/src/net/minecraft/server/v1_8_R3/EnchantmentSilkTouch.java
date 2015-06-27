package net.minecraft.server.v1_8_R3;

public class EnchantmentSilkTouch extends Enchantment {

    protected EnchantmentSilkTouch(int i, MinecraftKey minecraftkey, int j) {
        super(i, minecraftkey, j, EnchantmentSlotType.DIGGER);
        this.c("untouching");
    }

    public int a(int i) {
        return 20;
    }

    public int b(int i) {
        return super.a(i) + 80;
    }

    public int getMaxLevel() {
        return 3;
    }

    public boolean a(Enchantment enchantment) {
        return super.a(enchantment) && enchantment.id != EnchantmentSilkTouch.LOOT_BONUS_BLOCKS.id;
    }

    public boolean canEnchant(ItemStack itemstack) {
        return itemstack.getItem() == Items.SHEARS ? true : super.canEnchant(itemstack);
    }
}
