package cn.yzq.concurrent.future;

import java.util.function.Consumer;

public class FutureService {

    public <T>Future<T> submit(final FutureTask<T> task){
        AsynFuture<T> future = new AsynFuture<>();
        new Thread(()->{
            T result = task.call();
            future.done(result);
        }).start();
        return future;
    }

    public <T>void submit(final FutureTask<T> task, final Consumer<T> callback){
        new Thread(()->
            callback.accept(task.call())
        ).start();
    }

}
