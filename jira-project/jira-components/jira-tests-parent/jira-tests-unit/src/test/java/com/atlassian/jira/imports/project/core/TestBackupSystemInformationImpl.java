package com.atlassian.jira.imports.project.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestBackupSystemInformationImpl
{
    @Test
    public void testHappyPath()
    {
        final List pluginVersions = new ArrayList();
        pluginVersions.add("abc");
        pluginVersions.add("def");
        BackupSystemInformationImpl backupSystemInformation = new BackupSystemInformationImpl("123", "Special", pluginVersions, true, Collections.EMPTY_MAP, 0);
        assertEquals("123", backupSystemInformation.getBuildNumber());
        assertEquals("Special", backupSystemInformation.getEdition());
        assertTrue(backupSystemInformation.unassignedIssuesAllowed());

        // Now test pluginVersions
        assertEquals(2, backupSystemInformation.getPluginVersions().size());
        assertEquals("abc", backupSystemInformation.getPluginVersions().get(0));
        assertEquals("def", backupSystemInformation.getPluginVersions().get(1));
        // getPluginVersions() List should be unmodifiable
        try
        {
            backupSystemInformation.getPluginVersions().add("xyz");
            fail("getPluginVersions() List should be unmodifiable");
        }
        catch (UnsupportedOperationException e)
        {
            // Expected.
        }
    }

    @Test
    public void testNullArguments()
    {
        try
        {
            new BackupSystemInformationImpl("123", "hello", null, true, Collections.EMPTY_MAP, 0);
            fail("Should not construct with null Plugin versions");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

}
