package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comparator.ConstantsComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;

import org.apache.log4j.Logger;

import webwork.action.Action;

/**
 * A search renderer for the status.
 *
 * @since v4.0
 */
public class StatusSearchRenderer extends IssueConstantsSearchRenderer<Status>
{
    static final Logger log = Logger.getLogger(IssueSearcher.class);

    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;

    public StatusSearchRenderer(String searcherNameKey, final ConstantsManager constantsManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final FieldVisibilityManager fieldVisibilityManager, final WorkflowManager workflowManager,
            final ProjectManager projectManager)
    {
        super(SystemSearchConstants.forStatus(), searcherNameKey, constantsManager, velocityRequestContextFactory,
                applicationProperties, templatingEngine, fieldVisibilityManager);
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
    }

    public Collection<Status> getSelectListOptions(SearchContext searchContext)
    {
        Set<Status> uniqueStatus = new TreeSet<Status>(ConstantsComparator.COMPARATOR);
        List<Long> projectIds = searchContext.getProjectIds();
        if (projectIds == null || projectIds.isEmpty())
        {
            try
            {
                for (JiraWorkflow jiraWorkflow : workflowManager.getActiveWorkflows())
                {
                    uniqueStatus.addAll(jiraWorkflow.getLinkedStatusObjects());
                }
            }
            catch (WorkflowException e)
            {
                log.warn("Workflow exception occurred trying to get a workflow statuses. Returning all statuses", e);
                return constantsManager.getStatusObjects();
            }
        }
        else
        {

            List<String> issueTypeIds = searchContext.getIssueTypeIds();

            // If no issue type ids, then we want it all!
            if (issueTypeIds == null || issueTypeIds.isEmpty())
            {
                issueTypeIds = constantsManager.getAllIssueTypeIds();
            }

            for (Long projectId : projectIds)
            {
                if (projectManager.getProjectObj(projectId) != null)
                {
                    for (String issueTypeId : issueTypeIds)
                    {
                        try
                        {
                            JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
                            List<Status> linkedStatuses = workflow.getLinkedStatusObjects();
                            uniqueStatus.addAll(linkedStatuses);
                        }
                        catch (WorkflowException e)
                        {
                            log.warn("Workflow exception occurred trying to get a workflow with issuetype " + issueTypeId + " and projectId " + projectId, e);
                        }
                    }
                }
                else
                {
                    log.debug("Unable to find project with id " + projectId + " when trying to retrieve available statuses");
                }
            }
        }
        return uniqueStatus;
    }


    @Override
    protected Collection<Status> getAllSelectListOptions()
    {
        return constantsManager.getStatusObjects();
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User searcher, final SearchContext searchContext,
            final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder,
            final Map displayParameters, final Action action)
    {
        Map<String, Object> params = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);

        final StatusCategoryManager statusCategoryManager = ComponentAccessor.getComponent(StatusCategoryManager.class);
        if (statusCategoryManager.isStatusAsLozengeEnabled()) {
            params.put("simpleStatusJsonHelper", new SimpleStatusJsonHelper());
        }

        return params;
    }

    public static class SimpleStatusJsonHelper
    {
        public String convertToJson(@Nonnull SimpleStatus status)
        {
            final JSONObject json = new JSONObject();
            final StatusCategory category = status.getStatusCategory();

            try
            {
                json.put("id", status.getId());
                json.put("name", status.getName());
                json.put("description", status.getDescription());
                json.put("iconUrl", status.getIconUrl());

                if (null == category) {
                    json.put("statusCategory", JSONObject.NULL);
                } else {
                    final JSONObject jsonCategory = new JSONObject();

                    jsonCategory.put("id", category.getId());
                    jsonCategory.put("key", category.getKey());
                    jsonCategory.put("colorName", category.getColorName());

                    json.put("statusCategory", jsonCategory);
                }
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }

            return json.toString();
        }
    }
}
