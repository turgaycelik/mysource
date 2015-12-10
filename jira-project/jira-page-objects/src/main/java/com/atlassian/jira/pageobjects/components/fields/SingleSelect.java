package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Supplier;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class SingleSelect
{

    @Inject private PageElementFinder elementFinder;
    @Inject private ExtendedElementFinder extendedElementFinder;
    @Inject private AtlassianWebDriver webDriver;
    @Inject private Timeouts timeouts;

    private final PageElement parent;
    private final PageElement container;
    private final PageElement field;
    private final PageElement dropMenu;


    public SingleSelect(PageElement parent)
    {
        this.parent = parent;
        this.container = parent.find(By.className("aui-ss"), TimeoutType.AJAX_ACTION);
        this.field = parent.find(By.tagName("input"));
        this.dropMenu = parent.find(By.className("drop-menu"));
    }

    public String getId()
    {
        return container.getAttribute("id").replace("-single-select", "");
    }

    /**
     * This will click the 'dropdown' icon in the picker, opening or closing the suggestions depending on the current
     * state.
     *
     * @return this single select instance
     */
    public SingleSelect triggerSuggestions()
    {
        this.dropMenu.click();
        return this;
    }

    /**
     *
     *
     * @return list of suggestions at any given moment
     * @deprecated use {@link #getSuggestionsTimed()} as this method is prone to return wrong results due to race
     * conditions. Using {@link #getSuggestionsTimed()} will enforce clients to execute timed assertions and improve
     * reliability of the tests
     */
    @Deprecated
    public List<String> getSuggestions()
    {
        final List<String> suggestions = new ArrayList<String>();
        final PageElement layer = getActiveLayer();
        final List<PageElement> suggestionsEls = layer.findAll(By.cssSelector("a.aui-list-item-link"));
        for (PageElement suggestionsEl : suggestionsEls)
        {
            suggestions.add(suggestionsEl.getAttribute("title"));
        }
        return suggestions;
    }


    public TimedQuery<Iterable<String>> getSuggestionsTimed()
    {
        return Queries.forSupplier(timeouts, extendedElementFinder.within(getActiveLayer())
                .newQuery(By.className("aui-list-item-link"))
                .transform(PageElements.getAttribute("title"))
                .supplier());

    }

    private PageElement getActiveLayer()
    {
        // TODO typical fragile implementation - retrieving whatever active layer is on the page and hoping its the right one. booo :P
        return elementFinder.find(By.cssSelector(".ajs-layer.active"));
    }

    public TimedCondition isSuggestionsOpen()
    {
        return Conditions.and(getActiveLayer().timed().isPresent(), Conditions.forSupplier(timeouts, new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return getActiveLayer().find(By.id(getId() + "-suggestions")).isPresent();
            }
        }));
    }


    public SingleSelect select(String value)
    {
        webDriver.executeScript("window.focus()");

        assertTrue("Unable to find container of the Single-Select", field.isPresent());

        if (isAutocompleteDisabled())
        {
            type(value);
        }
        else
        {
            if (StringUtils.isEmpty(value))
            {
                clear();
                // Say no to suggestions
                if (container.timed().isPresent().byDefaultTimeout())
                {
                    clear();
                }
            }
            else
            {
                type(value);
                waitUntilTrue("Expected query " + value, hasQuery(value));
                try
                {
                    PageElement activeSuggestion = getActiveLayer().find(By.cssSelector("li.active"));
                    activeSuggestion.click();
                }
                catch(NoSuchElementException e)
                {
                    throw new NoSuchElementException("The SingleSelect's suggestions "
                            + "did not contain '" + value + "'.", e);
                }
            }
        }

        return this;
    }

    private TimedCondition hasQuery(String query)
    {
        return and(container.timed().isPresent(), container.timed().hasAttribute("data-query", query));
    }

    public String getValue()
    {
        return field.getValue();
    }

    public TimedQuery<String> getTimedValue()
    {
        return field.timed().getValue();
    }

    public boolean isAutocompleteDisabled()
    {
        return field.hasClass("aui-ss-disabled");
    }

    public SingleSelect clear()
    {
        // Since clear() doesn't dispatch events, we need to send an "input" event
        // to notify the SingleSelect that the input value has changed. We can do this
        // by typing "x" then backspace.
        field.clear().type("x\u0008");
        return this;
    }

    /**
     * Type into this single select without any additional validation
     *
     * @param text text to type
     * @return this single select instance
     */
    public SingleSelect type(CharSequence text)
    {
        clear();
        field.type(text);
        return this;
    }

    public String getError()
    {
        final PageElement error = parent.find(By.className("error"));

        if (error.isPresent())
        {
            return error.getText();
        }
        else
        {
            return null;
        }
    }
}
