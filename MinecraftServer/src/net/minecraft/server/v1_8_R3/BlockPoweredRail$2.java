package net.minecraft.server.v1_8_R3;

class BlockPoweredRail$2 {

    static final int[] a = new int[BlockMinecartTrackAbstract.EnumTrackPosition.values().length];

    static {
        try {
            BlockPoweredRail$2.a[BlockMinecartTrackAbstract.EnumTrackPosition.NORTH_SOUTH.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockPoweredRail$2.a[BlockMinecartTrackAbstract.EnumTrackPosition.EAST_WEST.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockPoweredRail$2.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_EAST.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockPoweredRail$2.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_WEST.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockPoweredRail$2.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_NORTH.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

        try {
            BlockPoweredRail$2.a[BlockMinecartTrackAbstract.EnumTrackPosition.ASCENDING_SOUTH.ordinal()] = 6;
        } catch (NoSuchFieldError nosuchfielderror5) {
            ;
        }

    }
}
