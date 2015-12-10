package com.atlassian.jira.plugin.issuetabpanel;

import java.util.List;

/**
 * Adapts old IssueTabPanel and IssueTabPanel2 implementations to the new IssueTabPanel3 interface.
 *
 * @since v6.0
 */
public class IssueTabPanel3Adaptor implements IssueTabPanel3
{
    private final IssueTabPanel issueTabPanel;

    public IssueTabPanel3Adaptor(IssueTabPanel issueTabPanel)
    {
        this.issueTabPanel = issueTabPanel;
    }

    public static IssueTabPanel3 createFrom(IssueTabPanel issueTabPanel)
    {
        return new IssueTabPanel3Adaptor(issueTabPanel);
    }

    @Override
    public void init(IssueTabPanelModuleDescriptor descriptor)
    {
        issueTabPanel.init(descriptor);
    }

    @Override
    public boolean showPanel(ShowPanelRequest request)
    {
        if (issueTabPanel instanceof IssueTabPanel2)
            return ((IssueTabPanel2)issueTabPanel).showPanel(request).isShow();
        else
            return issueTabPanel.showPanel(request.issue(), request.remoteUser());
    }

    @Override
    public List<IssueAction> getActions(GetActionsRequest request)
    {
        if (issueTabPanel instanceof IssueTabPanel2)
            return ((IssueTabPanel2)issueTabPanel).getActions(request).actions();
        else
            return issueTabPanel.getActions(request.issue(), request.remoteUser());
    }
}
