package com.github.worldsender.mcanm.common.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The functional interfaces from Java are disallowing to throw exceptions. We have to wrap them ourselves...
 *
 * @author WorldSEnder
 * @see https://stackoverflow.com/questions/27644361/
 */
public class ExceptionLessFunctions {
    /**
     * @throws E although this method never actually throws E, by declaring it, this forces the caller to catch it.
     *           Note that the exception gets thus "dragged outside" the wrapped object.
     */
    public static <E extends Throwable> Runnable uncheckedRunnable(ThrowingRunnable<E> t) {
        return () -> {
            try {
                t.accept();
            } catch (Throwable exception) {
                ExceptionLessFunctions.throwActualException(exception);
            }
        };
    }

    /**
     * @throws E
     * @see {@link #uncheckedRunnable(ThrowingRunnable)}
     */
    public static <T, E extends Exception> Consumer<T> uncheckedConsumer(ThrowingConsumer<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Throwable exception) {
                ExceptionLessFunctions.throwActualException(exception);
            }
        };
    }

    /**
     * @throws E
     * @see {@link #uncheckedRunnable(ThrowingRunnable)}
     */
    public static <T, E extends Exception> Supplier<T> uncheckedSupplier(ThrowingSupplier<T, E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable exception) {
                return ExceptionLessFunctions.throwActualException(exception);
            }
        };
    }

    /**
     * @throws E
     * @see {@link #uncheckedRunnable(ThrowingRunnable)}
     */
    public static <T, R, E extends Exception> Function<T, R> uncheckedFunction(ThrowingFunction<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable exception) {
                return ExceptionLessFunctions.throwActualException(exception);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Exception, T> T throwActualException(Throwable exception) throws E {
        throw (E) exception;
    }

    public interface ThrowingRunnable<E extends Throwable> {
        void accept() throws E;
    }

    public interface ThrowingConsumer<T, E extends Throwable> {
        void accept(T t) throws E;
    }

    public interface ThrowingSupplier<T, E extends Throwable> {
        T get() throws E;
    }

    public interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }
}
