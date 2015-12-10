package com.atlassian.jira.issue.link;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

public class IssueLinkTypeImpl extends AbstractOfBizValueWrapper implements IssueLinkType
{
    /**
     * Creates an IssueLinkType from the given GenericValue.
     * @param genericValue the GenericValue
     */
    public IssueLinkTypeImpl(GenericValue genericValue)
    {
        super(genericValue);

        if (!OfBizDelegator.ISSUE_LINK_TYPE.equals(genericValue.getEntityName()))
        {
            throw new IllegalArgumentException("Entity must be an 'IssueLinkType', not '" + genericValue.getEntityName() + "'.");
        }
    }

    @Override
    public Long getId()
    {
        return getGenericValue().getLong("id");
    }

    @Override
    public String getName()
    {
        return getGenericValue().getString(NAME_FIELD_NAME);
    }

    @Override
    public String getOutward()
    {
        return getGenericValue().getString(OUTWARD_FIELD_NAME);
    }

    @Override
    public String getInward()
    {
        return getGenericValue().getString(INWARD_FIELD_NAME);
    }

    @Override
    public String getStyle()
    {
        return getGenericValue().getString(STYLE_FIELD_NAME);
    }

    /**
     * Compare on name (in alphabetical order)
     *
     * @param other the other IssueLinkType to compare to
     */
    @Override
    public int compareTo(IssueLinkType other)
    {
        if (other == null)
        {
            // If the object we are comparing to is null, this object should appear after it
            return 1;
        }

        String otherName = other.getName();

        if (getName() == null && otherName == null)
        {
            return 0;
        }
        else if (getName() != null && otherName != null)
        {
            return getName().compareTo(otherName);
        }
        else if (otherName == null)
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public boolean isSubTaskLinkType()
    {
        return SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE.equals(getStyle());
    }

    /**
     * Checks if this link type is a System Link type. System link types are used by JIRA to denote a special
     * relationship between issues. For example, a sub-task is linked ot its parent issue using a link that
     * is of a system link type.
     */
    @Override
    public boolean isSystemLinkType()
    {
        return (getStyle() != null && getStyle().startsWith("jira_"));
    }
}
