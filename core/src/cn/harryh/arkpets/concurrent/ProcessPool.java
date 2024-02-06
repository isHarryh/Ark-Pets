package cn.harryh.arkpets.concurrent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class ProcessPool {
    public static final ExecutorService executorService =
            new ThreadPoolExecutor(20,
                    Integer.MAX_VALUE,
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    r -> {
                        Thread thread = Executors.defaultThreadFactory().newThread(r);
                        thread.setDaemon(true);
                        return thread;
                    });

    private static ProcessPool instance = null;

    public static synchronized ProcessPool getInstance() {
        if (instance == null)
            instance = new ProcessPool();
        return instance;
    }

    private ProcessPool() {
    }

    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    public FutureTask<ProcessResult> submit(Class<?> clazz, List<String> jvmArgs, List<String> args) {
        Callable<ProcessResult> task = () -> {
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
            int exitValue = process.waitFor();
            return new ProcessResult(exitValue, process.pid());
        };
        FutureTask<ProcessResult> futureTask = new FutureTask<>(task);
        executorService.submit(futureTask);
        return futureTask;
    }


    public record ProcessResult(int exitValue, long processId) {
        public boolean isSuccess() {
            return exitValue() == 0;
        }
    }
}
