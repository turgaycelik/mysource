/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractEditScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

@WebSudoRequired
public class EditScheme extends AbstractEditScheme
{
    private Long defaultLevel;

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getIssueSecuritySchemeManager();
    }

    public String getRedirectURL()
    {
        return "ViewIssueSecuritySchemes.jspa";
    }

    public String doDefault() throws Exception
    {
        setDefaultLevel(getScheme().getLong("defaultlevel"));
        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        GenericValue updatedScheme = getScheme();
        updatedScheme.setString("name", getName());
        updatedScheme.setString("description", getDescription());
        updatedScheme.set("defaultlevel", getDefaultLevel());

        ManagerFactory.getIssueSecuritySchemeManager().updateScheme(updatedScheme);

        return getRedirect(getRedirectURL());
    }

    public Long getDefaultLevel()
    {
        return defaultLevel;
    }

    public void setDefaultLevel(Long defaultLevel)
    {
        if (defaultLevel == null || defaultLevel.equals(new Long(-1)))
            this.defaultLevel = null;
        else
            this.defaultLevel = defaultLevel;
    }

    public Map getSecurityLevels()
    {
        return JiraEntityUtils.createEntityMap(ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(getSchemeId()), "id", "name");
    }
}
