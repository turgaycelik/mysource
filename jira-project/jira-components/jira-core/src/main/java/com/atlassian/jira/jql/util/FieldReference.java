package com.atlassian.jira.jql.util;

import java.util.List;

import com.atlassian.query.clause.Property;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;

public class FieldReference
{
    private final String name;
    private final Property property;

    public FieldReference(final List<String> names, final List<String> keys, final List<String> objectReferences)
    {
        Preconditions.checkArgument(names.size() > 0);
        this.name = join(checkNotNull(names));
        this.property = new Property(keys, objectReferences);
    }

    public String getName()
    {
        return name;
    }

    public Property getProperty()
    {
        return property;
    }

    public boolean isEntityProperty()
    {
        return property.getKeys().size() > 0;
    }

    private String join(List<String> strings)
    {
        return Joiner.on('.').join(strings);
    }
}
