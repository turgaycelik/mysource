package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;

/**
 * A generic page that contains warning in page content. Useful for checking if error page appear after user action.
 * <p/>
 * Example usage:
 * <code>jira.visit(GenericPageWithWarningMessage.class, pageUrl, "You must log in to access this page.");</code>
 *
 * @since v6.1
 */
public class GenericPageWithWarningMessage extends AbstractJiraPage
{
    private final String url;
    private final String expectedMessage;

    @ElementBy (id = "content")
    private PageElement content;

    public GenericPageWithWarningMessage(final String url, final String expectedMessage) {
        this.url = url;
        this.expectedMessage = expectedMessage;
    }

    @Override
    public TimedCondition isAt()
    {
        final PageElement warningMessage = content.find(By.className("warning"));
        return Conditions.forMatcher(warningMessage.timed().getText(), Matchers.containsString(expectedMessage));
    }

    @Override
    public String getUrl()
    {
        return url;
    }
}
