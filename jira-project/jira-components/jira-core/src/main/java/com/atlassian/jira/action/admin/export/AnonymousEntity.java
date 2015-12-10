package com.atlassian.jira.action.admin.export;

/**
 * An AnonymousEntity represents which fields to be anonymised, and is used by the {@link AnonymisingEntityXmlWriter}
 * and the {@link AnonymousGenericValue}.
 * <p/>
 * It contains an 'entityName', representing the GenericValue's entity name (eg: 'Issue'), and a fieldName, representing
 * a GenericValue's fieldName (eg: 'description', or 'id').
 * <p/>
 * If either of these are null, it represents a match against all of the fields / entities.
 * <p/>
 * NOTE: Due to the way that AnonymousEntity handles 'equals', it is impossible to determine a good hashCode.  There
 * will therefore be performance issues if you use this class as the key in a HashMap or HashSet.
 */
public final class AnonymousEntity
{
    private final String entityName;
    private final String fieldName;

    /**
     *
     * @param entityName
     * @param fieldName
     */
    public AnonymousEntity(String entityName, String fieldName)
    {
        this.entityName = entityName;
        this.fieldName = fieldName;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AnonymousEntity that = (AnonymousEntity) o;

        if (entityName != null && that.entityName != null && !entityName.equals(that.entityName))
        {
            return false;
        }
        if (fieldName != null && that.fieldName != null && !fieldName.equals(that.fieldName))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return 1; // there is no way to detemine a good hashcode
    }
}
