package cn.harryh.arkpets.process_pool;

public class TaskStatus {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    private final Status status;
    private final Throwable exception;
    private final Long processId;

    public Status getStatus() {
        return status;
    }

    public Throwable getException() {
        return exception;
    }

    public Long getProcessId() {
        return processId;
    }

    private TaskStatus(Status status, Long processId, Throwable exception) {
        this.status = status;
        this.processId = processId;
        this.exception = exception;
    }

    public static TaskStatus ofSuccess(Long processId) {
        return new TaskStatus(Status.SUCCESS, processId, null);
    }

    public static TaskStatus ofFailure(Long processId) {
        return new TaskStatus(Status.FAILURE, processId, null);
    }

    public static TaskStatus ofFailure(Long processId, Throwable exception) {
        return new TaskStatus(Status.FAILURE, processId, exception);
    }
}
