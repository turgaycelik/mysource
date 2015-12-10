package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.AbstractTimedQuery;
import com.atlassian.pageobjects.elements.query.ExpirationHandler;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static com.google.common.collect.Lists.transform;

/**
 * @since v4.4
 */
public class QueryableDropdownSelect implements AutoComplete
{
    @Inject protected PageElementFinder elementFinder;
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;
    @Inject protected ExtendedElementFinder extendedFinder;

    private final By containerSelector;
    private final By suggestionsSelector;

    protected PageElement container;
    protected PageElement field;
    protected PageElement suggestions;
    protected PageElement icon;
    protected PageElement label;

    public QueryableDropdownSelect(final By containerSelector, final By suggestionsSelector)
    {
        this.containerSelector = containerSelector;
        this.suggestionsSelector = suggestionsSelector;
    }

    @Init
    public void getElements()
    {
        container = elementFinder.find(containerSelector, TimeoutType.AJAX_ACTION);
        field = container.find(By.tagName("input"));
        icon = container.find(By.className("icon"));
        label = container.find(By.className("overlabel-apply"));
    }

    public boolean isPresent()
    {
        return container.isPresent();
    }

    @Override
    public TimedQuery<Boolean> timedIsPresent()
    {
        return container.timed().isPresent();
    }

    public AutoComplete clearQuery()
    {
        this.field.clear();
        return this;
    }

    public AutoComplete query(final String query)
    {
        this.field.type(query);
        Poller.waitUntilTrue(container.timed().hasAttribute("data-query", query));
        return this;
    }

    public AutoComplete down(final int steps) {
        for (int i =0; i < steps; i++) {
            this.field.type(Keys.DOWN);
        }
        return this;
    }

    public AutoComplete up(final int steps) {
        for (int i =0; i < steps; i++) {
            this.field.type(Keys.UP);
        }
        return this;
    }

    @Override
    public AutoComplete acceptUsingMouse(final MultiSelectSuggestion suggestion)
    {
        suggestion.click();
        return this;
    }

    @Override
    public AutoComplete acceptUsingKeyboard(MultiSelectSuggestion suggestion)
    {
        field.type(Keys.RETURN);
        return this;
    }

    public PageElement getLabel()
    {
        return label;
    }

    @Override
    public MultiSelectSuggestion getActiveSuggestion()
    {
        PageElement suggestionElement = getSuggestionsContainerElement().find(By.className("active"));
        return new MultiSelectSuggestion(suggestionElement);
    }

    @Override
    public TimedQuery<MultiSelectSuggestion> getTimedActiveSuggestion()
    {
        return new AbstractTimedQuery<MultiSelectSuggestion>(container.timed().isPresent(), ExpirationHandler.RETURN_CURRENT)
        {
            @Override
            protected boolean shouldReturn(MultiSelectSuggestion currentEval)
            {
                return true;
            }

            @Override
            protected MultiSelectSuggestion currentValue()
            {
                return getActiveSuggestion();
            }
        };
    }

    public PageElement getSuggestionsContainerElement()
    {
        return elementFinder.find(suggestionsSelector);
    }

    public List<PageElement> getSuggestionsElements()
    {
        return getSuggestionsContainerElement().findAll(By.tagName("li"));
    }

    
    public List<MultiSelectSuggestion> getSuggestions()
    {
        return transform(getSuggestionsElements(), MultiSelectSuggestion.BUILDER);
    }

    @Override
    public TimedQuery<Iterable<MultiSelectSuggestion>> getTimedSuggestions()
    {
        return transformTimed(timeouts, pageBinder,
                extendedFinder.within(getSuggestionsContainerElement()).newQuery(By.tagName("li")).supplier(),
                MultiSelectSuggestion.class);
    }
}
