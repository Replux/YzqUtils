package cn.yzq.concurrent;

import java.util.Date;

/**
 * 用于管理线程的生命周期
 * @since 2019-12-28 20:54:06
 */
public class ThreadWatcher {

    private Thread carrier;
    private volatile boolean finished;

    private Runnable task;

    public ThreadWatcher(Runnable task) {
        this.task = task;
    }

    public void execute(){
        this.finished=false;
        this.carrier = new Thread(()->{
            Thread runner = new Thread(this.task);
            runner.setDaemon(true);
            runner.start();
            try {
                runner.join();
                finished=true;
            } catch (InterruptedException e) {

            }
        });
        carrier.start();
    }


    public void shutdown(long mills){
        long before = new Date().getTime();
        while(!finished){
            if((new Date().getTime() - before)>=mills){
                this.carrier.interrupt();
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }

    }

    public boolean isFinished() {
        return finished;
    }
}
