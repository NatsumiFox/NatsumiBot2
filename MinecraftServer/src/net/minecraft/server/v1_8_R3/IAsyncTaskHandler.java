package net.minecraft.server.v1_8_R3;

import com.google.common.util.concurrent.ListenableFuture;

public interface IAsyncTaskHandler {

    ListenableFuture<Object> postToMainThread(Runnable runnable);

    boolean isMainThread();
}
