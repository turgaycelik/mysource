/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.linking;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

@WebSudoRequired
public class ViewLinkTypes extends JiraWebActionSupport
{
    private String name;
    private String outward;
    private String inward;

    private final IssueLinkTypeManager issueLinkTypeManager;

    public ViewLinkTypes(IssueLinkTypeManager issueLinkTypeManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
    }

    protected void doValidation()
    {
        //only do validation if one field is specified
        if (name != null || outward != null || inward != null)
        {
            if (!TextUtils.stringSet(name))
            {
                addError("name", getText("admin.errors.you.must.specify.a.name.for.this.link.type"));
            }

            if (!TextUtils.stringSet(outward))
            {
                addError("outward", getText("admin.errors.please.specify.a.description.for.the.outward.link"));
            }

            if (!TextUtils.stringSet(inward))
            {
                addError("inward", getText("admin.errors.please.specify.a.description.for.the.inward.link"));
            }

            // also check that no link with that name already exists
            Collection<IssueLinkType> existing = issueLinkTypeManager.getIssueLinkTypesByName(name);
            if (existing != null && existing.size() > 0)
            {
                addError("name", getText("admin.errors.another.link.type.with.that.name.already.exists"));
            }
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        return SUCCESS;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (name != null)
        {
            // User defined links do not have a style, so set style to null
            issueLinkTypeManager.createIssueLinkType(getName(), getOutward(), getInward(), null);
            refreshState();
        }

        return getResult();
    }

    @RequiresXsrfCheck
    public String doActivate() throws Exception
    {
        getApplicationProperties().setOption(APKeys.JIRA_OPTION_ISSUELINKING, true);
        return getResult();
    }

    @RequiresXsrfCheck
    public String doDeactivate() throws Exception
    {
        getApplicationProperties().setOption(APKeys.JIRA_OPTION_ISSUELINKING, false);
        return getResult();
    }

    /**
     * Get all the listeners in the system.
     *
     * @return A collection of GenericValues representing listeners
     */
    public Collection getLinkTypes()
    {
        return issueLinkTypeManager.getIssueLinkTypes();
    }

    public boolean getIssueLinking()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ISSUELINKING);
    }

    // Public --------------------------------------------------------
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

    private void refreshState()
    {
        name = null;
        inward = null;
        outward = null;
    }

}
