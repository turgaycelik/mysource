package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.pageobjects.components.fields.LegacyPickerSuggestion;
import com.atlassian.jira.pageobjects.components.fields.MultiSelectSuggestion;
import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.util.lang.GuavaPredicates;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static org.junit.Assert.assertTrue;

/**
 * <p/>
 * The old user/group picker that shows up as a popup window, with autocomplete suggestions. Should go away but it won't any time soon...
 *
 * @since v5.0
 */
public class LegacyPicker extends LegacyTriggerPicker
{
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;
    @Inject protected ExtendedElementFinder extendedFinder;


    protected PageElement container;


    public LegacyPicker(@Nullable PageElement form, String pickerId)
    {
        super(form, pickerId);
    }

    public LegacyPicker(String pickerId)
    {
        super(null, pickerId);
    }

    @Init
    protected void init()
    {
        container = root().find(By.id(pickerId + "_container"));
        field = container.find(By.id(pickerId));
        trigger = container.find(By.className("popup-trigger"));
        assertTrue("Picker root should have 'ajax_autocomplete' CSS class", container.hasClass("ajax_autocomplete"));
    }

    protected PageElement container()
    {
        return container;
    }

    @SuppressWarnings ("unchecked")
    public TimedQuery<Iterable<Suggestion>> getSuggestions()
    {
        return (TimedQuery) transformTimed(timeouts, pageBinder,
                extendedFinder.within(container.find(By.className("suggestions"))).newQuery(By.tagName("li")).supplier(),
                LegacyPickerSuggestion.class);
    }

    public TimedCondition hasActiveSuggestion()
    {
        return Conditions.forMatcher(getSuggestions(), Matchers.hasItem(SuggestionMatchers.isActive()));
    }

    public Suggestion getActiveSuggestion()
    {
        Poller.waitUntilTrue(hasActiveSuggestion());
        return Iterables.find(getSuggestions().now(), GuavaPredicates.forMatcher(SuggestionMatchers.isActive()));
    }

    public TimedCondition isSuggestionsPresent()
    {
        return container.timed().hasClass("dropdown-ready");
    }

    public TimedCondition hasSuggestionWithId(String substring)
    {
        return Conditions.forMatcher(getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring(substring, Suggestion.class)));
    }

    public Suggestion getSuggestionWithId(String substring)
    {
        Poller.waitUntilTrue(hasSuggestionWithId(substring));
        return Iterables.find(getSuggestions().now(),
                GuavaPredicates.forMatcher(SuggestionMatchers.idContainsSubstring(substring, Suggestion.class)));
    }


    public LegacyPicker query(String query)
    {
        this.field.type(query);
        return this;
    }

    public LegacyPicker clearQuery()
    {
        this.field.clear();
        return this;
    }

    public LegacyPicker acceptUsingMouse(MultiSelectSuggestion suggestion)
    {
        suggestion.click();
        return this;
    }

    public LegacyPicker acceptActiveSuggestion()
    {
        field.type(Keys.RETURN);
        return this;
    }

    public LegacyPicker down(int steps)
    {
        for (int i =0; i < steps; i++) {
            this.field.type(Keys.DOWN);
        }
        return this;
    }


    public LegacyPicker up(int steps)
    {
        for (int i =0; i < steps; i++) {
            this.field.type(Keys.UP);
        }
        return this;
    }
}
