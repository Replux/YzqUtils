import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.var;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class TestForkJoin {

    private final static int MAX_THREAD=200;

    private static class CalculatedRecursiveTask extends RecursiveTask<Integer>{

        private final int start;
        private final int end;

        public CalculatedRecursiveTask(int start, int end) {
            this.start = start;
            this.end = end;
        }


        @Override
        protected Integer compute() {
            if((end-start)<=MAX_THREAD){
                return IntStream.rangeClosed(start,end).sum();
            }else {
                int mid = (start+end)/2;
                var left = new CalculatedRecursiveTask(start,mid);
                var right = new CalculatedRecursiveTask(mid+1,end);
                left.fork();
                right.fork();

                return left.join()+right.join();
            }
        }


    }


    public static void main(String[] args) {
        final ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinTask<Integer> future = forkJoinPool.submit(new CalculatedRecursiveTask(0, 1000));
        try {
            Integer integer = future.get();
            System.out.println(integer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
