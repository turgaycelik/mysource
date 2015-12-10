package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.inject.Inject;

/**
 * Represents the Change Field Configuration Scheme page
 *
 * @since v4.4
 */
public class ChangeFieldSchemePage extends AbstractJiraPage
{
    private String URI = "/secure/project/SelectFieldLayoutScheme!default.jspa?projectId=%s";
    private Long projectId;

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy(className = "jiraformheader")
    private PageElement formHeader;

    @ElementBy(cssSelector = ".desc-wrap > a > b")
    private PageElement projectName;

    public ChangeFieldSchemePage(final long projectId)
    {
        this.projectId = projectId;
    }

    public boolean hasNoFieldConfigurationSchemes()
    {
        return !projectName.isPresent();
    }

    public String getProjectName()
    {
        return projectName.getText();
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, projectId);
    }

    @Override
    public TimedCondition isAt()
    {
        return formHeader.timed().isPresent();
    }
}
