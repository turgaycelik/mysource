package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public final class StringEntityBuilder implements EntityBuilder<String>
{
    private final String fieldName;

    public StringEntityBuilder(String fieldName)
    {
        this.fieldName = fieldName;
    }

    @Override
    public String build(GenericValue genericValue)
    {
        return genericValue.getString(fieldName);
    }
}
