package com.atlassian.jira.pageobjects.pages.admin;


import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since v4.4
 */
public class UalConfigurePage extends AbstractJiraPage
{
    @ElementBy(id= "ual")
    private PageElement ualContainer;

    private String project;

    public UalConfigurePage(String project)
    {
        this.project = project;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/applinks/listEntityLinks/com.atlassian.applinks.api.application.jira.JiraProjectEntityType/" + project;
    }

    @Override
    public TimedCondition isAt()
    {
        return ualContainer.timed().isPresent();
    }
}
