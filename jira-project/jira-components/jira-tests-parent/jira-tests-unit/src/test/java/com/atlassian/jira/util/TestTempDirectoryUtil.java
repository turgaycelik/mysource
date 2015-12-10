package com.atlassian.jira.util;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestTempDirectoryUtil
{
    @Test
    public void testGetSystemTempDir() throws Exception
    {
        File tmpFile = File.createTempFile("Test_", ".tmp");
        assertEquals(tmpFile.getParentFile(), new File(TempDirectoryUtil.getSystemTempDir()));
    }

    @Test
    public void testCreateTempDirectory() throws Exception
    {
        File myTempDirectory = TempDirectoryUtil.createTempDirectory("Test_");
        assertEquals(new File(TempDirectoryUtil.getSystemTempDir()), myTempDirectory.getParentFile());
        assertTrue(myTempDirectory.getName().startsWith("Test_"));
        assertTrue(myTempDirectory.isDirectory());
        assertTrue(myTempDirectory.exists());
        myTempDirectory.delete();
        assertFalse(myTempDirectory.exists());
    }
}
