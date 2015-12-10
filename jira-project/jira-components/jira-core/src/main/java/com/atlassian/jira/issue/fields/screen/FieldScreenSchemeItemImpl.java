package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
public class FieldScreenSchemeItemImpl extends AbstractGVBean implements FieldScreenSchemeItem
{
    private final FieldScreenSchemeManager fieldScreenSchemeManager;

    private Long id;
    private ScreenableIssueOperation issueOperation;
    // Do not cache field screen here, as field screens caching should be cached by field screen manager
    // If this object is cached, and it also caches field screens, a field screen could be cached in 2 places
    // So do not cache it here and call onto the field screen manager to retrieve the screen - the field screen manager could use a cache if it needs to.
    private Long fieldScreenId;
    private FieldScreenScheme fieldScreenScheme;
    private FieldScreenManager fieldScreenManager;

    public FieldScreenSchemeItemImpl(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        this(fieldScreenSchemeManager, (GenericValue) null, fieldScreenManager);
    }

    public FieldScreenSchemeItemImpl(FieldScreenSchemeManager fieldScreenSchemeManager, GenericValue genericValue, FieldScreenManager fieldScreenManager)
    {
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.fieldScreenManager = fieldScreenManager;
        setGenericValue(genericValue);
    }

    public FieldScreenSchemeItemImpl(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenSchemeItem fieldScreenSchemeItem, FieldScreenManager fieldScreenManager)
    {
        this(fieldScreenSchemeManager, (GenericValue) null, fieldScreenManager);
        setIssueOperation(fieldScreenSchemeItem.getIssueOperation());
        setFieldScreen(fieldScreenSchemeItem.getFieldScreen());
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            id = getGenericValue().getLong("id");
        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public ScreenableIssueOperation getIssueOperation()
    {
        return issueOperation;
    }

    public void setIssueOperation(ScreenableIssueOperation issueOperation)
    {
        this.issueOperation = issueOperation;
        if (issueOperation != null)
        {
            updateGV("operation", issueOperation.getId());
        }
        else
        {
            updateGV("operation", null);
        }
    }

    public FieldScreen getFieldScreen()
    {
        return fieldScreenManager.getFieldScreen(fieldScreenId);
    }

    public String getIssueOperationName()
    {
        if (getIssueOperation() != null)
        {
            return getIssueOperation().getNameKey();
        }
        else
        {
            return "admin.common.words.default";
        }
    }

    public void setFieldScreen(FieldScreen fieldScreen)
    {
        if (fieldScreen != null)
        {
            this.fieldScreenId = fieldScreen.getId();
            updateGV("fieldscreen", fieldScreenId);
        }
        else
        {
            this.fieldScreenId = null;
            updateGV("fieldscreen", null);
        }
    }

    public FieldScreenScheme getFieldScreenScheme()
    {
        return fieldScreenScheme;
    }

    public void setFieldScreenScheme(FieldScreenScheme fieldScreenScheme)
    {
        this.fieldScreenScheme = fieldScreenScheme;
        if (fieldScreenScheme != null)
        {
            updateGV("fieldscreenscheme", fieldScreenScheme.getId());
        }
        else
        {
            updateGV("fieldscreenscheme", null);
        }
    }

    public Long getFieldScreenId()
    {
        return fieldScreenId;
    }

    public void store()
    {
        if (isModified())
        {
            if (id == null)
            {
                fieldScreenSchemeManager.createFieldScreenSchemeItem(this);
            }
            else
            {
                fieldScreenSchemeManager.updateFieldScreenSchemeItem(this);
                setModified(false);
            }
        }
    }

    public void remove()
    {
        if (id != null)
        {
            fieldScreenSchemeManager.removeFieldScreenSchemeItem(this);
        }
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FieldScreenSchemeItem))
        {
            return false;
        }

        final FieldScreenSchemeItem fieldScreenSchemeItem = (FieldScreenSchemeItemImpl) o;

        if (getFieldScreenId() != null ? !getFieldScreenId().equals(fieldScreenSchemeItem.getFieldScreenId()) : fieldScreenSchemeItem.getFieldScreenId() != null)
        {
            return false;
        }
        if (fieldScreenScheme != null ? !fieldScreenScheme.equals(fieldScreenSchemeItem.getFieldScreenScheme()) : fieldScreenSchemeItem.getFieldScreenScheme() != null)
        {
            return false;
        }
        if (id != null ? !id.equals(fieldScreenSchemeItem.getId()) : fieldScreenSchemeItem.getId() != null)
        {
            return false;
        }
        if (issueOperation != null ? !issueOperation.equals(fieldScreenSchemeItem.getIssueOperation()) : fieldScreenSchemeItem.getIssueOperation() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (issueOperation != null ? issueOperation.hashCode() : 0);
        result = 29 * result + (getFieldScreenId() != null ? getFieldScreenId().hashCode() : 0);
        result = 29 * result + (fieldScreenScheme != null ? fieldScreenScheme.hashCode() : 0);
        return result;
    }

    public int compareTo(final FieldScreenSchemeItem o)
    {
        ScreenableIssueOperation issueOperation1 = getIssueOperation();
        ScreenableIssueOperation issueOperation2 = o.getIssueOperation();

        if (issueOperation1 == null)
        {
            if (issueOperation2 == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if (issueOperation2 == null)
            {
                return 1;
            }
            else
            {
                return issueOperation1.getId().compareTo(issueOperation2.getId());
            }
        }
    }
}
