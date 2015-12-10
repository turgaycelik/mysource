package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v4.4
 */
public class AdvancedSearch extends AbstractIssueNavigatorPage implements Page
{

    @Inject
    private PageBinder pageBinder;

    @ElementBy(id="advanced-search")
    protected PageElement jqlInput;

    /** @deprecated use {@link com.atlassian.jira.pageobjects.navigator.AbstractIssueNavigatorPage#searchButton} instead. */
    protected PageElement submitButton;

    @ElementBy(cssSelector = ".navigator-search .aui-message.error")
    protected PageElement jqlError;

    @ElementBy(id = "switchnavtype")
    protected PageElement switchNavType;

    @ElementBy(cssSelector = ".jqlerror-container .info")
    protected PageElement jqlInfo;

    @Nullable
    protected Long filterId;

    public AdvancedSearch() {
        // empty
    }

    public AdvancedSearch(Long filterId) {
        this.filterId = filterId;
    }

    @Init
    public void initialize()
    {
        this.submitButton = super.searchButton;
    }

    @Override
    public String getUrl()
    {
        if (filterId != null) {
            return "/issues/?filter=" + filterId;
        } else {
            return "/issues/?jql=";
        }
    }

    @Override
    public TimedCondition isAt()
    {
        waitUntilTrue("Page doesn't seem to have loaded yet...", super.isAt());
        return Conditions.isEqual(NavigatorMode.ADVANCED.toString(), toggleSearchMode(NavigatorMode.ADVANCED));
    }

    /**
     * @return The name of the current filter or null if no filter is selected.
     */
    public String getFilterName()
    {
        PageElement filterName = elementFinder.find(By.id("filter-name"));
        if (filterName.isPresent())
        {
            return filterName.getText();
        }
        else
        {
            return null;
        }
    }

    public String getJQL()
    {
        return jqlInput.getValue();
    }

    /**
     * @return {@code true} iff there is a JQL error.
     */
    public boolean hasJQLError()
    {
        return jqlError.isPresent();
    }

    public String getJQLError()
    {
        return jqlError.getText();
    }

    public String getJQLInfo()
    {
        return jqlInfo.getText();
    }

    public String returnJQLErrorMessage()
    {
        return elementFinder.find(By.cssSelector(".navigator-search .aui-message.error")).getText();
    }


    public AdvancedSearch enterQuery(final String query)
    {
        jqlInput.clear().type(query);
        return this;
    }

    public AdvancedSearch submit()
    {
        searchButton.click();
        return this;
    }

    @Override
    public AdvancedSearch switchToAdvanced()
    {
        return this;
    }

    /**
     * @return {@code true} iff a filter is selected and it is dirty.
     */
    public boolean isDirty()
    {
        String selector = "#filter-description .warning";
        return elementFinder.find(By.cssSelector(selector)).isPresent();
    }
}
