package com.atlassian.jira.propertyset;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettings;
import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.propertyset.CachingOfBizPropertyEntryStore.CacheKey;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore.PropertyEntry;
import com.atlassian.jira.util.map.CacheObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.mock.ofbiz.matchers.EntityFieldMapMatcher.entityFieldMap;
import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_ID;
import static com.atlassian.jira.propertyset.PropertySetEntity.ENTITY_NAME;
import static com.atlassian.jira.propertyset.PropertySetEntity.ID;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_ENTRY;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_KEY;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_NUMBER;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_STRING;
import static com.atlassian.jira.propertyset.PropertySetEntity.PROPERTY_TEXT;
import static com.atlassian.jira.propertyset.PropertySetEntity.SELECT_ID_AND_TYPE;
import static com.atlassian.jira.propertyset.PropertySetEntity.SELECT_ID_KEY_AND_TYPE;
import static com.atlassian.jira.propertyset.PropertySetEntity.SELECT_KEY;
import static com.atlassian.jira.propertyset.PropertySetEntity.TYPE;
import static com.atlassian.jira.propertyset.PropertySetEntity.VALUE;
import static com.opensymphony.module.propertyset.PropertySet.LONG;
import static com.opensymphony.module.propertyset.PropertySet.STRING;
import static com.opensymphony.module.propertyset.PropertySet.TEXT;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
@SuppressWarnings("ClassWithTooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class TestCachingOfBizPropertyEntryStore
{
    private static final List<String> NULL_LIST = null;
    private static final List<GenericValue> NO_GENERIC_VALUES = Collections.emptyList();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(10000L);

    private static final String TEST_ENTITY_1 = "TestEntity1";

    private static final Long TEST_ID_1 = 1L;

    private static final String TEST_KEY_1 = "TestKey1";
    private static final String TEST_KEY_2 = "TestKey2";

    private static final String VALUE1 = "Hello";

    private DelegatorInterface genericDelegator;
    @Mock private Cache<CacheKey,CacheObject<PropertyEntry>> cache;
    @Captor private ArgumentCaptor<CacheLoader<CacheKey,CacheObject<PropertyEntry>>> captor;

    private CachingOfBizPropertyEntryStore fixture;
    private CacheLoader<CacheKey,CacheObject<PropertyEntry>> loader;

    @Before
    public void setUp()
    {
        setUp(false);
    }

    // If having trouble figuring out what's wrong, explicitly calling setUp(true) to make the mock strict may help...
    private void setUp(boolean strict)
    {
        genericDelegator = strict ? mock(DelegatorInterface.class, new Strict()) : mock(DelegatorInterface.class);
        doAnswer(new Answer<Long>()
        {
            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable
            {
                return ID_GENERATOR.getAndIncrement();
            }
        }).when(genericDelegator).getNextSeqId(PROPERTY_ENTRY);

        final CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(anyString(), captor.capture(), any(CacheSettings.class))).thenReturn(cache);
        fixture = new CachingOfBizPropertyEntryStore(genericDelegator, cacheManager);
        loader = captor.getValue();
        assertNotNull("Did not capture a CacheLoader?!", loader);
    }

    @After
    public void tearDown()
    {
        genericDelegator = null;
        cache = null;
        captor = null;
        fixture = null;
        loader = null;
    }



    @Test
    public void testGetKeys() throws GenericEntityException
    {
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        final GenericValue entry2 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_2, STRING);

        setUp(true);
        mockKeysQuery(TEST_ENTITY_1, TEST_ID_1, entry1, entry2);

        assertThat(fixture.getKeys(TEST_ENTITY_1, TEST_ID_1), containsInAnyOrder(TEST_KEY_1, TEST_KEY_2));

        verifyKeysQuery(TEST_ENTITY_1, TEST_ID_1);
        verifyZeroInteractions(cache);
    }

    @Test
    public void testGetKeysOfType() throws GenericEntityException
    {
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        final GenericValue entry2 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_2, STRING);

        // Type filtering is assumed to have been done by the query itself, so entry2 will make it through here
        setUp(true);
        mockKeysQuery(TEST_ENTITY_1, TEST_ID_1, TEXT, entry1, entry2);

        assertThat(fixture.getKeys(TEST_ENTITY_1, TEST_ID_1, TEXT), containsInAnyOrder(TEST_KEY_1, TEST_KEY_2));

        verifyKeysQuery(TEST_ENTITY_1, TEST_ID_1, TEXT);
        verifyZeroInteractions(cache);
    }


    @Test
    public void testRemoveNonExistingKey() throws GenericEntityException
    {
        mockQuery(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1);

        fixture.removeEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1);

        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(cache);
    }

    @Test
    public void testRemoveExistingKey() throws GenericEntityException
    {
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, STRING);

        mockQuery(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, entry1);

        fixture.removeEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1);

        verifyQuery(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1);
        verifyEntryRemoved(entry1.getLong(ID), PROPERTY_STRING);
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(genericDelegator, cache);
    }


    @Test
    public void testRemoveExistingKeyWithDuplicateEntry() throws GenericEntityException
    {
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, STRING);
        final GenericValue entry2 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);

        mockQuery(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, entry1, entry2);

        fixture.removeEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1);

        verifyQuery(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1);
        verifyEntryRemoved(entry1.getLong(ID), PROPERTY_STRING);
        verifyEntryRemoved(entry2.getLong(ID), PROPERTY_TEXT);
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(genericDelegator, cache);
    }

    @Test
    public void testRemovePropertySet() throws GenericEntityException
    {
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, STRING);
        final GenericValue entry2 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        final GenericValue entry3 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_2, LONG);

        doReturn(asList(entry1, entry2, entry3)).when(genericDelegator).findByCondition(
                eq(PROPERTY_ENTRY),
                argThat(entityFieldMap(FieldMap.build(
                        ENTITY_NAME, TEST_ENTITY_1,
                        ENTITY_ID, TEST_ID_1))),
                eq(SELECT_ID_KEY_AND_TYPE),
                eq(NULL_LIST));

        fixture.removePropertySet(TEST_ENTITY_1, TEST_ID_1);

        verify(genericDelegator).findByCondition(
                eq(PROPERTY_ENTRY),
                argThat(entityFieldMap(FieldMap.build(
                        ENTITY_NAME, TEST_ENTITY_1,
                        ENTITY_ID, TEST_ID_1))),
                eq(SELECT_ID_KEY_AND_TYPE),
                eq(NULL_LIST));
        verifyEntryRemoved(entry1.getLong(ID), PROPERTY_STRING);
        verifyEntryRemoved(entry2.getLong(ID), PROPERTY_TEXT);
        verifyEntryRemoved(entry3.getLong(ID), PROPERTY_NUMBER);

        // Detecting duplicates for a key would not be worthwhile, so...
        verify(cache, times(2)).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_2));
        verifyNoMoreInteractions(genericDelegator, cache);
    }



    @Test
    public void testCreateNewProperty() throws GenericEntityException
    {
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1.set(ID, 1L);
        final GenericValue value1 = mockValue(PROPERTY_TEXT, 1L, VALUE1);
        final FieldMap fieldMap = FieldMap.build(
                ENTITY_NAME, TEST_ENTITY_1,
                ENTITY_ID, TEST_ID_1,
                PROPERTY_KEY, TEST_KEY_1);

        doReturn(NO_GENERIC_VALUES).when(genericDelegator).findByAnd(PROPERTY_ENTRY, fieldMap);

        doReturn(1L).when(genericDelegator).getNextSeqId(PROPERTY_ENTRY);
        doReturn(entry1).when(genericDelegator).makeValue(PROPERTY_ENTRY, new FieldMap(entry1));
        doReturn(value1).when(genericDelegator).makeValue(PROPERTY_TEXT, new FieldMap(value1));
        doReturn(2).when(genericDelegator).storeAll(asList(entry1, value1));

        fixture.setEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT, VALUE1);

        verify(genericDelegator).storeAll(asList(entry1, value1));
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(cache);
    }

    @Test
    public void testReplaceExistingPropertyWithSameType() throws GenericEntityException
    {
        setUp(true);

        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1.set(ID, 1L);
        final GenericValue value1 = mockValue(PROPERTY_TEXT, 1L, VALUE1);
        final FieldMap fieldMap = FieldMap.build(
                ENTITY_NAME, TEST_ENTITY_1,
                ENTITY_ID, TEST_ID_1,
                PROPERTY_KEY, TEST_KEY_1);

        doReturn(asList(entry1)).when(genericDelegator).findByAnd(PROPERTY_ENTRY, fieldMap);
        doReturn(value1).when(genericDelegator).makeValue(PROPERTY_TEXT, new FieldMap(value1));
        doReturn(2).when(genericDelegator).storeAll(asList(entry1, value1));

        fixture.setEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT, VALUE1);

        verify(genericDelegator).storeAll(asList(entry1, value1));
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(cache);
    }



    @Test
    public void testReplaceExistingPropertyWithSameTypeAndDuplicates() throws GenericEntityException
    {
        setUp(true);

        final GenericValue entry1orig = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1orig.set(ID, 3L);
        final GenericValue entry1old = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, STRING);
        entry1old.set(ID, 1L);
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1.set(ID, 3L);
        final GenericValue value1 = mockValue(PROPERTY_TEXT, 3L, VALUE1);
        final FieldMap fieldMap = FieldMap.build(
                ENTITY_NAME, TEST_ENTITY_1,
                ENTITY_ID, TEST_ID_1,
                PROPERTY_KEY, TEST_KEY_1);

        // Make sure it's a copy so changes to it don't pollute our original
        doReturn(asList(new MockGenericValue(entry1orig), new MockGenericValue(entry1old))).when(genericDelegator).findByAnd(PROPERTY_ENTRY, fieldMap);

        doReturn(0).when(genericDelegator).removeByAnd(PROPERTY_STRING, FieldMap.build(ID, 1L));
        doReturn(1).when(genericDelegator).removeByAnd(PROPERTY_ENTRY, FieldMap.build(ID, 1L));
        doReturn(value1).when(genericDelegator).makeValue(PROPERTY_TEXT, new FieldMap(value1));
        doReturn(2).when(genericDelegator).storeAll(asList(entry1, value1));

        fixture.setEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT, VALUE1);

        verify(genericDelegator).storeAll(asList(entry1, value1));
        verify(genericDelegator, never()).makeValue(PROPERTY_ENTRY, new FieldMap(entry1orig));
        verify(genericDelegator, never()).makeValue(PROPERTY_ENTRY, new FieldMap(entry1));
        verify(genericDelegator, never()).removeByAnd(PROPERTY_ENTRY, FieldMap.build(ID, 3L));
        verify(genericDelegator, never()).removeByAnd(PROPERTY_TEXT, FieldMap.build(ID, 3L));
        verify(genericDelegator).removeByAnd(PROPERTY_STRING, FieldMap.build(ID, 1L));
        verify(genericDelegator).removeByAnd(PROPERTY_ENTRY, FieldMap.build(ID, 1L));
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(cache);
    }


    @Test
    public void testReplaceExistingPropertyWithDifferentType() throws GenericEntityException
    {
        final GenericValue entry1orig = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, STRING);
        entry1orig.set(ID, 1L);
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1.set(ID, 1L);
        final GenericValue value1 = mockValue(PROPERTY_TEXT, 1L, VALUE1);
        final FieldMap fieldMap = FieldMap.build(
                ENTITY_NAME, TEST_ENTITY_1,
                ENTITY_ID, TEST_ID_1,
                PROPERTY_KEY, TEST_KEY_1);

        // Make sure it's a copy so changes to it don't pollute our original
        doReturn(asList(new MockGenericValue(entry1orig))).when(genericDelegator).findByAnd(PROPERTY_ENTRY, fieldMap);

        doReturn(value1).when(genericDelegator).makeValue(PROPERTY_TEXT, new FieldMap(value1));
        doReturn(1).when(genericDelegator).removeByAnd(PROPERTY_STRING, FieldMap.build(ID, 1L));
        doReturn(2).when(genericDelegator).storeAll(asList(entry1, value1));

        fixture.setEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT, VALUE1);

        verify(genericDelegator).storeAll(asList(entry1, value1));
        verify(genericDelegator, never()).makeValue(PROPERTY_ENTRY, new FieldMap(entry1orig));
        verify(genericDelegator, never()).makeValue(PROPERTY_ENTRY, new FieldMap(entry1));
        verify(genericDelegator).removeByAnd(PROPERTY_STRING, FieldMap.build(ID, 1L));
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(cache);
    }


    @Test
    public void testReplaceExistingPropertyWithDifferentTypeAndDuplicates() throws GenericEntityException
    {
        setUp(true);

        final GenericValue entry1orig = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, STRING);
        entry1orig.set(ID, 3L);
        final GenericValue entry1old = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1old.set(ID, 1L);
        final GenericValue entry1 = mockEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT);
        entry1.set(ID, 3L);
        final GenericValue value1 = mockValue(PROPERTY_TEXT, 3L, VALUE1);
        final FieldMap fieldMap = FieldMap.build(
                ENTITY_NAME, TEST_ENTITY_1,
                ENTITY_ID, TEST_ID_1,
                PROPERTY_KEY, TEST_KEY_1);

        // Make sure it's a copy so changes to it don't pollute our original
        doReturn(asList(new MockGenericValue(entry1orig), new MockGenericValue(entry1old))).when(genericDelegator).findByAnd(PROPERTY_ENTRY, fieldMap);

        doReturn(value1).when(genericDelegator).makeValue(PROPERTY_TEXT, new FieldMap(value1));
        doReturn(0).when(genericDelegator).removeByAnd(PROPERTY_TEXT, FieldMap.build(ID, 1L));
        doReturn(1).when(genericDelegator).removeByAnd(PROPERTY_ENTRY, FieldMap.build(ID, 1L));
        doReturn(1).when(genericDelegator).removeByAnd(PROPERTY_STRING, FieldMap.build(ID, 3L));
        doReturn(2).when(genericDelegator).storeAll(asList(entry1, value1));

        fixture.setEntry(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1, TEXT, VALUE1);

        verify(genericDelegator).storeAll(asList(entry1, value1));
        verify(genericDelegator, never()).makeValue(PROPERTY_ENTRY, new FieldMap(entry1orig));
        verify(genericDelegator, never()).makeValue(PROPERTY_ENTRY, new FieldMap(entry1));
        verify(genericDelegator).removeByAnd(PROPERTY_TEXT, FieldMap.build(ID, 1L));
        verify(genericDelegator).removeByAnd(PROPERTY_ENTRY, FieldMap.build(ID, 1L));
        verify(genericDelegator).removeByAnd(PROPERTY_STRING, FieldMap.build(ID, 3L));
        verify(cache).remove(new CacheKey(TEST_ENTITY_1, TEST_ID_1, TEST_KEY_1));
        verifyNoMoreInteractions(cache);
    }

    @Test
    public void cacheKeyShouldBeSerializable()
    {
        // Set up
        final Serializable cacheKey = new CacheKey("foo", 123, "bar");

        // Invoke
        final Object roundTripped = deserialize(serialize(cacheKey));

        // Check
        assertEquals(cacheKey, roundTripped);
        assertNotSame(cacheKey, roundTripped);
    }



    // DelegatorInterface isn't exactly easy to mock out.  The methods below are all associated with
    // making the tests half-way readable

    private static MockGenericValue mockEntry(String entityName, Long entityId, String key, int type)
    {
        return new MockGenericValue(PROPERTY_ENTRY, FieldMap.build(
                ID, ID_GENERATOR.getAndIncrement(),
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                PROPERTY_KEY, key,
                TYPE, type));
    }

    private static MockGenericValue mockValue(String typeEntity, Long entryId, Object rawValue)
    {
        return new MockGenericValue(typeEntity, FieldMap.build(
                ID, entryId,
                VALUE, rawValue));
    }

    void verifyKeysQuery(FieldMap fieldMap) throws GenericEntityException
    {
        verify(genericDelegator).findByCondition(
                eq(PROPERTY_ENTRY),
                argThat(entityFieldMap(fieldMap)),
                eq(SELECT_KEY),
                eq(NULL_LIST));
    }

    void verifyKeysQuery(String entityName, Long entityId) throws GenericEntityException
    {
        verifyKeysQuery(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId));
    }

    void verifyKeysQuery(String entityName, Long entityId, int type) throws GenericEntityException
    {
        verifyKeysQuery(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                TYPE, type));
    }

    void verifyQuery(FieldMap fieldMap) throws GenericEntityException
    {
        verify(genericDelegator).findByCondition(
                eq(PROPERTY_ENTRY),
                argThat(entityFieldMap(fieldMap)),
                eq(SELECT_ID_AND_TYPE),
                eq(NULL_LIST));
    }

    void verifyQuery(String entityName, Long entityId, String propertyKey) throws GenericEntityException
    {
        verifyQuery(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                PROPERTY_KEY, propertyKey));
    }

    void verifyEntryRemoved(Long id, String typeEntity) throws GenericEntityException
    {
        verify(genericDelegator).removeByAnd(PROPERTY_ENTRY, FieldMap.build(ID, id));
        verify(genericDelegator).removeByAnd(typeEntity, FieldMap.build(ID, id));
    }



    void mockKeysQuery(FieldMap fieldMap, GenericValue... values) throws GenericEntityException
    {
        doReturn(asList(values)).when(genericDelegator).findByCondition(
                eq(PROPERTY_ENTRY),
                argThat(entityFieldMap(fieldMap)),
                eq(SELECT_KEY),
                eq(NULL_LIST));
    }

    void mockKeysQuery(String entityName, Long entityId, GenericValue... values) throws GenericEntityException
    {
        mockKeysQuery(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId),
                values);
    }

    void mockKeysQuery(String entityName, Long entityId, int type, GenericValue... values) throws GenericEntityException
    {
        mockKeysQuery(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                TYPE, type),
                values);
    }

    void mockQuery(FieldMap fieldMap, GenericValue... values) throws GenericEntityException
    {
        doReturn(asList(values)).when(genericDelegator).findByCondition(
                eq(PROPERTY_ENTRY),
                argThat(entityFieldMap(fieldMap)),
                eq(SELECT_ID_AND_TYPE),
                eq(NULL_LIST));
    }

    void mockQuery(String entityName, Long entityId, String propertyKey, GenericValue... values) throws GenericEntityException
    {
        mockQuery(FieldMap.build(
                ENTITY_NAME, entityName,
                ENTITY_ID, entityId,
                PROPERTY_KEY, propertyKey),
                values);
    }
}

