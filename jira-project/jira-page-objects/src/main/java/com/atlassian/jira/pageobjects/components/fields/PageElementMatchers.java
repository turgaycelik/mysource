package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Generic Page Element Matchers for when dealing with page elements
 *
 * @since v5.2
 */
public class PageElementMatchers
{
    public static Matcher<PageElement> containsAttribute(final String attribute, final String value)
    {
        return new TypeSafeMatcher<PageElement>()
        {
            @Override
            public boolean matchesSafely(PageElement element)
            {
                return element.hasAttribute(attribute, value);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a page element with attribute ")
                        .appendValue(attribute)
                        .appendText(" and value ")
                        .appendValue(value);
            }
        };
    }

    public static Matcher<PageElement> containsAttributes(final String attribute, final Iterable<String> attributeValues)
    {
        Assertions.notNull("attributeValues", attributeValues);
        return new TypeSafeMatcher<PageElement>()
        {
            @Override
            public boolean matchesSafely(PageElement element)
            {
                return Iterables.contains(attributeValues, element.getAttribute(attribute));
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a page element with attributes ")
                        .appendValue(attributeValues);
            }
        };
    }
}
