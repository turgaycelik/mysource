package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.List;
import java.util.Locale;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.DirectoryTermKeys;
import com.atlassian.jira.crowd.embedded.TestData;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.crowd.embedded.api.OperationType.UPDATE_USER;
import static com.atlassian.jira.crowd.embedded.TestData.Attributes.ATTRIBUTE3;
import static com.atlassian.jira.crowd.embedded.TestData.Attributes.VALUE_22;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OfBizDirectoryDaoTest extends AbstractTransactionalOfBizTestCase
{
    private OfBizDirectoryDao directoryDao;
    private static final String UPDATED_DIRECTORY_NAME = "Updated Directory Name";
    private static final String UPDATED_DESCRIPTION = "New updated description";

    @Before
    public void setUp() throws Exception
    {
        final CacheManager cacheManager = new MemoryCacheManager();
        directoryDao = new OfBizDirectoryDao(getOfBizDelegator(), cacheManager);
    }

    @After
    public void tearDown() throws Exception
    {
        directoryDao = null;
    }

    @Test
    public void testAddDirectoryAndFindByNameAndFindById() throws Exception
    {
        Directory directory = directoryDao.add(TestData.Directory.getUnmockedTestData());

        TestData.Directory.assertEqualsTestDirectory(directory);

        directory = directoryDao.findByName(TestData.Directory.NAME);

        TestData.Directory.assertEqualsTestDirectory(directory);

        directory = directoryDao.findById(directory.getId());

        TestData.Directory.assertEqualsTestDirectory(directory);
    }

    @Test
    public void testAddAndUpdateDirectory() throws Exception
    {
        final DirectoryImpl directory = (DirectoryImpl) directoryDao.add(TestData.Directory.getUnmockedTestData());

        TestData.Directory.assertEqualsTestDirectory(directory);

        directory.setName(UPDATED_DIRECTORY_NAME);
        directory.setActive(false);
        directory.setDescription(UPDATED_DESCRIPTION);
        directory.setType(DirectoryType.UNKNOWN);
        directory.setImplementationClass(this.getClass().getCanonicalName());
        directory.setAllowedOperations(Sets.newHashSet(UPDATE_USER));
        directory.setAttributes(ImmutableMap.<String, String> of(ATTRIBUTE3, VALUE_22));

        directoryDao.update(directory);

        assertEquals(UPDATED_DIRECTORY_NAME, directory.getName());
        assertEquals(toLowerCase(UPDATED_DIRECTORY_NAME), directory.getLowerName());
        assertFalse(directory.isActive());
        assertEquals(UPDATED_DESCRIPTION, directory.getDescription());
        assertEquals(DirectoryType.UNKNOWN, directory.getType());
        assertEquals(this.getClass().getCanonicalName(), directory.getImplementationClass());
        assertEquals(toLowerCase(this.getClass().getCanonicalName()), directory.getLowerImplementationClass());

        assertEquals(1, directory.getAllowedOperations().size());
        assertTrue(directory.getAllowedOperations().contains(UPDATE_USER));

        assertEquals(1, directory.getAttributes().size());
        assertEquals(VALUE_22, directory.getAttributes().get(ATTRIBUTE3));
    }

    private Object toLowerCase(final String string)
    {
        return string.toLowerCase(Locale.ENGLISH);
    }

    @Test
    public void testRemoveDirectory() throws Exception
    {
        final Directory directory = directoryDao.add(TestData.Directory.getUnmockedTestData());

        directoryDao.remove(directory);

        try
        {
            directoryDao.findByName(directory.getName());
            fail("Exception should of been thrown");
        }
        catch (final DirectoryNotFoundException e)
        {

        }
    }

    @Test
    public void testSearch()
    {
        directoryDao.add(TestData.Directory.getUnmockedTestData());

        List<Directory> directories = directoryDao.search(QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory()).returningAtMost(
            EntityQuery.MAX_MAX_RESULTS));

        assertEquals(1, directories.size());

        TestData.Directory.assertEqualsTestDirectory(directories.get(0));

        // Test null and not null searches
        // We only support null or empty restrictions
        directories = directoryDao.search(null);
        assertEquals(1, directories.size());

        directories = directoryDao.search(QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory()).with(NullRestrictionImpl.INSTANCE).returningAtMost(
            EntityQuery.MAX_MAX_RESULTS));

        try
        {
            directories = directoryDao.search(QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory()).with(new TermRestriction(DirectoryTermKeys.TYPE, "xx")).returningAtMost(
                EntityQuery.MAX_MAX_RESULTS));
            fail("Search restrictions are not supported");
        }
        catch (UnsupportedOperationException e)
        {
            // All good. The above condition is not supported.
        }
    }

    @Test
    public void testSearchForName() throws Exception
    {
        directoryDao.add(TestData.Directory.getUnmockedTestData());

        EntityQuery<Directory> directoryQuery = QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory())
            .with(Restriction.on(DirectoryTermKeys.NAME).exactlyMatching("Test Directory"))
            .returningAtMost(EntityQuery.ALL_RESULTS);

        List<Directory> results = directoryDao.search(directoryQuery);
        assertEquals(1, results.size());
        assertEquals("Test Directory", results.get(0).getName());

        directoryQuery = QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory())
            .with(Restriction.on(DirectoryTermKeys.NAME).exactlyMatching("XXX Directory"))
            .returningAtMost(EntityQuery.ALL_RESULTS);

        results = directoryDao.search(directoryQuery);
        assertEquals(0, results.size());
    }
}
