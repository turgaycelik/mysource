package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;

/**
 * This is the v2 SPI for the <b><code>issue-tabpanel</code></b> JIRA module type. Issue tab panels allow plugins to
 * display their content in the JIRA view issue screen. Examples of issue tab panels include the "Comment", "Work Log",
 * and the "Source" tabs.
 * <p/>
 * Note that implementations of this interface <b>must be able to work when either the legacy or the new
 * <code>getActions</code> method is called</b>, since there may remain callers that use the legacy method. The
 * recommended way to do that is to extend the {@link AbstractIssueTabPanel2} class and override the hooks that are
 * available in that class.
 * <p/>
 * This plugin type is <a href="https://developer.atlassian.com/display/JIRADEV/Issue+Tab+Panel+Plugin+Module">documented
 * online</a>.
 *
 * @since v5.0
 *
 * @see IssueTabPanel3
 */
@PublicSpi
public interface IssueTabPanel2 extends IssueTabPanel
{
    /**
     * Indicates whether this tab should be shown on a given issue.
     *
     * @param request a ShowPanelRequest
     * @return a ShowPanelRequest indicating whether to show the panel or not
     */
    ShowPanelReply showPanel(ShowPanelRequest request);

    /**
     * Returns a list of issue actions in the order that you want them to be displayed. This method will only be called
     * if <code>showPanel</code> returned true for a given issue and user. The <code>request</code> parameter contains
     * the user that is viewing the tab, information as to whether the tab is being loaded using an AJAX request, and so
     * on.
     * <p/>
     * Example implementation:
     * <pre>
     * &#64;Override
     * public GetActionsReply getActions(GetActionsRequest request)
     * {
     *     if (!request.isAsynchronous())
     *     {
     *         return GetActionsReply.create(new AjaxTabPanelAction(request));
     *     }
     *
     *     return GetActionsReply.create(getActionsList(request));
     * }
     * </pre>
     * <p/>
     * Note that for the 'all' tab, the returned actions will be displayed in order according to the value returned by
     * <code>IssueAction.getTimePerformed()</code>.
     *
     * @param request a GetActionsRequest
     * @return a GetActionsReply containing the actions to display
     */
    GetActionsReply getActions(GetActionsRequest request);
}
