package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.util.NamedWithId;

/**
 *
 * @since v6.2
 */
public class AffectedScheme extends ParentlessAssociatedItem
{
    @Nonnull
    private final String schemeName;

    @Nonnull
    private final Long schemeId;

    public AffectedScheme(@Nonnull final NamedWithId scheme)
    {
        this(scheme.getName(), scheme.getId());
    }

    public AffectedScheme(@Nonnull final String schemeName, @Nonnull final Long schemeId)
    {
        this.schemeName = schemeName;
        this.schemeId = schemeId;
    }

    @Nonnull
    @Override
    public String getObjectName()
    {
        return schemeName;
    }

    @Nullable
    @Override
    public String getObjectId()
    {
        return Long.toString(schemeId);
    }

    @Nonnull
    @Override
    public Type getObjectType()
    {
        return Type.SCHEME;
    }
}
