package net.minecraft.server.v1_8_R3;

class BlockTripwireHook$1 {

    static final int[] a = new int[EnumDirection.values().length];

    static {
        try {
            BlockTripwireHook$1.a[EnumDirection.EAST.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockTripwireHook$1.a[EnumDirection.WEST.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockTripwireHook$1.a[EnumDirection.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockTripwireHook$1.a[EnumDirection.NORTH.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

    }
}
