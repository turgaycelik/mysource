package com.atlassian.jira.util;

import java.io.File;

import org.junit.Test;

public class PathUtilsTest
{
    private String tmpDir = System.getProperty("java.io.tmpdir");

    @Test (expected = PathTraversalException.class)
    public void dotDotShouldBeDisallowedIfOutsideSecureDir() throws Exception
    {
        PathUtils.ensurePathInSecureDir(tmpDir, new File(tmpDir, "../").getPath()); // ${tmpDir}/..
    }

    @Test
    public void dotDotShouldBeAllowedIfInsideSecureDir() throws Exception
    {
        PathUtils.ensurePathInSecureDir(tmpDir, new File(tmpDir, "a/b/../b").getAbsolutePath()); // ${tmpDir}/a/b
    }
}
