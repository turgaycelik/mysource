package com.atlassian.jira.config.managedconfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestDefaultManagedConfigurationItemStore
{
    @Mock OfBizDelegator ofBizDelegator;

    @Test
    public void testUpdateManagedConfigurationEntityCreate() throws Exception
    {
        ManagedConfigurationItem item = new ManagedConfigurationItemBuilder()
                .setItemId("customfield_123")
                .setItemType(ManagedConfigurationItemType.CUSTOM_FIELD)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setId(null)
                .build();

        // will first search for existing record
        expectFindEntity(item, Collections.<GenericValue>emptyList());

        // will then try to create record
        final Map<String, Object> createParams = DefaultManagedConfigurationItemStore.toGV(item);
        final MockGenericValue createdGv = new MockGenericValue(DefaultManagedConfigurationItemStore.ENTITY_NAME,
                MapBuilder
                        .newBuilder(createParams)
                        .add("id", 123L)
                        .toMap()
        );

        when(ofBizDelegator.createValue(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(createParams)))
                .thenReturn(createdGv);

        final ManagedConfigurationItem result = store().updateManagedConfigurationItem(item);
        assertThat(result, Matchers.notNullValue());
        assertThat(result.getId(), Matchers.equalTo(123L));
    }

    @Test
    public void testUpdateManagedConfigurationEntityUpdate() throws Exception
    {
        // item already exists, we are updating it
        ManagedConfigurationItem updatedItem = new ManagedConfigurationItemBuilder()
                .setItemId("customfield_123")
                .setItemType(ManagedConfigurationItemType.CUSTOM_FIELD)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED) // this is a new value, different to existing record
                .setId(123L)
                .build();

        // will first search for existing record
        final MockGenericValue existingGv = new MockGenericValue(DefaultManagedConfigurationItemStore.ENTITY_NAME,
                MapBuilder
                        .newBuilder(DefaultManagedConfigurationItemStore.toGV(updatedItem))
                        .add("accessLevel", ConfigurationItemAccessLevel.ADMIN.name()) // this is the old value
                        .toMap()
        );
        expectFindEntity(updatedItem, ImmutableList.<GenericValue>of(existingGv));

        // will then try to update record
        final Map<String, Object> updateParams = DefaultManagedConfigurationItemStore.toGV(updatedItem);
        final GenericValue newGv = new MockGenericValue(DefaultManagedConfigurationItemStore.ENTITY_NAME, updateParams);

        when(ofBizDelegator.makeValue(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(updateParams)))
                .thenReturn(newGv);
        doNothing().when(ofBizDelegator).store(eq(newGv));
        ofBizDelegator.store(newGv);

        final ManagedConfigurationItem result = store().updateManagedConfigurationItem(updatedItem);
        assertThat(result, Matchers.equalTo(updatedItem));
    }

    @Test(expected = DataAccessException.class)
    public void testUpdateManagedConfigurationEntityCreateGenericEntityException() throws Exception
    {
        ManagedConfigurationItem item = new ManagedConfigurationItemBuilder()
                .setItemId("customfield_123")
                .setItemType(ManagedConfigurationItemType.CUSTOM_FIELD)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setId(null)
                .build();

        // will first search for existing record
        expectFindEntity(item, Collections.<GenericValue>emptyList());

        // will then try to create record, and fail
        final Map<String, Object> createParams = DefaultManagedConfigurationItemStore.toGV(item);

        when(ofBizDelegator.createValue(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(createParams)))
                .thenThrow(DataAccessException.class);

        store().updateManagedConfigurationItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateManagedConfigurationEntityIAE()
    {
        store().updateManagedConfigurationItem(null);
    }

    @Test
    public void testGetManagedConfigurationEntitiesNone()
    {
        // if there are no entries of a type, the result will be empty
        for (ManagedConfigurationItemType type : ManagedConfigurationItemType.values())
        {
            expectFindEntities(type, Collections.<GenericValue>emptyList());

            Collection<ManagedConfigurationItem> result = store().getManagedConfigurationItems(type);
            assertThat(result, Matchers.<Collection>equalTo(Collections.emptyList()));
        }
    }

    @Test
    public void testGetManagedConfigurationEntitiesSome()
    {
        ManagedConfigurationItemType type = ManagedConfigurationItemType.CUSTOM_FIELD;

        final List<GenericValue> gvs = CollectionBuilder.<GenericValue>list(
                new MockGenericValue(DefaultManagedConfigurationItemStore.ENTITY_NAME, MapBuilder.build(
                        "id", 555L,
                        "itemId", "id_1",
                        "itemType", type.name(),
                        "accessLevel", ConfigurationItemAccessLevel.LOCKED.name()
                )),
                new MockGenericValue(DefaultManagedConfigurationItemStore.ENTITY_NAME, MapBuilder.build(
                        "id", 666L,
                        "itemId", "id_2",
                        "itemType", type.name(),
                        "accessLevel", ConfigurationItemAccessLevel.SYS_ADMIN.name()
                ))
        );

        expectFindEntities(type, gvs);

        Collection<ManagedConfigurationItem> result = store().getManagedConfigurationItems(type);
        assertThat(result, Matchers.<Collection>equalTo(ImmutableList.of(
                new ManagedConfigurationItemBuilder()
                        .setId(555L)
                        .setItemId("id_1")
                        .setItemType(type)
                        .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                        .build(),
                new ManagedConfigurationItemBuilder()
                        .setId(666L)
                        .setItemId("id_2")
                        .setItemType(type)
                        .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.SYS_ADMIN)
                        .build()
        )));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetManagedConfigurationEntitiesIAE()
    {
        store().getManagedConfigurationItems(null);
    }

    @Test
    public void testGetManagedConfigurationEntity()
    {
        // looking up specific entities

        final ManagedConfigurationItemType type = ManagedConfigurationItemType.CUSTOM_FIELD;
        final String goodItemId = "customfield_123";
        final String badItemId = "xxx";

        final List<GenericValue> gvs = CollectionBuilder.<GenericValue>list(
                new MockGenericValue(DefaultManagedConfigurationItemStore.ENTITY_NAME, MapBuilder.build(
                        "id", 555L,
                        "itemId", goodItemId,
                        "itemType", type.name(),
                        "accessLevel", ConfigurationItemAccessLevel.LOCKED.name()
                ))
        );

        expectFindEntity(goodItemId, type, gvs);
        expectFindEntity(badItemId, type, Collections.<GenericValue>emptyList());

        ManagedConfigurationItem result = store().getManagedConfigurationItem(goodItemId, type);
        assertThat(result, Matchers.equalTo(
                new ManagedConfigurationItemBuilder()
                        .setId(555L)
                        .setItemId(goodItemId)
                        .setItemType(type)
                        .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                        .build()
        ));

        result = store().getManagedConfigurationItem(badItemId, type);
        assertThat(result, Matchers.nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetManagedConfigurationEntityIAEName()
    {
        store().getManagedConfigurationItem(null, ManagedConfigurationItemType.CUSTOM_FIELD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetManagedConfigurationEntityIAEType()
    {
        store().getManagedConfigurationItem("xxx", null);
    }

    @Test
    public void testDeleteManagedConfigurationEntity()
    {
        final ManagedConfigurationItem goodItem = new ManagedConfigurationItemBuilder()
                .setId(555L)
                .setItemId("customfield_123")
                .setItemType(ManagedConfigurationItemType.CUSTOM_FIELD)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .build();
        final ManagedConfigurationItem badItem = new ManagedConfigurationItemBuilder()
                .setItemType(ManagedConfigurationItemType.CUSTOM_FIELD)
                .build();

        final Map<String,String> goodParams = MapBuilder.build(
                "itemId", goodItem.getItemId(),
                "itemType", goodItem.getItemType().name()
        );
        final Map<String,String> badParams = MapBuilder.build(
                "itemId", badItem.getItemId(),
                "itemType", badItem.getItemType().name()
        );

        when(ofBizDelegator.removeByAnd(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(goodParams)))
                .thenReturn(1);
        when(ofBizDelegator.removeByAnd(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(badParams)))
                .thenReturn(0);

        boolean result = store().deleteManagedConfigurationItem(goodItem);
        assertThat(result, Matchers.equalTo(true));

        result = store().deleteManagedConfigurationItem(badItem);
        assertThat(result, Matchers.equalTo(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteManagedConfigurationEntityIAE()
    {
        store().deleteManagedConfigurationItem(null);
    }

    private void expectFindEntity(ManagedConfigurationItem item, List<GenericValue> answer)
    {
        expectFindEntity(item.getItemId(), item.getItemType(), answer);
    }

    private void expectFindEntity(String itemId, ManagedConfigurationItemType type, List<GenericValue> answer)
    {
        Map<String, ?> params = MapBuilder.build(
                "itemId", itemId,
                "itemType", type.name()
        );

        when(ofBizDelegator.findByAnd(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(params)))
                .thenReturn(answer);
    }

    private void expectFindEntities(ManagedConfigurationItemType type, List<GenericValue> answer)
    {
        Map<String, ?> params = MapBuilder.build(
                "itemType", type.name()
        );

        when(ofBizDelegator.findByAnd(eq(DefaultManagedConfigurationItemStore.ENTITY_NAME), eq(params)))
                .thenReturn(answer);
    }

    private DefaultManagedConfigurationItemStore store()
    {
        return new DefaultManagedConfigurationItemStore(ofBizDelegator);
    }
}
