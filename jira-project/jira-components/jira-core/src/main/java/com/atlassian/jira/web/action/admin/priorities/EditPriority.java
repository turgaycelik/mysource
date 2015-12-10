/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.priorities;

import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

import static com.atlassian.jira.util.Colours.isHexColour;
import static org.apache.commons.lang.StringUtils.isBlank;

@WebSudoRequired
public class EditPriority extends AbstractEditConstant
{
    private boolean preview = false;
    private String statusColor;
    private final PriorityManager priorityManager;

    public EditPriority(PriorityManager priorityManager)
    {
        this.priorityManager = priorityManager;
    }

    public String doDefault() throws Exception
    {
        setStatusColor(getConstant().getString("statusColor"));
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!isPreview())
        {
            if (isBlank(getIconurl()))
            {
                addError("iconurl", getText("admin.errors.must.specify.url.for.icon"));
            }

            if (isBlank(getStatusColor()))
            {
                addError("statusColor", getText("admin.errors.must.specify.priority.color"));
            }
            else if (!isHexColour(getStatusColor()))
            {
                addError("statusColor", getText("admin.errors.must.specify.priority.color.as.hex.value"));
            }

            super.doValidation();
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isPreview())
        {
            return INPUT;
        }
        else
        {
            Priority priority = priorityManager.getPriority(id);
            priorityManager.editPriority(priority, name, description, iconurl, statusColor);
            if (getHasErrorMessages())
            {
                return ERROR;
            }
            else
            {
                return getRedirect(getRedirectPage());
        }
    }
    }

    protected String getConstantEntityName()
    {
        return "Priority";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.priority.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "priority";
    }

    protected GenericValue getConstant(String id)
    {
        Priority priorityObject = getConstantsManager().getPriorityObject(id);
        return (priorityObject != null) ? priorityObject.getGenericValue() : null;
    }

    protected String getRedirectPage()
    {
        return "ViewPriorities.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getPriorities();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshPriorities();
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(String statusColor)
    {
        this.statusColor = statusColor;
    }

    public boolean isPreview()
    {
        return preview;
    }

    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }
}
