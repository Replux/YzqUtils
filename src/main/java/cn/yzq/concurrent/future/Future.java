package cn.yzq.concurrent.future;

public interface Future<T> {
    T get() throws InterruptedException;
}
