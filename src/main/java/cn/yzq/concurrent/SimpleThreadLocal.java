package cn.yzq.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SimpleThreadLocal<T> {

    private final Map<Thread,T> container = new HashMap<>();

    final public synchronized void set(T t){
        container.put(Thread.currentThread(),t);
    }

    final public synchronized T get(){
        return Optional.of(container.get(Thread.currentThread())).orElseGet(this::initialValue);
    }

    public T initialValue(){
        return null;
    }
}
