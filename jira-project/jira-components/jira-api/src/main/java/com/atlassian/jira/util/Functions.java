package com.atlassian.jira.util;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.concurrent.ConcurrentMap;

/**
 * Useful standard functions.
 */
public class Functions
{
    /**
     * Memoizer maps an input to an output and always returns the mapped result
     * rather than calling the wrapped function every time. Useful when the
     * wrapped function is expensive to call.
     * <p>
     * Note, once mapped the input and the result can only be externally
     * removed. Also, if the supplied function returns nulls then no memoization
     * will occur.
     * 
     * @param <T> the input type. MUST be immutable.
     * @param <R> the result type.
     * @param function for supplying the initial value that gets mapped
     * @param map for storing the key-value mappings
     * @return a memoizing function.
     */
    public static <T, R> Function<T, R> memoize(final Function<T, R> function, final ConcurrentMap<T, R> map)
    {
        return new Memoizer<T, R>(function, map);
    }

    static class Memoizer<T, R> implements Function<T, R>
    {
        private final Function<T, R> function;
        private final ConcurrentMap<T, R> map;

        Memoizer(final Function<T, R> function, final ConcurrentMap<T, R> map)
        {
            this.function = function;
            this.map = map;
        }

        public R get(final T input)
        {
            R result = map.get(input);
            while (result == null)
            {
                map.putIfAbsent(input, function.get(input));
                result = map.get(input);
            }
            return result;
        }
    };

    /**
     * Get a function that always returns the input.
     * 
     * @param <T> the type of the input and the output for the function.
     * @return the identity function.
     */
    @SuppressWarnings("unchecked")
    public static <T> Function<T, T> identity()
    {
        return (Function<T,T>)IdentityFunction.INSTANCE;
    }

    /**
     * Get a visitor that will apply the given {@code function} before delegating to another visitor.
     *
     * @param <T> the inferred type of the function's input
     * @param <V> the inferred type of the function's output, which will be passed to the delegate visitor
     * @param mappingFunction the mapping function to apply
     * @param delegate the visitor to call with the function output values
     * @return a visitor that will accept input values for the function
     */
    public static <T,V> Visitor<T> mappedVisitor(Function<T,V> mappingFunction, Visitor<V> delegate)
    {
        return new MappedVisitor<T,V>(mappingFunction, delegate);
    }

    /**
     * Get a function that always the input downcast to the supplied class.
     * 
     * @param <T> the type of the input and the output for the function.
     * @return the identity function.
     */
    public static <T, R extends T> Function<T, R> downcast(final Class<R> subclass)
    {
        return new Downcaster<T, R>(subclass);
    }

    /**
     * Use with care. Used to coerce a type.
     * @param <T> to coerce to.
     */
    static final class Downcaster<T, R extends T> implements Function<T, R>
    {
        private final Class<R> subclass;

        public Downcaster(final Class<R> subclass)
        {
            this.subclass = notNull("subclass", subclass);
        }

        public R get(final T input)
        {
            return subclass.cast(input);
        };
    }

    /**
     * Transform to a super class. Usually needs to be called with explicit type parameters, eg:
     * <code>
     * Functions.&lt;SuperClass, SubClass&gt; coerceToSuper();
     * </code>
     * 
     * @param <S> the super class.
     * @param <T> the sub class.
     */
    public static <S, T extends S> Function<T, S> coerceToSuper()
    {
        return new Coercer<S, T>();
    }

    /**
     * Use with care. Used to coerce a type.
     * @param <T> to coerce to.
     */
    static final class Coercer<S, T extends S> implements Function<T, S>
    {
        public S get(final T input)
        {
            return input;
        };
    }

    /**
     * Map to an atlassian-util-concurrent Function.
     * 
     * @param <T> input type
     * @param <R> output type
     * @param input the function to map
     * @return the mapped function.
     */
    static <T, R> Function<T, R> map(final com.atlassian.util.concurrent.Function<T, R> input)
    {
        return new UtilConcurrentAdapter<T, R>(input);
    }

    static class UtilConcurrentAdapter<T, R> implements Function<T, R>
    {
        private final com.atlassian.util.concurrent.Function<T, R> delegate;

        UtilConcurrentAdapter(final com.atlassian.util.concurrent.Function<T, R> delegate)
        {
            this.delegate = delegate;
        }

        public R get(final T input)
        {
            return delegate.get(input);
        }
    }

    /**
     * Map to a google-collections Function.
     * 
     * @param <T> input type
     * @param <R> output type
     * @param function the function to map
     * @return the mapped function.
     */
    public static <T, R> com.google.common.base.Function<T, R> toGoogleFunction(final Function<T, R> function)
    {
        return new ToGoogleAdapter<T, R>(function);
    }

    static class ToGoogleAdapter<T, R> implements com.google.common.base.Function<T, R>
    {
        private final Function<T, R> delegate;

        ToGoogleAdapter(final Function<T, R> delegate)
        {
            this.delegate = delegate;
        }

        public R apply(final T from)
        {
            return delegate.get(from);
        }
    }

    static class IdentityFunction<T> implements Function<T,T>
    {
        static final IdentityFunction<?> INSTANCE = new IdentityFunction();

        @Override
        public T get(final T input)
        {
            return input;
        }
    }

    static class MappedVisitor<T,V> implements Visitor<T>
    {
        private final Function<T,V> function;
        private final Visitor<V> delegate;

        MappedVisitor(Function<T, V> function, Visitor<V> delegate)
        {
            this.function = notNull("function", function);
            this.delegate = notNull("delegate", delegate);
        }

        @Override
        public void visit(final T element)
        {
            delegate.visit(function.get(element));
        }

        @Override
        public String toString()
        {
            return "MappedVisitor[function=" + function + ",delegate=" + delegate + ']';
        }
    }
}
