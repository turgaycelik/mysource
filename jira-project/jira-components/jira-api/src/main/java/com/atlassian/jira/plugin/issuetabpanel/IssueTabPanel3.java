package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;

import java.util.List;

/**
 * This is the SPI for the <b><code>issue-tabpanel</code></b> JIRA module type. Issue tab panels allow plugins to
 * display their content in the JIRA view issue screen. Examples of issue tab panels include the "Comment", "Work Log",
 * and the "Source" tabs.
 * <p/>
 * This plugin type is <a href="https://developer.atlassian.com/display/JIRADEV/Issue+Tab+Panel+Plugin+Module">documented
 * online</a>.
 *
 * @since v6.0
 * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel
 * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel2
 */
@PublicSpi
public interface IssueTabPanel3
{
    void init(IssueTabPanelModuleDescriptor descriptor);

    /**
     * Indicates whether this tab should be shown on a given issue.
     *
     * @param request a ShowPanelRequest
     * @return a ShowPanelRequest indicating whether to show the panel or not
     */
    boolean showPanel(ShowPanelRequest request);

    /**
     * Returns a list of issue actions in the order that you want them to be displayed.
     * This method will only be called if <code>showPanel</code> returned true for a given context.
     * The <code>request</code> parameter contains the user that is viewing the tab, information as to whether the tab
     * is being loaded using an AJAX request, and so on.
     * <p/>
     * Note that for the 'all' tab, the returned actions will be displayed in order according to the value returned by
     * <code>IssueAction.getTimePerformed()</code>.
     *
     *
     * @param request a GetActionsRequest
     * @return the actions to display
     */
    List<IssueAction> getActions(GetActionsRequest request);
}
