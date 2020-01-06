import cn.yzq.concurrent.lock.CASLock;
import cn.yzq.concurrent.lock.exception.GetLockException;
import org.junit.Test;

public class TestCASLock {

    private final static CASLock casLock = new CASLock();

    @Test
    public void test() throws InterruptedException {
        for(int i=0;i<7;i++){
            new Thread(()->
                    doSomething2()
            ).start();
            Thread.sleep(1900);
        }
    }
    private static void doSomething2(){

        try {
            casLock.tryLock();
            casLock.tryLock();
            Thread.sleep(2000);
            casLock.tryLock();
            Thread.sleep(2000);
        }
        catch (GetLockException e) {
            System.out.println(Thread.currentThread().getName()+" tryLock failed");
        }catch (Exception e1){

        }finally {
            try {
                casLock.unlock();
                casLock.unlock();
                casLock.unlock();
            } catch (GetLockException e) {
                e.printStackTrace();
            }
        }





    }
}
