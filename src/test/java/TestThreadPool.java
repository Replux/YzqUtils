import cn.yzq.concurrent.SimpleThreadPool;
import cn.yzq.concurrent.lock.CASLock;
import cn.yzq.concurrent.lock.exception.GetLockException;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicStampedReference;


public class TestThreadPool {



    public static void main(String[] args) throws InterruptedException {
        /**
         * 在32位cpu中，传输long(64位)需要俩次，因此无法保证原子性
         */
        /**
         * AutomicReference不仅用来保证原子性，而且可以在匿名内部类中用于修改final对象的值
         */

    }


}
