package com.atlassian.jira.pageobjects.elements;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for Aui Messages.
 *
 * @since v5.2
 */
public final class MessageMatchers
{

    private MessageMatchers()
    {
    }

    public static Matcher<AuiMessage> hasText(final String text)
    {
        return new TypeSafeMatcher<AuiMessage>()
        {
            @Override
            public boolean matchesSafely(AuiMessage auiMessage)
            {
                return auiMessage.getMessage().now().contains(text);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("an AUI message containing the following text ").appendValue(text);
            }
        };
    }
}
