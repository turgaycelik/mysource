package com.atlassian.jira.auditing;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.GenericValue;

/**
 *
 * @since v6.2
 */
public class AuditRecordImpl implements AuditRecord
{
    public static final String ID = "id";
    public static final String AUTHOR_KEY = "authorKey";
    public static final String REMOTE_ADDR = "remoteAddress";
    public static final String CATEGORY = "category";
    public static final String CREATED = "created";
    public static final String SUMMARY = "summary";
    public static final String OBJECT_TYPE = "objectType";
    public static final String OBJECT_NAME = "objectName";
    public static final String OBJECT_PARENT_ID = "objectParentId";
    public static final String OBJECT_PARENT_NAME = "objectParentName";
    public static final String OBJECT_ID = "objectId";
    public static final String AUTHOR_TYPE = "authorType";
    public static final String EVENT_SOURCE = "eventSourceName";
    public static final String SEARCH_FIELD = "searchField";

    protected final GenericValue gv;
    protected final ImmutableList<AssociatedItem> associatedItems;
    protected final ImmutableList<ChangedValue> changedValues;

    public AuditRecordImpl(final GenericValue gv, final Iterable<AssociatedItem> associatedItems, final Iterable<ChangedValue> changedValues)
    {
        this.gv = gv;
        this.associatedItems = ImmutableList.copyOf(associatedItems);
        this.changedValues = ImmutableList.copyOf(changedValues);
    }

    @Override
    @Nonnull
    public Long getId() {
        return gv.getLong(ID);
    }

    @Override
    @Nullable
    public String getAuthorKey()
    {
        return gv.getString(AUTHOR_KEY);
    }

    @Override
    @Nullable
    public String getRemoteAddr()
    {
        return gv.getString(REMOTE_ADDR);
    }

    @Override
    @Nonnull
    public Date getCreated()
    {
        return gv.getTimestamp(CREATED);
    }

    @Override
    @Nonnull
    public AuditingCategory getCategory()
    {
        return AuditingCategory.getCategoryById(gv.getString(CATEGORY));
    }

    @Override
    @Nonnull
    public String getSummary()
    {
        return gv.getString(SUMMARY);
    }

    @Override
    @Nonnull
    public String getEventSource()
    {
        return gv.getString(EVENT_SOURCE);
    }

    @Override
    @Nonnull
    public Iterable<AssociatedItem> getAssociatedItems()
    {
        return associatedItems;
    }

    @Override
    @Nonnull
    public Iterable<ChangedValue> getValues()
    {
        return changedValues;
    }

    @Override
    @Nullable
    public AssociatedItem getObjectItem()
    {
        if (gv.getString(OBJECT_NAME) != null && gv.getString(OBJECT_TYPE) != null)
        {
            return new AssociatedItemImpl(gv);
        }
        return null;
    }
}
