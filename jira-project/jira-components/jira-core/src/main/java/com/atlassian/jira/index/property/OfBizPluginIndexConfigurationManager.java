package com.atlassian.jira.index.property;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.IndexDocumentConfigurationFactory;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import static com.atlassian.jira.entity.Entity.ENTITY_PROPERTY_INDEX_DOCUMENT;
import static com.atlassian.jira.entity.EntityConstants.EXTREMELY_LONG_MAXIMUM_LENGTH;
import static com.atlassian.jira.entity.EntityConstants.LONG_VARCHAR_MAXIMUM_LENGTH;
import static com.atlassian.jira.index.IndexDocumentConfigurationFactory.IndexDocumentConfigurationParseException;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.DOCUMENT;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.ENTITY_KEY;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.ID;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.MODULE_KEY;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.PLUGIN_KEY;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocument.UPDATED;
import static com.atlassian.jira.index.property.EntityPropertyIndexDocumentFactory.EntityPropertyIndexDocumentImpl;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v6.2
 */
public class OfBizPluginIndexConfigurationManager implements PluginIndexConfigurationManager
{
    private static final Logger log = Logger.getLogger(OfBizPluginIndexConfigurationManager.class);

    private final EntityEngine entityEngine;
    private final IndexDocumentConfigurationFactory indexDocumentConfigurationFactory;
    private final ReindexMessageManager reindexMessageManager;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;

    public OfBizPluginIndexConfigurationManager(final EntityEngine entityEngine, final IndexDocumentConfigurationFactory indexDocumentConfigurationFactory, final ReindexMessageManager reindexMessageManager, final JsonEntityPropertyManager jsonEntityPropertyManager)
    {
        this.entityEngine = entityEngine;
        this.indexDocumentConfigurationFactory = indexDocumentConfigurationFactory;
        this.reindexMessageManager = reindexMessageManager;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
    }

    static void restrictLength(final String field, final String value, final int maximumLength)
    {
        if (value.length() > maximumLength)
        {
            throw new IllegalArgumentException("Value of " + field + " is too long. Maximum length is " + maximumLength);
        }
    }

    @Override
    public Iterable<PluginIndexConfiguration> getDocumentsForEntity(@Nonnull final String entityName)
    {
        final List<EntityPropertyIndexDocument> indexDocuments = Select.from(ENTITY_PROPERTY_INDEX_DOCUMENT)
                .whereEqual(ENTITY_KEY, entityName)
                .runWith(entityEngine)
                .asList();

        return Iterables.filter(Iterables.transform(indexDocuments, new Function<EntityPropertyIndexDocument, PluginIndexConfiguration>()
        {
            @Override
            public PluginIndexConfiguration apply(final EntityPropertyIndexDocument indexDocument)
            {
                try
                {
                    return new PluginIndexConfigurationImpl(indexDocument.getPluginKey(),
                            indexDocument.getModuleKey(),
                            indexDocumentConfigurationFactory.fromXML(indexDocument.getDocument()),
                            indexDocument.getUpdated());
                }
                catch (final IndexDocumentConfigurationParseException e)
                {
                    log.error("The configuration of index document for entity " + entityName + " is invalid id=" + indexDocument.getId(), e);
                    return null;
                }
            }
        }), Predicates.<PluginIndexConfiguration>notNull());
    }

    @Override
    public void put(@Nonnull final String pluginKey, @Nonnull final String moduleKey, @Nonnull final IndexDocumentConfiguration document)

