package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class EditUserProperties extends UserProperty
{
    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9\\s]+");

    public EditUserProperties(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, final UserPropertyManager userPropertyManager, final UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
    }

    protected String doExecute() throws Exception
    {
        retrieveUserMetaProperties();
        return getResult();
    }

    @RequiresXsrfCheck
    public String doAdd()
    {
        // Validate
        validateUserInput();

        // Check if we found any errors from the above validation call
        if (hasAnyErrors())
        {
            // If we did
            retrieveUserMetaProperties();
            return ERROR;
        }
        else
        {
            userPropertyManager.getPropertySet(getUser()).setString(getTrueKey(), value);
            return redirectToView();
        }
    }

    private void validateUserInput()
    {
        if (getUser() == null)
        {
            addErrorMessage(getText("admin.errors.users.user.does.not.exist"));
            // no use going any further since we dont have a user to perform the action on.
            return;
        }

        if (StringUtils.isBlank(value))
        {
            addError(VALUE_PARAM_NAME, getText("admin.errors.userproperties.value.empty"));
        }
        else if (value.length() > 250)
        {
            addError(VALUE_PARAM_NAME, getText("admin.errors.userproperties.value.too.long"));
        }

        // Check that the key is not empty and only contains allowable chars and is not over 200 in length
        if (StringUtils.isBlank(key))
        {
            addError(KEY_PARAM_NAME, getText("admin.errors.userproperties.key.empty"));
        }
        else if(!PATTERN.matcher(key).matches())
        {
            addError(KEY_PARAM_NAME, getText("admin.errors.userproperties.key.cannot.use.special.characters"));
        }
        else if (key.length() > 200)
        {
            addError(KEY_PARAM_NAME, getText("admin.errors.userproperties.key.too.long"));
        }
        else
        {
            PropertySet ps = userPropertyManager.getPropertySet(getUser());
            if(ps.getString(getTrueKey()) != null)
            {
                addError(KEY_PARAM_NAME, getText("admin.errors.userproperties.key.already.exists"));
            }
        }

    }

    private String redirectToView()
    {
        return getRedirect("EditUserProperties.jspa?name=" + JiraUrlCodec.encode(getName()));
    }
}
