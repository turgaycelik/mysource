package com.atlassian.jira.auditing;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;

/**
 * Single entry that is going to be saved in the AuditLog
 *
 * @since v6.2
 */
@ExperimentalApi
public class RecordRequest
{
    private final AuditingCategory category;
    private final String categoryName;
    private final String summary;
    private final ApplicationUser author;
    private final String remoteAddress;
    private final String eventSource;

    private AssociatedItem objectItem;

    private ImmutableList<AssociatedItem> associatedItems;
    private ImmutableList<ChangedValue> changedValues;

    public RecordRequest(@Nonnull AuditingCategory category, @Nonnull String summary, @Nonnull String eventSource, @Nullable ApplicationUser author, @Nullable String remoteAddress)
    {
        this.category = category;
        this.categoryName = getI18n().getText(category.getNameI18nKey());
        this.summary = getI18n().getText(summary);
        this.author = author;
        this.remoteAddress = remoteAddress;
        this.eventSource = eventSource;
    }

    public RecordRequest(@Nonnull AuditingCategory category, @Nonnull String summary)
    {
        this(category, summary, StringUtils.EMPTY, null, null);
    }

    public RecordRequest(@Nonnull AuditingCategory category, @Nonnull String summary, @Nonnull String eventSource)
    {
        this(category, summary, eventSource, null, null);
    }

    public RecordRequest forObject(@Nonnull AssociatedItem item)
    {
        objectItem = item;
        return this;
    }

    public RecordRequest forObject(@Nonnull final AssociatedItem.Type type, final @Nonnull String name)
    {
        return forObject(type, name, (String)null);
    }

    public RecordRequest forObject(@Nonnull final AssociatedItem.Type type, final @Nonnull String name, final @Nullable Long id)
    {
        return forObject(type, name, id != null ? Long.toString(id) : null);
    }

    public RecordRequest forObject(@Nonnull final AssociatedItem.Type type, final @Nonnull String name, final @Nullable String id)
    {
        objectItem = new AssociatedItemImpl(name, id, type);
        return this;
    }

    public RecordRequest withAssociatedItem(@Nonnull final AssociatedItem.Type type, final @Nonnull String name, final @Nullable String id)
    {
        associatedItems = ImmutableList.<AssociatedItem>of(new AssociatedItemImpl(name, id, type));
        return this;
    }

    public RecordRequest withAssociatedItems(@Nonnull AssociatedItem... items)
    {
        associatedItems = ImmutableList.copyOf(items);
        return this;
    }

    public RecordRequest withAssociatedItems(@Nonnull Iterable<AssociatedItem> items)
    {
        associatedItems = ImmutableList.copyOf(items);
        return this;
    }

    public RecordRequest withChangedValues(@Nonnull ChangedValue... values)
    {
        changedValues = ImmutableList.copyOf(values);
        return this;
    }

    public RecordRequest withChangedValues(@Nonnull Iterable<ChangedValue> values)
    {
        changedValues = ImmutableList.copyOf(values);
        return this;
    }

    @Nonnull
    public AuditingCategory getCategory()
    {
        return category;
    }

    @Nonnull
    public String getCategoryName() {
        return categoryName;
    }

    @Nonnull
    public String getSummary()
    {
        return summary;
    }

    @Nonnull
    public String getEventSource()
    {
        return eventSource;
    }

    @Nullable
    public ApplicationUser getAuthor()
    {
        return author;
    }

    @Nullable
    public String getRemoteAddress()
    {
        return remoteAddress;
    }

    public ImmutableList<AssociatedItem> getAssociatedItems()
    {
        return associatedItems;
    }

    public ImmutableList<ChangedValue> getChangedValues()
    {
        return changedValues;
    }

    @Nullable
    public AssociatedItem getObjectItem()
    {
        return objectItem;
    }

    protected I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return ComponentAccessor.getI18nHelperFactory().getInstance(Locale.ENGLISH);
    }

    private static class AssociatedItemImpl implements AssociatedItem
    {
        private final String name;
        private final String id;
        private final Type type;

        public AssociatedItemImpl(final String name, final String id, final Type type)
        {
            this.name = name;
            this.id = id;
            this.type = type;
        }

        @Nonnull
        @Override
        public String getObjectName()
        {
            return name;
        }

        @Nullable
        @Override
        public String getObjectId()
        {
            return id;
        }

        @Nullable
        @Override
        public String getParentName()
        {
            return null;
        }

        @Nullable
        @Override
        public String getParentId()
        {
            return null;
        }

        @Nonnull
        @Override
        public Type getObjectType()
        {
            return type;
        }
    }
}
