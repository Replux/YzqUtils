package cn.yzq.concurrent;

public class CountDown {

    private final int total;
    private int counter;

    public CountDown(int total) {
        this.total = total;
    }

    public synchronized void down(){
        this.counter++;
        this.notifyAll();
    }
    public synchronized void await() throws InterruptedException {
        while (counter!=total){
            this.wait();
        }
    }
}
