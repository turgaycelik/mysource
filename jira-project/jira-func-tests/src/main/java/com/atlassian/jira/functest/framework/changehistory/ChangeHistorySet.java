package com.atlassian.jira.functest.framework.changehistory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This represents a easier way to test change history.  A ChangeHistorySet is a set of
 * {@link ChangeHistoryField} objects by a specified user.
 *
 * @since v3.13
 */
public class ChangeHistorySet
{
    final ChangeHistoryList containingChangeHistoryList;
    final String changedBy;
    final Set fieldChanges = new HashSet();

    public ChangeHistorySet(ChangeHistoryList changeHistoryList, String changedBy)
    {
        this.containingChangeHistoryList = changeHistoryList;
        this.changedBy = ChangeHistoryField.canonical(changedBy);
    }

    /**
     * Constructs a new ChangeHistorySet based on an existing one.
     * @param changeHistorySet The existing ChangeHistorySet.
     */
    public ChangeHistorySet(final ChangeHistorySet changeHistorySet)
    {
        this(changeHistorySet.getContainingChangeHistoryList(), changeHistorySet.changedBy);
        fieldChanges.addAll(changeHistorySet.getFieldChanges());
    }

    public ChangeHistoryList getContainingChangeHistoryList()
    {
        return containingChangeHistoryList;
    }

    public String getChangedBy()
    {
        return changedBy;
    }

    public Set getFieldChanges()
    {
        return fieldChanges;
    }

    /**
     * Adds a new {@link ChangeHistoryField} to the change history entry. It is a complete field entry with old and new value
     *
     * @param fieldName the name of the field
     * @param oldValue  the old value
     * @param newValue  the new value
     * @return this
     */
    public ChangeHistorySet add(String fieldName, String oldValue, String newValue)
    {
        ChangeHistoryField field = new ChangeHistoryField(this, fieldName, oldValue, newValue);
        fieldChanges.add(field);
        return this;
    }

    /**
     * A shortcut method to add change history field entries.  This will look back up in the containing
     * {@link ChangeHistoryList}  and get the previous field values for the named field.  It will then use the previous
     * "new value" as the "old value" in this new {@link ChangeHistoryField} .
     *
     * @param fieldName the name of the field
     * @param newValue  the new value
     * @return this
     */
    public ChangeHistorySet add(String fieldName, String newValue)
    {
        ChangeHistoryField lastValue = lastOf(fieldName);
        if (lastValue != null)
        {
            return add(fieldName, lastValue.getNewValue(), newValue);
        }
        else
        {
            return add(fieldName, "", newValue);
        }
    }

    /**
     * Returns the last {@link ChangeHistoryField} with the specified field name.  Does this by navigating back up
     * the enclosing {@link ChangeHistoryList}
     *
     * @param fieldName the name of the field
     * @return null if it cant be found
     */
    public ChangeHistoryField lastOf(String fieldName)
    {
        int len = containingChangeHistoryList.size() - 1;
        for (int i = len; i >= 0; i--)
        {
            ChangeHistorySet set = (ChangeHistorySet) containingChangeHistoryList.get(i);
            for (Iterator iterator = set.iterator(); iterator.hasNext();)
            {
                ChangeHistoryField field = (ChangeHistoryField) iterator.next();
                if (field.getFieldName().equals(fieldName))
                {
                    return field;
                }
            }
        }
        return null;
    }

    public Iterator iterator()
    {
        return fieldChanges.iterator();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ChangeHistorySet that = (ChangeHistorySet) o;

        if (changedBy != null ? !changedBy.equals(that.changedBy) : that.changedBy != null)
        {
            return false;
        }
        if (fieldChanges != null ? !fieldChanges.equals(that.fieldChanges) : that.fieldChanges != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (changedBy != null ? changedBy.hashCode() : 0);
        result = 31 * result + (fieldChanges != null ? fieldChanges.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("\n\tChange By : ").append(changedBy);
        for (final Object o : fieldChanges)
        {
            ChangeHistoryField changeHistoryField = (ChangeHistoryField) o;
            sb.append("\n\t");
            sb.append(changeHistoryField.toString());
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns true if that ChangeHistorySet has the same name and has zero of more fields
     * that occur in this ChangeHistorySet
     * 
     * @param that the other ChangeHistorySet 
     * @return true if we are a super set of that ChangeHistorySet 
     */
    public boolean isSuperSetOf(final ChangeHistorySet that)
    {
        if (this == that)
        {
            return true;
        }
        if (that == null || getClass() != that.getClass())
        {
            return false;
        }

        if (changedBy != null ? !changedBy.equals(that.changedBy) : that.changedBy != null)
        {
            return false;
        }

        final Set thisFields = this.getFieldChanges();
        final Set thatFields = that.getFieldChanges();
        if (thisFields.size() == 0 && thatFields.size() == 0)
        {
            return true;
        }
        // ok fuzzy match the change items in them and see if we contain some of them
        for (final Object o : thatFields)
        {
            ChangeHistoryField thatField = (ChangeHistoryField) o;
            if (! thisFields.contains(thatField))
            {
                return false;
            }
        }
        return true;
    }
}

