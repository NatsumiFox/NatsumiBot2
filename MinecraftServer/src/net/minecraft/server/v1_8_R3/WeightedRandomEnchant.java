package net.minecraft.server.v1_8_R3;

public class WeightedRandomEnchant extends WeightedRandom.WeightedRandomChoice {

    public final Enchantment enchantment;
    public final int level;

    public WeightedRandomEnchant(Enchantment enchantment, int i) {
        super(enchantment.getRandomWeight());
        this.enchantment = enchantment;
        this.level = i;
    }
}
