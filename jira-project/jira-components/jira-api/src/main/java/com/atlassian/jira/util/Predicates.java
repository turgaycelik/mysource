package com.atlassian.jira.util;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.dbc.Assertions;

public class Predicates
{
    /**
     * Return a predicate that always returns true.
     *
     * @param <T> the type of input the predicate will work for.
     *
     * @return a predicate that always returns true.
     */
    public static <T> Predicate<T> truePredicate()
    {
        return TruePredicate.getInstance();
    }


    /**
     * Return a predicate that always returns false.
     *
     * @param <T> the type of input the predicate will work for.
     *
     * @return a predicate that always returns false.
     */
    public static <T> Predicate<T> falsePredicate()
    {
        return FalsePredicate.getInstance();
    }

    /**
     * A predicate that check that some input equals the passed argument.
     *
     * @param <T> the type
     * @param check the element to check equality of, must not be null.
     * @return a Predicate that will return true if the input matches the argument.
     */
    public static <T> Predicate<T> equalTo(@Nonnull final T check)
    {
        Assertions.notNull("check", check);
        return new Predicate<T>()
        {
            public boolean evaluate(final T input)
            {
                return check.equals(input);
            }
        };
    }

    /**
     * A predicate that check that the input is not null.
     *
     * @param <T> the type
     * @return a Predicate that will return true if the input is not null.
     */
    public static <T> Predicate<T> notNull()
    {
        return new Predicate<T>()
        {
            public boolean evaluate(final T input)
            {
                return input != null;
            }
        };
    }

    /**
     * A predicate that check that the input is null.
     *
     * @param <T> the type
     * @return a Predicate that will return true if the input is null.
     */
    public static <T> Predicate<T> isNull()
    {
        return new Predicate<T>()
        {
            public boolean evaluate(final T input)
            {
                return input != null;
            }
        };
    }

    public static <T> Predicate<T> isInstanceOf(final Class<? extends T> clazz)
    {
        Assertions.notNull("clazz", clazz);
        return new Predicate<T>()
        {
            public boolean evaluate(final T input)
            {
                return (input == null) || clazz.isAssignableFrom(input.getClass());
            }
        };
    }

    /**
     * Create a composite predicate that evaluates to true when both the passed predicates evaluate to true.
     *
     * @param left the first predicate.
     * @param right the second predicate.
     * @return a composite predicate that is only true when both the passed predicates evaluate to true.
     */
    public static <T> Predicate<T> allOf(final Predicate<? super T> left, final Predicate<? super T> right)
    {
        return new And<T>(left, right);
    }

    public static class And<T> extends Composite<T>
    {
        And(final Predicate<? super T> left, final Predicate<? super T> right)
        {
            super(left, right);
        }

        public boolean evaluate(final T input)
        {
            return left.evaluate(input) && right.evaluate(input);
        }
    }

    /**
     * Create a composite predicate that evaluates to true when one the passed predicates evaluate to true.
     *
     * @param left the first predicate.
     * @param right the second predicate.
     * @return a composite predicate that is only true when both the passed predicates evaluate to true.
     */
    public static <T> Predicate<T> anyOf(final Predicate<? super T> left, final Predicate<? super T> right)
    {
        return new Or<T>(left, right);
    }

    public static class Or<T> extends Composite<T>
    {
        Or(final Predicate<? super T> left, final Predicate<? super T> right)
        {
            super(left, right);
        }

        public boolean evaluate(final T input)
        {
            return left.evaluate(input) || right.evaluate(input);
        }
    }

    /**
     * Create a predicate that inverts the behaviour of the passed predicate.
     *
     * @param predicate the predicate to negate. Cannot be null.
     * @param <T> the type of the input for the predicate.
     *
     * @return a predicate that inverts the behaviour of the passed predicate.
     */
    public static <T> Predicate<T> not(final Predicate<? super T> predicate)
    {
        return new NotPredicate<T>(Assertions.notNull("predicate", predicate));
    }

    public static abstract class Composite<T> implements Predicate<T>
    {
        final Predicate<? super T> left;
        final Predicate<? super T> right;

        Composite(final Predicate<? super T> left, final Predicate<? super T> right)
        {
            this.left = Assertions.notNull("left", left);
            this.right = Assertions.notNull("right", right);
        }
    }

    /**
     * A predicate that always returns true. It is a singleton.
     */
    public final static class TruePredicate<T> implements Predicate<T>
    {
        private static final Predicate<?> instance = new TruePredicate<Object>();

        private TruePredicate()
        {
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            throw new CloneNotSupportedException();
        }

        public boolean evaluate(final Object input)
        {
            return true;
        }

        @SuppressWarnings ({ "unchecked" })
        public static <T>Predicate<T> getInstance()
        {
            return (Predicate<T>) instance;
        }
    }

    /**
     * A predicate that always returns false. It is a singleton.
     */
    public final static class FalsePredicate<T> implements Predicate<T>
    {
        private static final Predicate<?> instance = new FalsePredicate<Object>();

        private FalsePredicate()
        {
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            throw new CloneNotSupportedException();
        }

        public boolean evaluate(final Object input)
        {
            return false;
        }

        @SuppressWarnings ({ "unchecked" })
        public static <T>Predicate<T> getInstance()
        {
            return (Predicate<T>) instance;
        }
    }

    private final static class NotPredicate<T> implements Predicate<T>
    {
        private final Predicate<? super T> delegate;

        private NotPredicate(Predicate<? super T> delegate)
        {
            this.delegate = delegate;
        }

        public boolean evaluate(final T input)
        {
            return !delegate.evaluate(input);
        }
    }
}
