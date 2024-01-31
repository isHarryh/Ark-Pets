package cn.harryh.arkpets.process_pool;

public class ProcessHolder {
    private final Long processID;
    private final Process process;

    public Long getProcessID() {
        return processID;
    }

    public Process getProcess() {
        return process;
    }

    public static ProcessHolder holder(Process process) {
        return new ProcessHolder(process);
    }

    private ProcessHolder(Process process) {
        this.process = process;
        this.processID = process.pid();
    }

    @Override
    public String toString() {
        return "ProcessHolder [processID=" + processID + "]";
    }
}
