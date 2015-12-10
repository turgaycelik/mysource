package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.opensymphony.module.propertyset.PropertySet;

public class DeleteUserProperty extends UserProperty
{
    private boolean confirm;

    public DeleteUserProperty(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, final UserPropertyManager userPropertyManager, UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
    }

    protected void doValidation()
    {
        if (getUser() == null)
        {
            addErrorMessage(getText("admin.errors.users.user.does.not.exist"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm && !hasAnyErrors())
        {
            // Get the 'true' key, ie the META PREFIX + the 'key'
            String trueKey = getTrueKey();
            PropertySet ps = userPropertyManager.getPropertySet(getUser());
            // If the KEY currently exists for this user, remove the entry
            if (ps.exists(trueKey))
            {
                ps.remove(trueKey);
            }
        }

        // return to the Edit user properties page
        return returnComplete("EditUserProperties.jspa?name=" + JiraUrlCodec.encode(getName()));
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }
}
