package com.atlassian.jira.scheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.util.NamedWithDescription;
import com.atlassian.jira.util.NamedWithId;

/**
 * This object is used to represent a scheme in JIRA (this could be a permission, notification, etc, scheme). This
 * object holds all the {@link SchemeEntity}'s which are a part of this scheme.
 */
public class Scheme implements NamedWithDescription, NamedWithId
{
    private Long id;
    private String type;
    private String name;
    private String description;
    private ArrayList<SchemeEntity> entities;

    public Scheme()
    {
        this(null, null, null, null, Collections.<SchemeEntity>emptyList());
    }

    public Scheme(String type, String name)
    {
        this(null, type, name, null, Collections.<SchemeEntity>emptyList());
    }

    public Scheme(Long id, String type, String name, Collection<SchemeEntity> entities)
    {
        this(id, type, name, null, entities);
    }

    public Scheme(Long id, String type, String name, String description, Collection<SchemeEntity> entities)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.entities = new ArrayList<SchemeEntity>(entities);
    }

    public String getType()
    {
        return type;
    }

    /**
     *
     * @param type
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    /**
     *
     * @param name
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public Collection<SchemeEntity> getEntities()
    {
        if (entities == null)
        {
            return Collections.emptyList();
        }
        return entities;
    }

    /**
     *
     * @param entities
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void setEntities(Collection<SchemeEntity> entities)
    {
        this.entities = new ArrayList<SchemeEntity>(entities);
    }

    /**
     *
     * @param entity
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void addEntity(SchemeEntity entity)
    {
        this.entities.add(entity);
    }

    /**
     *
     * @param entity
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void removeEntity(SchemeEntity entity)
    {
        this.entities.remove(entity);
    }

    public boolean containsSameEntities(Scheme other)
    {
        // NOTE: this assumes that we are not comparing the id field of the entities
        return getEntities().size() == other.getEntities().size() && getEntities().containsAll(other.getEntities());
    }

    public Scheme cloneScheme()
    {
        return new Scheme(null, type, "Clone of " + name, new ArrayList<SchemeEntity>(entities));
    }

    public Long getId()
    {
        return id;
    }

    /**
     *
     * @param id
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     *
     * @param description
     * @deprecated this class will be made immutable, create a new Scheme instead.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<SchemeEntity> getEntitiesByType(Object typeId)
    {
        List<SchemeEntity> matchedEntities = new ArrayList<SchemeEntity>();
        if (entities != null)
        {
            for (final SchemeEntity schemeEntity : entities)
            {
                if (schemeEntity.getEntityTypeId().equals(typeId))
                {
                    matchedEntities.add(schemeEntity);
                }
            }
        }
        return matchedEntities;
    }
}
