package com.atlassian.jira.util.system.patch;

/**
 * This simple object that describes the information about an AppliedPatch in JIRA
 *
 * @since v4.0
 */
public interface AppliedPatchInfo
{
    /**
     * @return the issue key for the patch
     */
    public String getIssueKey();

    /**
     * @return a description of the patch
     */
    public String getDescription();

}
