package com.atlassian.jira.web.action.issue;

import com.atlassian.fugue.Option;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.web.action.AjaxHeaders.isPjaxRequest;

/**
 * @since v5.0
 */
public class ViewSubtaskFragmentAction extends AbstractIssueSelectAction
{
    private static final String SUBTASK_PANEL_KEY = "com.atlassian.jira.jira-view-issue-plugin:view-subtasks";
    private final WebInterfaceManager webInterfaceManager;
    private Map<String, Object> webPanelParams;

    public ViewSubtaskFragmentAction(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;

    }

    @VisibleForTesting
    protected Map<String,Object> getWebPanelContext()
    {
        if (webPanelParams == null)
        {
            final Issue issue = getIssueObject();

            webPanelParams = new HashMap<String, Object>();
            webPanelParams.put("user", getLoggedInUser());
            webPanelParams.put("project", issue.getProjectObject());
            webPanelParams.put("issue", issue);
            webPanelParams.put("action", this);
            final JiraHelper jiraHelper = new JiraHelper(request, issue.getProjectObject(), webPanelParams);
            webPanelParams.put("helper", jiraHelper);
            webPanelParams.put("isAsynchronousRequest", isPjaxRequest(request));

        }
        return webPanelParams;
    }

    public String getHtml()
    {
        final Map<String, Object> webPanelContext = getWebPanelContext();
        final List<WebPanelModuleDescriptor> webPanels = webInterfaceManager.getDisplayableWebPanelDescriptors("atl.jira.view.issue.left.context", webPanelContext);

        for (final WebPanelModuleDescriptor webPanel : webPanels)
        {
            final Option<String> moduleOutput = SafePluginPointAccess.call(new Callable<String>() {
                @Override
                public String call() throws Exception
                {
                    if (SUBTASK_PANEL_KEY.equals(webPanel.getCompleteKey()))
                    {
                        final WebPanel module = webPanel.getModule();
                        return module != null ? module.getHtml(webPanelContext) : StringUtils.EMPTY;
                    }
                    return null;
                }
            });
            if (moduleOutput.isDefined())
            {
                return moduleOutput.get();
            }
        }
        return StringUtils.EMPTY;

    }
}
