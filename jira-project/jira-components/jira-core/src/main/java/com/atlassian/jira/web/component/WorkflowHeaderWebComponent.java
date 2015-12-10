package com.atlassian.jira.web.component;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.template.TemplateSources;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class WorkflowHeaderWebComponent
{
    private final WebInterfaceManager webInterfaceManager;
    private final ProjectWorkflowSchemeHelper helper;
    private final VelocityParamFactory velocityParamFactory;

    public WorkflowHeaderWebComponent(WebInterfaceManager webInterfaceManager, ProjectWorkflowSchemeHelper helper,
            VelocityParamFactory velocityParamFactory)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.helper = helper;
        this.velocityParamFactory = velocityParamFactory;
    }

    public String getHtml(JiraWorkflow jiraWorkflow, String helpPath, Long projectId)
    {
        WebPanel first = Iterables.getFirst(webInterfaceManager.getWebPanels("workflow.header"), null);
        if (first != null)
        {
            final Map<String, Object> context = Maps.newHashMap();
            context.put("jiraWorkflow", jiraWorkflow);
            context.put("active", true);
            context.put("helpUtil", HelpUtil.getInstance());
            context.put("sharedProjects", helper.getProjectsForWorkflow(jiraWorkflow.getName()));
            context.put("projectId", projectId);
            context.put("issueTypeId", getIssueTypeFromRequest());
            context.put("helpPath", HelpUtil.getInstance().getHelpPath(helpPath));
            context.put("displayUpdatedDate", shouldDisplayUpdatedDate());

            return first.getHtml(velocityParamFactory.getDefaultVelocityParams(context));
        }
        else
        {
            return "";
        }
    }

    public String getHtml(JiraWorkflow jiraWorkflow, String helpPath)
    {
        return getHtml(jiraWorkflow, helpPath, null);
    }

    public String getLinksHtml(JiraWorkflow workflow, Long projectId, String viewMode, boolean editable)
    {
        Map<String, Object> context = Maps.newHashMap();
        context.put("workflow", workflow);
        context.put("issueTypeId", getIssueTypeFromRequest());
        context.put("project", projectId);
        context.put("wfName", JiraUrlCodec.encode(workflow.getName()));
        context.put("wfMode", JiraUrlCodec.encode(workflow.getMode()));
        context.put("viewMode", viewMode);
        context.put("editable", editable);

        VelocityTemplatingEngine velocityEngine = ComponentAccessor.getComponent(VelocityTemplatingEngine.class);

        return velocityEngine.render(TemplateSources.file("/templates/jira/admin/view-workflow-links.vm"))
                .applying(velocityParamFactory.getDefaultVelocityParams(context))
                .asHtml();
    }

    //CAS-233: We are getting this from the request so that we don't change the signature of the public methods
    // of this class as this would make the workflow designer dependent on a particular release of JIRA.
    private static String getIssueTypeFromRequest()
    {
        final HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null)
        {
            final String issueType = request.getParameter("issueType");
            if (issueType != null)
            {
                return JiraUrlCodec.encode(issueType);
            }
        }
        return null;
    }

    /**
     * The standard workflow designer displays the last updated date within itself. Therefore is no need to display the
     * last updated date banner when using the standard workflow designer.
     *
     * @return whether the last updated date banner should be displayed in the header
     */
    private static boolean shouldDisplayUpdatedDate()
    {
        boolean shouldDisplayUpdatedDate = false;
        final HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null)
        {
            final String requestURI = request.getRequestURI();
            if (requestURI != null)
            {
                shouldDisplayUpdatedDate = isUriForClassicOrTextMode(requestURI);
            }
        }
        return shouldDisplayUpdatedDate;
    }

    private static boolean isUriForClassicOrTextMode(String uri)
    {
        return !uri.endsWith("/WorkflowDesigner.jspa");
    }
}
