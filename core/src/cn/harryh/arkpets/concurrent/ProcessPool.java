/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.concurrent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public final class ProcessPool implements Executor {
    private final ExecutorService executorService =
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

    private static volatile ProcessPool instance = null;

    public static ProcessPool getInstance() {
        if (instance == null)
            synchronized (ProcessPool.class) {
                if (instance == null)
                    instance = new ProcessPool();
            }
        return instance;
    }

    private ProcessPool() {
    }

    @Override
    public void execute(Runnable task) {
        try {
            executorService.submit(task);
        } catch (RejectedExecutionException ignored) {
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public Future<ProcessResult> submit(Class<?> clazz, List<String> jvmArgs, List<String> args) {
        FutureTask<ProcessResult> task = new FutureTask<> (() -> {
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
            Map<String, String> env = builder.environment();
            env.remove("GDK_BACKEND");
            Process process = builder.inheritIO().start();
            int exitValue = process.waitFor();
            return new ProcessResult(exitValue, process.pid());
        });
        executorService.submit(task);
        return task;
    }


    public record ProcessResult(int exitValue, long processId) {
        public boolean isSuccess() {
            return exitValue() == 0;
        }
    }


    public static class UnexpectedExitCodeException extends Exception {
        private final int exitCode;
        private final long processId;

        public UnexpectedExitCodeException(int exitCode, long processId) {
            this.exitCode = exitCode;
            this.processId = processId;
        }

        @Override
        public String getMessage() {
            return "The process exited with a non-zero exit code: " + exitCode;
        }

        public long getProcessId() {
            return processId;
        }
    }
}
