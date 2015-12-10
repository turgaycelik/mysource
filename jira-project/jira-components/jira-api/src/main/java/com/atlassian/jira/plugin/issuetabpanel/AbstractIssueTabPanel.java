package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public abstract class AbstractIssueTabPanel implements IssueTabPanel
{
    protected IssueTabPanelModuleDescriptor descriptor;

    public void init(IssueTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }
}
