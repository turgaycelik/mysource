package com.atlassian.jira.auditing;

import com.atlassian.annotations.ExperimentalApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Object representing singular change in AuditLog
 *
 * @since v6.2
 */
@ExperimentalApi
public interface ChangedValue
{
    @Nonnull
    String getName();

    @Nullable
    String getFrom();

    @Nullable
    String getTo();
}
