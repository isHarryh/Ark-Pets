package cn.harryh.arkpets.exception;

public class NoServerRunningException extends Exception {
    public NoServerRunningException() {
        super("Can not find a running server");
    }
}
