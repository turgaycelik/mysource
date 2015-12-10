package com.atlassian.jira.ofbiz;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityWhereString;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;

/**
 * Integration test of {@link DefaultOfBizDelegator} that uses an in-memory database.
 */
public class TestDefaultOfBizDelegator
{
    private static final long PROJECT_ID_1 = 1;
    private static final long PROJECT_ID_2 = 2;
    private static final long PROJECT_ID_3 = 3;
    private static final List<Long> EMPTY_ID_LIST = Collections.emptyList();
    private static final String DELEGATOR_NAME = "default";
    private static final String ENTITY_ID = "id";
    private static final String ENTITY_NAME = Entity.Name.PROJECT;
    private static final String NAME_FIELD = "name";

    private static Map<String, Object> getProjectFields(final long projectId, final String projectName)
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(ENTITY_ID, projectId);
        fields.put(NAME_FIELD, projectName);
        return fields;
    }

    private OfBizDelegator ofBizDelegator;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp()
    {
        // Getting a new clean GenericDelegator sets up an in-memory database.
        GenericDelegator.removeGenericDelegator(DELEGATOR_NAME);
        final GenericDelegator genericDelegator = GenericDelegator.getGenericDelegator(DELEGATOR_NAME);
        ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        new MockComponentWorker().init().addMock(OfBizDelegator.class, ofBizDelegator);

        // Make sure we have a clean state before we commence
        cleanup();

        // Set up entities for the tests
        ofBizDelegator.createValue(ENTITY_NAME, getProjectFields(PROJECT_ID_1, "test project 1"));
        ofBizDelegator.createValue(ENTITY_NAME, getProjectFields(PROJECT_ID_2, "test project 2"));
        ofBizDelegator.createValue(ENTITY_NAME, getProjectFields(PROJECT_ID_3, "test project 3"));
    }

    @After
    public void tearDown()
    {
        cleanup();
        GenericDelegator.removeGenericDelegator("default");
    }

    private void cleanup()
    {
        // remove all projects, to avoid ID collision
        ofBizDelegator.removeByCondition(ENTITY_NAME, new EntityWhereString("1=1"));
    }

    @Test
    public void testRemoveByOr() throws GenericModelException
    {
        // remove one entity and leave 2
        int totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, Arrays.asList(PROJECT_ID_1));
        assertEquals(1, totalRemoved);

        // Assert that two are still present
        long entitiesPresent = ofBizDelegator.getCount(ENTITY_NAME);
        assertEquals(2L, entitiesPresent);

        // Set the batch size to one.
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "2");

        // Remove both entities with a batch size set to 1
        // This will make sure that we actually batch our queries
        totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, Arrays.asList(PROJECT_ID_2, PROJECT_ID_3));
        assertEquals(2, totalRemoved);

        // Assert that no entities are present
        entitiesPresent = ofBizDelegator.getCount(ENTITY_NAME);
        assertEquals(0L, entitiesPresent);
    }

    @Test
    public void testRemoveByOrWithEmptyList() throws GenericModelException
    {
        int results = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, EMPTY_ID_LIST);
        assertEquals(0, results);

        // Assert that we still have 3 entities, and non were deleted
        long entitiesPresent = ofBizDelegator.getCount(ENTITY_NAME);
        assertEquals(3L, entitiesPresent);
    }

    @Test
    public void testRemoveByOrWithInvalidParam()
    {
        try
        {
            ofBizDelegator.removeByOr(ENTITY_NAME, "notandentityvalue", EMPTY_ID_LIST);
            fail("We should have thrown an GenericModelException");
        }
        catch (GenericModelException e)
        {
            // this is expected
        }

        try
        {
            ofBizDelegator.removeByOr("notanentity", ENTITY_ID, EMPTY_ID_LIST);
            fail("We should have thrown an GenericModelException");
        }
        catch (GenericModelException e)
        {
            // this is expected
        }

    }

    @Test
    public void testRemoveByOrWithLargeBatchSize() throws GenericModelException
    {
        // Set the batch size to one.
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "5");

        // Remove both entities with a batch size set to 1
        // This will make sure that we actually batch our queries
        int totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, Arrays.asList(PROJECT_ID_1, PROJECT_ID_2, PROJECT_ID_3));
        assertEquals(3, totalRemoved);
    }

    @Test
    public void testRemoveByOrWithInvalidIDs() throws GenericModelException
    {
        // Set the batch size to one.
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "5");

        // Remove both entities with a batch size set to 1
        // This will make sure that we actually batch our queries
        int totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, Arrays.asList(PROJECT_ID_1, 2000000L, PROJECT_ID_3));
        assertEquals(2, totalRemoved);
    }

    @Test
    public void transformShouldUpdateTheDatabaseAndReturnTheNewValues()
    {
        // Set up to append "-new" the first two projects' names
        final EntityCondition condition = new EntityExpr("id", LESS_THAN_EQUAL_TO, PROJECT_ID_2);
        final List<String> orderBy = singletonList("id");
        final String suffix = "-new";

        // Invoke
        final List<GenericValue> transformedValues =
                ofBizDelegator.transform(ENTITY_NAME, condition, orderBy, NAME_FIELD, new Transformation()
        {
            @Override
            public void transform(final GenericValue entity)
            {
                entity.delegatorName = DELEGATOR_NAME;
                entity.setString(NAME_FIELD, entity.get(NAME_FIELD).toString() + suffix);
            }
        });

        // Check
        assertEquals(2, transformedValues.size());
        assertProject(PROJECT_ID_1, "test project 1" + suffix, transformedValues.get(0));
        assertProject(PROJECT_ID_2, "test project 2" + suffix, transformedValues.get(1));
        // -- Check project 3 was unchanged
        assertProjectName(PROJECT_ID_3, "test project 3");
    }

    private void assertProjectName(final long id, final String expectedName)
    {
        assertEquals(expectedName, ofBizDelegator.findById(ENTITY_NAME, id).getString(NAME_FIELD));
    }

    @Test
    public void transformOneShouldUpdateTheDatabaseAndReturnTheNewValue()
    {
        // Set up to append "-updated" the last project's name
        final EntityCondition condition = new EntityExpr("id", GREATER_THAN_EQUAL_TO, String.valueOf(PROJECT_ID_3));
        final String suffix = "-updated";

        // Invoke
        final GenericValue transformedValue =
                ofBizDelegator.transformOne(ENTITY_NAME, condition, NAME_FIELD, new Transformation()
        {
            @Override
            public void transform(final GenericValue entity)
            {
                entity.delegatorName = DELEGATOR_NAME;
                entity.setString(NAME_FIELD, entity.get(NAME_FIELD).toString() + suffix);
            }
        });

        // Check
        assertNotNull(transformedValue);
        assertProject(PROJECT_ID_3, "test project 3" + suffix, transformedValue);
        // -- Check projects 1 and 2 were unchanged
        assertProjectName(PROJECT_ID_1, "test project 1");
        assertProjectName(PROJECT_ID_2, "test project 2");
    }

    private void assertProject(final long expectedId, final String expectedName, final GenericValue actualProject)
    {
        // Check the returned GenericValue to make sure it was transformed
        assertEquals(expectedId, actualProject.getLong(ENTITY_ID).longValue());
        assertEquals(expectedName, actualProject.getString(NAME_FIELD));

        // Check the transformed values were written to the database
        final GenericValue storedProject = ofBizDelegator.findById(ENTITY_NAME, expectedId);
        assertEquals(actualProject, storedProject);
    }

    @Test
    public void transformOneShouldThrowExceptionIfNoRowsMatchTheCondition()
    {
        assertTransformOneRejected(new EntityExpr("id", LESS_THAN_EQUAL_TO, "-1"), 0);
    }

    @Test
    public void transformOneShouldThrowExceptionIfMoreThanOneRowMatchesTheCondition()
    {
        assertTransformOneRejected(new EntityExpr("id", LESS_THAN_EQUAL_TO, String.valueOf(PROJECT_ID_2)), 2);
    }

    private void assertTransformOneRejected(final EntityCondition condition, final int expectedCount)
    {
        // Invoke
        try
        {
            ofBizDelegator.transformOne(ENTITY_NAME, condition, NAME_FIELD, new Transformation()
            {
                @Override
                public void transform(final GenericValue entity)
                {
                    // Doesn't matter for this test
                }
            });
            fail("Expected a " + IllegalStateException.class.getName());
        }
        catch (final IllegalStateException expected)
        {
            assertTrue(expected.getMessage().startsWith(
                    "Expected one match for " + condition + " but found " + expectedCount + ": "));
        }
    }
}
