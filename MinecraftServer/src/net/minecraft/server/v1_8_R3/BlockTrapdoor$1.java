package net.minecraft.server.v1_8_R3;

class BlockTrapdoor$1 {

    static final int[] a = new int[EnumDirection.values().length];

    static {
        try {
            BlockTrapdoor$1.a[EnumDirection.NORTH.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockTrapdoor$1.a[EnumDirection.SOUTH.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockTrapdoor$1.a[EnumDirection.WEST.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockTrapdoor$1.a[EnumDirection.EAST.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

    }
}
