package net.minecraft.server.v1_8_R3;

class BlockSkull$2 {

    static final int[] a = new int[EnumDirection.values().length];

    static {
        try {
            BlockSkull$2.a[EnumDirection.UP.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockSkull$2.a[EnumDirection.NORTH.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockSkull$2.a[EnumDirection.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockSkull$2.a[EnumDirection.WEST.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockSkull$2.a[EnumDirection.EAST.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

    }
}
