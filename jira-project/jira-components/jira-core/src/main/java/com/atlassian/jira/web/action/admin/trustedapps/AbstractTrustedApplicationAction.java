package com.atlassian.jira.web.action.admin.trustedapps;

import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationBuilder;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationInfo;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * As usual, actions beget lovely deep inheritance hierarchies as it is the ONLY convenient
 * way to share code...
 *
 * @since v3.12
 */
public abstract class AbstractTrustedApplicationAction extends JiraWebActionSupport
{
    protected final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
    protected final TrustedApplicationService service;

    public AbstractTrustedApplicationAction(TrustedApplicationService service)
    {
        this.service = service;
    }

    public String doDefault()
    {
        if (getId() <= 0)
        {
            addErrorMessage(getText("admin.trustedapps.edit.id.missing"));
            return ERROR;
        }
        loadTrustedApplicationInfo();
        return INPUT;
    }

    @RequiresXsrfCheck
    protected final String doExecute() throws Exception
    {
        doExecuteAction();
        return (getJiraServiceContext().getErrorCollection().hasAnyErrors()) ? ERROR : getRedirect("ViewTrustedApplications.jspa");
    }

    /** do the actual action, any errors are added to the service context. */
    protected abstract void doExecuteAction();

    public void setId(long id)
    {
        builder.setId(id);
    }

    public long getId()
    {
        return builder.getId();
    }

    public String getName()
    {
        return builder.getName();
    }

    private void loadTrustedApplicationInfo()
    {
        final TrustedApplicationInfo applicationInfo = service.get(getJiraServiceContext(), getId());
        if (applicationInfo == null)
        {
            addErrorMessage(getText("admin.trustedapps.edit.id.not.found", String.valueOf(getId())));
            setId(0);
        }
        else
        {
            builder.set(applicationInfo);
        }
    }

    public boolean isEditable()
    {
        return getErrorMessages().isEmpty();
    }
}
