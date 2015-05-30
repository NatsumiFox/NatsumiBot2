package net.minecraft.server.v1_8_R3;

class EntityMinecartAbstract$1 {

    static final int[] a;
    static final int[] b = new int[BlockMinecartTrackAbstract.EnumTrackPosition.values().length];

    static {
        try {
            EntityMinecartAbstract$1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            EntityMinecartAbstract$1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            EntityMinecartAbstract$1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            EntityMinecartAbstract$1.b[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        a = new int[EntityMinecartAbstract.EnumMinecartType.values().length];

        try {
            EntityMinecartAbstract$1.a[EntityMinecartAbstract.EnumMinecartType.CHEST.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

        try {
            EntityMinecartAbstract$1.a[EntityMinecartAbstract.EnumMinecartType.FURNACE.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror5) {
            ;
        }

        try {
            EntityMinecartAbstract$1.a[EntityMinecartAbstract.EnumMinecartType.TNT.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror6) {
            ;
        }

        try {
            EntityMinecartAbstract$1.a[EntityMinecartAbstract.EnumMinecartType.SPAWNER.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror7) {
            ;
        }

        try {
            EntityMinecartAbstract$1.a[EntityMinecartAbstract.EnumMinecartType.HOPPER.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror8) {
            ;
        }

        try {
            EntityMinecartAbstract$1.a[EntityMinecartAbstract.EnumMinecartType.COMMAND_BLOCK.ordinal()] = 6;
        } catch (NoSuchFieldError nosuchfielderror9) {
            ;
        }

    }
}
