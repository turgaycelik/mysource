package com.atlassian.jira.imports.project.parser;

import java.sql.Timestamp;
import java.util.Map;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalEntityProperty;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;

import com.google.common.collect.ImmutableMap;

/**
 * @since v6.2
 */
public class EntityPropertyParserImpl implements EntityPropertyParser
{

    private Function<String, Long> toLong()
    {
        return new Function<String, Long>()
        {
            @Override
            public Long get(final String input)
            {
                return Long.valueOf(input);
            }
        };
    }

    private Function<String, Timestamp> toTimestamp()
    {
        return new Function<String, Timestamp>()
        {
            @Override
            public Timestamp get(final String input)
            {
                return Timestamp.valueOf(input);
            }
        };
    }

    @Override
    public ExternalEntityProperty parse(final Map<String, String> attributes) throws ParseException
    {
        final Long id = verifyRequiredAttribute(attributes, EntityProperty.ID, toLong());
        final Long entityId = verifyRequiredAttribute(attributes, EntityProperty.ENTITY_ID, toLong());
        final String key = verifyRequiredAttribute(attributes, EntityProperty.KEY, Functions.<String>identity());
        final Timestamp updated = verifyRequiredAttribute(attributes, EntityProperty.UPDATED, toTimestamp());
        final Timestamp created = verifyRequiredAttribute(attributes, EntityProperty.CREATED, toTimestamp());
        final String value = verifyRequiredAttribute(attributes, EntityProperty.VALUE, Functions.<String>identity());
        final String entityName = verifyRequiredAttribute(attributes, EntityProperty.ENTITY_NAME, Functions.<String>identity());

        return new ExternalEntityProperty(id, entityName, entityId, key, value, created, updated);
    }

    @Override
    public EntityRepresentation getEntityRepresentation(final ExternalEntityProperty entityProperty, final Long newEntityId)
    {
        final ImmutableMap.Builder<String, String> attrBuilder = ImmutableMap.builder();
        attrBuilder.put(EntityProperty.ID, entityProperty.getId().toString());
        attrBuilder.put(EntityProperty.ENTITY_NAME, entityProperty.getEntityName());
        attrBuilder.put(EntityProperty.ENTITY_ID, newEntityId.toString());
        attrBuilder.put(EntityProperty.KEY, entityProperty.getKey());
        attrBuilder.put(EntityProperty.VALUE, entityProperty.getValue());
        attrBuilder.put(EntityProperty.CREATED, entityProperty.getCreated().toString());
        attrBuilder.put(EntityProperty.UPDATED, entityProperty.getUpdated().toString());

        return new EntityRepresentationImpl(ENTITY_PROPERTY_ENTITY_NAME, attrBuilder.build());
    }

    private <T> T verifyRequiredAttribute(final Map<String, String> attributes, final String key, Function<String, T> transform)
            throws ParseException
    {
        String id = attributes.get(EntityProperty.ID);
        id = id == null ? "" : " '" + id + "'";
        if (!attributes.containsKey(key))
        {
            throw new ParseException("The " + key + " of " + EntityProperty.ENTITY_NAME + id + " is missing ");
        }
        try
        {
            return transform.get(attributes.get(key));
        }
        catch (NumberFormatException e)
        {
            throw new ParseException("The " + key + " of " + EntityProperty.ENTITY_NAME + id + " has invalid value " + attributes.get(key));
        }
        catch (IllegalArgumentException e)
        {
            throw new ParseException("The " + key + " of " + EntityProperty.ENTITY_NAME + id + " has invalid value " + attributes.get(key));
        }
    }
}
