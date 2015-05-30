package net.minecraft.server.v1_8_R3;

class BlockButtonAbstract$1 {

    static final int[] a = new int[EnumDirection.values().length];

    static {
        try {
            BlockButtonAbstract$1.a[EnumDirection.EAST.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockButtonAbstract$1.a[EnumDirection.WEST.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockButtonAbstract$1.a[EnumDirection.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockButtonAbstract$1.a[EnumDirection.NORTH.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockButtonAbstract$1.a[EnumDirection.UP.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

        try {
            BlockButtonAbstract$1.a[EnumDirection.DOWN.ordinal()] = 6;
        } catch (NoSuchFieldError nosuchfielderror5) {
            ;
        }

    }
}
