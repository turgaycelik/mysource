package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.util.lang.GuavaPredicates;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementActions;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.hasId;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Minimal implementation of a FrotherControl. Constructor takes in the id the frother control is bound to.
 *
 * @since v4.4
 */
public class MultiSelect
{
    @Inject protected PageElementFinder elementFinder;
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;
    @Inject protected ExtendedElementFinder extendedFinder;

    protected PageElement textArea;
    protected PageElement selectDiv;
    protected PageElement errorDiv;
    protected PageElement suggestionsContainer;
    protected PageElement dropTrigger;

    protected final String id;
    protected final Function<String, By> itemLocator;

    @Init
    public void initialize()
    {
        this.selectDiv =  elementFinder.find(By.id(id + "-multi-select"));
        this.textArea = selectDiv.find(By.id(id + "-textarea"));
        this.dropTrigger = selectDiv.find(By.className("drop-menu"));
        this.errorDiv = elementFinder.find(By.id(id + "-error"));
        this.suggestionsContainer = elementFinder.find(By.id(id + "-suggestions"));
        waitUntilTrue(isPresent());
    }

    protected TimedQuery<Boolean> isPresent()
    {
        return Conditions.and(
                selectDiv.timed().isPresent(),
                textArea.timed().isPresent()
        );
    }

    /**
     * Constructs the minimal implementation based on a given select[multiple] id.
     *
     * @param id the id of the select[multiple] element
     */
    public MultiSelect(final String id)
    {
        this.id = id;
        this.itemLocator = new Function<String, By>()
        {
            @Override
            public By apply(@Nullable String itemName)
            {
                //means find all items
                if(itemName == null)
                {
                    return By.cssSelector(".representation li");
                }
                else
                {
                    return By.cssSelector(".representation li[title=\"" + itemName + "\"]");
                }

            }
        };
    }

    /**
     * Constructs the minimal implementation based on a given select[multiple] id.
     *
     * @param id the id of the select[multiple] element
     * @param itemLocator a function that given a string will create a locator to locate the item for this multiselect given the name or all items if no name is provided
     */
    public MultiSelect(final String id, Function<String, By> itemLocator)
    {
        this.id = id;
        this.itemLocator = itemLocator;
    }


    /**
     * Clears the current issue query and types the new query.
     *
     * @param query the query to look up issues
     * @return this multi select instance
     */
    public MultiSelect query(CharSequence query)
    {
        textArea.type(query);
        return this;
    }

    /**
     * Focus on multi select, input some value and blur
     *
     * @param query input for multi select
     * @return this multi select instance
     */
    public MultiSelect freeInput(CharSequence query)
    {
        textArea.type(query);
        textArea.javascript().execute("arguments[0].blur()");
        return this;
    }


    /**
     * Open suggestions drop-down using the clickable trigger in the query input element.
     *
     * Note: if the suggestions are currently open, this action will close them. Therefore this method does not
     * wait for the outcome of the trigger. Use {@link #isSuggestionsPresent()} to wait for the expected
     * result.
     *
     * @return this multi select
     */
    public MultiSelect triggerSuggestions()
    {
        dropTrigger.click();
        return this;
    }

    public MultiSelect clearQuery()
    {
        textArea.clear();
        return this;
    }

    public MultiSelect awayFromQueryInputArea()
    {
        textArea.javascript().execute("AJS.$(arguments[0]).trigger('blur');");
        Poller.waitUntilFalse("Suggestions should not be present", isSuggestionsPresent());
        return this;
    }

    /**
     * Adds an item by typing it in and picking the first suggestion. Assumes that the item passed in
     * will be used as the lozenge label
     *
     * @param item the item to add
     */
    public void add(final String item, final String label)
    {
        addNotWait(item);
        waitUntilTrue("Expected item " + item + "to be added, but was not", hasItem(label));
    }

    /**
     * Adds an item by typing it in and picking the first suggestion. Assumes that the item passed in
     * will be used as the lozenge label
     *
     * @param item the item to add
     */
    public void add(final String item)
    {
        addNotWait(item);
        waitUntilTrue("Expected item " + item + "to be added, but was not", hasItem(item));
    }


    public void addNotWait(final String item)
    {
        textArea.type(item);
        waitUntilTrue("Expected suggestions to be present, but was not", isSuggestionsPresent());
        // TODO flaky!
        getFirstSuggestion().click();
    }

    public MultiSelect selectActiveSuggestion()
    {
        waitUntilTrue(hasActiveSuggestion());
        getActiveSuggestion().click();
        return this;
    }

