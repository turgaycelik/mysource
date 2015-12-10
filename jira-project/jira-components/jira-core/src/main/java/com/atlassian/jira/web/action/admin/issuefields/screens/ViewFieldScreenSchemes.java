package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class ViewFieldScreenSchemes extends AbstractFieldScreenSchemeAction
{
    private String confirm;
    private Map issueTypeScreenSchemeMap;
    private Map projectsMap;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenManager fieldScreenManager;
    private Collection fieldScreens;
    private Long fieldScreenId;

    public ViewFieldScreenSchemes(FieldScreenSchemeManager fieldScreenSchemeManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenSchemeManager);
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenManager = fieldScreenManager;
        issueTypeScreenSchemeMap = new HashMap();
        projectsMap = new HashMap();
    }

    protected String doExecute() throws Exception
    {
        return getResult();
    }

    public String doAddNewFieldScreenScheme() throws Exception
    {
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doAddFieldScreenScheme()
    {
        validateName(false);

        if (!invalidInput())
        {
            FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(getFieldScreenSchemeManager(), null);
            fieldScreenScheme.setName(getFieldScreenSchemeName());
            fieldScreenScheme.setDescription(getFieldScreenSchemeDescription());
            fieldScreenScheme.store();
            // Create a default scheme entity
            FieldScreenSchemeItem fieldScreenSchemeItem = new FieldScreenSchemeItemImpl(getFieldScreenSchemeManager(), (GenericValue) null, fieldScreenManager);
            fieldScreenSchemeItem.setIssueOperation(null);
            fieldScreenSchemeItem.setFieldScreen(fieldScreenManager.getFieldScreen(getFieldScreenId()));
            fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItem);
            return returnCompleteWithInlineRedirect("ConfigureFieldScreenScheme.jspa?id=" + fieldScreenScheme.getId());
        }

        return ERROR;
    }

    @RequiresXsrfCheck
    public String doDeleteFieldScreenScheme()
    {
        validateId();

        if (!invalidInput())
        {
            FieldScreenScheme fieldScreenScheme = getFieldScreenScheme();
            fieldScreenScheme.remove();
            return redirectToView();
        }

        return getResult();
    }

    public String doViewDeleteFieldScreenScheme()
    {
        validateId();

        if (!invalidInput())
        {
            return "confirm";
        }

        return getResult();
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public Collection getIssueTypeScreenSchemes(FieldScreenScheme fieldScreenScheme)
    {
        if (!issueTypeScreenSchemeMap.containsKey(fieldScreenScheme.getId()))
        {
            issueTypeScreenSchemeMap.put(fieldScreenScheme.getId(), issueTypeScreenSchemeManager.getIssueTypeScreenSchemes(fieldScreenScheme));
        }

        return (Collection) issueTypeScreenSchemeMap.get(fieldScreenScheme.getId());
    }

    public Collection getFieldScreens()
    {
        if (fieldScreens == null)
        {
            fieldScreens = fieldScreenManager.getFieldScreens();
        }

        return fieldScreens;
    }

    public Long getFieldScreenId()
    {
        return fieldScreenId;
    }

    public void setFieldScreenId(Long fieldScreenId)
    {
        this.fieldScreenId = fieldScreenId;
    }
}
