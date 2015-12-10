package com.atlassian.jira.matchers;

import com.atlassian.jira.issue.Issue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Matchers for the {@link com.atlassian.jira.issue.Issue} domain object.
 *
 * @since 5.1
 */
public final class IssueMatchers
{

    private IssueMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Matcher<Issue> issueWithId(final Long id)
        {
            notNull("id", id);
            return new TypeSafeMatcher<Issue>()
            {
                @Override
                protected boolean matchesSafely(Issue issue)
                {
                    return id.equals(issue.getId());
                }

                @Override
                public void describeTo(Description description)
                {
                    description.appendText("Issue with ID=").appendValue(id);
                }
            };
        }

    public static Matcher<Issue> issueWithKey(final String key)
    {
        notNull("key", key);
        return new TypeSafeMatcher<Issue>()
        {
            @Override
            protected boolean matchesSafely(Issue issue)
            {
                return key.equals(issue.getKey());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Issue with key ").appendValue(key);
            }
        };
    }
}
