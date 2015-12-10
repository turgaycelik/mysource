package com.atlassian.jira.imports.project.mapper;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @since v3.13
 */
public class SimpleProjectImportIdMapperImpl extends AbstractMapper implements SimpleProjectImportIdMapper
{
    public void flagValueAsRequired(final String oldId)
    {
        // parent will ignore nulls
        super.flagValueAsRequired(oldId);
    }

    public void registerOldValue(final String oldId, final String oldKey)
    {
        super.registerOldValue(oldId, oldKey);
    }

    ///CLOVER:OFF - this is only here for testing purposes
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
    ///CLOVER:ON
}
