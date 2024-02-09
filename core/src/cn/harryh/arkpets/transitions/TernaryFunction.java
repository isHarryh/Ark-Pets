/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;


/** The interface represents a math function that accepts three arguments and produces a result.
 * @param <I> The type of the input to the function.
 * @param <O> The type of the output of the function.
 */
@SuppressWarnings("unused")
public interface TernaryFunction<I extends Number, O extends Number> {
    /** Applies this function to the given arguments.
     * @param a The 1st argument.
     * @param b The 2nd argument.
     * @param c The 3rd argument.
     * @return The function result.
     */
    O apply(I a, I b, I c);

    TernaryFunction<Float, Float> LINEAR = (b, e, p) -> b + p * (e - b);

    TernaryFunction<Float, Float> EASE_OUT_CUBIC = (b, e, p) -> b + (1 - (float)Math.pow(1 - p, 3)) * (e - b);
}
