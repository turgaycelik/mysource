package com.atlassian.jira.bc.project.version;

import com.atlassian.annotations.PublicApi;

/**
 * Instance of {@link com.atlassian.jira.bc.project.version.VersionService.VersionAction} that indicates
 * the {@link com.atlassian.jira.project.version.Version} should be swapped out for the specified version in
 * affected issues.
 *
 * @since v3.13
 */
@PublicApi
public final class SwapVersionAction implements VersionService.VersionAction
{
    private final Long swapVersionId;

    public SwapVersionAction(final Long swapTo)
    {
        swapVersionId = swapTo;
    }

    public boolean isSwap()
    {
        return true;
    }

    public Long getSwapVersionId()
    {
        return swapVersionId;
    }

    public String toString()
    {
        return "Swap for id " + swapVersionId;
    }
}
