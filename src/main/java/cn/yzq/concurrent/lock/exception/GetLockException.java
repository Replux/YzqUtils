package cn.yzq.concurrent.lock.exception;

public class GetLockException extends Exception {
    public GetLockException(){
        super();
    }

    public GetLockException(String message){
        super(message);
    }
}
