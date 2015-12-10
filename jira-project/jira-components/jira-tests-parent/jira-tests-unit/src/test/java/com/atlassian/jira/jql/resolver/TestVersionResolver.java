package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestVersionResolver
{
    private static final List<Version> EMPTY_VERSION_LIST = Collections.emptyList();

    @Mock
    private VersionManager versionManager;

    private VersionResolver resolver;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        resolver = new VersionResolver(versionManager);
    }

    @Test
    public void testGetIdsFromNameHappyPath() throws Exception
    {
        // Set up
        final Version mockVersion1 = new MockVersion(1L, "version1");
        final Version mockVersion2 = new MockVersion(2L, "version1");
        when(versionManager.getVersionsByName("version1")).thenReturn(asList(mockVersion1, mockVersion2));

        // Invoke
        final List<String> result = resolver.getIdsFromName("version1");

        // Check
        assertEquals(2, result.size());
        assertTrue(result.contains(mockVersion1.getId().toString()));
        assertTrue(result.contains(mockVersion2.getId().toString()));
    }

    @Test
    public void testGetIdsFromNameDoesntExist() throws Exception
    {
        // Set up
        when(versionManager.getVersionsByName("abc")).thenReturn(EMPTY_VERSION_LIST);

        // Invoke
        final List<String> result = resolver.getIdsFromName("abc");

        // Check
        assertEquals(0, result.size());
    }

    @Test
    public void testIdExistsNoVersionFlag() throws Exception
    {
        // Set up
        final Long NO_VERSION = new Long(VersionManager.NO_VERSIONS);
        when(versionManager.getVersion(NO_VERSION)).thenReturn(null);

        // Invoke and check
        assertFalse(resolver.idExists(NO_VERSION));
    }

    @Test
    public void testGetIdExists() throws Exception
    {
        // Set up
        final MockVersion mockVersion = new MockVersion(2L, "version1");
        when(versionManager.getVersion(2L)).thenReturn(mockVersion);

        // Invoke
        final Version result = resolver.get(2L);

        // Check
        assertEquals(mockVersion, result);
    }

    @Test
    public void testGetIdDoesntExist() throws Exception
    {
        // Set up
        when(versionManager.getVersion(100L)).thenReturn(null);

        // Invoke
        final Version result = resolver.get(100L);

        // Check
        assertNull(result);
    }

    @Test
    public void testNameExists() throws Exception
    {
        // Set up
        final Version version = new MockVersion(1000, "name");
        when(versionManager.getVersionsByName("name")).thenReturn(singletonList(version));
        when(versionManager.getVersionsByName("noname")).thenReturn(EMPTY_VERSION_LIST);

        // Invoke and check
        assertTrue(resolver.nameExists("name"));
        assertFalse(resolver.nameExists("noname"));
    }

    @Test
    public void testIdExists() throws Exception
    {
        // Set up
        when(versionManager.getVersion(10L)).thenReturn(new MockVersion(1000, "name"));
        when(versionManager.getVersion(11L)).thenReturn(null);

        // Invoke and check
        assertTrue(resolver.idExists(10L));
        assertFalse(resolver.idExists(11L));
    }
}
