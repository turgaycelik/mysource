package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static java.lang.String.format;

/**
 * Represents the selection of a workflow scheme.
 *
 * @since v4.4
 */
public class SelectWorkflowScheme extends AbstractJiraPage
{
    @ElementBy (id = "schemeId_select")
    private PageElement select;

    @ElementBy (id = "schemeId_select")
    private SelectElement schemeSelect;

    @ElementBy (id = "projectId")
    private PageElement projectIdElement;

    private final long projectId;

    public SelectWorkflowScheme(long projectId)
    {
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

    public String getSelectedScheme()
    {
        if (select.isPresent())
        {
            return schemeSelect.getSelected().text();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getUrl()
    {
        return format("/secure/project/SelectProjectWorkflowScheme!default.jspa?projectId=%d", projectId);
    }

    @Override
    public TimedCondition isAt()
    {
        return select.timed().isPresent();
    }
}
