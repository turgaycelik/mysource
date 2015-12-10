package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.util.dbc.Assertions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for multi select items.
 *
 * @since v5.1
 */
public class LozengeMatchers
{

    public static Matcher<MultiSelect.Lozenge> withName(final String name)
    {
        Assertions.notNull("name", name);
        return new TypeSafeMatcher<MultiSelect.Lozenge>()
        {
            @Override
            public boolean matchesSafely(MultiSelect.Lozenge item)
            {
                return name.equals(item.getName());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a lozenge with name ").appendValue(name);
            }
        };
    }
}
