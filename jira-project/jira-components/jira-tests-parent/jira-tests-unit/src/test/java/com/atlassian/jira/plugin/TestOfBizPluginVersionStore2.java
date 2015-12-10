package com.atlassian.jira.plugin;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.plugin.OfBizPluginVersionStore.PLUGIN_VERSION_CREATED;
import static com.atlassian.jira.plugin.OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME;
import static com.atlassian.jira.plugin.OfBizPluginVersionStore.PLUGIN_VERSION_ID;
import static com.atlassian.jira.plugin.OfBizPluginVersionStore.PLUGIN_VERSION_KEY;
import static com.atlassian.jira.plugin.OfBizPluginVersionStore.PLUGIN_VERSION_NAME;
import static com.atlassian.jira.plugin.OfBizPluginVersionStore.PLUGIN_VERSION_VERSION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link OfBizPluginVersionStore}.
 */
public class TestOfBizPluginVersionStore2
{
    // Constants
    private static final long PLUGIN_VERSION_ROW_ID = 321;
    private static final Date CREATION_DATE = new Date(12345);
    private static final String PLUGIN_KEY = "somePluginKey";
    private static final String PLUGIN_NAME = "somePluginName";
    private static final String PLUGIN_VERSION = "somePluginVersion";

    // Fixture
    private PluginVersionStore store;
    @Mock private GenericValue mockPersistedValue;
    @Mock private OfBizDelegator mockOfBizDelegator;
    @Mock private PluginVersion mockPluginVersion;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        when(mockPersistedValue.getTimestamp(PLUGIN_VERSION_CREATED)).thenReturn(new Timestamp(CREATION_DATE.getTime()));
        when(mockPersistedValue.getLong(PLUGIN_VERSION_ID)).thenReturn(PLUGIN_VERSION_ROW_ID);
        when(mockPersistedValue.getString(PLUGIN_VERSION_KEY)).thenReturn(PLUGIN_KEY);
        when(mockPersistedValue.getString(PLUGIN_VERSION_VERSION)).thenReturn(PLUGIN_VERSION);

        when(mockPluginVersion.getCreated()).thenReturn(CREATION_DATE);
        when(mockPluginVersion.getKey()).thenReturn(PLUGIN_KEY);
        when(mockPluginVersion.getName()).thenReturn(PLUGIN_NAME);
        when(mockPluginVersion.getVersion()).thenReturn(PLUGIN_VERSION);

        this.store = new OfBizPluginVersionStore(mockOfBizDelegator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteByKeyShouldNotAcceptNullKey()
    {
        // Invoke
        store.deleteByKey(null);
    }

    @Test
    public void deletingByKeyShouldCorrectlyInvokeOfBizDelegator()
    {
        // Invoke
        store.deleteByKey(PLUGIN_KEY);

        // Check
        final Map expectedCriteria = EasyMap.build(PLUGIN_VERSION_KEY, PLUGIN_KEY);
        verify(mockOfBizDelegator).removeByAnd(PLUGIN_VERSION_ENTITY_NAME, expectedCriteria);
    }

    @Test
    public void savingShouldInsertWhenNoRowExists()
    {
        // Set up
        when(mockOfBizDelegator.findByField(PLUGIN_VERSION_ENTITY_NAME, PLUGIN_VERSION_KEY, PLUGIN_KEY)).thenReturn(Collections.<GenericValue>emptyList());
        when(mockOfBizDelegator.createValue(eq(PLUGIN_VERSION_ENTITY_NAME), any(Map.class))).thenReturn(mockPersistedValue);

        // Invoke
        final long rowId = store.save(mockPluginVersion);

        // Check
        final ArgumentCaptor<Map> rowCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockOfBizDelegator).createValue(eq(PLUGIN_VERSION_ENTITY_NAME), rowCaptor.capture());
        final Map<String, Object> insertedRow = rowCaptor.getValue();
        assertEquals(CREATION_DATE, insertedRow.get(PLUGIN_VERSION_CREATED));
        assertEquals(PLUGIN_KEY, insertedRow.get(PLUGIN_VERSION_KEY));
        assertEquals(PLUGIN_NAME, insertedRow.get(PLUGIN_VERSION_NAME));
        assertEquals(PLUGIN_VERSION, insertedRow.get(PLUGIN_VERSION_VERSION));
        assertEquals(PLUGIN_VERSION_ROW_ID, rowId);
    }

    @Test
    public void savingShouldUpdateWhenRowExists() throws GenericEntityException
    {
        // Set up
        when(mockPersistedValue.getLong(PLUGIN_VERSION_ID)).thenReturn(PLUGIN_VERSION_ROW_ID);
        when(mockOfBizDelegator.findByField(PLUGIN_VERSION_ENTITY_NAME, PLUGIN_VERSION_KEY, PLUGIN_KEY))
                .thenReturn(Collections.singletonList(mockPersistedValue));
        when(mockOfBizDelegator.findById(PLUGIN_VERSION_ENTITY_NAME, PLUGIN_VERSION_ROW_ID)).thenReturn(mockPersistedValue);

        // Invoke
        final long rowId = store.save(mockPluginVersion);

        // Check
        verify(mockPersistedValue).store();
        assertEquals(PLUGIN_VERSION_ROW_ID, rowId);
    }
}
