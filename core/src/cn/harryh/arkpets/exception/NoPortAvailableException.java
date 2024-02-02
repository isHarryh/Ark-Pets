package cn.harryh.arkpets.exception;

public class NoPortAvailableException extends Exception {
    public NoPortAvailableException() {
        super("No port available");
    }
}
