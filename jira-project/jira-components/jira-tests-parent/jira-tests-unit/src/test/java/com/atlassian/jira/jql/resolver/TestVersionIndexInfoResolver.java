package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestVersionIndexInfoResolver extends MockControllerTestCase
{
    @Test
    public void testGetIndexedValuesStringHappyPath() throws Exception
    {
        final MockVersion mockVersion1 = new MockVersion(1L, "version1");
        final MockVersion mockVersion2 = new MockVersion(2L, "version1");

        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.getIdsFromName("version1");
        mockController.setReturnValue(CollectionBuilder.newBuilder("1", "2").asList());
        mockController.replay();

        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        final List<String> result = resolver.getIndexedValues("version1");
        assertEquals(2, result.size());
        assertTrue(result.contains(mockVersion1.getId().toString()));
        assertTrue(result.contains(mockVersion2.getId().toString()));

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesStringIsId() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        nameResolver.getIdsFromName("2");
        mockController.setReturnValue(Collections.emptyList());
        nameResolver.idExists(2L);
        mockController.setReturnValue(true);
        mockController.replay();

        final List<String> result = resolver.getIndexedValues("2");
        assertEquals(1, result.size());
        assertTrue(result.contains("2"));
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesStringIsIdDoesntExist() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        nameResolver.getIdsFromName("2");
        mockController.setReturnValue(Collections.emptyList());
        nameResolver.idExists(2L);
        mockController.setReturnValue(false);
        mockController.replay();

        final List<String> result = resolver.getIndexedValues("2");
        assertEquals(0, result.size());
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesStringIsNotNameOrId() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        nameResolver.getIdsFromName("abc");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final List<String> result = resolver.getIndexedValues("abc");
        assertEquals(0, result.size());
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongExists() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        nameResolver.idExists(2L);
        mockController.setReturnValue(true);
        mockController.replay();

        final List<String> result = resolver.getIndexedValues(2L);
        assertEquals(1, result.size());
        assertTrue(result.contains("2"));
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongIdIsName() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        final MockVersion mockVersion1 = new MockVersion(2L, "100");
        final MockVersion mockVersion2 = new MockVersion(1L, "100");

        nameResolver.idExists(100L);
        mockController.setReturnValue(false);
        nameResolver.getIdsFromName("100");
        mockController.setReturnValue(CollectionBuilder.newBuilder("2", "1").asList());
        mockController.replay();

        final List<String> result = resolver.getIndexedValues(100L);
        assertEquals(2, result.size());
        assertTrue(result.contains(mockVersion1.getId().toString()));
        assertTrue(result.contains(mockVersion2.getId().toString()));
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongIdIsNameDoesntExist() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        nameResolver.idExists(100L);
        mockController.setReturnValue(false);
        nameResolver.getIdsFromName("100");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final List<String> result = resolver.getIndexedValues(100L);
        assertTrue(result.isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetIndexedValue() throws Exception
    {
        final NameResolver<Version> nameResolver = mockController.getMock(NameResolver.class);
        VersionIndexInfoResolver resolver = new VersionIndexInfoResolver(nameResolver);

        final MockVersion mockVersion1 = new MockVersion(1L, "Version 1");

        mockController.replay();

        final String indexedValue = resolver.getIndexedValue(mockVersion1);
        assertEquals("1", indexedValue);

        try
        {
            resolver.getIndexedValue(null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        mockController.verify();
    }
}
