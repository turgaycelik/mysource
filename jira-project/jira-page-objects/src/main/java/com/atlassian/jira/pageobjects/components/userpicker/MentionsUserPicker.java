package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.util.TimedQueryFactory;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;

import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.hasItemThat;
import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.hasItems;
import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.hasId;
import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.isActive;
import static com.atlassian.pageobjects.elements.query.Conditions.forMatcher;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;
import static org.hamcrest.Matchers.allOf;

/**
 * Represents mentions picker (incl. dropdown).
 *
 * @since v5.2
 */
public class MentionsUserPicker
{

    @Inject PageBinder pageBinder;
    @Inject ExtendedElementFinder finder;
    @Inject TimedQueryFactory queryFactory;

    @ElementBy (id = "mentionDropDown")
    protected PageElement mentionsDropdown;

    protected final PageElement queryTarget;

    // where is the query for users typed into?
    public MentionsUserPicker(PageElement queryTarget)
    {
        this.queryTarget = queryTarget;
    }


    public TimedCondition isOpen()
    {
        return mentionsDropdown.timed().isVisible();
    }

    public TimedQuery<Iterable<UserSuggestion>> suggestions()
    {
        return queryFactory.forSupplier(searchSuggestions().supplier());
    }

    public boolean hasSuggestions()
    {
        return searchSuggestions().search().iterator().hasNext();
    }

    public TimedCondition hasSuggestion(final String username)
    {
        return forMatcher(suggestions(), hasItemThat(hasId(username, UserSuggestion.class)));
    }

    public TimedCondition hasActiveSuggestion(final String username)
    {
        return forMatcher(suggestions(), hasItemThat(allOf(hasId(username, UserSuggestion.class), isActive(UserSuggestion.class))));
    }

    public UserSuggestion getSuggestion(final String username)
    {
        return searchSuggestions().filter(hasId(username)).searchOne();
    }

    protected ExtendedElementFinder.SearchQuery<UserSuggestion> searchSuggestions()
    {
        return finder.within(mentionsDropdown).newQuery(UserSuggestion.ELEMENT_SELECTOR).bindTo(UserSuggestion.class);
    }


    /**
     * Select active suggestion by pressing enter.
     *
     * @return this user picker instance
     */
    public MentionsUserPicker selectActiveSuggestion()
    {
        return typeSpecialKey(Keys.RETURN);
    }

    public MentionsUserPicker up()
    {
        return typeSpecialKey(Keys.UP);
    }

    public MentionsUserPicker down()
    {
        return typeSpecialKey(Keys.DOWN);
    }

    protected MentionsUserPicker typeSpecialKey(Keys key)
    {
        // precondition: must be open
        checkIsOpen();
        queryTarget.type(key);
        return this;
    }

    private void checkIsOpen() {stateTrue("Mentions dropdown must be open", isOpen().now());}

    public static class UserSuggestion implements Suggestion
    {
        public static final By ELEMENT_SELECTOR = By.tagName("li");

        private final PageElement listElement;
        private final PageElement link;

        public UserSuggestion(PageElement pageElement)
        {
            this.listElement = pageElement; // li
            this.link = pageElement.find(By.tagName("a"));
        }

        @Override
        public Suggestion click()
        {
            link.click();
            return this;
        }

        @Override
        public TimedCondition isActive()
        {
            return listElement.timed().hasClass("active");
        }

        @Override
        public TimedQuery<String> getText()
        {
            return listElement.timed().getText();
        }

        @Override
        public TimedQuery<String> getId()
        {
            return getUsername();
        }

        public TimedQuery<String> getUsername()
        {
            return link.timed().getAttribute("rel");
        }

        @Override
        public String toString()
        {
            return "UserSuggestion[username=" + getUsername().now() + "]";
        }
    }
}
