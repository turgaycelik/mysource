package com.atlassian.jira.matchers;

import com.google.common.base.Function;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runners.model.InitializationError;

import static org.hamcrest.Matchers.instanceOf;

/**
 * Matchers for Java errors.
 *
 * @since 2.1
 */
public final class ErrorMatchers
{
    private ErrorMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }


    // based off ExpectedException internals

    public static Matcher<Throwable> withMessage(final String... expectedSubstrings)
    {
        return withMessage(LangMatchers.containsInOrder(expectedSubstrings));
    }

    public static Matcher<Throwable> withMessage(final Matcher<String> messageMatcher)
    {
        return new TypeSafeMatcher<Throwable>()
        {
            @Override
            public boolean matchesSafely(Throwable throwable)
            {
                return messageMatcher.matches(throwable.getMessage());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a Throwable with message ").appendDescriptionOf(messageMatcher);
            }
        };
    }

    public static Matcher<Throwable> withCause(Class<? extends Throwable> causeType)
    {
        // can be fixed when moved to Hamcrest 1.2
        return withCause(LangMatchers.isInstance(causeType));
    }

    public static Matcher<Throwable> withCause(final Matcher<? extends Throwable> causeMatcher)
    {
        return new TypeSafeMatcher<Throwable>()
        {
            @Override
            public boolean matchesSafely(Throwable throwable)
            {
                return causeMatcher.matches(throwable.getCause());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a Throwable with cause (").appendDescriptionOf(causeMatcher).appendText(")");
            }
        };
    }

    public static <T extends Throwable> Matcher<Throwable> specificError(final Class<T> errorClass, final Matcher<T> specificMatcher)
    {
        return new TypeSafeMatcher<Throwable>()
        {
            @Override
            public boolean matchesSafely(Throwable throwable)
            {
                return instanceOf(errorClass).matches(throwable) && specificMatcher.matches(errorClass.cast(throwable));
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("an instance of ").appendValue(errorClass)
                        .appendText(" that is (").appendDescriptionOf(specificMatcher).appendText(")");
            }
        };
    }

    public static Matcher<InitializationError> withCauses(final Matcher<Iterable<Throwable>> causesMatcher)
    {
        return new TypeSafeMatcher<InitializationError>()
        {
            @Override
            public boolean matchesSafely(InitializationError initializationError)
            {
                return causesMatcher.matches(initializationError.getCauses());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a JUnit InitializationError with causes ").appendDescriptionOf(causesMatcher);
            }
        };
    }


    public static <T, U> Matcher<T> withTransformed(final Matcher<U> valueMatcher, final Function<T, U> transformer)
    {
        return new TypeSafeMatcher<T>()
        {
            @Override
            protected boolean matchesSafely(final T item)
            {
                return valueMatcher.matches(transformer.apply(item));
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("an Object transformed that matches ").appendDescriptionOf(valueMatcher);
            }
        };
    }

}
