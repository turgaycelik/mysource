package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.atlassian.jira.pageobjects.components.IssuePickerPopup.IssuePickerRow;


/**
 * Matcher for Issue Picker Rows
 *
 * @since v5.1
 */

public class IssuePickerRowMatchers
{
    public static Matcher<IssuePickerRow> hasIssueKey(final String key)
    {
        Assertions.notNull("key", key);
        return new TypeSafeMatcher<IssuePickerRow>()
        {
            @Override
            public boolean matchesSafely(IssuePickerRow issue)
            {
                return issue.getRowKey().now().matches(key);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Issue list contains issue with key ").appendValue(key);
            }
        };
    }

    public static Matcher<IssuePickerRow> containsIssueKeys(final Iterable<String> issueKeys)
    {
        Assertions.notNull("issueKeys", issueKeys);
        return new TypeSafeMatcher<IssuePickerRow>()
        {
            @Override
            public boolean matchesSafely(IssuePickerRow issue)
            {
                return Iterables.contains(issueKeys, issue.getRowKey().now());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Issue list contains issue with keys ").appendValue(issueKeys);
            }
        };
    }
}
