package com.atlassian.jira.scheme;

import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public abstract class AbstractSelectProjectScheme extends AbstractProjectAndSchemeAwareAction
{
    private String[] schemeIds = new String[] { "" };

    @Override
    public String doDefault() throws Exception
    {
        if (getProject() != null)
        {
            Collection<String> schemeIds = Collections2.transform(getSchemeManager().getSchemes(getProject()), new Function<GenericValue, String>()
            {
                @Override
                public String apply(GenericValue scheme)
                {
                    return scheme.getLong("id").toString();
                }
            });
            if (!schemeIds.isEmpty())
            {
                setSchemeIds(schemeIds.toArray(new String[schemeIds.size()]));
            }
        }

        if (hasPermission())
        {
            return super.doDefault();
        }
        else
        {
            return "securitybreach";
        }
    }

    @Override
    protected void doValidation()
    {
        if (getProjectObject() == null)
        {
            addErrorMessage("You must select a project for this scheme");
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getSchemeManager().removeSchemesFromProject(getProjectObject());
        for (String schemeId : getSchemeIds())
        {
            if (TextUtils.stringSet(schemeId))
            {
                Scheme scheme = getSchemeManager().getSchemeObject(new Long(schemeId));
                getSchemeManager().addSchemeToProject(getProjectObject(), scheme);
            }
        }

        if (hasPermission())
        {
            return getRedirect(getProjectReturnUrl());
        }
        else
        {
            return "securitybreach";
        }
    }

    @Override
    public String getRedirectURL()
    {
        return null;
    }

    protected String getProjectReturnUrl()
    {
        return "/plugins/servlet/project-config/" + getProjectObject().getKey() + "/summary";
    }

    protected boolean hasPermission()
    {
        return getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInUser());
    }

    public Collection<GenericValue> getSchemes() throws GenericEntityException
    {
        return getSchemeManager().getSchemes();
    }

    public String[] getSchemeIds()
    {
        return schemeIds;
    }

    public void setSchemeIds(String[] schemeIds)
    {
        this.schemeIds = schemeIds;
    }
}
