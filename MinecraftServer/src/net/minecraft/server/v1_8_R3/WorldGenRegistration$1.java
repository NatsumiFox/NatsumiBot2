package net.minecraft.server.v1_8_R3;

class WorldGenRegistration$1 {

    static final int[] a = new int[EnumDirection.values().length];

    static {
        try {
            WorldGenRegistration$1.a[EnumDirection.NORTH.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            WorldGenRegistration$1.a[EnumDirection.SOUTH.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

    }
}
