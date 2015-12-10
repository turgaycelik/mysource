package com.atlassian.jira.rest.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.List;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v6.3
 */
public class NoCache extends TypeSafeDiagnosingMatcher<Response>
{
    private static final String CACHE_CHECK = "Cache-Control";

    @Override
    protected boolean matchesSafely(final Response item, final Description mismatchDescription)
    {
        List<Object> object = item.getMetadata().get(CACHE_CHECK);
        if (object == null || object.isEmpty())
        {
            mismatchDescription.appendText("No cache headers present.");
            return false;
        }
        else if (object.size() > 1)
        {
            mismatchDescription.appendText("Multiple Cache Headers present: ").appendValue(object);
            return false;
        }
        else if (!object.get(0).equals(never()))
        {
            mismatchDescription.appendText("Invalid Cache Headers present: ").appendValue(object);
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("Not cached: ").appendValue(never());
    }
}
