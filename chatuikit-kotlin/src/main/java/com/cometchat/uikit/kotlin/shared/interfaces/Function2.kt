package com.cometchat.uikit.kotlin.shared.interfaces

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is a functional interface similar to Java's BiFunction.
 *
 * @param T1 The type of the first argument
 * @param T2 The type of the second argument
 * @param R The type of the result
 */
fun interface Function2<T1, T2, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t1 The first function argument
     * @param t2 The second function argument
     * @return The function result
     */
    fun apply(t1: T1, t2: T2): R
}
