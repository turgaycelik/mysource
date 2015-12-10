package com.atlassian.jira.index.property;

import java.sql.Timestamp;
import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.base.Objects;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.DOCUMENT;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.ENTITY_KEY;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.ID;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.MODULE_KEY;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.PLUGIN_KEY;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.UPDATED;


/**
 *
 * @since v6.2
 */
public class EntityPropertyIndexDocumentFactory extends AbstractEntityFactory<EntityPropertyIndexDocument>
{
    @Override
    public Map<String, Object> fieldMapFrom(final EntityPropertyIndexDocument indexDocument)
    {
        return new FieldMap()
                .add(ID, indexDocument.getId())
                .add(PLUGIN_KEY, indexDocument.getPluginKey())
                .add(MODULE_KEY, indexDocument.getModuleKey())
                .add(ENTITY_KEY, indexDocument.getEntityKey())
                .add(UPDATED, indexDocument.getUpdated())
                .add(DOCUMENT, indexDocument.getDocument());
    }

    @Override
    public String getEntityName()
    {
        return Entity.Name.ENTITY_PROPERTY_INDEX_DOCUMENT;
    }

    @Override
    public EntityPropertyIndexDocument build(final GenericValue gv)
    {
        return new EntityPropertyIndexDocumentImpl(gv.getLong(EntityProperty.ID),
                gv.getString(PLUGIN_KEY), gv.getString(MODULE_KEY), gv.getString(ENTITY_KEY), gv.getString(DOCUMENT), gv.getTimestamp(UPDATED));
    }

    static class EntityPropertyIndexDocumentImpl implements EntityPropertyIndexDocument
    {
        private final Long id;
        private final String pluginKey;
        private final String moduleKey;
        private final String entityKey;
        private final String document;
        private final Timestamp timestamp;

        public EntityPropertyIndexDocumentImpl(final Long id, final String pluginKey, final String moduleKey, final String entityKey, final String document, final Timestamp timestamp)
        {
            this.id = id;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.entityKey = entityKey;
            this.document = document;
            this.timestamp = timestamp;
        }

        @Override
        public Long getId()
        {
            return id;
        }

        @Override
        public String getPluginKey()
        {
            return pluginKey;
        }

        @Override
        public String getModuleKey()
        {
            return moduleKey;
        }

        @Override
        public String getDocument()
        {
            return document;
        }

        @Override
        public Timestamp getUpdated()
        {
            return timestamp;
        }

        @Override
        public String getEntityKey()
        {
            return entityKey;
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(EntityPropertyIndexDocument.class)
                    .add("pluginKey", pluginKey)
                    .add("moduleKey", moduleKey)
                    .add("entityKey", entityKey)
                    .add("document", document)
                    .add("timestamp", timestamp)
                    .toString();
        }
    }
}
