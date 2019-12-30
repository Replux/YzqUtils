package cn.yzq.concurrent.future;

public class AsynFuture<T> implements Future<T> {

    private volatile boolean done = false;

    private T result;

    public synchronized void done(T result){
        this.result=result;
        this.done=true;
        this.notifyAll();
    }

    @Override
    public T get() throws InterruptedException {
        synchronized (this){
            while(!done){
                this.wait();
            }
        }
        return result;
    }
}
