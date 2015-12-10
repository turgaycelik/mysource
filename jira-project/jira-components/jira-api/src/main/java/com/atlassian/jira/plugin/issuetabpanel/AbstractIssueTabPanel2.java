package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * Convenience class for implementors of {@link IssueTabPanel2}.
 */
@PublicSpi
public abstract class AbstractIssueTabPanel2 implements IssueTabPanel2
{
    /**
     * The descriptor from the <code>atlassian-plugins.xml</code> file.
     */
    private IssueTabPanelModuleDescriptor descriptor;

    /**
     * Initialises this AbstractIssueTabPanel2's descriptor and calls the initialisation hook in the subclass.
     *
     * @param descriptor an IssueTabPanelModuleDescriptor
     */
    public final void init(IssueTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
        init();
    }

    /**
     * Bridge method that delegates to the new {@link IssueTabPanel2#showPanel(ShowPanelRequest)} method.
     *
     * @param issue The Issue.
     * @param remoteUser The viewing user.
     * @return <code>true</code> if we should show this tab panel to the given User for the given Issue.
     */
    @Override
    public final boolean showPanel(Issue issue, User remoteUser)
    {
        return showPanel(new ShowPanelRequest(issue, remoteUser)).isShow();
    }

    /**
     * Bridge method that delegates to the new {@link IssueTabPanel2#getActions(GetActionsRequest)} method.
     *
     * @param issue The Issue.
     * @param remoteUser The viewing user.
     * @return a List<IssueAction>
     */
    @Override
    public final List<IssueAction> getActions(Issue issue, User remoteUser)
    {
        return getActions(new GetActionsRequest(issue, remoteUser, false, false, null)).actions();
    }

    /**
     * @return the IssueTabPanelModuleDescriptor for this IssueTabPanel
     */
    protected final IssueTabPanelModuleDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * Subclasses of AbstractIssueTabPanel2 may implement this method in order to perform initialisation work.
     */
    protected void init()
    {
        // do nothing
    }
}
