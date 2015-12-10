package com.atlassian.jira.dev.reference.plugin.actions;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;

/**
 * An action that prints out value of the reference resource.
 * 
 * @since v4.3
 */
public class ReferenceResourceAction extends JiraWebActionSupport
{
    private static final String NO_KEY = "no_key";
    private static final String NOT_FOUND = "not_found";

    private final JiraAuthenticationContext authenticationContext;
    private String resourceKey;
    private String resource;

    public ReferenceResourceAction(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (StringUtils.isEmpty(resourceKey))
        {
            return NO_KEY;
        }
        resource = authenticationContext.getI18nHelper().getText(resourceKey);
        if (resource.equals(resourceKey))
        {
            return NOT_FOUND;
        }
        return SUCCESS;
    }

    public String getResourceValue()
    {
        return resource;
    }

    public String getResourceKey()
    {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey)
    {
        this.resourceKey = resourceKey;
    }
}
