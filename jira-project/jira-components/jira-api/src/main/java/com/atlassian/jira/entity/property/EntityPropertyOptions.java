package com.atlassian.jira.entity.property;

import com.atlassian.annotations.Internal;

/**
 * This is a holder object for Entity Property Options.
 * Currently it is used to optionally skip permission checks when performing operations on entity properties.
 *
 * @since v6.3
 */
public class EntityPropertyOptions
{
    private final boolean skipPermissionChecks;

    @Internal
    private EntityPropertyOptions(boolean skipPermissionChecks)
    {
        this.skipPermissionChecks = skipPermissionChecks;
    }

    /**
     * Default options for entity property operations
     *
     * @return default EntityPropertyOptions
     */
    public static EntityPropertyOptions defaults()
    {
        return new EntityPropertyOptions(false);
    }

    public boolean skipPermissionChecks()
    {
        return skipPermissionChecks;
    }

    public static class Builder
    {
        private boolean skipPermissionChecks;

        public Builder skipPermissionChecks()
        {
            this.skipPermissionChecks = true;
            return this;
        }

        public EntityPropertyOptions build()
        {
            return new EntityPropertyOptions(skipPermissionChecks);
        }
    }
}
