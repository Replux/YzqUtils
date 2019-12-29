import cn.yzq.concurrent.SimpleThreadPool;

import java.util.stream.IntStream;


public class TestThreadPool {

    public static void main(String[] args) throws InterruptedException {
        SimpleThreadPool threadPool = new SimpleThreadPool();
        IntStream.range(0,200).forEach(i->
            threadPool.submit(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            })
        );
        Thread.sleep(10000);
        threadPool.shutdown();
        System.out.println(threadPool.isShutdown());
    }
}
