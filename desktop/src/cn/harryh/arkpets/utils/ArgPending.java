/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

abstract public class ArgPending {
    public static String[] argCache = new String[0];
    private static final String argPrefix = "-";
    private final String pattern;

    /** Initializes an Argument Pending instance.
     * @param pattern The specified argument string to be match.
     */
    public ArgPending(String pattern) {
        this.pattern = pattern;
    }

    /** Initializes an Argument Pending instance and deal a given arguments list.
     * @param pattern The specified argument string to be match.
     * @param args The given arguments list to be dealt.
     */
    public ArgPending(String pattern, String[] args) {
        this.pattern = pattern;
        this.handle(args);
    }

    /** Handles a given arguments list.
     * If the pattern specified before matches one of the arguments, the method {@code process()} will be invoked.
     * @param args The given arguments list to be dealt.
     * @return Whether the pattern specified before matches one of the arguments.
     */
    public boolean handle(String[] args) {
        if (args.length == 0)
            return false;
        String addition = null;
        String command = null;
        boolean specified = false;

        for (String arg : args) {
            if (arg != null) {
                if (specified) {
                    if (arg.indexOf(argPrefix) != 0)
                        addition = arg;
                    else
                        break;
                } else if (arg.equals(pattern)) {
                    command = arg;
                    specified = true;
                }
            }
        }

        if (command != null) {
            process(command, addition);
            return true;
        }
        return false;
    }

    /** Processes an argument.
     * @param command The argument string, e.g. "{@code -n}" in "{@code -n 10}".
     * @param addition The additional string which is nullable, e.g. "{@code 10}" in "{@code -n 10}".
     */
    abstract protected void process(String command, String addition);
}
