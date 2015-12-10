package com.atlassian.jira.entity;

import java.util.Map;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyImpl;
import com.atlassian.jira.ofbiz.FieldMap;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.property.EntityProperty.CREATED;
import static com.atlassian.jira.entity.property.EntityProperty.ENTITY_ID;
import static com.atlassian.jira.entity.property.EntityProperty.ENTITY_NAME;
import static com.atlassian.jira.entity.property.EntityProperty.ID;
import static com.atlassian.jira.entity.property.EntityProperty.KEY;
import static com.atlassian.jira.entity.property.EntityProperty.UPDATED;
import static com.atlassian.jira.entity.property.EntityProperty.VALUE;

/**
 * @since v6.1
 */
public class EntityPropertyFactory extends AbstractEntityFactory<EntityProperty>
{
    @Override
    public String getEntityName()
    {
        return "EntityProperty";
    }

    @Override
    public EntityProperty build(GenericValue gv)
    {
        return EntityPropertyImpl.existing(
                gv.getLong(ID),
                gv.getString(ENTITY_NAME),
                gv.getLong(ENTITY_ID),
                gv.getString(KEY),
                gv.getString(VALUE),
                gv.getTimestamp(CREATED),
                gv.getTimestamp(UPDATED) );
    }

    @Override
    public Map<String, Object> fieldMapFrom(EntityProperty entityProperty)
    {
        return new FieldMap()
                .add(ID, entityProperty.getId())
                .add(ENTITY_NAME, entityProperty.getEntityName())
                .add(ENTITY_ID, entityProperty.getEntityId())
                .add(KEY, entityProperty.getKey())
                .add(VALUE, entityProperty.getValue())
                .add(CREATED, entityProperty.getCreated())
                .add(UPDATED, entityProperty.getUpdated());
    }
}
