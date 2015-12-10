package com.atlassian.jira;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * A simple java bean that returns i18n name and description values.
 */
public abstract class AbstractSimpleI18NBean
{
    protected String id;
    private JiraAuthenticationContext authenticationContext;
    protected String name;
    protected String nameKey;
    protected String description;
    protected String descriptionKey;

    public AbstractSimpleI18NBean(String id, String name, String description, String nameKey, String descriptionKey)
    {
        init(id, name, description, nameKey, descriptionKey, null);
    }

    public AbstractSimpleI18NBean(String id, String name, String description, String nameKey, String descriptionKey, JiraAuthenticationContext authenticationContext)
    {
        init(id, name, description, nameKey, descriptionKey, authenticationContext);
    }

    private void init(String id, String name, String description, String nameKey, String descriptionKey, JiraAuthenticationContext authenticationContext)
    {
        this.descriptionKey = descriptionKey;
        this.nameKey = nameKey;
        this.description = description;
        this.name = name;
        this.id = id;
        this.authenticationContext = authenticationContext;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        if (nameKey != null)
        {
            return getText(nameKey);
        }

        return name;
    }

    public String getDescription()
    {
        if (descriptionKey != null)
        {
            return getText(descriptionKey);
        }

        return description;
    }

    protected String getText(String key)
    {
        return getAuthenticationContext().getI18nHelper().getText(key);
    }

    private JiraAuthenticationContext getAuthenticationContext()
    {
        if(authenticationContext == null)
        {
            authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        }
        return authenticationContext;
    }
}
