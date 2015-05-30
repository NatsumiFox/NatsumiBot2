package net.minecraft.server.v1_8_R3;

class TileEntityPiston$1 {

    static final int[] a = new int[EnumDirection.EnumAxis.values().length];

    static {
        try {
            TileEntityPiston$1.a[EnumDirection.EnumAxis.X.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            TileEntityPiston$1.a[EnumDirection.EnumAxis.Y.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            TileEntityPiston$1.a[EnumDirection.EnumAxis.Z.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

    }
}
