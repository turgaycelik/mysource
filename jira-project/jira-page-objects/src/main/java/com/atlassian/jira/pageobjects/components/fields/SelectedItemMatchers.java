package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.pages.admin.roles.SelectedItemList;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for Watchers.
 *
 * @since v5.2
 */
public class SelectedItemMatchers
{
    public static Matcher<SelectedItemList.SelectedItem> hasWatcherKey(final String key)
    {
        Assertions.notNull("key", key);
        return new TypeSafeMatcher<SelectedItemList.SelectedItem>()
        {
            @Override
            public boolean matchesSafely(SelectedItemList.SelectedItem issue)
            {
                return issue.getRowKey().now().matches(key);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Watcher list contains watcher with key ").appendValue(key);
            }
        };
    }

    public static Matcher<SelectedItemList.SelectedItem> containsMatcherKeys(final Iterable<String> watcherKey)
    {
        Assertions.notNull("watcherKey", watcherKey);
        return new TypeSafeMatcher<SelectedItemList.SelectedItem>()
        {
            @Override
            public boolean matchesSafely(SelectedItemList.SelectedItem watcher)
            {
                return Iterables.contains(watcherKey, watcher.getRowKey().now());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Watcher list contains watchers with keys ").appendValue(watcherKey);
            }
        };
    }
}
