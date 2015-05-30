package net.minecraft.server.v1_8_R3;

public class ItemCoal extends Item {

    public ItemCoal() {
        this.a(true);
        this.setMaxDurability(0);
        this.a(CreativeModeTab.l);
    }

    public String e_(ItemStack itemstack) {
        return itemstack.getData() == 1 ? "item.charcoal" : "item.coal";
    }
}
