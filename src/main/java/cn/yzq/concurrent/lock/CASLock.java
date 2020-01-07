package cn.yzq.concurrent.lock;

import cn.yzq.concurrent.lock.exception.GetLockException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 支持trylock
 * @since 2020-1-6 18:36:41
 */
public class CASLock {

    private final AtomicInteger value = new AtomicInteger(0);

    private volatile Thread lockedThread;

    public void tryLock() throws GetLockException {
        int current = value.get();

        if(0!=current && !Thread.currentThread().equals(lockedThread)){
            throw new GetLockException("Get the lock failed");
        }

        int next = current+1;
        if(value.compareAndSet(current,next)){
            lockedThread=Thread.currentThread();
        }else {
            throw new GetLockException("Get the lock failed");
        }
    }

    public void unlock() throws GetLockException {
        int current = value.get();

        if(0==current){
            return;
        }

        if(lockedThread.equals(Thread.currentThread())){
            int next = current-1;
            value.compareAndSet(current,next);
            if(0==value.get()){
                lockedThread=null;
            }
        }

    }
}
