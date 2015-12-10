package com.atlassian.jira.index.property;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.entity.EntityConstants;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.IndexDocumentConfigurationFactory;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.DelegatorInterface;

import static com.atlassian.jira.index.IndexDocumentConfigurationFactory.IndexDocumentConfigurationParseException;
import static com.atlassian.jira.mock.Strict.strict;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestOfBizPluginIndexConfigurationManager
{
    private static final String MODULE_KEY_PROPERTY = "moduleKey";
    private static final String MODULE_KEY_1 = "module-key-1";
    private static final String MODULE_KEY_2 = "module-key-2";
    private static final String MODULE_KEY_3 = "module-key-3";
    private static final String ENTITY_KEY_1 = "entity-key-1";
    private static final String ENTITY_KEY_2 = "entity-key-2";
    private static final String PLUGIN_KEY_PROPERTY = "pluginKey";
    private static final String PLUGIN_KEY_1 = "plugin-key-1";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private OfBizPluginIndexConfigurationManager documentManager;
    @Mock
    private IndexDocumentConfigurationFactory indexDocumentConfigurationFactory;
    @Mock
    private ReindexMessageManager reindexMessageManager;
    @Mock
    private JsonEntityPropertyManager jsonEntityPropertyManager;

    static void assertContainsDocument(final Iterable<PluginIndexConfiguration> documents, final String pluginKey,
            final String moduleKey, IndexDocumentConfiguration indexDocumentConfiguration)
    {
        assertThat(documents, Matchers.<PluginIndexConfiguration>hasItem(hasProperty(MODULE_KEY_PROPERTY, is(moduleKey))));
        assertThat(documents, Matchers.<PluginIndexConfiguration>hasItem(hasProperty(PLUGIN_KEY_PROPERTY, is(pluginKey))));
        assertThat(documents, Matchers.<PluginIndexConfiguration>hasItem(hasProperty("indexDocumentConfiguration", hasProperty("entityKey", is(indexDocumentConfiguration.getEntityKey())))));
    }

    @Before
    public void setup()
    {
        final MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator()
        {
            @Override
            public DelegatorInterface getDelegatorInterface()
            {
                return mock(DelegatorInterface.class, strict());
            }
        };
        final EntityEngine entityEngine = new EntityEngineImpl(ofBizDelegator);
        documentManager = new OfBizPluginIndexConfigurationManager(entityEngine, indexDocumentConfigurationFactory, reindexMessageManager, jsonEntityPropertyManager);
    }

    @Test
    public void gettingDocumentForEntityKey()
    {
        final IndexDocumentConfiguration indexDocumentConfiguration1 = mockIndexDocumentConfiguration(ENTITY_KEY_1, true);
        final IndexDocumentConfiguration indexDocumentConfiguration2 = mockIndexDocumentConfiguration(ENTITY_KEY_2, true);
        final IndexDocumentConfiguration indexDocumentConfiguration3 = mockIndexDocumentConfiguration(ENTITY_KEY_1, true);

        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration1);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_2, indexDocumentConfiguration2);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_3, indexDocumentConfiguration3);

        final Iterable<PluginIndexConfiguration> documentsForEntity1 = documentManager.getDocumentsForEntity(ENTITY_KEY_1);

        assertThat(documentsForEntity1, Matchers.<PluginIndexConfiguration>iterableWithSize(2));
        assertContainsDocument(documentsForEntity1, PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration1);
        assertContainsDocument(documentsForEntity1, PLUGIN_KEY_1, MODULE_KEY_3, indexDocumentConfiguration3);

        final Iterable<PluginIndexConfiguration> documentsForEntity2 = documentManager.getDocumentsForEntity(ENTITY_KEY_2);

        assertThat(documentsForEntity2, Matchers.<PluginIndexConfiguration>iterableWithSize(1));
        assertContainsDocument(documentsForEntity2, PLUGIN_KEY_1, MODULE_KEY_2, indexDocumentConfiguration2);
    }

    @Test
    public void shouldStoreAndThenReturnConfigurationWhenNewOneIsAdded()
    {
        //having
        final IndexDocumentConfiguration indexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_1, true);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration);

        //when
        final Iterable<PluginIndexConfiguration> documentsForEntity = documentManager.getDocumentsForEntity(ENTITY_KEY_1);

        //then
        assertThat(documentsForEntity, Matchers.<PluginIndexConfiguration>iterableWithSize(1));
        assertContainsDocument(documentsForEntity, PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration);
        verify(reindexMessageManager).pushMessage(null, "jira.plugin.index.configuration");
    }

    @Test
    public void shouldReplaceDocumentWhenDifferentConfigurationForPluginAndModuleIsAdded()
    {
        //having
        final IndexDocumentConfiguration indexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_1, true);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration);
        final IndexDocumentConfiguration newIndexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_2, true);

        //when
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, newIndexDocumentConfiguration);

        //then
        assertThat(documentManager.getDocumentsForEntity(ENTITY_KEY_1), Matchers.<PluginIndexConfiguration>iterableWithSize(0));
        final Iterable<PluginIndexConfiguration> documentsForEntity = documentManager.getDocumentsForEntity(ENTITY_KEY_2);
        assertThat(documentsForEntity, Matchers.<PluginIndexConfiguration>iterableWithSize(1));
        assertContainsDocument(documentsForEntity, PLUGIN_KEY_1, MODULE_KEY_1, newIndexDocumentConfiguration);
        verify(reindexMessageManager, times(2)).pushMessage(null, "jira.plugin.index.configuration");
    }

    @Test
    public void shouldNotReplaceDocumentWhenTheSameConfigurationForPluginAndModuleIsAddedAndShouldNotPutReindexMessage()
    {
        //having
        final IndexDocumentConfiguration indexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_1, true);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration);
        final IndexDocumentConfiguration newIndexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_1, true);

        //when

        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, newIndexDocumentConfiguration);

        //then
        final Iterable<PluginIndexConfiguration> documentsForEntity = documentManager.getDocumentsForEntity(ENTITY_KEY_1);
        assertThat(documentsForEntity, Matchers.<PluginIndexConfiguration>iterableWithSize(1));
        assertContainsDocument(documentsForEntity, PLUGIN_KEY_1, MODULE_KEY_1, newIndexDocumentConfiguration);
        verify(reindexMessageManager, times(1)).pushMessage(null, "jira.plugin.index.configuration");
    }

    @Test
    public void shouldNotAddReindexMessageWhenThereAreNoAffectedKeysForEntity()
    {
        //having
        final IndexDocumentConfiguration indexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_1, false);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, indexDocumentConfiguration);
        final IndexDocumentConfiguration newIndexDocumentConfiguration = mockIndexDocumentConfiguration(ENTITY_KEY_1, false);

        //when

        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, newIndexDocumentConfiguration);

        //then
        final Iterable<PluginIndexConfiguration> documentsForEntity = documentManager.getDocumentsForEntity(ENTITY_KEY_1);
        assertThat(documentsForEntity, Matchers.<PluginIndexConfiguration>iterableWithSize(1));
        assertContainsDocument(documentsForEntity, PLUGIN_KEY_1, MODULE_KEY_1, newIndexDocumentConfiguration);
        verify(reindexMessageManager, never()).pushMessage(null, "jira.plugin.index.configuration");
    }

    @Test
    public void settingTooLongPluginKey()
    {
        final String pluginKey = StringUtils.repeat('a', EntityConstants.LONG_VARCHAR_MAXIMUM_LENGTH + 1);
        exception.expect(IllegalArgumentException.class);
        documentManager.put(pluginKey, MODULE_KEY_1, mockIndexDocumentConfiguration(ENTITY_KEY_1, true));
        verify(reindexMessageManager, never()).pushMessage(null, "jira.plugin.index.configuration");
    }

    @Test
    public void settingTooLongDocument()
    {
        final String document = StringUtils.repeat('a', EntityConstants.EXTREMELY_LONG_MAXIMUM_LENGTH + 1);
        exception.expect(IllegalArgumentException.class);
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, mockIndexDocumentConfiguration(document, true));
        verify(reindexMessageManager, never()).pushMessage(null, "jira.plugin.index.configuration");
    }

    @Test
    public void removingDocument()
    {
        documentManager.put(PLUGIN_KEY_1, MODULE_KEY_1, mockIndexDocumentConfiguration(ENTITY_KEY_1, true));

        final Iterable<PluginIndexConfiguration> documentsForEntity = documentManager.getDocumentsForEntity(ENTITY_KEY_1);
        assertThat(documentsForEntity, Matchers.<PluginIndexConfiguration>iterableWithSize(1));
        documentManager.remove(PLUGIN_KEY_1);
        assertThat(documentManager.getDocumentsForEntity(ENTITY_KEY_1), Matchers.<PluginIndexConfiguration>emptyIterable());
        verify(reindexMessageManager).pushMessage(null, "jira.plugin.index.configuration");
    }

    private IndexDocumentConfiguration mockIndexDocumentConfiguration(final String entityKey, boolean matching)
    {
        try
        {
            final IndexDocumentConfiguration indexDocumentConfiguration = mock(IndexDocumentConfiguration.class);
            when(indexDocumentConfiguration.getEntityKey()).thenReturn(entityKey);
            final String propKey = "propKey1";
            when(indexDocumentConfiguration.getConfigurationElements()).thenReturn(ImmutableList.of(
                    new IndexDocumentConfiguration.ConfigurationElement(entityKey, propKey, "path1", IndexDocumentConfiguration.Type.TEXT)));
            when(jsonEntityPropertyManager.countByEntityNameAndPropertyKey(entityKey, propKey)).thenReturn(matching ? 1l : 0);
            when(indexDocumentConfigurationFactory.toXML(indexDocumentConfiguration)).thenReturn(entityKey);
            when(indexDocumentConfigurationFactory.fromXML(entityKey)).thenReturn(indexDocumentConfiguration);
            return indexDocumentConfiguration;
        }
        catch (final IndexDocumentConfigurationParseException e)
        {
            throw new RuntimeException(e);
        }
    }
}
