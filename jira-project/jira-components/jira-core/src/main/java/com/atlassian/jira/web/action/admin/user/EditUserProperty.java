package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUrlCodec;

import static org.apache.commons.lang.StringUtils.isBlank;

public class EditUserProperty extends UserProperty
{
    public EditUserProperty(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, UserPropertyManager userPropertyManager, UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
    }

    protected String doExecute() throws Exception
    {
        if (key == null || isBlank(key) || !userPropertyKeyExists())
        {
            addErrorMessage(getText("admin.errors.userproperty.non.existing.property"));
            return "invalidkey";
        }
        setValue(userPropertyManager.getPropertySet(getUser()).getString(getTrueKey()));
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doUpdate()
    {
        if (isBlank(value))
        {
            addError("value", getText("admin.errors.userproperty.value.empty"));
        }
        else if (value.length() > 250)
        {
            addError("value", getText("admin.errors.userproperty.value.too.long"));
        }
        else if (!userPropertyKeyExists())
        {
            addErrorMessage(getText("admin.errors.userproperty.non.existing.property"));
        }

        // Check if we found any errors
        if (invalidInput())
        {
            // If we did
            retrieveUserMetaProperties();
            return ERROR;
        }
        else
        {
            userPropertyManager.getPropertySet(getUser()).setString(getTrueKey(), value);
            return returnComplete("EditUserProperties.jspa?name=" + JiraUrlCodec.encode(getName()));
        }
    }

    private boolean userPropertyKeyExists()
    {
        return userPropertyManager.getPropertySet(getUser()).exists(getTrueKey());
    }

}
