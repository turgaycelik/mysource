package com.atlassian.jira.imports.project.core;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.builder.ToStringBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * @since v3.13
 */
public class EntityRepresentationImpl implements EntityRepresentation
{
    private final String entityName;
    private final Map<String, String> entityValues;

    public EntityRepresentationImpl(final String entityName, final Map<String,String> entityValues)
    {
        this.entityName = entityName;
        //entityValues can have null values here (eg ID) so we cannot use immutable collections
        this.entityValues = Collections.unmodifiableMap(entityValues);
    }

    public String getEntityName()
    {
        return entityName;
    }

    public Map<String, String> getEntityValues()
    {
        return entityValues;
    }

    @Override
    public String toString()
    {
        return String.format(ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE));
    }
}
