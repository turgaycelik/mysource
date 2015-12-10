package com.atlassian.jira.auditing;

import javax.annotation.Nullable;

/**
 * @since v6.2
 */
public abstract class ParentlessAssociatedItem implements AssociatedItem
{
    @Nullable
    @Override
    final public String getParentName()
    {
        return null;
    }

    @Nullable
    @Override
    final public String getParentId()
    {
        return null;
    }
}
