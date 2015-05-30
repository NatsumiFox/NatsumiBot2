package net.minecraft.server.v1_8_R3;

class BlockSapling$1 {

    static final int[] a = new int[BlockWood.EnumLogVariant.values().length];

    static {
        try {
            BlockSapling$1.a[BlockWood.EnumLogVariant.SPRUCE.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockSapling$1.a[BlockWood.EnumLogVariant.BIRCH.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockSapling$1.a[BlockWood.EnumLogVariant.JUNGLE.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockSapling$1.a[BlockWood.EnumLogVariant.ACACIA.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockSapling$1.a[BlockWood.EnumLogVariant.DARK_OAK.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

        try {
            BlockSapling$1.a[BlockWood.EnumLogVariant.OAK.ordinal()] = 6;
        } catch (NoSuchFieldError nosuchfielderror5) {
            ;
        }

    }
}
