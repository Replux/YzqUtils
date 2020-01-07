import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeTest {

    public static void main(String[] args) {
        Unsafe unsafe = getUnsafe();
        System.out.println(unsafe);
    }

    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe)field.get(null);
        } catch (Exception e) {
            throw new SecurityException();
        }
    }

    interface Counter{
        void increment();
        long getCounter();
    }

    static class CASCounter implements Counter{
        private volatile long counter = 0;
        private Unsafe unsafe;
        private long offset;

        public CASCounter() throws Exception{
            this.unsafe = getUnsafe();
            this.offset = this.unsafe.objectFieldOffset(CASCounter.class.getDeclaredField("counter"));
        }

        @Override
        public void increment() {
            long current=counter;
            while(!unsafe.compareAndSwapLong(this,offset,current,current+1)){
                current=counter;
            }

        }

        @Override
        public long getCounter() {
            return 0;
        }
    }
}
