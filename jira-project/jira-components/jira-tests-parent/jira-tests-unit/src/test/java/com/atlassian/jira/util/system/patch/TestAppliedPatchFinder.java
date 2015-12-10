package com.atlassian.jira.util.system.patch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Set;

import com.atlassian.jira.util.TempDirectoryUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class TestAppliedPatchFinder
{

    @Test
    public void testFindsAnchorFile()
    {
        AppliedPatchFinder appliedPatchFinder = new AppliedPatchFinder();

        URL patchAnchor = appliedPatchFinder.findPatchFileURL();
        assertNotNull(patchAnchor);
    }

    @Test
    public void testMissingFileReturnsEmptySet()
    {
        AppliedPatchFinder appliedPatchFinder = new AppliedPatchFinder()
        {
            @Override
            File findPatchFileAnchor()
            {
                return null;
            }
        };

        final Set<AppliedPatchInfo> appliedPatchInfoSet = appliedPatchFinder.getAppliedPatches();
        assertNotNull(appliedPatchInfoSet);
        assertEquals(0, appliedPatchInfoSet.size());
    }

    @Test
    public void testReturnsPatches() throws IOException
    {
        final File directory = TempDirectoryUtil.createTempDirectory("TestPatchFinder.testReturnsPatches");
        final File anchorFile = new File(directory, "patch.anchor");

        createPatchFile("JRA-666", directory).deleteOnExit();
        createPatchFile("JRA-999", directory).deleteOnExit();

        AppliedPatchFinder appliedPatchFinder = new AppliedPatchFinder()
        {
            @Override
            File findPatchFileAnchor()
            {
                return anchorFile;
            }
        };

        final Set<AppliedPatchInfo> appliedPatchInfoSet = appliedPatchFinder.getAppliedPatches();
        assertNotNull(appliedPatchInfoSet);
        assertEquals(2,appliedPatchInfoSet.size());

        final String endOfLine = System.getProperty("line.separator");
        assertTrue(appliedPatchInfoSet.contains(new AppliedPatchFinder.AppliedPatchInfoImpl("JRA-666", "This is the patch for JRA-666" + endOfLine)));
        assertTrue(appliedPatchInfoSet.contains(new AppliedPatchFinder.AppliedPatchInfoImpl("JRA-999", "This is the patch for JRA-999" + endOfLine)));
    }

    private File createPatchFile(final String key, final File directory) throws IOException
    {
        File f = new File(directory, key + ".patch");
        PrintWriter out = new PrintWriter(new FileWriter(f));
        out.println("This is the patch for " + key);
        out.close();

        return f;
    }
}
