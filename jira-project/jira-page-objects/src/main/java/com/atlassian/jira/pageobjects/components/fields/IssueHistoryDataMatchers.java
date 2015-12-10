package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.pages.viewissue.HistoryModule;
import com.atlassian.jira.util.dbc.Assertions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for issue history data
 *
 * @since v5.2
 */
public class IssueHistoryDataMatchers
{
    public static Matcher<HistoryModule.IssueHistoryData> containsHistoryRow(final String fieldName, final String originalValue, final String newValue)
    {
        Assertions.notNull("fieldName", fieldName);
        Assertions.notNull("originalValue", originalValue);
        Assertions.notNull("newValue", newValue);
        return new TypeSafeMatcher<HistoryModule.IssueHistoryData>()
        {
            @Override
            public boolean matchesSafely(HistoryModule.IssueHistoryData item)
            {
                return item.getFieldName().contains(fieldName)
                        && item.getOldValue().contains(originalValue)
                        && item.getNewValue().contains(newValue);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a IssueHistoryDataMatchers with matching fieldName, originalValue, and newValue.");
            }
        };
    }

    public static Matcher<HistoryModule.IssueHistoryData> containsNewValue(final String fieldName, final String newValue)
    {
        Assertions.notNull("fieldName", fieldName);
        Assertions.notNull("newValue", newValue);
        return new TypeSafeMatcher<HistoryModule.IssueHistoryData>()
        {
            @Override
            public boolean matchesSafely(HistoryModule.IssueHistoryData item)
            {
                return item.getFieldName().contains(fieldName)
                        && item.getNewValue().contains(newValue);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a IssueHistoryDataMatchers with matching fieldName and newValue.");
            }
        };
    }
}
