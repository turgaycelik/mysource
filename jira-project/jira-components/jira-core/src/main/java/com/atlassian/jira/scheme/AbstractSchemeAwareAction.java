/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.SecurityTypeUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.notification.SchemeAware;
import com.opensymphony.util.TextUtils;

public abstract class AbstractSchemeAwareAction extends JiraWebActionSupport implements SchemeAware
{
    private Long schemeId;
    private GenericValue scheme;
    private Scheme schemeObject;

    public abstract SchemeManager getSchemeManager();

    public abstract String getRedirectURL() throws GenericEntityException;

    /**
     * Gets the scheme Id for the permission scheme
     *
     * @return The scheme Id
     */
    public Long getSchemeId()
    {
        return schemeId;
    }

    /**
     * Sets the schemeId of the permission scheme
     *
     * @param schemeId The value to be set
     */
    public void setSchemeId(Long schemeId)
    {
        this.schemeId = schemeId;
    }

    /**
     * Gets the permission scheme
     *
     * @return The permission scheme of the current action
     * @throws org.ofbiz.core.entity.GenericEntityException
     * @deprecated use {@link #getSchemeObject()}
     */
    public GenericValue getScheme() throws GenericEntityException
    {
        if (scheme == null)
        {
            scheme = getSchemeManager().getScheme(schemeId);
            log.debug("using get scheme instead of get scheme object");
        }
        return scheme;
    }

    public Scheme getSchemeObject() throws DataAccessException
    {
        if (schemeObject == null)
        {
            schemeObject = getSchemeManager().getSchemeObject(schemeId);
        }
        return schemeObject;
    }

    public void doNameValidation(String name, String mode)
    {
        if (!TextUtils.stringSet(name))
            addError("name", getText("admin.errors.specify.a.name.for.this.scheme"));
        else
        {
            try
            {
                //First check that the name doesnt already exist as the same jcase
                Scheme existingScheme = getSchemeManager().getSchemeObject(name.trim());

                //if the existing scheme is not the current scheme
                if (existingScheme != null && !(existingScheme.getId().equals(schemeId)))
                    addError("name", getText("admin.errors.a.scheme.with.this.name.exists"));
                else
                {
                    //Check to see if the name already exists in a different case
                    List<Scheme> schemes = getSchemeManager().getSchemeObjects();

                    for (Scheme scheme : schemes)
                    {
                        if (name.trim().equalsIgnoreCase(scheme.getName()) && !scheme.getId().equals(schemeId))
                        {
                            addError("name", getText("admin.errors.a.scheme.with.this.name.exists"));
                            break;
                        }
                    }
                }
            }
            catch (DataAccessException e)
            {
                addErrorMessage(getText("admin.errors.error.occured.trying.to.the.scheme", mode));
            }
        }
    }

    /**
     * This method is moving logic that used to be in the jsp to the server. Basically if the parameter is blank, we show nothing except in the case
     * of the group
     */
    public String formatSecurityTypeParameter(String type, String parameter)
    {
        return SecurityTypeUtils.formatSecurityTypeParameter(type, parameter, getI18nHelper());
    }
}
