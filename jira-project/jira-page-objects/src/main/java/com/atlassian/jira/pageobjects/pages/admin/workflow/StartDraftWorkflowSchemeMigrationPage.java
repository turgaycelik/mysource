package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static java.lang.String.format;

/**
 *
 * @since v5.2
 */
public class StartDraftWorkflowSchemeMigrationPage extends AbstractJiraPage
{
    @ElementBy (name = "projectId")
    private PageElement projectIdElement;

    @ElementBy (name = "schemeId")
    private PageElement schemeIdElement;

    @ElementBy (name = "draftMigration")
    private PageElement draftMigration;

    @ElementBy (name = "Associate")
    private PageElement submitButton;

    private final Long schemeId;
    private final Long projectId;

    public StartDraftWorkflowSchemeMigrationPage(Long schemeId)
    {
        this(schemeId, null);
    }

    public StartDraftWorkflowSchemeMigrationPage(Long schemeId, Long projectId)
    {
        this.schemeId = schemeId;
        this.projectId = projectId;
    }

    public Long getProjectId()
    {
        if (projectIdElement.isPresent())
        {
            return Long.valueOf(projectIdElement.getValue());
        }
        else
        {
            return null;
        }
    }

    public Long getSchemeId()
    {
        if (schemeIdElement.isPresent())
        {
            return Long.valueOf(schemeIdElement.getValue());
        }
        else
        {
            return null;
        }
    }

    public Boolean isDraftMigration()
    {
        if (draftMigration.isPresent())
        {
            return Boolean.valueOf(draftMigration.getValue());
        }
        else
        {
            return null;
        }
    }

    public boolean isSubmitPresent()
    {
        return submitButton.isPresent();
    }

    @Override
    public String getUrl()
    {
        return format("/secure/project/SelectProjectWorkflowSchemeStep2!default.jspa?draftMigration=true&schemeId=%d&projectId=%d", schemeId, projectId);
    }

    @Override
    public TimedCondition isAt()
    {
        return projectIdElement.timed().isPresent();
    }
}
