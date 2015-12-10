/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.priorities;

import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

import static org.apache.commons.lang.StringUtils.isBlank;

@WebSudoRequired
public class ViewPriorities extends AbstractViewConstants
{
    private boolean preview = false;
    private String statusColor;
    private final PriorityManager priorityManager;

    public ViewPriorities(final TranslationManager translationManager, PriorityManager priorityManager)
    {
        super(translationManager);
        this.priorityManager = priorityManager;
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

    @RequiresXsrfCheck
    public String doAddPriority() throws Exception
    {
        if (isPreview())
        {
            return INPUT;
        }
        else
        {
            if (isBlank(getIconurl()))
            {
                addError("iconurl", getText("admin.errors.must.specify.url.for.icon.of.priority"));
            }

            if (isBlank(getStatusColor()))
            {
                addError("statusColor", getText("admin.errors.must.specify.color"));
            }

            addField("statusColor", getStatusColor());

            return super.doAddConstant();
        }
    }

    protected String redirectToView()
    {
        return getRedirect("ViewPriorities.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY;
    }

    public boolean isPreview()
    {
        return preview;
    }

    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(String statusColor)
    {
        this.statusColor = statusColor;
    }

    @Override
    protected GenericValue addConstant() throws GenericEntityException
    {
        Priority priority = priorityManager.createPriority(name, description, iconurl, statusColor);
        return priority.getGenericValue();
    }

    @RequiresXsrfCheck
    public String doMoveDown()
    {
        priorityManager.movePriorityDown(down);
        return getResult();
    }

    @RequiresXsrfCheck
    public String doMoveUp()
    {
        priorityManager.movePriorityUp(up);
        return getResult();
    }

    @RequiresXsrfCheck
    public String doMakeDefault() throws Exception
    {
        priorityManager.setDefaultPriority(make);
        return SUCCESS;
    }
}