    {
        Assertions.notNull("pluginKey", pluginKey);
        restrictLength("pluginKey", pluginKey, LONG_VARCHAR_MAXIMUM_LENGTH);
        Assertions.notNull("moduleKey", moduleKey);
        restrictLength("moduleKey", moduleKey, LONG_VARCHAR_MAXIMUM_LENGTH);
        Assertions.notNull("document", document);
        final String documentValue = indexDocumentConfigurationFactory.toXML(document);
        Assertions.notBlank("documentValue", documentValue);
        restrictLength("documentValue", documentValue, EXTREMELY_LONG_MAXIMUM_LENGTH);
        final String entityKey = document.getEntityKey();
        Assertions.notNull("entityKey", entityKey);
        restrictLength("entityKey", entityKey, LONG_VARCHAR_MAXIMUM_LENGTH);

        final Option<EntityPropertyIndexDocument> entityPropertyIndexDocument = getDocumentForPluginAndModule(pluginKey, moduleKey);

        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        entityPropertyIndexDocument.fold(new Supplier<Void>()
        {
            @Override
            public Void get()
            {
                final EntityPropertyIndexDocument indexDocument = new EntityPropertyIndexDocumentImpl(null, pluginKey, moduleKey,
                        entityKey, documentValue, timestamp);
                doDbOperation(pluginKey, moduleKey, document, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        entityEngine.createValue(ENTITY_PROPERTY_INDEX_DOCUMENT, indexDocument);
                        checkReindexRequired(document);
                    }
                });

                return null;
            }
        }, new Function<EntityPropertyIndexDocument, Void>()
        {
            @Override
            public Void apply(final EntityPropertyIndexDocument existing)
            {
                if (!configurationsEquals(document, existing.getDocument()))
                {
                    doDbOperation(pluginKey, moduleKey, document, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Update.into(ENTITY_PROPERTY_INDEX_DOCUMENT)
                                    .set(DOCUMENT, documentValue)
                                    .set(UPDATED, timestamp)
                                    .set(ENTITY_KEY, entityKey)
                                    .whereEqual(ID, existing.getId())
                                    .execute(entityEngine);
                            checkReindexRequired(document);
                        }
                    });
                }
                return null;
            }
        });
    }

    private void doDbOperation(final String pluginKey, final String moduleKey, final IndexDocumentConfiguration document, final Runnable command)
    {
        try
        {
            command.run();
        }
        catch (final DataAccessException e)
        {

            log.debug("Cannot add IndexDocumentConfiguration for "
                    + "pluginKey= " + pluginKey + " moduleKey = " + moduleKey + " document = " + document.toString(), e);
            //this may happen if two clustered noted are starting at the same time
            //lets fall back to db consistency checks and just ignore this fact
        }
    }

    private void checkReindexRequired(final IndexDocumentConfiguration document)
    {
        final Iterable<IndexDocumentConfiguration.ConfigurationElement> configurationElements = document.getConfigurationElements();
        if (Iterables.any(configurationElements,
                new Predicate<IndexDocumentConfiguration.ConfigurationElement>()
                {
                    @Override
                    public boolean apply(final IndexDocumentConfiguration.ConfigurationElement input)
                    {
                        return jsonEntityPropertyManager.countByEntityNameAndPropertyKey(input.getEntityKey(), input.getPropertyKey()) > 0;
                    }
                }
        ))
        {
            reindexMessageManager.pushMessage(null, "jira.plugin.index.configuration");
        }
    }

    private boolean configurationsEquals(final IndexDocumentConfiguration newConfiguration, final String existingConfigurationStr)
    {
        try
        {
            final IndexDocumentConfiguration existingConfiguration = indexDocumentConfigurationFactory.fromXML(existingConfigurationStr);
            return existingConfiguration.equals(newConfiguration);
        }
        catch (final IndexDocumentConfigurationParseException e)
        {
            return false;
        }
    }

    @Override
    public void remove(@Nonnull final String pluginKey)
    {
        Delete.from(ENTITY_PROPERTY_INDEX_DOCUMENT)
                .whereEqual(PLUGIN_KEY, pluginKey)
                .execute(entityEngine);
    }

    private Option<EntityPropertyIndexDocument> getDocumentForPluginAndModule(final String pluginKey, final String moduleKey)
    {
        final EntityPropertyIndexDocument entityPropertyIndexDocument = Select.from(ENTITY_PROPERTY_INDEX_DOCUMENT)
                .whereEqual(PLUGIN_KEY, pluginKey)
                .whereEqual(MODULE_KEY, moduleKey)
                .runWith(entityEngine)
                .singleValue();
        return Option.option(entityPropertyIndexDocument);
    }

    public static class PluginIndexConfigurationImpl implements PluginIndexConfiguration
    {
        private final String pluginKey;
        private final String moduleKey;
        private final IndexDocumentConfiguration indexDocumentConfiguration;
        private final Timestamp lastUpdated;

        @VisibleForTesting
        public PluginIndexConfigurationImpl(final String pluginKey, final String moduleKey, final IndexDocumentConfiguration indexDocumentConfiguration, final Timestamp lastUpdated)
        {
            this.pluginKey = checkNotNull(pluginKey);
            this.moduleKey = checkNotNull(moduleKey);
            this.indexDocumentConfiguration = checkNotNull(indexDocumentConfiguration);
            this.lastUpdated = checkNotNull(lastUpdated);
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
        public IndexDocumentConfiguration getIndexDocumentConfiguration()
        {
            return indexDocumentConfiguration;
        }

        @Override
        public Timestamp getLastUpdated()
        {
            return lastUpdated;
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this)
                    .add("pluginKey", pluginKey)
                    .add("moduleKey", moduleKey)
                    .add("indexDocumentConfiguration", indexDocumentConfiguration)
                    .add("lastUpdated", lastUpdated)
                    .toString();
        }
    }
}
