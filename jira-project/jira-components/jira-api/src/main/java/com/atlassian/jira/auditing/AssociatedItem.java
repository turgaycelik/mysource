package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 *
 * @since v6.2
 */
@ExperimentalApi
public interface AssociatedItem
{
    public enum Type {
        USER,
        PROJECT,
        GROUP,
        SCHEME,
        REMOTE_DIRECTORY,
        WORKFLOW,
        PERMISSIONS,
        VERSION,
        CUSTOM_FIELD,
        PROJECT_CATEGORY,
        PROJECT_COMPONENT,
        PROJECT_ROLE,
        LICENSE
    }

    @Nonnull
    String getObjectName();

    @Nullable
    String getObjectId();

    @Nullable
    String getParentName();

    @Nullable
    String getParentId();

    @Nonnull
    Type getObjectType();
}
