package net.minecraft.server.v1_8_R3;

class BlockVine$1 {

    static final int[] a = new int[EnumDirection.values().length];

    static {
        try {
            BlockVine$1.a[EnumDirection.UP.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockVine$1.a[EnumDirection.NORTH.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockVine$1.a[EnumDirection.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockVine$1.a[EnumDirection.EAST.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockVine$1.a[EnumDirection.WEST.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

    }
}
