package com.atlassian.jira.entity.property;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.matchers.ReflectionEqualTo;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.Entity.ENTITY_PROPERTY;
import static com.atlassian.jira.mock.Strict.strict;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @since v6.1
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("ConstantConditions")  // null checks
public class TestJsonEntityPropertyManagerImpl
{
    private static final String ENTITY_NAME = "foo.properties";
    private static final Long ENTITY_ID = 42L;
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY_PREFIX = "key";
    private static final String VALUE1 = "{ \"some\": \"JSON1\" }";
    private static final String VALUE2 = "[ \"some\", null, \"JSON2\" ]";
    private static final String VALUE3 = "{ \"some\": \"JSON3\", \"Answer\": 42, \"true?\": false }";
    private static final String VALUE4 = "{ \"ToInfinityAndBeyond\": 9e999 }";
    private static final String VALUE5 = "true";


    private static final String OTHER_ENTITY_NAME = "other.properties";
    private static final Long OTHER_ENTITY_ID = 6L;
    private static final String OTHER_KEY = "otherkey";
    private static final String OTHER_KEY_PREFIX = "other";

    private static final int COUNT = 42;
    private static final List<String> EMPTY_STRING_LIST = ImmutableList.of();

    DelegatorInterface genericDelegator;
    MockOfBizDelegator ofBizDelegator;
    EventPublisher eventPublisher = mock(EventPublisher.class, strict());
    EntityEngine entityEngine;
    JsonEntityPropertyManagerImpl entityPropertyManager;

    @Before
    public void setUp()
    {
        genericDelegator = mock(DelegatorInterface.class, strict());
        ofBizDelegator = new MockOfBizDelegator()
        {
            @Override
            public DelegatorInterface getDelegatorInterface()
            {
                return genericDelegator;
            }
        };
        entityEngine = new EntityEngineImpl(ofBizDelegator);
        entityPropertyManager = new JsonEntityPropertyManagerImpl(entityEngine, eventPublisher);
        assertCount(0);
    }

    @After
    public void tearDown()
    {
        entityPropertyManager = null;
        entityEngine = null;
        ofBizDelegator = null;
    }



    // There is no MockGenericDelegator and countByCondition isn't visible at the OfBizDelegator level,
    // so the testCount* tests are a bit silly at present.  It would be good to expose countByCondition in
    // ofBizDelegator at some point.

    @Test
    public void testCount() throws GenericEntityException
    {
        final FieldMap fieldMap = new FieldMap(EntityProperty.ENTITY_NAME, ENTITY_NAME);
        doReturn(COUNT).when(genericDelegator).countByCondition(
                eq(ENTITY_PROPERTY.getEntityName()),
                isNull(String.class),
                toStringEq(new EntityConditionList(asList(
                        new EntityFieldMap(fieldMap, EntityOperator.AND),
                        new EntityExpr(EntityProperty.KEY, EntityOperator.LIKE, KEY_PREFIX + '%')),
                        EntityOperator.AND)),
                refEq(new EntityFindOptions()));

        assertEquals(COUNT, entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .keyPrefix(KEY_PREFIX)
                .offset(3)
                .maxResults(5)
                .count() );
    }

    @Test
    public void testCountByEntity() throws GenericEntityException
    {
        final FieldMap fieldMap = new FieldMap()
                .add(EntityProperty.ENTITY_NAME, ENTITY_NAME)
                .add(EntityProperty.ENTITY_ID, ENTITY_ID);
        doReturn(COUNT).when(genericDelegator).countByCondition(
                eq(ENTITY_PROPERTY.getEntityName()),
                isNull(String.class),
                refEq(new EntityFieldMap(fieldMap, EntityOperator.AND)),
                refEq(new EntityFindOptions()));

        assertEquals(COUNT, entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .entityId(ENTITY_ID)
                .count());
    }

