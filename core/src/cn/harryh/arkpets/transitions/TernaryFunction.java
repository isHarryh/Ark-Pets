/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;


/** The interface represents a math function that accepts three arguments and produces a result.
 * @param <I> The type of the input to the function.
 * @param <O> The type of the output of the function.
 */
public interface TernaryFunction<I extends Number, O extends Number> {
    /** Applies this function to the given arguments.
     * @param a The 1st argument.
     * @param b The 2nd argument.
     * @param c The 3rd argument.
     * @return The function result.
     */
    O apply(I a, I b, I c);

    TernaryFunction<Float, Float> LINEAR = new TernaryFunction<>() {
        @Override
        public Float apply(Float start, Float end, Float progress) {
            return start + progress * (end - start);
        }
    };
}
