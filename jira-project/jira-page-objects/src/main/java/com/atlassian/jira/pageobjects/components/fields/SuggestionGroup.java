package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.framework.fields.HasId;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents a suggesytion group in the automcomplete dropdowns.
 *
 * @since v5.1
 */
public class SuggestionGroup implements HasId
{
    @Inject protected Timeouts timeouts;
    @Inject protected PageBinder pageBinder;

    protected final PageElement container;

    public SuggestionGroup(PageElement container)
    {
        this.container = container;
    }

    public TimedQuery<String> getId()
    {
        return container.timed().getAttribute("id");
    }


    @SuppressWarnings ("unchecked")
    public TimedQuery<Iterable<Suggestion>> getSuggestions()
    {
        return (TimedQuery) Queries.forSupplier(timeouts, new Supplier<Iterable<MultiSelectSuggestion>>()
        {
            @Override
            public Iterable<MultiSelectSuggestion> get()
            {
                return PageElements.transform(pageBinder, container.findAll(By.tagName("li")), MultiSelectSuggestion.class);
            }
        });
    }
}
