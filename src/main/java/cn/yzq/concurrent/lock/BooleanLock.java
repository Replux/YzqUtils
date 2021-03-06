package cn.yzq.concurrent.lock;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 一个可以被打断的锁
 * @since 2019-12-28 20:55:08
 */
public class BooleanLock{

    private Thread owner;

    private List<Thread> blockedThreads = new ArrayList<>();

    public BooleanLock() {
        this.owner = null;
    }

    public synchronized void lock() throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        while(owner!=null){
            blockedThreads.add(currentThread);
            this.wait();
        }
        blockedThreads.remove(currentThread);
        owner = currentThread;
    }

    public synchronized void lock(long mills) throws InterruptedException, TimeoutException {
        if(mills<=0){
            lock();
        }
        long before = new Date().getTime()+mills;
        Thread currentThread = Thread.currentThread();
        while(owner!=null){
            blockedThreads.add(currentThread);
            this.wait();
            if(new Date().getTime()-before>0){
                throw new TimeoutException();
            }
        }
        owner=currentThread;
    }

    public synchronized void unlock() {
        if(Thread.currentThread().equals(owner)){
            owner=null;
            this.notifyAll();
        }
    }

    public List<Thread> getBlockedThread() {
        return Collections.unmodifiableList(blockedThreads);
    }

    public int getBlockedThreadNum() {
        return blockedThreads.size();
    }
}