    @Test
    public void testCountByEntityNameAndPropertyKey() throws GenericEntityException
    {
        final FieldMap fieldMap = new FieldMap()
                .add(EntityProperty.ENTITY_NAME, ENTITY_NAME)
                .add(EntityProperty.KEY, KEY1);

        doReturn(COUNT).when(genericDelegator).countByCondition(
                eq(ENTITY_PROPERTY.getEntityName()),
                isNull(String.class),
                refEq(new EntityFieldMap(fieldMap, EntityOperator.AND)),
                refEq(new EntityFindOptions()));

        assertEquals(COUNT, entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .key(KEY1)
                .count());
    }

    @Test
    public void testDeleteByFullQualification()
    {
        Long id = create();
        assertIds(id);
        entityPropertyManager.delete(OTHER_ENTITY_NAME, ENTITY_ID, KEY1);
        assertIds(id);
        entityPropertyManager.delete(ENTITY_NAME, OTHER_ENTITY_ID, KEY1);
        assertIds(id);
        entityPropertyManager.delete(ENTITY_NAME, ENTITY_ID, KEY2);
        assertIds(id);
        entityPropertyManager.delete(ENTITY_NAME, ENTITY_ID, KEY1);
        assertIds();
        entityPropertyManager.delete(ENTITY_NAME, ENTITY_ID, KEY1);
        assertIds();
    }

    @Test
    public void testDeleteByEntity()
    {
        entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .entityId(ENTITY_ID)
                .delete();
        assertCount(0);

        Long id1 = create();
        Long id2 = create(OTHER_ENTITY_NAME, ENTITY_ID, KEY1, VALUE1);
        Long id3 = create(ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE1);
        Long id4 = create(ENTITY_NAME, ENTITY_ID, KEY2, VALUE1);
        Long id5 = create(OTHER_ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE1);

        assertIds(id1, id2, id3, id4, id5);
        entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .entityId(ENTITY_ID)
                .delete();
        assertIds(id2, id3, id5);

        entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .entityId(ENTITY_ID)
                .delete();
        assertIds(id2, id3, id5);
    }

