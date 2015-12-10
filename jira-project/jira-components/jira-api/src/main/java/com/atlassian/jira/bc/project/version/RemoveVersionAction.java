package com.atlassian.jira.bc.project.version;

import com.atlassian.annotations.PublicApi;

/**
 * Instance of {@link com.atlassian.jira.bc.project.version.VersionService.VersionAction} that indicates
 * the {@link com.atlassian.jira.project.version.Version} should be removed from affected issues.
 *
 * Use {@link VersionService#REMOVE} instead of instantiating this class. 
 *
 * @since v3.13
 */
@PublicApi
public final class RemoveVersionAction implements VersionService.VersionAction
{
    public boolean isSwap()
    {
        return false;
    }

    public Long getSwapVersionId()
    {
        return null;
    }

    public String toString()
    {
        return "Remove";
    }
}
