package cn.yzq.concurrent;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 简化版线程池
 * @since 2019-12-28 20:53:56
 */
public class SimpleThreadPool extends Thread{

    //DEFAULT OR CONSTANTS:
    private final static int DEFAULT_TASK_QUEUE_CAPACITY=1000; //任务上限
    private final static String THREAD_PREFIX="ThreadPool-";
    public final static DiscardPolicy DEFAULT_DISCARD_POLICY=()->{
        throw new DiscardException("Discarding this task");
    };

    //component:
    private final static LinkedList<Runnable> TASK_QUEUE=new LinkedList<>();
    private final static List<Worker> WORKER_LIST = new ArrayList<>();

    //attributes:
    private final int taskCapacity;
    private final DiscardPolicy discardPolicy;
    private static volatile int num=0;
    private final static ThreadGroup group = new ThreadGroup("ThreadPool");
    private volatile boolean shutdown =false;
    private int maxNum;
    private int minNum;
    private int activeNum;

    //constructor:
    public SimpleThreadPool() {
        this(
                4,
                8,
                16,
                DEFAULT_TASK_QUEUE_CAPACITY,
                DEFAULT_DISCARD_POLICY
        );
    }

    public SimpleThreadPool(int minNum,int activeNum,int maxNum) {
        this(
                minNum,
                activeNum,
                maxNum,
                DEFAULT_TASK_QUEUE_CAPACITY,
                DEFAULT_DISCARD_POLICY
        );
    }

    public SimpleThreadPool(int minNum,int activeNum,int maxNum,int taskCapacity, DiscardPolicy discardPolicy) {
        this.minNum = minNum;
        this.activeNum = activeNum;
        this.maxNum = maxNum;
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
        synchronized (WORKER_LIST){
            int count = WORKER_LIST.size();
            while(count>0){
                for(Worker worker: WORKER_LIST){
                    if(!TaskState.RUNNING.equals(worker.getTaskState())){
                        worker.interrupt();
                        worker.close();
                        count--;
                    }else {
                        Thread.sleep(10);
                    }
                }
            }
        }
        shutdown =true;
    }

    //getter:
    public int getWorkerNum() {
        return WORKER_LIST.size();
    }

    public int getTaskCapacity() {
        return taskCapacity;
    }

    public int getTaskCount(){
        return TASK_QUEUE.size();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public int getMinNum() {
        return minNum;
    }

    public int getActiveNum() {
        return activeNum;
    }

    //private method:
    private void init() {
        for(int i=0;i<minNum;++i){
            createWork();
        }
        this.start();
    }

    /**
     * 该函数被单线程调用所以是线程安全的
     */
    private void createWork(){
        Worker task = new Worker(group,THREAD_PREFIX + (num++) ) ;
        task.start();
        WORKER_LIST.add(task);
    }


    @Override
    public void run() {
        while(!shutdown){
            try {
                Thread.sleep(500);
                int workerNum = this.getWorkerNum();
                if(this.getTaskCount()>activeNum && workerNum<activeNum){
                    int diffNum=activeNum-workerNum;
                    for(int i=0;i<diffNum;++i){
                        createWork();
                    }
                }else if(this.getTaskCount()>maxNum && workerNum<maxNum){
                    int diffNum=maxNum-workerNum;
                    for(int i=0;i<diffNum;++i){
                        createWork();
                    }
                }

                if(TASK_QUEUE.isEmpty() && this.getWorkerNum()>activeNum) synchronized (WORKER_LIST){
                    if(TASK_QUEUE.isEmpty()){
                        int releaseNum = this.getWorkerNum()-activeNum;
                        for(Iterator<Worker> iterator = WORKER_LIST.iterator(); iterator.hasNext();){
                            if(releaseNum<=0){
                                break;
                            }
                            Worker worker = iterator.next();
                            //防止打断正在运行的工作线程
                            if(!TaskState.RUNNING.equals(worker.getTaskState())){
                                worker.close();
                                worker.interrupt();
                                iterator.remove();
                                releaseNum--;
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {

            }
        }
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

}

