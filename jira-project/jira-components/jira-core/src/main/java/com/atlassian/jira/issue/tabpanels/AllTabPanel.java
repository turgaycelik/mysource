package com.atlassian.jira.issue.tabpanels;

import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel2;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsReply;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelInvoker;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelReply;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class AllTabPanel extends AbstractIssueTabPanel2
{
    private final PluginAccessor pluginAccessor;
    private final IssueTabPanelInvoker issueTabPanelInvoker;

    public AllTabPanel(PluginAccessor pluginAccessor, IssueTabPanelInvoker issueTabPanelInvoker)
    {
        this.pluginAccessor = pluginAccessor;
        this.issueTabPanelInvoker = issueTabPanelInvoker;
    }

    @Override
    public GetActionsReply getActions(GetActionsRequest request)
    {
        List<IssueAction> allActions = Lists.newArrayList();

        List<IssueTabPanelModuleDescriptor> showTabPanels = getVisibleTabPanels(request);
        for (IssueTabPanelModuleDescriptor issueTabPanel : showTabPanels)
        {
            GetActionsRequest showAllActionsRequest = new GetActionsRequest(request.issue(), request.remoteUser(), request.isAsynchronous(), true, null);
            List<IssueAction> actions = issueTabPanelInvoker.invokeGetActions(showAllActionsRequest, issueTabPanel);

            if (actions != null)
            {
                for (IssueAction action : actions)
                {
                    // See if the returned actions should be actually shown on the all tab
                    if (action.isDisplayActionAllTab())
                    {
                        allActions.add(action);
                    }
                }
            }
        }

        // This is a bit of a hack to indicate that there is nothing to display in the all tab panel
        if (allActions.isEmpty())
        {
            return GetActionsReply.create(new GenericMessageAction(descriptor().getI18nBean().getText("viewissue.noactions")));
        }

        Collections.sort(allActions, IssueActionComparator.COMPARATOR);
        return GetActionsReply.create(allActions);
    }

    private List<IssueTabPanelModuleDescriptor> getVisibleTabPanels(GetActionsRequest request)
    {
        final List<IssueTabPanelModuleDescriptor> descriptors =
                pluginAccessor.getEnabledModuleDescriptorsByClass(IssueTabPanelModuleDescriptor.class);

        List<IssueTabPanelModuleDescriptor> visibleTabPanels = Lists.newArrayListWithCapacity(descriptors.size());
        for (IssueTabPanelModuleDescriptor tabPanelDescriptor : descriptors)
        {
            boolean showPanel = issueTabPanelInvoker.invokeShowPanel(new ShowPanelRequest(request.issue(), request.remoteUser()), tabPanelDescriptor);
            if (showPanel && !AllTabPanel.class.isAssignableFrom(tabPanelDescriptor.getModuleClass()))
            {
                visibleTabPanels.add(tabPanelDescriptor);
            }
        }

        return visibleTabPanels;
    }

    @Override
    public ShowPanelReply showPanel(ShowPanelRequest request)
    {
        return ShowPanelReply.create(true);
    }
}
