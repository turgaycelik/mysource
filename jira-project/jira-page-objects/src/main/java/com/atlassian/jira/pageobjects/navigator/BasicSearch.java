package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.MultiSelectElement;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.annotation.Nullable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Author: Geoffrey Wong
 * Page for Basic Mode Issue Navigator page (whilst KickAss Navigator still in development)
 */
public class BasicSearch extends AbstractIssueNavigatorPage
{

    @Nullable
    protected Long filterId;

    /** @deprecated use {@link com.atlassian.jira.pageobjects.navigator.AbstractIssueNavigatorPage#searchButton} instead. */
    protected PageElement search;

    @ElementBy (id = "searcher-pid")
    protected MultiSelectElement projectSelect;
    
    @ElementBy (id = "searcher-type")
    protected MultiSelectElement issueTypeSelect;
    
    @ElementBy (id = "searcher-status")
    protected MultiSelectElement issueStatusSelect;

    public String getUrl()
    {
        if (filterId != null)
        {
            return "/secure/IssueNavigator.jspa?navType=simple&mode=show&requestId=" + filterId;
        } else
        {
            return "/secure/IssueNavigator!switchView.jspa?navType=simple&mode=show&createNew=true";
        }
    }

    public BasicSearch()
    {
        // empty
    }
    
    public BasicSearch(Long filterId)
    {
        this.filterId = filterId;
    }

    @Init
    public void initialize()
    {
        this.search = super.searchButton;
    }

    @Override
    public TimedCondition isAt()
    {
        waitUntilTrue("Page doesn't seem to have loaded yet...", super.isAt());
        return Conditions.isEqual(NavigatorMode.BASIC.toString(), toggleSearchMode(NavigatorMode.BASIC));
    }

    public BasicSearch selectProject(String project)
    {
        projectSelect.select(Options.text(project));
        return this;
    }
    
    public BasicSearch selectIssueType(String issueType)
    {
        issueTypeSelect.select(Options.text(issueType));
        return this;
    }
    
    public BasicSearch selectIssueStatus(String status)
    {
        issueStatusSelect.select(Options.text(status));
        return this;
    }

    public BasicSearch search()
    {
        searchButton.click();
        return this;
    }

    public void expandAllNavigatorSections()
    {
        expandNavigatorSection("common-concepts-projectcomponents-group");
        expandNavigatorSection("navigator-filter-subheading-issueattributes-group");
        expandNavigatorSection("navigator-filter-subheading-datesandtimes-group");
        expandNavigatorSection("navigator-filter-subheading-workratio-group");
        expandNavigatorSection("navigator-filter-subheading-customfields-group");
    }

    public void expandNavigatorSection(final String sectionId)
    {
        if (!elementFinder.findAll(By.id(sectionId)).isEmpty())
        {
            final String toggleClass = elementFinder.find(By.xpath("//fieldset[@id='" + sectionId + "']")).getAttribute("class");
            if (toggleClass.contains("collapsed"))
            {
                elementFinder.find(By.xpath("//fieldset[@id='" + sectionId + "']/legend/span")).click();
            }
        }
    }

    /** @deprecated This method effectively does nothing. */
    public IssueNavigatorSummaryPage switchToSummary()
    {
        return pageBinder.bind(IssueNavigatorSummaryPage.class);
    }

    @Override
    public BasicSearch switchToSimple()
    {
        return this;
    }

}
