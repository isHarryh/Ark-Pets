package cn.harryh.arkpets.exception;

public class ServerRunningException extends Exception {
    public ServerRunningException() {
        super("Server is already running");
    }
}
