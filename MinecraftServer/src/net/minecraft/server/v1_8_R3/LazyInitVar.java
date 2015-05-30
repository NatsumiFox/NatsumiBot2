package net.minecraft.server.v1_8_R3;

public abstract class LazyInitVar<T> {

    private T a;
    private boolean b = false;

    public LazyInitVar() {}

    public T c() {
        if (!this.b) {
            this.b = true;
            this.a = this.init();
        }

        return this.a;
    }

    protected abstract T init();
}
