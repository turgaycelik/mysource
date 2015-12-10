package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.2
 */
public class ChangedValueImpl implements ChangedValue
{
    private final String name;
    private final String from;
    private final String to;

    public ChangedValueImpl(final String name, final String from, final String to)
    {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nullable
    @Override
    public String getFrom()
    {
        return from;
    }

    @Nullable
    @Override
    public String getTo()
    {
        return to;
    }
}
