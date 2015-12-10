package com.atlassian.jira.util.system;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.atlassian.jira.util.system.release.ReleaseInfoMarker;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestReleaseInfo
{
    private final Class testClass = ReleaseInfoMarker.class;
    private final File file;

    public TestReleaseInfo()
    {
        // create the file that we use to test
        final String classFile = "ReleaseInfoMarker.class";
        String path = testClass.getResource(classFile).getPath();
        path = path.substring(0, path.indexOf(classFile));
        this.file = new File(path + "release.info");
    }

    @Test
    public void testNoReleaseInfo()
    {
        file.delete();
        final ReleaseInfo releaseInfo = ReleaseInfo.getReleaseInfo(testClass);
        assertFalse(releaseInfo.hasInfo());
        assertEquals("unknown", releaseInfo.getInfo());
    }

    @Test
    public void testGetSourceRelease()
    {
        assertFileContains("source");
    }

    @Test
    public void testGetWarRelease()
    {
        assertFileContains("war");
    }

    @Test
    public void testGetStandaloneRelease()
    {
        assertFileContains("standalone");
    }

    private void assertFileContains(final String expected)
    {
        try
        {
            FileWriter writer = new FileWriter(file);
            writer.write(expected);
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        final ReleaseInfo releaseInfo = ReleaseInfo.getReleaseInfo(testClass);
        assertTrue(releaseInfo.hasInfo());
        assertEquals(expected, releaseInfo.getInfo());
    }

    @After
    public void tearDown() throws Exception
    {
        file.delete();
    }
}
