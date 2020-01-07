package cn.yzq.concurrent;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 利用countdownLatch实现一个cyclicBarrier
 * @since 2020-1-7 14:18:54
 */
public class ThreadBarrier{

    private Counter counter;

    public ThreadBarrier(int count,Runnable runnable) {
        counter= new Counter(count,runnable);
    }


    public void await() {
        counter.await();
    }


    static class Counter extends CountDownLatch{

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition barrier = lock.newCondition();
        private final Runnable runnable;

        public Counter(int count,Runnable runnable) {
            super(count);
            this.runnable=runnable;
        }

        @Override
        public void countDown() {
            try {
                lock.lock();
                super.countDown();
                if (getCount() == 0) {
                    barrier.signalAll();
                    new Thread(runnable).start();
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void await(){
            countDown();
            if(getCount()!=0){
                lock.lock();
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }







    public static void main(String[] args) {

        Random random = new Random(new Date().getTime());
        ThreadBarrier threadBarrier = new ThreadBarrier(201,()->{
            System.out.println("_________");
        });
        for(int i=0;i<200;i++){
            new Thread(()->{
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(2000));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadBarrier.await();
                System.out.println(Thread.currentThread().getName()+" finished");
            }).start();
        }
        threadBarrier.await();

    }

}
