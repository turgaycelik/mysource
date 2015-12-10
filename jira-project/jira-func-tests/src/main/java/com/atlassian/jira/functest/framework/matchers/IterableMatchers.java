package com.atlassian.jira.functest.framework.matchers;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.google.common.base.Preconditions.checkArgument;
//import static com.atlassian.jira.util.dbc.Assertions.is;

/**
 * Some moar iterable matchers.
 *
 * @since 5.1
 */
public final class IterableMatchers
{

    private IterableMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }


    @SuppressWarnings ("unchecked")
    public static <E> Matcher<Iterable<E>> emptyIterable(Class<E> elementType)
    {
        return (Matcher)Matchers.emptyIterable();
    }

    public static <E> Matcher<Iterable<E>> iterableWithSize(int expected, Class<E> elementType)
    {
        return Matchers.iterableWithSize(expected);
    }

    public static <E> Matcher<Iterable<E>> hasSizeOfAtLeast(final int expectedMinimumSize, Class<E> elementType)
    {
        return hasSizeOfAtLeast(expectedMinimumSize);
    }

    public static <E> Matcher<Iterable<E>> hasSizeOfAtLeast(final int expectedMinimumSize)
    {
        checkArgument(expectedMinimumSize >= 0, "Expected minimum size must be at least 0");
        return new TypeSafeDiagnosingMatcher<Iterable<E>>()
        {
            @Override
            protected boolean matchesSafely(Iterable<E> items, Description mismatchDescription)
            {
                final int size = Iterables.size(items);
                if (size < expectedMinimumSize)
                {
                    mismatchDescription.appendText("Expected size of at least ").appendValue(expectedMinimumSize)
                            .appendText(" but was only ").appendValue(size);
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("an iterable with size of at least ").appendValue(expectedMinimumSize)
                        .appendText(" or more");
            }
        };
    }

    public static <E> Matcher<Iterable<E>> isSingleton(Class<E> type, E singleElement)
        {
            return isSingleton(singleElement);
        }

    @SuppressWarnings ("unchecked")
    public static <E> Matcher<Iterable<E>> isSingleton(E expectedElement)
    {
        return isSingleton((Matcher<E>)Matchers.is(expectedElement));
    }

    public static <E> Matcher<Iterable<E>> isSingleton(Matcher<E> singleElementMatcher)
    {
        return Matchers.allOf(Matchers.<E>iterableWithSize(1), hasItemThat(singleElementMatcher));
    }

    public static <E> Matcher<Iterable<E>> hasItems(Class<E> itemType, E... items)
    {
        return Matchers.hasItems(items);
    }

    @SuppressWarnings ("unchecked")
    public static <E> Matcher<Iterable<E>> hasItemsThat(Class<E> itemType, Matcher<E>... items)
    {
        return (Matcher)Matchers.hasItems(items);
    }

    @SuppressWarnings ("unchecked")
    public static <E> Matcher<Iterable<E>> hasItemThat(Matcher<E> itemMatcher)
    {
        return (Matcher)Matchers.hasItem(itemMatcher);
    }

    public static <E> Matcher<Iterable<E>> hasNoItemThat(Matcher<E> itemMatcher)
    {
        return Matchers.not(hasItemThat(itemMatcher));
    }

    @SuppressWarnings ("unchecked")
    public static <E> Matcher<Iterable<E>> containsAt(E item, int at)
    {
        return containsAt((Matcher<E>)Matchers.is(item), at);
    }

    public static <E> Matcher<Iterable<E>> containsFirst(E item)
    {
        return containsAt(item, 0);
    }

    public static <E> Matcher<Iterable<E>> containsFirst(Matcher<E> itemMatcher)
    {
        return containsAt(itemMatcher, 0);
    }

    public static <E> Matcher<Iterable<E>> containsLast(E item)
    {
        return containsLast((Matcher<E>)Matchers.is(item));
    }

    public static <E> Matcher<Iterable<E>> containsLast(final Matcher<E> itemMatcher)
    {
        return new TypeSafeDiagnosingMatcher<Iterable<E>>()
        {
            @Override
            protected boolean matchesSafely(Iterable<E> items, Description mismatchDescription)
            {
                E lastItem = Iterables.getLast(items);
                return itemMatcher.matches(lastItem);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("an iterable containing an element at last position that matches ")
                        .appendDescriptionOf(itemMatcher);
            }
        };
    }

    public static <E> Matcher<Iterable<E>> containsAt(final Matcher<E> itemMatcher, final int at)
    {
        checkArgument(at >= 0, "at must be at least 0");
        return Matchers.allOf(IterableMatchers.<E>hasSizeOfAtLeast(at+1), new TypeSafeDiagnosingMatcher<Iterable<E>>()
        {
            @Override
            protected boolean matchesSafely(Iterable<E> items, Description mismatchDescription)
            {
                E itemAt = Iterables.get(items, at);
                return itemMatcher.matches(itemAt);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("an iterable containing an element at position ").appendValue(at)
                        .appendText(" that matches ").appendDescriptionOf(itemMatcher);
            }
        });
    }
}
