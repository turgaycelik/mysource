package com.atlassian.jira.util.system.patch;

import java.util.Set;

/**
 * This is used to detect if any patches have been applied to JIRA.
 * <p/>
 * Normally this will be empty since JIRA is not supplied in patched form.  However if a patch has to be applied then
 * this class can be look for those patches and report on them
 * <p/>
 * This is designed as a support improvement.
 *
 * @since v4.0
 */
public class AppliedPatches
{
    /**
     * @return an array of applied patch strings.  Normally this will be empty in a proper released of JIRA.  This will
     *         NEVER be null!
     */
    public static Set<AppliedPatchInfo> getAppliedPatches()
    {
       return new AppliedPatchFinder().getAppliedPatches();
    }
}