    /**
     * Removes a given item by clicking on the (x) next to the lozenge.
     *
     * @param item the item to remove
     */
    public void remove(final String item)
    {
        final Lozenge itemByName = getItemByName(item);
        itemByName.remove();
        waitUntilFalse("Expected item " + item + " to be removed, but was not", hasItem(item));
    }

    public void clearAllItems()
    {
        final Iterable<Lozenge> items = getItems().now();
        for (final Lozenge item : items)
        {
            final String name = item.getName();
            item.remove();
            waitUntilFalse("Expected item " + item + " to be removed, but was not", hasItem(name));
        }
        waitUntilFalse(hasItems());
    }


    public TimedCondition isSuggestionsLoading()
    {
        return dropTrigger.timed().hasClass("loading");
    }

    public TimedQuery<Boolean> isSuggestionsPresent()
    {
        return suggestionsContainer.timed().isVisible();
    }

    public TimedQuery<Iterable<SuggestionGroup>> allSuggestionGroups()
    {
        return transformTimed(timeouts, pageBinder,
                extendedFinder.within(suggestionsContainer).newQuery(By.tagName("ul")).supplier(),
                SuggestionGroup.class);
    }

    public TimedCondition hasSuggestionGroup(String groupId)
    {
        return Conditions.forMatcher(allSuggestionGroups(), Matchers.hasItem(hasId(groupId, SuggestionGroup.class)));

    }

    public SuggestionGroup getSuggestionGroup(final String groupId)
    {
        Poller.waitUntilTrue(hasSuggestionGroup(groupId));
        return Iterables.find(allSuggestionGroups().now(), new Predicate<SuggestionGroup>()
        {
            @Override
            public boolean apply(SuggestionGroup input)
            {
                String inputId = input.getId().now();
                return groupId.equals(inputId);
            }
        });
    }

    private Suggestion getFirstSuggestion()
    {
        return Iterables.get(allSuggestions().now(), 0);
    }

    public TimedQuery<String> getActiveSuggestionText()
    {
        return Queries.forSupplier(timeouts, new Supplier<String>()
        {
            @Override
            public String get()
            {
                final Suggestion active = getActiveSuggestion();
                if (active != null)
                {
                    return active.getText().now();
                }
                else
                {
                    return "";
                }
            }
        });
    }

    public TimedCondition hasActiveSuggestion()
    {
        return Conditions.forMatcher(allSuggestions(), Matchers.hasItem(SuggestionMatchers.isActive()));
    }

    public Suggestion getActiveSuggestion()
    {
        Poller.waitUntilTrue(hasActiveSuggestion());
        return Iterables.find(allSuggestions().now(), GuavaPredicates.forMatcher(SuggestionMatchers.isActive()));
    }

    @SuppressWarnings ("unchecked")
    public TimedQuery<Iterable<Suggestion>> allSuggestions()
    {
        return (TimedQuery) transformTimed(timeouts, pageBinder,
                extendedFinder.within(suggestionsContainer).newQuery(By.tagName("li")).supplier(),
                MultiSelectSuggestion.class);
    }

    public Lozenge getItemByName(final String name)
    {
        final PageElement element = selectDiv.find(itemLocator.apply(name));
        return pageBinder.bind(Lozenge.class, element);
    }

    public TimedQuery<Iterable<Lozenge>> getItems()
    {
        return transformTimed(timeouts, pageBinder,
                extendedFinder.within(selectDiv).newQuery(itemLocator.apply(null)).supplier(),
                Lozenge.class);
    }

    public TimedQuery<Boolean> hasItems()
    {
        return selectDiv.find(itemLocator.apply(null)).timed().isPresent();
    }

    public TimedQuery<Boolean> hasItem(final String name)
    {
        return selectDiv.find(itemLocator.apply(name)).timed().isPresent();
    }



    public String getError()
    {
        return errorDiv.isPresent() ? StringUtils.trimToNull(errorDiv.getText()) : null;
    }
    
    public String waitUntilError()
    {
        Poller.waitUntilTrue(errorDiv.timed().isPresent());
        return StringUtils.trimToNull(errorDiv.getText());
    }



    public static class Lozenge
    {
        @Inject protected PageElementActions actions;

        private final PageElement item;

        public Lozenge(final PageElement item)
        {
            this.item = item;
        }

        public String getName()
        {
            return item.getAttribute("title");
        }

        public void remove()
        {
            // hover over the element to make the remove icon visible
            if (deleteIcon().isVisible())
            {
                deleteIcon().click();
            }
            else
            {
                // thanks WebDriver, not being able to click hidden things is sooo useful.
                deleteIcon().javascript().execute("jQuery(arguments[0]).click();");
            }
        }

        private PageElement deleteIcon()
        {
            return item.find(By.className("item-delete"));
        }
    }

}
