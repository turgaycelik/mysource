package com.atlassian.jira.rest.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import javax.ws.rs.core.Response;

/**
 * @since v6.3
 */
public class BodyMatcher<T> extends TypeSafeDiagnosingMatcher<Response>
{
    private final Class<T> type;
    private final Matcher<? super T> bodyMatcher;

    public BodyMatcher(final Class<T> type, final Matcher<? super T> bodyMatcher)
    {
        this.type = type;
        this.bodyMatcher = bodyMatcher;
    }

    @Override
    protected boolean matchesSafely(final Response item, final Description mismatchDescription)
    {
        final Object entity = item.getEntity();
        if (type.isInstance(entity))
        {
            if (bodyMatcher.matches(entity))
            {
                return true;
            }
            else
            {
                mismatchDescription.appendText("Body of type ")
                        .appendText(entity.getClass().getSimpleName())
                        .appendText(" ");
                bodyMatcher.describeMismatch(entity, mismatchDescription);
                return false;
            }
        }
        else
        {
            mismatchDescription.appendText("Body of type ").appendText(entity.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("Body of type ")
                .appendText(type.getSimpleName())
                .appendText(" ")
                .appendDescriptionOf(bodyMatcher);
    }
}
