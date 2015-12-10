package com.atlassian.jira.matchers;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.google.common.base.Preconditions;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AmendmentMatchers
{
    private AmendmentMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Matcher<Amendment> isError()
    {
        return new TypeSafeMatcher<Amendment>()
        {
            @Override
            protected boolean matchesSafely(final Amendment item)
            {
                return item.isError();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Should be error.");
            }
        };
    }

    public static Matcher<Amendment> withMessage(final String message)
    {
        Preconditions.checkNotNull(message);
        return new TypeSafeMatcher<Amendment>()
        {

            @Override
            protected boolean matchesSafely(final Amendment item)
            {
                return message.equals(item.getMessage());
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Message expected to be: " + message);
            }
        };
    }

}
