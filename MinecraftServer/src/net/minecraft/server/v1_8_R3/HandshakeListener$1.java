package net.minecraft.server.v1_8_R3;

class HandshakeListener$1 {

    static final int[] a = new int[EnumProtocol.values().length];

    static {
        try {
            HandshakeListener$1.a[EnumProtocol.LOGIN.ordinal()] = 1;
        } catch (NoSuchFieldError nosuchfielderror) {
            ;
        }

        try {
            HandshakeListener$1.a[EnumProtocol.STATUS.ordinal()] = 2;
        } catch (NoSuchFieldError nosuchfielderror1) {
            ;
        }

    }
}
