/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JavaProcess {
    private JavaProcess() {
    }

    /** Execute the main function in a java class.
     * @param clazz The java class to execute.
     * @param waitForExitValue Whether to wait for the process to end.
     *                         If false, this method will always return 0 instead of the exit value.
     * @param jvmArgs JVM Arguments.
     * @param args Arguments.
     * @return The exit value of the process (if you wait for exit value).
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting.
     */
    public static int exec(Class<?> clazz, boolean waitForExitValue, List<String> jvmArgs, List<String> args)
            throws IOException, InterruptedException {
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
        if (waitForExitValue) {
            process.waitFor();
            return process.exitValue();
        }
        return 0;
    }

    /** Execute the main function in a java class. (Without arguments)
     * @param clazz The java class to execute.
     * @param waitForExitValue Whether to wait for the process to end.
     *                         If false, this method will always return 0 instead of the exit value.
     * @return The exit value of the process (if you wait for exit value).
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting.
     */
    public static int exec(Class<?> clazz, boolean waitForExitValue)
            throws IOException, InterruptedException {
        List<String> emptyList = new ArrayList<>();
        return exec(clazz, waitForExitValue, emptyList, emptyList);
    }

    public static class UnexpectedExitCodeException extends Exception {
        private final int exitCode;

        public UnexpectedExitCodeException(int exitCode) {
            this.exitCode = exitCode;
        }

        @Override
        public String getMessage() {
            return "The process exited with a non-zero exit code: " + exitCode;
        }
    }
}
