package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelInvoker;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the Activity block section on View Issue page.
 * Expensive one so it cached per request/per user/per issue
 *
 * @since v4.4
 */
public class ActivityBlockViewIssueContextProvider implements CacheableContextProvider
{
    private static final String ORDER_DESC = "desc";
    protected final Logger log = Logger.getLogger(ActivityBlockViewIssueContextProvider.class);

    private final PluginAccessor pluginAccessor;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory requestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final IssueTabPanelInvoker issueTabPanelInvoker;

    public ActivityBlockViewIssueContextProvider(PluginAccessor pluginAccessor, JiraAuthenticationContext authenticationContext,
            VelocityRequestContextFactory requestContextFactory, ApplicationProperties applicationProperties, IssueTabPanelInvoker issueTabPanelInvoker)
    {
        this.pluginAccessor = pluginAccessor;
        this.authenticationContext = authenticationContext;
        this.requestContextFactory = requestContextFactory;
        this.applicationProperties = applicationProperties;
        this.issueTabPanelInvoker = issueTabPanelInvoker;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        boolean isAsynchronous = (Boolean) consumeParam(context, "isAsynchronousRequest");
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();


        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final ArrayList<IssueTabPanelModuleDescriptor> tabPanelDescriptors = getTabPanels(issue, user);

        String currentPageKey = getCurrentTabPanel(tabPanelDescriptors);
        IssueTabPanelModuleDescriptor currentTabPanelModuleDescriptor = getTabPanelModuleDescriptor(currentPageKey, tabPanelDescriptors);

        final String actionSortOrder = getActionSortOrder();

        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();

        boolean showAllFlag = (requestContext.getRequestParameter("showAll") != null);
        String focusId = requestContext.getRequestParameter("focusedCommentId");
        GetActionsRequest getActionsRequest = new GetActionsRequest(issue, user, isAsynchronous, showAllFlag, focusId);
        List<IssueAction> issueActions = issueTabPanelInvoker.invokeGetActions(getActionsRequest, currentTabPanelModuleDescriptor);

        final List<IssueAction> actions = Lists.newArrayList(issueActions);
        sort(actions, actionSortOrder);

        paramsBuilder.add("tabPanels", tabPanelDescriptors);
        paramsBuilder.add("hasPanels", !tabPanelDescriptors.isEmpty());
        paramsBuilder.add("currentPageKey", currentPageKey);
        paramsBuilder.add("currentTab", currentTabPanelModuleDescriptor);
        paramsBuilder.add("isSortable", currentTabPanelModuleDescriptor.isSortable() && actions != null && actions.size() > 1);
        paramsBuilder.add("actions", actions);
        paramsBuilder.add("hasActions", actions != null && !actions.isEmpty());
        paramsBuilder.add("actionError", actions == null);
        paramsBuilder.add("actionsSortOrder", actionSortOrder);


        return paramsBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());

    }

    private void sort(List<IssueAction> actions, String actionSortOrder)
    {
        if (ORDER_DESC.equals(actionSortOrder))
        {
            Collections.reverse(actions);
        }
    }

    private String getActionSortOrder()
    {
        final String defaultOrder = applicationProperties.getDefaultBackedString(APKeys.JIRA_ISSUE_ACTIONS_ORDER);

        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        String actionOrder = requestContext.getRequestParameter("actionOrder");
        final VelocityRequestSession session = requestContext.getSession();
        if (StringUtils.isNotBlank(actionOrder))
        {
            if (!actionOrder.equals(defaultOrder))
            {
                session.setAttribute(SessionKeys.VIEWISSUE_ACTION_ORDER, actionOrder);
                return actionOrder;
            }

            session.removeAttribute(SessionKeys.VIEWISSUE_ACTION_ORDER);
            return defaultOrder;
        }

        actionOrder = (String) session.getAttribute(SessionKeys.VIEWISSUE_ACTION_ORDER);

        if (StringUtils.isNotBlank(actionOrder))
        {
            return actionOrder;
        }

        return defaultOrder;
    }


    private String getCurrentTabPanel(List<IssueTabPanelModuleDescriptor> tabPanels)
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        String page = requestContext.getRequestParameter("page");
        final VelocityRequestSession session = requestContext.getSession();
        if (StringUtils.isNotBlank(page) && isTabPanelVisisble(page, tabPanels))
        {
            session.setAttribute(SessionKeys.VIEWISSUE_PAGE, page);
            return page;
        }
        page = (String) session.getAttribute(SessionKeys.VIEWISSUE_PAGE);
        if (StringUtils.isNotBlank(page) && isTabPanelVisisble(page, tabPanels))
        {
            return page;
        }


        return getDefaultPage(tabPanels);
    }

    private boolean isTabPanelVisisble(String tabPanelKey, List<IssueTabPanelModuleDescriptor> tabPanels)
    {
        return getTabPanelModuleDescriptor(tabPanelKey, tabPanels) != null;
    }

    private IssueTabPanelModuleDescriptor getTabPanelModuleDescriptor(String key, List<IssueTabPanelModuleDescriptor> tabPanels)
    {
        for (IssueTabPanelModuleDescriptor tabPanel : tabPanels)
        {
            if (tabPanel.getCompleteKey().equals(key))
            {
                return tabPanel;
            }
        }

        return null;

    }

    private ArrayList<IssueTabPanelModuleDescriptor> getTabPanels(Issue issue, User user)
    {
        final List<IssueTabPanelModuleDescriptor> allTabPanels = pluginAccessor.getEnabledModuleDescriptorsByClass(IssueTabPanelModuleDescriptor.class);
        final ArrayList<IssueTabPanelModuleDescriptor> filteredTabPanels = Lists.newArrayListWithCapacity(allTabPanels.size());
        for (IssueTabPanelModuleDescriptor descriptor : allTabPanels)
        {
            if (issueTabPanelInvoker.invokeShowPanel(new ShowPanelRequest(issue, user), descriptor))
            {
                filteredTabPanels.add(descriptor);
            }
        }
        Collections.sort(filteredTabPanels, ModuleDescriptorComparator.COMPARATOR);

        return filteredTabPanels;
    }

    /**
     * @param issueTabPanels a List<IssueTabPanelModuleDescriptor>
     * @return the default page (as specified by the {@link IssueTabPanelModuleDescriptor})
     * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor#isDefault()
     */
    private String getDefaultPage(List<IssueTabPanelModuleDescriptor> issueTabPanels)
    {
        for (final IssueTabPanelModuleDescriptor descriptor : issueTabPanels)
        {
            if (descriptor.isDefault())
            {
                return descriptor.getCompleteKey();
            }
        }

        // if no defaults are found, then just return the first one (if there are any)
        if (!issueTabPanels.isEmpty())
        {
            return issueTabPanels.get(0).getCompleteKey();
        }
        else
        {
            return null;
        }
    }

    /**
     * Helper method used to consumer context parameters that we don't want to propagate into the issue tab panel
     * velocity context.
     *
     * @param context the context map
     * @param paramName the name of the parameter
     * @return <code>context</code> minus the entry with key=<code>paramName</code>
     */
    private Object consumeParam(Map<String, Object> context, String paramName)
    {
        Object result = context.get(paramName);
        context.remove(paramName);

        return result;
    }
}
