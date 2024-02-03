package cn.harryh.arkpets.process_pool;

import cn.harryh.arkpets.socket.InteriorSocketServer;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class ProcessPool {
    private final Set<ProcessHolder> processHolderHashSet = new HashSet<>();
    private final java.util.concurrent.ExecutorService executorService = InteriorSocketServer.getThreadPool();
    private static ProcessPool instance = null;

    public static ProcessPool getInstance() {
        if (instance == null)
            instance = new ProcessPool();
        return instance;
    }

    private ProcessPool() {
    }

    public void shutdown() {
        processHolderHashSet.forEach(processHolder -> processHolder.getProcess().destroy());
    }

    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    public FutureTask<TaskStatus> submit(Class<?> clazz, List<String> jvmArgs, List<String> args) {
        Callable<TaskStatus> task = () -> {
            // Attributes preparation
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = clazz.getName();
            // Command preparation
            List<String> command = new ArrayList<>();
            command.add(javaBin);
            if (!jvmArgs.isEmpty())
                command.addAll(jvmArgs);
            command.add("-cp");
            command.add(classpath);
            command.add(className);
            if (!args.isEmpty())
                command.addAll(args);
            // Process execution
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.inheritIO().start();
            ProcessHolder processHolder = ProcessHolder.holder(process);
            processHolderHashSet.add(processHolder);
            int status = process.waitFor();
            processHolderHashSet.remove(processHolder);
            if (status == 0) {
                return TaskStatus.ofSuccess(process.pid());
            }
            return TaskStatus.ofFailure(process.pid());
        };
        FutureTask<TaskStatus> futureTask = new FutureTask<>(task);
        executorService.submit(futureTask);
        return futureTask;
    }
}
