/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.linking;

import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

@WebSudoRequired
public class EditLinkType extends JiraWebActionSupport
{
    Long id;
    String name;
    String outward;
    String inward;
    private IssueLinkType linkType;
    private final IssueLinkTypeManager issueLinkTypeManager;

    public EditLinkType(IssueLinkTypeManager issueLinkTypeManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
    }

    public String doDefault() throws Exception
    {
        name = getIssueLinkType().getName();
        outward = getIssueLinkType().getOutward();
        inward = getIssueLinkType().getInward();

        return super.doDefault();
    }

    public void doValidation()
    {
        //only do validation if one field is specified
        if (name != null || outward != null || inward != null)
        {
            if (!TextUtils.stringSet(name))
            {
                addError("name", getText("editlink.name.notspecified"));
            }

            if (!TextUtils.stringSet(outward))
            {
                addError("outward", getText("editlink.outward.desc.notspecified"));
            }

            if (!TextUtils.stringSet(inward))
            {
                addError("inward", getText("editlink.inward.desc.notspecified"));
            }

            if (name != null)
            {
                // Check if name already exists and if name conlflicts with a pre-exisiting name.
                if (duplicateLinkName())
                {
                    addError("name", getText("editlink.name.alreadyexists"));
                }
            }
        }
        if (getIssueLinkType() == null)
        {
            addErrorMessage(getText("editlink.id.notfound", id));
        }
    }

    /**
     * Check if IssueLinkType name already exists - if so, get id from this IssueTypeLink name to ensure that selected
     * IssueLinkType name is being edited. This addresses the situation where an IssueLinkType is renamed to conflict
     * with a pre-existing IssueLinkType.
     */
    private boolean duplicateLinkName()
    {
        // Obtain all existing IssueLinkTypes
        Collection<IssueLinkType> existingIssueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByName(getName());

        if (existingIssueLinkTypes != null)
        {
            for (final IssueLinkType issueLinkType : existingIssueLinkTypes)
            {
                if (!id.equals(issueLinkType.getId()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        issueLinkTypeManager.updateIssueLinkType(getIssueLinkType(), getName(), getOutward(), getInward());

        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect("ViewLinkTypes!default.jspa");
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    private IssueLinkType getIssueLinkType()
    {
        if (linkType == null)
        {
            linkType = issueLinkTypeManager.getIssueLinkType(id);
        }

        return linkType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getOutward()
    {
        return outward;
    }

    public void setOutward(String outward)
    {
        this.outward = outward;
    }

    public String getInward()
    {
        return inward;
    }

    public void setInward(String inward)
    {
        this.inward = inward;
    }
}
