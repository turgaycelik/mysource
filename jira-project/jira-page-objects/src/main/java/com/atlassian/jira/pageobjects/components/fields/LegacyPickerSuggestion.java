package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the Suggestion implementation in the legacy pickers.
 *
 * @since v5.2
 */
public class LegacyPickerSuggestion implements Suggestion
{
    protected final PageElement container;
    public LegacyPickerSuggestion(PageElement container)
    {
        this.container = notNull(container);
    }

    public Suggestion click()
    {
        container.click();
        return this;
    }

    public TimedCondition isActive()
    {
        return container.timed().hasClass("active");
    }

    public TimedQuery<String> getText()
    {
        return container.find(By.className("yad")).timed().getText();
    }

    public TimedQuery<String> getId()
    {
        return container.find(By.className("yad")).timed().getAttribute("id");
    }
}
