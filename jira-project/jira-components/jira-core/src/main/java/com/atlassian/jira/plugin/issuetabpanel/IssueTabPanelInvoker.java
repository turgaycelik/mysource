package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.Internal;

import java.util.List;

/**
 * This class is used to safely call into {@link IssueTabPanel} and {@link IssueTabPanel2} implementations. The methods
 * in this class do not propagate any exception that may be thrown. Instead, they log exception and display an error
 * message where possible.
 *
 * @since v5.0
 */
@Internal
public interface IssueTabPanelInvoker
{
    /**
     * Calls the <code>showPanel</code> method in the IssueTabPanel. If the <code>showPanel</code> implementation throws
     * any exceptions this method logs the exception and returns a reply containing <code>true</code>.
     *
     *
     * @param request a ShowPanelRequest
     * @param descriptor the tab panel's module descriptor
     * @return a ShowPanelReply
     */
    boolean invokeShowPanel(ShowPanelRequest request, IssueTabPanelModuleDescriptor descriptor);

    /**
     * Calls the <code>getActions</code> method on the IssueTabPanel. If the <code>getActions</code> implementation
     * throws any exceptions then this method logs the exception and returns a reply containing a single action that
     * renders an error message..
     *
     * @param request a GetActionsRequest
     * @param descriptor the tab panel's module descriptor
     * @return a GetActionsReply
     */
    List<IssueAction> invokeGetActions(GetActionsRequest request, IssueTabPanelModuleDescriptor descriptor);
}
