package cn.yzq.concurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 简化版线程池
 * @since 2019-12-28 20:53:56
 */
public class SimpleThreadPool {

    //DEFAULT OR CONSTANTS:
    private final static int DEFAULT_SIZE=8;
    private final static int DEFAULT_TASK_QUEUE_CAPACITY=1000;
    private final static String THREAD_PREFIX="ThreadPool-";
    public final static DiscardPolicy DEFAULT_DISCARD_POLICY=()->{
        throw new DiscardException("Discarding this task");
    };

    //component:
    private final static LinkedList<Runnable> TASK_QUEUE=new LinkedList<>();
    private final static List<Worker> WORKER_LIST = new ArrayList<>();

    //attributes:
    private final int size;
    private final int taskCapacity;
    private final DiscardPolicy discardPolicy;
    private static volatile int num=0;
    private final static ThreadGroup group = new ThreadGroup("ThreadPool");
    private volatile boolean shutdown =false;


    //constructor:
    public SimpleThreadPool() {
        this(DEFAULT_SIZE,DEFAULT_TASK_QUEUE_CAPACITY,DEFAULT_DISCARD_POLICY);
    }

    public SimpleThreadPool(int size, int taskCapacity, DiscardPolicy discardPolicy) {
        this.size = size;
        this.taskCapacity=taskCapacity;
        this.discardPolicy=discardPolicy;
        init();
    }
    //public method:
    public void submit(Runnable runnable){
        if(shutdown){
            throw new IllegalStateException("the thread pool has been shutdown, So you shouldn't submit task");
        }
        synchronized (TASK_QUEUE){
            if(TASK_QUEUE.size()>taskCapacity){
                discardPolicy.discard();
            }
            TASK_QUEUE.addLast(runnable);
            TASK_QUEUE.notifyAll();
        }
    }
    public void shutdown() throws InterruptedException {
        while(!TASK_QUEUE.isEmpty()){
            Thread.sleep(10);
        }

        int count = WORKER_LIST.size();
        while(count>0){
            for(Worker worker: WORKER_LIST){
                if(TaskState.BLOCKED.equals(worker.getTaskState() )){
                    worker.interrupt();
                    worker.close();
                    count--;
                }else {
                    Thread.sleep(10);
                }
            }
        }
        shutdown =true;
    }

    //getter:
    public int getSize() {
        return size;
    }

    public int getTaskCapacity() {
        return taskCapacity;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    //private method:
    private void init() {
        for(int i=0;i<size;++i){
            createWorks();
        }
    }

    private void createWorks(){
        Worker task = new Worker(group,THREAD_PREFIX + (num++) ) ;
        task.start();
        WORKER_LIST.add(task);
    }



    //public inner class:
    public interface DiscardPolicy{
        void discard() throws DiscardException;
    }

    public static class DiscardException extends RuntimeException{

        public DiscardException(String message) {
            super(message);
        }
    }

    //private inner class:
    private enum TaskState{
        FREE,RUNNING,BLOCKED,DEAD
    }

    private static class Worker extends Thread{

        private volatile TaskState taskState= TaskState.FREE;

        public Worker(ThreadGroup group, String name) {
           super(group,name);
        }

        public TaskState getTaskState() {
            return taskState;
        }
        public void close(){
            taskState= TaskState.DEAD;
        }

        @Override
        public void run() {
            OUTER:
            while(!this.taskState.equals(TaskState.DEAD)){
                Runnable runnable;
                synchronized (TASK_QUEUE){
                    while(TASK_QUEUE.isEmpty()){
                        try {
                            taskState = TaskState.BLOCKED;
                            TASK_QUEUE.wait();
                        } catch (InterruptedException e) {
                            break OUTER;
                        }
                    }
                    runnable = TASK_QUEUE.removeFirst();
                }
                if(runnable!=null){
                    taskState = TaskState.RUNNING;
                    runnable.run();
                    taskState = TaskState.FREE;
                }
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        SimpleThreadPool threadPool = new SimpleThreadPool();
        IntStream.range(0,40).forEach(i->{
            threadPool.submit(()->{
                System.out.println("[ "+i+" before]the task has been executed by "+Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
        Thread.sleep(10000);
        threadPool.shutdown();
        System.out.println(threadPool.isShutdown());
    }
}
