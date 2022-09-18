/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.lang.InterruptedException;


public class JavaProcess {
    private JavaProcess() {
    }

    /** Excute the main function in a java class.
     * @param clazz The java class to excute.
     * @param jvmArgs JVM Arguments.
     * @param args Arguments.
     * @return The exit value of the process.
     * @throws IOException
     * @throws InterruptedException
     */
    public static int exec(Class clazz, List<String> jvmArgs, List<String> args)
        throws IOException,InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getName();
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.inheritIO().start();
        process.waitFor();
        return process.exitValue();
    }

    /** Excute the main function in a java class. (Without arguments)
     * @param clazz The java class to excute.
     * @return The exit value of the process.
     * @throws IOException
     * @throws InterruptedException
     */
    public static int exec(Class clazz)
        throws IOException,InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getName();
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.inheritIO().start();
        process.waitFor();
        return process.exitValue();
    }
}
