package com.atlassian.jira.util.system.patch;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 */
public class TestAppliedPatches
{
    @Test
    public void testIsNeverNull()
    {
        final Set<AppliedPatchInfo> patches = AppliedPatches.getAppliedPatches();
        assertNotNull(patches);
        //
        // this will be empty unless we are producing a patched version of JIRA
        assertEquals(0, patches.size());
    }
}
