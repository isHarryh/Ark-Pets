/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;


/** The interface represents a function that accepts three arguments and produces a result.
 * @param <I> The type of the input to the function.
 * @param <O> The type of the output of the function.
 */
public interface TernaryFunction<I, O> {
    /** Applies this function to the given arguments.
     * @param a The 1st argument.
     * @param b The 2nd argument.
     * @param c The 3rd argument.
     * @return The function result.
     */
    O apply(I a, I b, I c);
}
