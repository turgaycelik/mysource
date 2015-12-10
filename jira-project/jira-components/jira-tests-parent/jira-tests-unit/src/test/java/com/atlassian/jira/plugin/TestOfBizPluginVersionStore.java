package com.atlassian.jira.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableMap;

/**
 * Tests the OfBizPluginVersionStore.
 * 
 * @since v3.13
 */
public class TestOfBizPluginVersionStore
{

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final String TEST_KEY = "test.key";
    private static final String TEST_NAME = "Test Name";
    private static final String TEST_VERSION = "0.1.1";

    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @Test
    public void testCreate() throws GenericEntityException
    {
        List<GenericValue> all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertEquals(Collections.emptyList(), all);

        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        final Date created = new Date();
        final PluginVersion newPluginVersion = new PluginVersionImpl(TEST_KEY, TEST_NAME, TEST_VERSION, created);
        final PluginVersion pluginVersion = ofBizPluginVersionStore.create(newPluginVersion);

        all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertEquals(Collections.singletonList(toPluginVersionGV(pluginVersion)), all);
    }

    @Test
    public void testCreateNullPluginVersion()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not create a plugin version record from a null PluginVersion.");
        ofBizPluginVersionStore.create(null);
    }

    @Test
    public void testUpdate()
    {
        final List<GenericValue> all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertTrue(all.isEmpty());
        GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                        OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION,
                        TEST_VERSION));

        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        final PluginVersion pluginVersion = new PluginVersionImpl(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID),
                "new.key", "New Name", "0.2.2", new Date());
        final PluginVersion updatedPluginVersion = ofBizPluginVersionStore.update(pluginVersion);

        pluginVersionGV = ofBizDelegator.findByPrimaryKey(
                OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_ID,
                        pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID)));
        assertEquals(toPluginVersionGV(updatedPluginVersion), pluginVersionGV);
    }

    @Test
    public void testUpdateNullId()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        final PluginVersion pluginVersion = new PluginVersionImpl(TEST_KEY, TEST_NAME, TEST_VERSION, new Date());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You can not update a plugin version with a null id.");
        ofBizPluginVersionStore.update(pluginVersion);
    }

    @Test
    public void testUpdateNullPluginVersion()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You can not update a plugin version with a null id.");
        ofBizPluginVersionStore.update(null);
    }

    @Test
    public void testUpdateNoRecordForId()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        final PluginVersion pluginVersion = new PluginVersionImpl(new Long(54231), TEST_KEY, TEST_NAME, TEST_VERSION, new Date());
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unable to find plugin version record with id '54231'");
        ofBizPluginVersionStore.update(pluginVersion);
    }

    @Test
    public void testDelete()
    {
        final GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                        OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION,
                        TEST_VERSION));
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        assertTrue(ofBizPluginVersionStore.delete(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID)));

        final List<GenericValue> all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertEquals(Collections.emptyList(), all);
    }

    @Test
    public void testDeleteNoId()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unable to delete a plugin version with a null id.");
        ofBizPluginVersionStore.delete(null);
    }

    @Test
    public void testDeleteNoRecordToDelete()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        assertFalse(ofBizPluginVersionStore.delete(new Long(10000)));

        final List<GenericValue> all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertEquals(Collections.emptyList(), all);
    }

    @Test
    public void testGetById()
    {
        final long time = System.currentTimeMillis();
        final GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                        OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION,
                        TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, new Timestamp(time)));
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        final PluginVersion pluginVersion = ofBizPluginVersionStore.getById(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID));
        assertEquals(toPluginVersionGV(pluginVersion), pluginVersionGV);
    }

    @Test
    public void testGetByIdNoRecord()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        assertNull(ofBizPluginVersionStore.getById(new Long(123)));
    }

    @Test
    public void testGetByIdNullId()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        assertNull(ofBizPluginVersionStore.getById(null));
    }

    @Test
    public void testGetAll()
    {
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        // Test where there are none
        assertEquals(Collections.emptyList(), ofBizPluginVersionStore.getAll());

        // Add 2
        final long time = System.currentTimeMillis();
        final GenericValue firstPluginVersion = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                        OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION,
                        TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, new Timestamp(time)));

        final GenericValue secondPluginVersion = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                        OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION,
                        TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, new Timestamp(System.currentTimeMillis())));

        final List<PluginVersion> all = ofBizPluginVersionStore.getAll();
        assertEquals(Arrays.asList(toPluginVersionGV(firstPluginVersion), toPluginVersionGV(secondPluginVersion)), all);
    }

    @Test
    public void testConvertToParams()
    {
        final Long id = new Long(54231);
        final Date created = new Date();
        final PluginVersion pluginVersion = new PluginVersionImpl(id, TEST_KEY, TEST_NAME, TEST_VERSION, created);
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        final Map<String, Object> map = ofBizPluginVersionStore.convertToParams(pluginVersion);

        assertEquals(new ImmutableMap.Builder<String, Object>().put("id", id)
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY)
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME)
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION)
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, created)
                .build(), map);

        final PluginVersion pluginVersionNoId = new PluginVersionImpl(TEST_KEY, TEST_NAME, TEST_VERSION, created);
        final Map<String, Object> mapNoId = ofBizPluginVersionStore.convertToParams(pluginVersionNoId);

        assertEquals(
                new ImmutableMap.Builder<String, Object>().put(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY)
                        .put(OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME)
                        .put(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION)
                        .put(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, created)
                        .build(), mapNoId);
    }

    @Test
    public void testConvertFromGV()
    {
        final Timestamp time = new Timestamp(System.currentTimeMillis());
        final GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME,
                MapBuilder.<String, Object> build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                        OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION,
                        TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, time));
        final OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        final PluginVersion pluginVersion = ofBizPluginVersionStore.convertFromGV(pluginVersionGV);
        assertEquals(toPluginVersionGV(pluginVersionGV), pluginVersion);
    }

    private GenericValue toPluginVersionGV(final PluginVersion pluginVersion)
    {
        return new MockGenericValue("PluginVersion", ImmutableMap.builder()
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_ID, pluginVersion.getId())
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, pluginVersion.getKey())
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_NAME, pluginVersion.getName())
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, pluginVersion.getVersion())
                .put(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, pluginVersion.getCreated())
                .build());
    }

    private PluginVersion toPluginVersionGV(final GenericValue pluginVersionGV)
    {
        final Map<String, Object> fields = pluginVersionGV.getAllFields();

        final Long id = (Long) fields.get(OfBizPluginVersionStore.PLUGIN_VERSION_ID);
        final String key = (String) fields.get(OfBizPluginVersionStore.PLUGIN_VERSION_KEY);
        final String name = (String) fields.get(OfBizPluginVersionStore.PLUGIN_VERSION_NAME);
        final String version = (String) fields.get(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION);
        final Date created = (Date) fields.get(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED);

        return new PluginVersionImpl(id, key, name, version, created);
    }

}
