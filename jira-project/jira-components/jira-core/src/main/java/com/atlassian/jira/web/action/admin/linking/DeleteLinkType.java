/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.linking;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeDestroyer;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;

@WebSudoRequired
public class DeleteLinkType extends JiraWebActionSupport
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkTypeDestroyer issueLinkTypeDestroyer;
    private final IssueLinkManager issueLinkManager;

    Long id;
    boolean confirm;
    private IssueLinkType linkType;
    private Collection<IssueLink> links;
    private Long swapLinkTypeId;
    String action = "swap";

    public DeleteLinkType(IssueLinkTypeManager issueLinkTypeManager, IssueLinkTypeDestroyer issueLinkTypeDestroyer, IssueLinkManager issueLinkManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkTypeDestroyer = issueLinkTypeDestroyer;
        this.issueLinkManager = issueLinkManager;
    }

    protected void doValidation()
    {
        if (getLinkType() == null)
        {
            addErrorMessage(getText("admin.errors.linking.link.type.not.found",id));
        }

        if (action.equalsIgnoreCase("swap"))
        {
            if (swapLinkTypeId.equals(id))
                addError("swapLinkTypeId", getText("admin.errors.linking.move.links.to.link.type.being.deleted"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            try
            {
                IssueLinkType swapLinkType = null;
                if (action.equalsIgnoreCase("swap"))
                    swapLinkType = issueLinkTypeManager.getIssueLinkType(swapLinkTypeId);

                issueLinkTypeDestroyer.removeIssueLinkType(getId(), swapLinkType, getLoggedInUser());
            }
            catch (RemoveException e)
            {
                log.error("Error occurred while removing link type with id '" + getId() + "'.", e);
                addErrorMessage(getText("admin.errors.linking.error.occured.deleting"));
            }
        }

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

    public IssueLinkType getLinkType()
    {
        if (linkType == null)
        {
            linkType = issueLinkTypeManager.getIssueLinkType(id);
        }

        return linkType;
    }

    public Collection getLinks()
    {
        if (links == null)
        {
            links = issueLinkManager.getIssueLinks(getId());

            if (links == null)
            {
                links = Collections.emptyList();
            }
        }

        return links;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Long getSwapLinkTypeId()
    {
        return swapLinkTypeId;
    }

    public void setSwapLinkTypeId(Long swapLinkTypeId)
    {
        this.swapLinkTypeId = swapLinkTypeId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Collection getOtherLinkTypes()
    {
        Collection<IssueLinkType> otherTypes = newArrayList();

        Collection<IssueLinkType> linkTypes =  issueLinkTypeManager.getIssueLinkTypes();
        for (IssueLinkType linkType : linkTypes)
        {
            if (!linkType.equals(getLinkType()))
            { otherTypes.add(linkType); }
        }

        return otherTypes;
    }
}
