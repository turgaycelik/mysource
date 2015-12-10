package com.atlassian.jira.functest.matcher;

import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueLink;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.common.base.Objects.equal;

/**
 * Used to test whether an issue is linked to another.
 *
 * @since v5.0
 */
public class LinkedIssuesMatcher extends TypeSafeMatcher<Issue>
{
    public static Matcher<Issue> hasLinkWithInwardIssue(String issueKey, String linkTypeName)
    {
        return new LinkedIssuesMatcher(issueKey, linkTypeName, false);
    }

    public static Matcher<Issue> hasLinkWithOutwardIssue(String issueKey, String linkTypeName)
    {
        return new LinkedIssuesMatcher(issueKey, linkTypeName, true);
    }

    private final String expectedIssueKey;
    private final String expectedLinkTypeName;
    private final boolean outward;

    public LinkedIssuesMatcher(String expectedIssueKey, String expectedLinkTypeName, boolean outward)
    {
        this.expectedIssueKey = expectedIssueKey;
        this.expectedLinkTypeName = expectedLinkTypeName;
        this.outward = outward;
    }

    @Override
    public boolean matchesSafely(Issue issue)
    {
        if (issue != null && issue.fields != null && issue.fields.issuelinks != null)
        {
            for (IssueLink issueLink : issue.fields.issuelinks)
            {
                IssueLink.IssueLinkRef linkedIssue = outward ? issueLink.outwardIssue() : issueLink.inwardIssue();

                String linkTypeName = issueLink.type() != null ? issueLink.type().name() : null;
                String linkedIssueKey = linkedIssue != null ? linkedIssue.key() : null;

                if (equal(expectedLinkTypeName, linkTypeName) && equal(expectedIssueKey, linkedIssueKey))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("an Issue containing a link to ").appendValue(expectedIssueKey).appendText(" with description ").appendValue(expectedLinkTypeName);
    }
}
