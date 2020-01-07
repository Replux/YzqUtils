package cn.yzq.concurrent.lock;

/**
 * 读写锁
 * @since 2020-1-6 18:37:20
 */
public class ReadWriteLock {

    private volatile int readingReaders=0;
    private volatile int waitingReaders=0;
    private volatile int writingWriters=0; //最多只有一个
    private volatile int waitingWriters=0;
    private volatile boolean preferWriter;

    public ReadWriteLock() {
        this(true);
    }

    public ReadWriteLock(boolean preferWriter) {
        this.preferWriter = preferWriter;
    }

    public synchronized void readLock() throws InterruptedException {
        try {
            ++this.waitingReaders;
            while (writingWriters>0 || (preferWriter&&waitingWriters>0) ){ //有writers在写，则无法读
                this.wait();
            }
            ++this.readingReaders;
        } finally {
            --this.waitingReaders;
        }
    }

    public synchronized void readUnlock(){
        --this.readingReaders;
        this.notifyAll();
    }

    public synchronized void writeLock() throws InterruptedException {
        try {
            ++this.waitingWriters;
            while (readingReaders>0 || writingWriters>0){ //有writers在写，或有readers在读，则无法继续写
                this.wait();
            }
            ++this.writingWriters;
        } finally {
            --this.waitingWriters;
        }
    }

    public synchronized void writeUnlock(){
        --this.writingWriters;
        this.notifyAll();
    }
}
