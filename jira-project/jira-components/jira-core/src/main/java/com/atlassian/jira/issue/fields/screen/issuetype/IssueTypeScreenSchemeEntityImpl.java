package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.comparator.IssueTypeKeyComparator;
import com.atlassian.jira.issue.fields.screen.AbstractGVBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * Copyright (c) 2002-2006
 * All rights reserved.
 */
public class IssueTypeScreenSchemeEntityImpl extends AbstractGVBean implements IssueTypeScreenSchemeEntity
{
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final ConstantsManager constantsManager;
    private final IssueTypeKeyComparator comparator;

    private Long id;
    // Do not cache issue types as the ConstantsManager should be responsible for managing the cache of issue types
    private String issueTypeId;
    // Do not cache field screen scheme as field screen scheme manager should be responsible for managing the cache
    // So if this object is cached the field screen scheme will be cached in 2 places
    // So do not cache here but call onto the field screens scheme manager, which can use teh cache if it needs to.
    private Long fieldScreenSchemeId;
    private IssueTypeScreenScheme issueTypeScreenScheme;


    public IssueTypeScreenSchemeEntityImpl(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, GenericValue genericValue, FieldScreenSchemeManager fieldScreenSchemeManager, ConstantsManager constantsManager)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.constantsManager = constantsManager;
        this.comparator =  new IssueTypeKeyComparator(constantsManager);
        setGenericValue(genericValue);
    }

    public IssueTypeScreenSchemeEntityImpl(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity, FieldScreenSchemeManager fieldScreenSchemeManager, ConstantsManager constantsManager)
    {
        this(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
        setIssueTypeId(issueTypeScreenSchemeEntity.getIssueTypeId());
        setFieldScreenScheme(issueTypeScreenSchemeEntity.getFieldScreenScheme());
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public IssueType getIssueTypeObject()
    {
        if (getIssueTypeId() != null)
            return constantsManager.getIssueTypeObject(getIssueTypeId());
        else
            return null;
    }

    public GenericValue getIssueType()
    {
        if (getIssueTypeId() != null)
            return constantsManager.getIssueType(getIssueTypeId());
        else
            return null;
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
        updateGV("issuetype", issueTypeId);
    }

    public FieldScreenScheme getFieldScreenScheme()
    {
        return fieldScreenSchemeManager.getFieldScreenScheme(fieldScreenSchemeId);
    }

    public void setFieldScreenScheme(FieldScreenScheme fieldScreenScheme)
    {
        if (fieldScreenScheme != null)
        {
            this.fieldScreenSchemeId = fieldScreenScheme.getId();
        }
        else
        {
            this.fieldScreenSchemeId = null;
        }

        updateGV("fieldscreenscheme", fieldScreenSchemeId);
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme()
    {
        return issueTypeScreenScheme;
    }

    public void setIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme)
    {
        this.issueTypeScreenScheme = issueTypeScreenScheme;
        if (issueTypeScreenScheme != null)
            updateGV("scheme", issueTypeScreenScheme.getId());
        else
            updateGV("scheme", null);
    }

    public Long getFieldScreenSchemeId()
    {
        return fieldScreenSchemeId;
    }

    public void store()
    {
        if (isModified())
        {
            if (getGenericValue() == null)
            {
                issueTypeScreenSchemeManager.createIssueTypeScreenSchemeEntity(this);
            }
            else
            {
                issueTypeScreenSchemeManager.updateIssueTypeScreenSchemeEntity(this);
                setModified(false);
            }
        }
    }

    public void remove()
    {
        if (getGenericValue() != null)
        {
            issueTypeScreenSchemeManager.removeIssueTypeScreenSchemeEntity(this);
        }
    }

    @SuppressWarnings ("RedundantIfStatement")
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof IssueTypeScreenSchemeEntityImpl)) return false;

        final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = (IssueTypeScreenSchemeEntity) o;

        if (fieldScreenSchemeId != null ? !fieldScreenSchemeId.equals(issueTypeScreenSchemeEntity.getFieldScreenSchemeId()) : issueTypeScreenSchemeEntity.getFieldScreenSchemeId() != null) return false;
        if (id != null ? !id.equals(issueTypeScreenSchemeEntity.getId()) : issueTypeScreenSchemeEntity.getId() != null) return false;
        if (issueTypeId != null ? !issueTypeId.equals(issueTypeScreenSchemeEntity.getIssueTypeId()) : issueTypeScreenSchemeEntity.getIssueTypeId() != null) return false;
        if (issueTypeScreenScheme != null ? !issueTypeScreenScheme.equals(issueTypeScreenSchemeEntity.getIssueTypeScreenScheme()) : issueTypeScreenSchemeEntity.getIssueTypeScreenScheme() != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + (issueTypeId != null ? issueTypeId.hashCode() : 0);
        result = 29 * result + (fieldScreenSchemeId != null ? fieldScreenSchemeId.hashCode() : 0);
        result = 29 * result + (issueTypeScreenScheme != null ? issueTypeScreenScheme.hashCode() : 0);
        return result;
    }

    public int compareTo(IssueTypeScreenSchemeEntity other)
    {
        return comparator.compare(this.getIssueTypeId(), other.getIssueTypeId());
    }
}
