package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;

/**
 * An abstract class that can be used as a base for creating implementations of IssueTabPanel3;
 */
@PublicSpi
public abstract class AbstractIssueTabPanel3 implements IssueTabPanel3
{
    protected IssueTabPanelModuleDescriptor descriptor;

    public void init(IssueTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }
}