    @Test
    public void testDeleteByEntityNameAndPropertyKey()
    {
        entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .key(KEY1)
                .delete();
        assertCount(0);

        Long id1 = create();
        Long id2 = create(OTHER_ENTITY_NAME, ENTITY_ID, KEY1, VALUE1);
        Long id3 = create(ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE1);
        Long id4 = create(ENTITY_NAME, ENTITY_ID, KEY2, VALUE1);
        Long id5 = create(OTHER_ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE1);

        assertIds(id1, id2, id3, id4, id5);
        entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .key(KEY1)
                .delete();
        assertIds(id2, id4, id5);

        entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .key(KEY1)
                .delete();
        assertIds(id2, id4, id5);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testQueryDoesNotAllowNullEntityName()
    {
        entityPropertyManager.query().entityName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindKeysWithPrefixDoesNotAllowBlankEntityName()
    {
        entityPropertyManager.query().entityName("    ");
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFindKeysWithPrefixDoesNotAllowNullQuery()
    {
        entityPropertyManager.query().keyPrefix(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindKeysWithPrefixDoesNotAllowBlankQuery()
    {
        entityPropertyManager.query().keyPrefix("    ");
    }

    @Test
    public void testFindKeysByEntityNameAndKeyPrefix()
    {
        create();
        create(OTHER_ENTITY_NAME, ENTITY_ID, KEY1, VALUE2);
        create(ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE3);
        create(ENTITY_NAME, ENTITY_ID, KEY2, VALUE4);
        create(OTHER_ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE5);
        create(OTHER_ENTITY_NAME, OTHER_ENTITY_ID, OTHER_KEY, VALUE1);

        assertEquals(asList(KEY1, KEY2), entityPropertyManager.findKeys(ENTITY_NAME, KEY_PREFIX));
        assertEquals(asList(OTHER_KEY), entityPropertyManager.findKeys(OTHER_ENTITY_NAME, OTHER_KEY_PREFIX));
        assertEquals(EMPTY_STRING_LIST, entityPropertyManager.findKeys(ENTITY_NAME, OTHER_KEY_PREFIX));
        assertEquals(EMPTY_STRING_LIST, entityPropertyManager.findKeys(OTHER_ENTITY_NAME, KEY2));
    }

    @Test
    public void testFindKeysWithQuery()
    {
        create();
        create(ENTITY_NAME, ENTITY_ID, KEY2, VALUE4);
        create(OTHER_ENTITY_NAME, ENTITY_ID, KEY1, VALUE2);
        create(ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE3);
        create(OTHER_ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE5);
        create(OTHER_ENTITY_NAME, OTHER_ENTITY_ID, OTHER_KEY, VALUE1);

        assertEquals(asList(KEY1, KEY2), entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .keyPrefix(KEY_PREFIX)
                .findDistinctKeys() );
        assertEquals(asList(OTHER_KEY), entityPropertyManager.query()
                .entityName(OTHER_ENTITY_NAME)
                .key(OTHER_KEY)
                .findDistinctKeys() );
        assertEquals(asList(KEY1), entityPropertyManager.query()
                .key(KEY1)
                .findDistinctKeys() );
        assertEquals(EMPTY_STRING_LIST, entityPropertyManager.query()
                .key("asdf")
                .findDistinctKeys() );
        assertEquals(asList(KEY1, OTHER_KEY), entityPropertyManager.query()
                .entityName(OTHER_ENTITY_NAME)
                .findDistinctKeys() );
    }

    @Test
    public void testGetAndPutSimpleReplace()
    {
        assertCount(0);
        entityPropertyManager.put(ENTITY_NAME, ENTITY_ID, KEY1, VALUE1);
        assertCount(1);
        assertEquals(null, entityPropertyManager.get(OTHER_ENTITY_NAME, ENTITY_ID, KEY1));
        assertEquals(null, entityPropertyManager.get(ENTITY_NAME, OTHER_ENTITY_ID, KEY1));
        assertEquals(null, entityPropertyManager.get(ENTITY_NAME, ENTITY_ID, KEY2));
        assertEquals(VALUE1, entityPropertyManager.get(ENTITY_NAME, ENTITY_ID, KEY1).getValue());
        assertCount(1);
        entityPropertyManager.put(ENTITY_NAME, ENTITY_ID, KEY1, VALUE2);
        assertCount(1);
        assertEquals(VALUE2, entityPropertyManager.get(ENTITY_NAME, ENTITY_ID, KEY1).getValue());
        assertCount(1);
    }

    @Test
    public void testPutDryRunErrorCases()
    {
        assertBadPutDryRun("entityName should not be empty!", "", KEY1, "{}");
        assertBadPutDryRun("entityId should not be null!", ENTITY_NAME, KEY1, "{}");
        assertBadPutDryRun("key should not be empty!", ENTITY_NAME, "", "{}");
        assertFieldTooLong("entityName", JsonEntityPropertyManagerImpl.MAXIMUM_ENTITY_NAME_LENGTH,
                exceed(JsonEntityPropertyManagerImpl.MAXIMUM_ENTITY_NAME_LENGTH), KEY1, VALUE1);
        assertFieldTooLong("key", JsonEntityPropertyManagerImpl.MAXIMUM_KEY_LENGTH,
                ENTITY_NAME, exceed(JsonEntityPropertyManagerImpl.MAXIMUM_KEY_LENGTH), VALUE1);
        assertFieldTooLong("json", entityPropertyManager.getMaximumValueLength(),
                ENTITY_NAME, KEY1, exceed(entityPropertyManager.getMaximumValueLength()));
        assertBadJson("was expecting double-quote", ENTITY_NAME, KEY1, "{ asdf: \"Barewords are not permitted as object keys\" }");
        assertBadJson("'T' (code 84)", ENTITY_NAME, KEY1, "This is not a JSON");
        assertBadJson("']' (code 93)", ENTITY_NAME, KEY1, "[ \"Trailing ',' is disallowed\", 1, 2, ]");
        assertBadJson("was expecting a colon", ENTITY_NAME, KEY1, "{ \"Need to use ':' in objects, not ','\", 1 }");
        assertBadJson("Unexpected string", ENTITY_NAME, KEY1, "{\"key\":\"value\"}\n\n\"sss\"");
        assertBadJson("Unexpected number", ENTITY_NAME, KEY1, "{\"key\":\"value\"}\n\n1");
        assertBadJson("Unexpected character t", ENTITY_NAME, KEY1, "{\"key\":\"value\"}\n\ntrue");
        assertBadJson("Unexpected character (", ENTITY_NAME, KEY1, "({\"key\":\"value\"})");
        assertBadJson("Unexpected character (", ENTITY_NAME, KEY1, "{\"key\":\"value\"}\n\n({}))");
        assertBadJson("Unexpected string", ENTITY_NAME, KEY1, "{\"key\":\"value\"}\"q\"");
        assertBadJson("Unexpected character (", ENTITY_NAME, KEY1, "({\"key\":\"value\"}\"q\")");
    }

    @Test
    public void testPutDryRunWithNullJson()
    {
        entityEngine = mock(EntityEngine.class, strict());
        entityPropertyManager = new JsonEntityPropertyManagerImpl(entityEngine, eventPublisher);
        entityPropertyManager.putDryRun(ENTITY_NAME, KEY1, null);
    }

    @Test
    public void testGetResolveMultipleEntities()
    {
        final long t = System.currentTimeMillis();
        assertCount(0);

        // Note: the timestamps must differ in the *seconds* position for it to count, because
        // Timestamp ignores differences in that range.  See its javadocs for an explanation.
        create(1L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE1, t, t);
        create(4L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE5, t, t+2000);
        create(3L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE4, t, t+2000);
        create(2L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE2, t, t);
        create(5L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE3, t, t+1000);
        assertCount(5);

        assertEquals(VALUE5, entityPropertyManager.get(ENTITY_NAME, ENTITY_ID, KEY1).getValue());

        // And any others should have been deleted, too
        assertIds(4L);
    }

    @Test
    public void testFindResolveMultipleEntities()
    {
        final long t = System.currentTimeMillis();
        assertCount(0);

        // Note: the timestamps must differ in the *seconds* position for it to count, because
        // Timestamp ignores differences in that range.  See its javadocs for an explanation.
        create(1L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE1, t, t);
        create(4L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE5, t, t+2000);
        create(3L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE4, t, t+2000);
        create(9L, OTHER_ENTITY_NAME, ENTITY_ID, KEY1, VALUE2, t, t+10000);
        create(2L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE2, t, t);
        create(5L, ENTITY_NAME, ENTITY_ID, KEY1, VALUE3, t, t+1000);
        create(6L, OTHER_ENTITY_NAME, ENTITY_ID, OTHER_KEY, VALUE3, t, t+10000);
        create(7L, ENTITY_NAME, OTHER_ENTITY_ID, KEY1, VALUE3, t, t+10000);
        create(8L, ENTITY_NAME, ENTITY_ID, KEY2, VALUE3, t, t+10000);
        create(10L, OTHER_ENTITY_NAME, ENTITY_ID, KEY2, VALUE3, t, t+10000);
        assertCount(10);

        assertEquals(asList(9L, 10L, 6L), ids(entityPropertyManager.query()
                .entityName(OTHER_ENTITY_NAME)
                .find() ));
        assertCount(10);

        assertEquals(asList(9L, 10L), ids(entityPropertyManager.query()
                .entityName(OTHER_ENTITY_NAME)
                .keyPrefix(KEY_PREFIX)
                .find() ));
        assertCount(10);

        assertEquals(asList(8L, 10L), ids(entityPropertyManager.query()
                .key(KEY2)
                .find() ));
        assertCount(10);

        assertEquals(asList(7L, 4L, 9L), ids(entityPropertyManager.query()
                .key(KEY1)
                .find() ));
        assertIds(4L, 9L, 6L, 7L, 8L, 10L);

        assertEquals(asList(7L, 4L, 8L), ids(entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .maxResults(7)
                .find() ));
        assertCount(6);

        assertEquals(asList(7L, 4L), ids(entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .maxResults(2)
                .find() ));
        // Note: offset is deliberately ignored if maxResults isn't set
        assertEquals(asList(7L, 4L, 8L), ids(entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .offset(1)
                .find() ));
        assertEquals(asList(4L), ids(entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .offset(1)
                .maxResults(1)
                .find() ));
        assertEquals(Collections.<Long>emptyList(), ids(entityPropertyManager.query()
                .entityName(ENTITY_NAME)
                .offset(3)
                .maxResults(1)
                .find() ));
        assertIds(4L, 9L, 6L, 7L, 8L, 10L);
    }



    private void assertCount(int expectedCount)
    {
        assertEquals(expectedCount, ofBizDelegator.getCount(ENTITY_PROPERTY.getEntityName()));
    }

    private void assertIds(Long... expectedIds)
    {
        final List<Long> expected = asList(expectedIds);
        Collections.sort(expected);

        final List<Long> actual = new ArrayList<Long>(expectedIds.length);
        final List<GenericValue> gvList = ofBizDelegator.findAll(ENTITY_PROPERTY.getEntityName());
        for (GenericValue gv : gvList)
        {
            actual.add(gv.getLong(EntityProperty.ID));
        }
        Collections.sort(actual);
        if (!Objects.equal(expected, actual))
        {
            assertEquals(gvList.toString() + '\n', expected, actual);
        }
    }

    private String exceed(int limit)
    {
        final int len = limit + 1;
        final StringBuilder sb = new StringBuilder(len);
        while (sb.length() < len)
        {
            sb.append("0123456789abcdef");
        }
        sb.setLength(len);
        return sb.toString();
    }

    private Long create()
    {
        return create(ENTITY_NAME, ENTITY_ID, KEY1, VALUE1);
    }

    private Long create(String entityName, Long entityId, String key, String value)
    {
        return entityEngine.createValue(
                ENTITY_PROPERTY,
                EntityPropertyImpl.forCreate(entityName, entityId, key, value))
                .getId();
    }

    private EntityProperty create(Long id, String entityName, Long entityId, String key, String value,
            long created, long updated)
    {
        return entityEngine.createValue(ENTITY_PROPERTY, EntityPropertyImpl.existing(
                id, entityName, entityId, key, value, new Timestamp(created), new Timestamp(updated)));
    }

    private void assertFieldTooLong(String field, int maxLen, String entityName, String key, String value)
    {
        try
        {
            entityPropertyManager.putDryRun(entityName, key, value);
            fail("Expected an InvalidJsonPropertyException for field '" + field +
                    "', but the putDryRun succeeded, instead");
        }
        catch (FieldTooLongJsonPropertyException ftl)
        {
            assertEquals("field", field, ftl.getField());
            assertEquals("maximumLength", maxLen, ftl.getMaximumLength());
            assertEquals("actualLength", maxLen+1, ftl.getActualLength());
        }
    }

    private void assertBadJson(String expectedSubstring, String entityName, String key, String value)
    {
        try
        {
            entityPropertyManager.putDryRun(entityName, key, value);
            fail("Expected an IllegalArgumentException containing the text '" + expectedSubstring +
                    "', but the putDryRun succeeded, instead");
        }
        catch (InvalidJsonPropertyException ijpe)
        {
            assertThat(ijpe.toString(), containsString(expectedSubstring));
        }
    }

    private void assertBadPutDryRun(String expectedSubstring, String entityName, String key, String value)
    {
        try
        {
            entityPropertyManager.putDryRun(entityName, key, value);
        }
        catch (FieldTooLongJsonPropertyException ftl)
        {
            fail("Expected a normal IllegalArgumentException, not this: " + ftl);
        }
        catch (InvalidJsonPropertyException ijpe)
        {
            fail("Expected a normal IllegalArgumentException, not this: " + ijpe);
        }
        catch (IllegalArgumentException iae)
        {
            assertThat(iae.toString(), containsString(expectedSubstring));
        }
    }

    static List<Long> ids(List<EntityProperty> entityProperties)
    {
        final List<Long> ids = new ArrayList<Long>(entityProperties.size());
        for (EntityProperty entityProperty : entityProperties)
        {
            ids.add(entityProperty.getId());
        }
        return ids;
    }

    /**
     * Because EntityFieldMap and EntityFindOptions don't implement equals. :(
     */
    static <T> T refEq(T expected)
    {
        return argThat(ReflectionEqualTo.reflectionEqualTo(expected));
    }

    static <T> T toStringEq(T expected)
    {
        return argThat(Matchers.<T>hasToString(expected.toString()));
    }
}
