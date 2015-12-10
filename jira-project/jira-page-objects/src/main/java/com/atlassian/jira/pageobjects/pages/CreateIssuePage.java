package com.atlassian.jira.pageobjects.pages;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Create issue page.
 *
 * @since v5.1
 */
public class CreateIssuePage extends AbstractJiraPage
{
    @Inject
    PageBinder binder;

    @ElementBy (id = "issue-create-submit")
    PageElement submit;

    @Override
    public TimedCondition isAt()
    {
        return submit.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        return "/secure/CreateIssue!default.jspa";
    }

    public CreateIssueDetailsPage submit() {
        this.submit.click();
        return pageBinder.bind(CreateIssueDetailsPage.class);
    }

    public AutoComplete getProjectQuickSearch()
    {
        return binder.bind(QueryableDropdownSelect.class, By.id("project-single-select"), By.id("project-suggestions"));
    }

    public AutoComplete getIssueTypeQuickSearch() {
        return binder.bind(QueryableDropdownSelect.class, By.id("issuetype-single-select"), By.id("issuetype-suggestions"));
    }
}
