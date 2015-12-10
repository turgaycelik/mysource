package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.comparator.IssueTypeKeyComparator;
import com.atlassian.jira.issue.fields.screen.AbstractGVBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class FieldLayoutSchemeEntityImpl extends AbstractGVBean implements FieldLayoutSchemeEntity
{
    private final FieldLayoutManager fieldLayoutManager;
    private final ConstantsManager constantsManager;
    private final IssueTypeKeyComparator issueTypeKeyComparator;

    private Long id;
    private String issueTypeId;
    private Long fieldLayoutId;
    private FieldLayoutScheme fieldLayoutScheme;


    public FieldLayoutSchemeEntityImpl(FieldLayoutManager fieldLayoutManager, GenericValue genericValue, ConstantsManager constantsManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.constantsManager = constantsManager;
        this.issueTypeKeyComparator =  new IssueTypeKeyComparator(constantsManager);
        setGenericValue(genericValue);
        init();
    }

    public Long getId()
    {
        return id;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public GenericValue getIssueType()
    {
        if (getIssueTypeId() != null)
            return constantsManager.getIssueType(getIssueTypeId());
        else
            return null;
    }

    public IssueType getIssueTypeObject()
    {
        if (getIssueTypeId() != null)
            return constantsManager.getIssueTypeObject(getIssueTypeId());
        else
            return null;
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
        updateGV("issuetype", issueTypeId);
    }

    public Long getFieldLayoutId()
    {
        return fieldLayoutId;
    }

    public void setFieldLayoutId(Long fieldLayoutId)
    {
        this.fieldLayoutId = fieldLayoutId;
        updateGV("fieldlayout", fieldLayoutId);
    }

    public FieldLayoutScheme getFieldLayoutScheme()
    {
        return fieldLayoutScheme;
    }

    public void setFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        this.fieldLayoutScheme = fieldLayoutScheme;
        if (fieldLayoutScheme != null)
            updateGV("scheme", fieldLayoutScheme.getId());
        else
            updateGV("scheme", null);
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
            this.issueTypeId = getGenericValue().getString("issuetype");
            this.fieldLayoutId = getGenericValue().getLong("fieldlayout");
        }

        setModified(false);
    }

    public void store()
    {
        if (isModified())
        {
            if (getGenericValue() == null)
            {
                fieldLayoutManager.createFieldLayoutSchemeEntity(this);
            }
            else
            {
                fieldLayoutManager.updateFieldLayoutSchemeEntity(this);
            }
        }
    }

    public void remove()
    {
        if (getGenericValue() != null)
        {
            fieldLayoutManager.removeFieldLayoutSchemeEntity(this);
        }
    }

    public int compareTo(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        return issueTypeKeyComparator.compare(this.getIssueTypeId(), fieldLayoutSchemeEntity.getIssueTypeId());
    }
}
