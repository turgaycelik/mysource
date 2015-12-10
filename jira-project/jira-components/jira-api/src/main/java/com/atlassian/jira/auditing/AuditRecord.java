package com.atlassian.jira.auditing;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 * @since v6.2
 */
@ExperimentalApi
public interface AuditRecord
{
    @Nonnull
    Long getId();

    @Nonnull
    Date getCreated();

    @Nonnull
    AuditingCategory getCategory();

    @Nonnull
    String getSummary();

    @Nonnull
    String getEventSource();

    @Nullable
    String getRemoteAddr();

    @Nullable
    String getAuthorKey();

    @Nonnull
    Iterable<AssociatedItem> getAssociatedItems();

    @Nonnull
    Iterable<ChangedValue> getValues();

    @Nullable
    AssociatedItem getObjectItem();
}
