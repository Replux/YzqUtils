

import java.util.Random;


public class TestMain {

    private final static Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
    }


    public static void sleep(long mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }





}
