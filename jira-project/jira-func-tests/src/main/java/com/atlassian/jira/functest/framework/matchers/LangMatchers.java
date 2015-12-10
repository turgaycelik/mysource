package com.atlassian.jira.functest.framework.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * More matchers for core Java thingies.
 *
 * @since 2.1
 */
public final class LangMatchers
{

    private LangMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }

    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> isInstance(Class<? extends T> type)
    {
        return (Matcher<T>) Matchers.instanceOf(type);
    }

    public static Matcher<String> containsInOrder(final CharSequence... substrings)
    {
        return new TypeSafeMatcher<String>()
        {
            @Override
            public boolean matchesSafely(String item)
            {
                int index = -1;
                for (CharSequence substring : substrings)
                {
                    index = item.indexOf(substring.toString(), index);
                    if (index < 0)
                    {
                        return false;
                    }
                    else
                    {
                        index += substring.length();
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a string that contains (in order): ").appendValueList("(", ",", ")", substrings);
            }
        };
    }
}
