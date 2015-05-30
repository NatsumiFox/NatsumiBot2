package net.minecraft.server.v1_8_R3;

class BlockPressurePlateBinary$1 {

    static final int[] a = new int[BlockPressurePlateBinary.EnumMobType.values().length];

    static {
        try {
            BlockPressurePlateBinary$1.a[BlockPressurePlateBinary.EnumMobType.EVERYTHING.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockPressurePlateBinary$1.a[BlockPressurePlateBinary.EnumMobType.MOBS.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

    }
}
