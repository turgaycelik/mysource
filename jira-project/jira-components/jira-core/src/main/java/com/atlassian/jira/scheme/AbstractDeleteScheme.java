package com.atlassian.jira.scheme;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.google.common.base.Strings;

import java.util.List;

public abstract class AbstractDeleteScheme extends AbstractSchemeAwareAction
{
    private boolean confirmed;

    @Override
    public String doDefault() throws Exception
    {
        // Check to see that the scheme exists
        Scheme scheme = getSchemeObject();
        if (scheme == null)
        {
            addErrorMessage(getText("admin.errors.deletescheme.nonexistent.scheme"));
            return INPUT;
        }

        return super.doDefault();
    }

    @Override
    protected void doValidation()
    {
        if (getSchemeId() == null)
        {
            addError("schemeId", getText("admin.errors.deletescheme.no.scheme.specified"));
        }
        if (getSchemeManager().getDefaultSchemeObject() != null && getSchemeManager().getDefaultSchemeObject().getId().equals(getSchemeId()))
        {
            addErrorMessage(getText("admin.errors.deletescheme.cannot.delete.default"));
        }
        if (!confirmed)
        {
            addErrorMessage(getText("admin.errors.deletescheme.confirmation"));
        }
        if (isActive())
        {
            addErrorMessage(getText("admin.errors.deletescheme.cannot.delete.active", getName()));
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Scheme scheme = getSchemeObject();
        // Handle concurrency gracefully.
        // if someone else has concurrently deleted this and you are tryint to delete, then just don't do anything.
        if (scheme != null)
        {
            //If there are projects already attached then reattach to the default scheme
            // NOTE: we should never be doing this as validation will stop us ever getting to a state where we are
            // deleting an active scheme from an associated project. This is because, for example, removing a workflow
            // scheme from a project requires a migration of all issues in the project to the newly-mapped workflow,
            // which we are not doing below.
            List<Project> projects = getProjects(scheme);

            for (Project project : projects)
            {
                getSchemeManager().removeSchemesFromProject(project);
                getSchemeManager().addDefaultSchemeToProject(project);
            }

            getSchemeManager().deleteScheme(getSchemeId());
        }

        return returnCompleteWithInlineRedirect(getRedirectURL());
    }

    public boolean isActive()
    {
        return !getProjects(getSchemeObject()).isEmpty();
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public String getName()
    {
        return getSchemeObject().getName();
    }

    public String getDescription()
    {
        return Strings.emptyToNull(getSchemeObject().getDescription());
    }

    public List<Project> getProjects(Scheme scheme)
    {
        return getSchemeManager().getProjects(scheme);
    }
}
