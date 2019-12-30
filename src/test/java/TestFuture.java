import cn.yzq.concurrent.future.Future;
import cn.yzq.concurrent.future.FutureService;

public class TestFuture {

    public static void main(String[] args) throws InterruptedException {
        FutureService futureService = new FutureService();
        Future<String> future = futureService.submit(() -> {
            try {
                Thread.sleep(10_000);
                System.out.println("+++++++++++");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "DONE";
        });
        System.out.println(future.get());
    }
}
