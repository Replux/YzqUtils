package cn.yzq.concurrent.lock;

import java.util.List;
import java.util.concurrent.TimeoutException;


public interface Lock {

    void lock() throws InterruptedException;

    void lock(long mills) throws InterruptedException, TimeoutException;

    void unlock();

    /**
     * @return List is unmodifiable
     */
    List<Thread> getBlockedThread();

    int getBlockedThreadNum();
}
