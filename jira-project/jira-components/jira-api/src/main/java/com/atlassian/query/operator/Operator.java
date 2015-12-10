package com.atlassian.query.operator;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.Predicate;

import java.util.Comparator;
import javax.annotation.concurrent.Immutable;

/**
 * Represents the query operators.
 */
@Immutable
@PublicApi
public enum Operator
{
    LIKE("~")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the '~' operator.");
                }
            },
    NOT_LIKE("!~")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the '!~' operator.");
                }
            },
    EQUALS("=")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the '=' operator.");
                }
            },
    NOT_EQUALS("!=")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the '!=' operator.");
                }
            },
    IN("in")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'in' operator.");
                }
            },
    NOT_IN("not in")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'not in' operator.");
                }
            },
    IS("is")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'is' operator.");
                }
            },
    IS_NOT("is not")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'is not' operator.");
                }
            },
    LESS_THAN("<")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    return new Predicate<T>()
                    {
                        public boolean evaluate(final T input)
                        {
                            return comparator.compare(input, operand) < 0;
                        }
                    };
                }
            },
    LESS_THAN_EQUALS("<=")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    return new Predicate<T>()
                    {
                        public boolean evaluate(final T input)
                        {
                            return comparator.compare(input, operand) <= 0;
                        }
                    };
                }
            },
    GREATER_THAN(">")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    return new Predicate<T>()
                    {
                        public boolean evaluate(final T input)
                        {
                            return comparator.compare(input, operand) > 0;
                        }
                    };
                }
            },
    GREATER_THAN_EQUALS(">=")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T value)
                {
                    return new Predicate<T>()
                    {
                        public boolean evaluate(final T input)
                        {
                            return comparator.compare(input, value) >= 0;
                        }
                    };
                }
            },
    WAS("was")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'was' operator.");
                }
            },
    WAS_NOT("was not")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'was not' operator.");
                }
            },
    WAS_IN("was in")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'was in' operator.");
                }
            },
    WAS_NOT_IN("was not in")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'was not in' operator.");
                }
            },
    CHANGED("changed")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'changed' operator.");
                }
            },
    NOT_CHANGED("not changed")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'not changed' operator.");
                }
            },
    BEFORE("before")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'before' operator.");
                }
            },
    AFTER("after")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'after' operator.");
                }
            },
    FROM("from")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'from' operator.");
                }
            },
    TO("to")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'to' operator.");
                }
            },
    ON("on")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'on' operator.");
                }
            },
    DURING("during")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'during' operator.");
                }
            },
    BY("by")
            {
                public <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand)
                {
                    throw new IllegalStateException("You can not get a predicate for the 'by' operator.");
                }
            };

    private final String displayName;

    private Operator(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayString()
    {
        return displayName;
    }

    public abstract <T> Predicate<T> getPredicateForValue(final Comparator<? super T> comparator, final T operand);


}
