package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * This is the SPI for the <b><code>issue-tabpanel</code></b> JIRA module type. Issue tab panels allow plugins to
 * display their content in the JIRA view issue screen. Examples of issue tab panels include the "Comment", "Work Log",
 * and the "Source" tabs.
 * <p/>
 * Plugins that target JIRA 5.0 and onwards probably want to implement {@link IssueTabPanel2} or {@link IssueTabPanel3} instead, especially
 * if they need to support AJAX loading of the tab contents.
 * <p/>
 * This plugin type is <a href="https://developer.atlassian.com/display/JIRADEV/Issue+Tab+Panel+Plugin+Module">documented
 * online</a>.
 *
 * @see IssueTabPanel3
 */
@PublicSpi
public interface IssueTabPanel
{
    void init(IssueTabPanelModuleDescriptor descriptor);

    /**
     * Return a list of issue actions in the order that you want them to be displayed. <p> Note that for the 'all' tab,
     * they will be displayed in order according to the value returned by {@link IssueAction#getTimePerformed()} </p>
     * <p/>
     * the user that is viewing the tab can affect which objects are shown, as well as which operations are available on
     * each.
     *
     * @param issue The Issue that the objects belong to.
     * @param remoteUser The user viewing this tab.
     * @return A List of {@link IssueAction} objects.
     */
    List<IssueAction> getActions(Issue issue, User remoteUser);

    /**
     * Whether or not to show this tab panel to the given User for the given Issue.
     *
     * @param issue The Issue.
     * @param remoteUser The viewing user.
     * @return <code>true</code> if we should show this tab panel to the given User for the given Issue.
     */
    boolean showPanel(Issue issue, User remoteUser);
}
