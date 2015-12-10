package com.atlassian.jira.action.admin.export;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * A genericValue that returns 'x' characters for all the fields listed in {@link #anonymousEntities}.  It 'wraps' a
 * normal GenericValue, and returns the normal values for all non-mapped entity/fieldname values.
 */
class AnonymousGenericValue extends GenericValue
{
    private static final char REPLACEMENT_CHAR = 'x';

    /**
     * A collection of {@link AnonymousEntity} objects
     */
    private final Collection anonymousEntities;

    /**
     * @param entity The target GenericValue to wrap
     * @param anonymousEntities A collection of {@link AnonymousEntity} objects specifying which fields to anonymise
     */
    public AnonymousGenericValue(GenericValue entity, Collection anonymousEntities)
    {
        super(entity);
        this.anonymousEntities = anonymousEntities;
    }

    public String getString(String fieldName)
    {
        String entityName = getEntityName();
        String fieldValue = super.getString(fieldName);
        if (fieldValue == null)
        {
            return null;
        }
        if (shouldAnonymiseEntity(entityName, fieldName))
        {
            StringBuilder sb = new StringBuilder();
            char[] chars = fieldValue.toCharArray();
            for (char c : chars)
            {
                if (Character.isLetterOrDigit(c))
                {
                    sb.append(REPLACEMENT_CHAR);
                }
                else
                {
                    sb.append(c);
                }

            }
            return sb.toString();
        }
        else
        {
            return fieldValue;
        }
    }

    private boolean shouldAnonymiseEntity(String entityName, String fieldName)
    {
        return anonymousEntities.contains(new AnonymousEntity(entityName, fieldName));
    }
}
