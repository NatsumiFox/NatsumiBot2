package net.minecraft.server.v1_8_R3;

class BlockLever$1 {

    static final int[] a;
    static final int[] b;
    static final int[] c = new int[EnumDirection.EnumAxis.values().length];

    static {
        try {
            BlockLever$1.c[EnumDirection.EnumAxis.X.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockLever$1.c[EnumDirection.EnumAxis.Z.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        b = new int[BlockLever.EnumLeverPosition.values().length];

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.EAST.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.WEST.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.NORTH.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror5) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.UP_Z.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror6) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.UP_X.ordinal()] = 6;
        } catch (NoSuchFieldError nosuchfielderror7) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.DOWN_X.ordinal()] = 7;
        } catch (NoSuchFieldError nosuchfielderror8) {
            ;
        }

        try {
            BlockLever$1.b[BlockLever.EnumLeverPosition.DOWN_Z.ordinal()] = 8;
        } catch (NoSuchFieldError nosuchfielderror9) {
            ;
        }

        a = new int[EnumDirection.values().length];

        try {
            BlockLever$1.a[EnumDirection.DOWN.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror10) {
            ;
        }

        try {
            BlockLever$1.a[EnumDirection.UP.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror11) {
            ;
        }

        try {
            BlockLever$1.a[EnumDirection.NORTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror12) {
            ;
        }

        try {
            BlockLever$1.a[EnumDirection.SOUTH.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror13) {
            ;
        }

        try {
            BlockLever$1.a[EnumDirection.WEST.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror14) {
            ;
        }

        try {
            BlockLever$1.a[EnumDirection.EAST.ordinal()] = 6;
        } catch (NoSuchFieldError nosuchfielderror15) {
            ;
        }

    }
}
