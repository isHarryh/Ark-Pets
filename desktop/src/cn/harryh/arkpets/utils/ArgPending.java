/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

abstract public class ArgPending {
    public static String[] argCache = new String[0];
    private static final String argPrefix = "-";
    private final String pattern;

    /** Initialize an Argument Pending instance.
     * @param pattern The specified argument string to be match.
     */
    public ArgPending(String pattern) {
        this.pattern = pattern;
    }

    /** Initialize an Argument Pending instance and deal a given arguments list.
     * @param pattern The specified argument string to be match.
     * @param args The given arguments list to be dealt.
     */
    public ArgPending(String pattern, String[] args) {
        this.pattern = pattern;
        this.judge(args);
    }

    /** Deal a given arguments list.
     * If the pattern specified before matches one of the arguments, the method {@code process()} will be invoked.
     * @param args The given arguments list to be dealt.
     * @return Whether the pattern specified before matches one of the arguments.
     */
    public boolean judge(String[] args) {
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

    /** To process the argument.
     * @param command The argument string.
     * @param addition The additional string which is nullable.
     */
    abstract protected void process(String command, String addition);
}
