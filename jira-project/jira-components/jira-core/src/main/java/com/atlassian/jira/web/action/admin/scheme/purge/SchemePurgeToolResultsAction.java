package com.atlassian.jira.web.action.admin.scheme.purge;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@WebSudoRequired
public class SchemePurgeToolResultsAction extends AbstractSchemePurgeAction
{
    private ErrorCollection deletionErrors;
    private List<Scheme> successfullyDeletedSchemes;

    public SchemePurgeToolResultsAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public void doValidation()
    {
    }

    protected String doExecute() throws Exception
    {
        deletionErrors = new SimpleErrorCollection();
        successfullyDeletedSchemes = new ArrayList<Scheme>();
        for (final Scheme scheme : getSchemeObjs())
        {
            List<Project> projects = getSchemeManager(getSelectedSchemeType()).getProjects(scheme);
            if (!projects.isEmpty())
            {
                deletionErrors.addErrorMessage(getText("admin.scheme.purge.result.action.reassociated.scheme", "<strong>", scheme.getName(), "</strong>"));
            }
            else
            {
                getSchemeManager(getSelectedSchemeType()).deleteScheme(scheme.getId());
                successfullyDeletedSchemes.add(scheme);
            }
        }

        // Clear out the session
        ActionContext.getSession().remove(SELECTED_SCHEME_IDS_TO_DELETE_KEY);

        return SUCCESS;
    }

    public ErrorCollection getDeletionErrors()
    {
        return deletionErrors;
    }

    public List getSuccessfullyDeletedSchemes()
    {
        return successfullyDeletedSchemes;
    }
}
