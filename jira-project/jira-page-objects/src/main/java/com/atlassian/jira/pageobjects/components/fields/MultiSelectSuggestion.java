package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.base.Function;
import org.openqa.selenium.By;

import javax.annotation.Nullable;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.4
 */
public class MultiSelectSuggestion implements Suggestion
{
    public static final Function<PageElement, MultiSelectSuggestion> BUILDER = new Function<PageElement, MultiSelectSuggestion>()
    {
        @Override
        public MultiSelectSuggestion apply(@Nullable PageElement from)
        {
            return new MultiSelectSuggestion(from);
        }
    };

    protected final PageElement container;

    public MultiSelectSuggestion(PageElement container)
    {
        this.container = notNull(container);
    }

    @Override
    public Suggestion click()
    {
        findLink().click();
        return this;
    }



    @Override
    public TimedQuery<String> getId()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public TimedCondition isActive()
    {
        return container.timed().hasClass("active");
    }

    @Override
    public TimedQuery<String> getText()
    {
        return container.timed().getText();
    }

    public String getMainLabel()
    {
        final String all =  findLink().getText();
        // TODO we can do better by spanning main label in List.js. But not on RC day!
        return all.substring(0, all.length() - getAliasLabel().length()).trim();
    }

    private PageElement findLink()
    {
        return container.find(By.tagName("a"));
    }

    public String getAliasLabel()
    {
        PageElement aliasSpan = container.find(By.className("aui-item-suffix"));
        if (aliasSpan.isPresent())
        {
            return aliasSpan.getText();
        }
        else
        {
            return "";
        }
    }

    @Override
    public String toString()
    {
        return asString("Suggestion[mainLabel=", getMainLabel(), ",aliasLabel=", getAliasLabel(), "]");
    }

    @Override
    public int hashCode()
    {
        return getMainLabel().hashCode() * 37 + getAliasLabel().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!MultiSelectSuggestion.class.isInstance(obj))
        {
            return false;
        }
        final MultiSelectSuggestion that = (MultiSelectSuggestion) obj;
        return this.getMainLabel().equals(that.getMainLabel()) && this.getAliasLabel().equals(that.getAliasLabel());
    }
}
