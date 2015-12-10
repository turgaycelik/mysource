/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;


/**
 * This objected is used to hold the values of an entity which is part of a scheme. This could be a permission, notification etc.
 */
public class SchemeEntity implements Comparable
{
    private Long id;
    private String type;
    private String parameter;
    private Object entityTypeId;
    // Currently only used for Notification Scheme Entities
    private Object templateId;
    private Long schemeId;

    /**
     * Create a new SchemeEntity object
     * @param type The entity type. This could be "group", "reporter" etc
     * @param entityTypeId The id of the entity. For Permission, Notification and Issue Security schemes this should be a long.
     */
    public SchemeEntity(String type, Object entityTypeId)
    {
        if (type == null)
            throw new IllegalArgumentException("Type passed can NOT be null");
        if (entityTypeId == null)
            throw new IllegalArgumentException("EntityTypeId passed can NOT be null");
        this.type = type;
        this.entityTypeId = entityTypeId;
    }

    /**
     * Create a new SchemeEntity object
     * @param type The entity type. This could be "group", "reporter" etc
     * @param parameter The parameter value of the entity. If the type is group this value will be the group name
     * @param entityTypeId The id of the entity. For Permission, Notification and Issue Security schemes this should be a long.
     */
    public SchemeEntity(String type, String parameter, Object entityTypeId)
    {
        this(type, entityTypeId);
        this.parameter = parameter;
    }

    public SchemeEntity(String type, String parameter, Object entityTypeId, Object templateId)
    {
        this(type, parameter, entityTypeId);
        this.templateId = templateId;
    }

    /**
     * Create a new SchemeEntity object
     * <p>
     * Used when constructing from a GenericValue object - with the ability to specify the GV id in the scheme object and the
     * template ID associated with this entity. Only used in notifications at present.
     *
     * @param id            the id of this entity - obtained from the GenericValue object
     * @param type          the entity type. This could be "group", "reporter" etc
     * @param parameter     the parameter value of the entity. If the type is group this value will be the group name
     * @param entityTypeId  the id of the entity. For Permission, Notification and Issue Security schemes this should be a long.
     * @param templateId    the id of the template associated with this entity. Only used for Notification entities at present
     * @param schemeId      the id of the scheme that this entity is a part of.
     */
    public SchemeEntity(Long id, String type, String parameter, Object entityTypeId, Object templateId, Long schemeId)
    {
        this(type, parameter, entityTypeId);
        this.templateId = templateId;
        this.id = id;
        this.schemeId = schemeId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getParameter()
    {
        return parameter;
    }

    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    public Object getEntityTypeId()
    {
        return entityTypeId;
    }

    public void setEntityTypeId(Object entityTypeId)
    {
        this.entityTypeId = entityTypeId;
    }

    public Object getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Object templateId)
    {
        this.templateId = templateId;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public void setSchemeId(Long schemeId)
    {
        this.schemeId = schemeId;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof SchemeEntity))
            return false;

        final SchemeEntity schemeEntity = (SchemeEntity) o;

        // NOTE: This equals method needs to handle null values in the way that it is currently working, treating
        // two null values as equal. The SchemeDistiller relies on this quite heavilly.
        if (entityTypeId != null ? !entityTypeId.equals(schemeEntity.entityTypeId) : schemeEntity.entityTypeId != null)
            return false;
        if (parameter != null ? !parameter.equals(schemeEntity.parameter) : schemeEntity.parameter != null)
            return false;
        if (type != null ? !type.equals(schemeEntity.type) : schemeEntity.type != null)
            return false;
        if (templateId != null ? !templateId.equals(schemeEntity.templateId) : schemeEntity.templateId != null)
            return false;
        if (id != null ? !id.equals(schemeEntity.getId()) : schemeEntity.getId() != null)
            return false;
        if (schemeId != null ? !schemeId.equals(schemeEntity.getSchemeId()) : schemeEntity.getSchemeId() != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (type != null ? type.hashCode() : 0);
        result = 29 * result + (parameter != null ? parameter.hashCode() : 0);
        result = 29 * result + (entityTypeId != null ? entityTypeId.hashCode() : 0);
        result = 29 * result + (templateId != null ? templateId.hashCode() : 0);
        result = 29 * result + (id != null ? id.hashCode() : 0);
        result = 29 * result + (schemeId != null ? schemeId.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return ("Type = " + type + " Parameter = " + parameter + " EntityTypeId = " + entityTypeId + " TemplateId = " + templateId + " Id = " + id + " SchemeId = " + schemeId);
    }

    public int compareTo(Object o)
    {
        if (this == o)
            return 0;
        if (!(o instanceof SchemeEntity))
            return 1;

        final SchemeEntity schemeEntity = (SchemeEntity) o;

        // We just want to sort on the natural order of the parameter
        if (parameter != null && schemeEntity.parameter != null)
            return parameter.compareTo(schemeEntity.parameter);
        else
            return 0;
    }
}
