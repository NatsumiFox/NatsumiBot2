package net.minecraft.server.v1_8_R3;

class BlockMonsterEggs$1 {

    static final int[] a = new int[BlockMonsterEggs.EnumMonsterEggVarient.values().length];

    static {
        try {
            BlockMonsterEggs$1.a[BlockMonsterEggs.EnumMonsterEggVarient.COBBLESTONE.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            BlockMonsterEggs$1.a[BlockMonsterEggs.EnumMonsterEggVarient.STONEBRICK.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

        try {
            BlockMonsterEggs$1.a[BlockMonsterEggs.EnumMonsterEggVarient.MOSSY_STONEBRICK.ordinal()] = 3;
        } catch (NoSuchFieldError nosuchfielderror2) {
            ;
        }

        try {
            BlockMonsterEggs$1.a[BlockMonsterEggs.EnumMonsterEggVarient.CRACKED_STONEBRICK.ordinal()] = 4;
        } catch (NoSuchFieldError nosuchfielderror3) {
            ;
        }

        try {
            BlockMonsterEggs$1.a[BlockMonsterEggs.EnumMonsterEggVarient.CHISELED_STONEBRICK.ordinal()] = 5;
        } catch (NoSuchFieldError nosuchfielderror4) {
            ;
        }

    }
}
