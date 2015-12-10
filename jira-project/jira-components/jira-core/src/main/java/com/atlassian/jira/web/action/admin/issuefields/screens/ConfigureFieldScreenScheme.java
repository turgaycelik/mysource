package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

@WebSudoRequired
public class ConfigureFieldScreenScheme extends AbstractFieldScreenSchemeItemAction
{
    private final ProjectIssueTypeScreenSchemeHelper helper;
    private List<Project> projects;

    public ConfigureFieldScreenScheme(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager,
            final ProjectIssueTypeScreenSchemeHelper helper)
    {
        super(fieldScreenSchemeManager, fieldScreenManager);
        this.helper = helper;
    }

    protected void doValidation()
    {
        validateId();
    }

    protected String doExecute() throws Exception
    {
        return getResult();
    }

    @RequiresXsrfCheck
    public String doDeleteFieldScreenSchemeItem()
    {
        validateIssueOperationId();

        if (!invalidInput())
        {
            getFieldScreenScheme().removeFieldScreenSchemeItem(getIssueOperation());
            return redirectToView();
        }

        return getResult();
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final FieldScreenScheme fieldScreenScheme = getFieldScreenScheme();
            projects = helper.getProjectsForFieldScreenScheme(fieldScreenScheme);
        }
        return projects;
    }
}
