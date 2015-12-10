package com.atlassian.jira.bc.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static java.util.Collections.singletonList;

/**
 * This class implements constant-related business cases.
 *
 * @since v4.2
 */
public class DefaultConstantsService implements ConstantsService
{
    private final I18nHelper.BeanFactory i18n;
    private final ConstantsManager constantsMgr;
    private final PermissionManager permissionMgr;
    private final WorkflowSchemeManager workflowSchemeMgr;
    private final WorkflowManager workflowMgr;
    private final IssueTypeSchemeManager issueTypeSchemeMgr;
    private final StatusCategoryManager statusCategoryManager;

    /**
     * Creates a new DefaultConstantsService.
     *
     * @param i18n a I18nHelper
     * @param constantsManager a ConstantsManager
     * @param permissionManager a PermissionManager
     * @param workflowSchemeMgr a WorkflowSchemeManager
     * @param workflowMgr a WorkflowManager
     * @param issueTypeSchemeMgr an IssueTypeSchemeManager
     * @param statusCategoryManager a StatusCategoryManager
     */
    public DefaultConstantsService(I18nHelper.BeanFactory i18n, ConstantsManager constantsManager,
            PermissionManager permissionManager, WorkflowSchemeManager workflowSchemeMgr, WorkflowManager workflowMgr,
            IssueTypeSchemeManager issueTypeSchemeMgr, final StatusCategoryManager statusCategoryManager)
    {
        this.constantsMgr = constantsManager;
        this.permissionMgr = permissionManager;
        this.workflowSchemeMgr = workflowSchemeMgr;
        this.workflowMgr = workflowMgr;
        this.i18n = i18n;
        this.issueTypeSchemeMgr = issueTypeSchemeMgr;
        this.statusCategoryManager = statusCategoryManager;
    }

    public ServiceOutcome<Status> getStatusById(User user, String statusId)
    {
        Status status = constantsMgr.getStatusObject(statusId);
        if (status != null && isStatusVisible(user, status))
        {
            return ServiceOutcomeImpl.ok(status);
        }
        return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.not.found", statusId));
    }

    @Override
    public ServiceOutcome<Status> getStatusByName(User user, String statusName)
    {
        Status status = constantsMgr.getStatusByName(statusName);
        if (status != null && isStatusVisible(user, status))
        {
            return ServiceOutcomeImpl.ok(status);
        }
        return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.not.found", statusName));
    }

    @Override
    public ServiceOutcome<Status> getStatusByTranslatedName(User user, String statusName)
    {
        Status status = constantsMgr.getStatusByTranslatedName(statusName);
        if (status != null && isStatusVisible(user, status))
        {
            return ServiceOutcomeImpl.ok(status);
        }
        return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.not.found", statusName));
    }

    /**
     * Returns whether or not this user can see any project with any workflow that
     * matches the specified status.
     * <p/>
     * <strong>WARNING</strong>: It is not suitable to call this method in a loop.
     * Construct an {@link IsStatusVisible} predicate for the user, instead.
     *
     * @param user the user looking for the status
     * @param status the status of interest
     * @return whether or not this user can see any project with any workflow that
     *         matches the specified status
     */
    private boolean isStatusVisible(User user, Status status)
    {
        return new IsStatusVisible(user, permissionMgr, workflowMgr, workflowSchemeMgr).apply(status);
    }

    public ServiceOutcome<Collection<Status>> getAllStatuses(User user)
    {
        return ServiceOutcomeImpl.ok(Collections2.filter(constantsMgr.getStatusObjects(), new IsStatusVisible(user, permissionMgr, workflowMgr, workflowSchemeMgr)));
    }

    public ServiceOutcome<Collection<IssueType>> getAllIssueTypes(User user)
    {
        Set<IssueType> visibleIssueTypes = new HashSet<IssueType>();
        Collection<Project> visibleProjects = permissionMgr.getProjectObjects(Permissions.BROWSE, user);
        for (Project project : visibleProjects)
        {
            for (IssueType visibleType : issueTypeSchemeMgr.getIssueTypesForProject(project))
            {
                visibleIssueTypes.add(visibleType);
            }
        }
        return ServiceOutcomeImpl.ok((Collection<IssueType>) visibleIssueTypes);
    }

    public ServiceOutcome<IssueType> getIssueTypeById(User user, String issueTypeId)
    {
        IssueType issueType = constantsMgr.getIssueTypeObject(issueTypeId);
        if (issueType != null)
        {
            // check if the issue type is visible via a project. needs improvement...
            Collection<Project> visibleProjects = permissionMgr.getProjectObjects(Permissions.BROWSE, user);
            for (Project project : visibleProjects)
            {
                for (IssueType visibleType : issueTypeSchemeMgr.getIssueTypesForProject(project))
                {
                    if (issueTypeId.equals(visibleType.getId()))
                    {
                        return ServiceOutcomeImpl.ok(issueType);
                    }
                }
            }
        }

        return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.issuetype.not.found", issueTypeId));
    }



    /**
     * A caching filter that avoids redundant evaluation of workflows while determining
     * which statuses a user can see.  Any status it has already seen is remembered.
     * When asked about a status it has not seen, it checks for it in the next workflow.
     * If it runs out of workflows, then it scans projects until it finds at least one
     * workflow it hasn't seen before.  Once it runs out of projects, the interation
     * tests fail with very little overhead.
     * <p/>
     * The point is that each workflow is visited at most once, regardless of how many
     * workflows you have or how they are distributed among however many projects.
     * <p/>
     * This predicate is specific to the user and retains a significant amount of state
     * in order to filter as efficiently as possible, so it is not appropriate to retain
     * it across requests.  It may be overkill for a single status, but its ability to
     * avoid processing the same workflow multiple times likely makes this worthwhile.
     */
    static class IsStatusVisible implements Predicate<Status>
    {
        final Set<String> seenStatusIds = new HashSet<String>();
        final HashSet<String> seenWorkflowNames = new HashSet<String>();
        final LinkedList<String> pendingWorkflowNames = new LinkedList<String>();
        final Iterator<Project> projects;
        private final PermissionManager permissionManager;
        private final WorkflowManager workflowManager;
        private final WorkflowFetcher workflowFetcher;

        static interface WorkflowFetcher
        {
            List<String> getWorkflowNames(Project project);
        }

        IsStatusVisible(final User user, PermissionManager permissionManager, WorkflowManager workflowManager, final WorkflowSchemeManager workflowSchemeManager)
        {
            this(user, permissionManager, workflowManager, new WorkflowFetcher()
            {
                public List<String> getWorkflowNames(Project project)
                {
                    try
                    {
                        GenericValue workflowScheme = workflowSchemeManager.getWorkflowScheme(project.getGenericValue());
                        if (workflowScheme == null)
                        {
                            return singletonList(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
                        }

                        List<String> workflowNames = new ArrayList<String>(128);
                        GenericValue defaultEntity = workflowSchemeManager.getDefaultEntity(workflowScheme);
                        if (defaultEntity != null)
                        {
                            workflowNames.add(defaultEntity.getString("workflow"));
                        }
                        for (GenericValue entity : workflowSchemeManager.getNonDefaultEntities(workflowScheme))
                        {
                            workflowNames.add(entity.getString("workflow"));
                        }
                        return workflowNames;
                    }
                    catch (GenericEntityException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        IsStatusVisible(final User user, PermissionManager permissionManager, WorkflowManager workflowManager, WorkflowFetcher workflowFetcher)
        {
            this.permissionManager = permissionManager;
            this.workflowManager = workflowManager;
            projects = this.permissionManager.getProjectObjects(Permissions.BROWSE, user).iterator();
            this.workflowFetcher = workflowFetcher;
        }

        @Override
        public boolean apply(final Status input)
        {
            do
            {
                if (seenStatusIds.contains(input.getId()))
                {
                    return true;
                }
            }
            while (findMoreStatuses());

            return false;
        }

        private boolean findMoreStatuses()
        {
            boolean changed = false;

            while (!changed && hasMoreWorkflows())
            {
                final JiraWorkflow workflow = workflowManager.getWorkflow(pendingWorkflowNames.removeFirst());

                @SuppressWarnings ("unchecked")
                final List<StepDescriptor> steps = workflow.getDescriptor().getSteps();
                for (StepDescriptor step : steps)
                {
                    final String linkedStatusId = (String)step.getMetaAttributes().get("jira.status.id");
                    changed |= seenStatusIds.add(linkedStatusId);
                }
            }

            return changed;
        }

        private boolean hasMoreWorkflows()
        {
            while (pendingWorkflowNames.isEmpty())
            {
                if (!projects.hasNext())
                {
                    return false;
                }
                for (String workflowName : workflowFetcher.getWorkflowNames(projects.next()))
                {
                    if (seenWorkflowNames.add(workflowName))
                    {
                        pendingWorkflowNames.addLast(workflowName);
                    }
                }
            }
            return true;
        }
    }

    public ServiceOutcome<Collection<StatusCategory>> getAllStatusCategories(User user)
    {
        if (!isStatusAsLozengeEnabled())
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.any"));
        }

        return ServiceOutcomeImpl.<Collection<StatusCategory>>ok(statusCategoryManager.getStatusCategories());
    }

    public ServiceOutcome<Collection<StatusCategory>> getUserVisibleStatusCategories(User user)
    {
        if (!isStatusAsLozengeEnabled())
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.any"));
        }

        return ServiceOutcomeImpl.<Collection<StatusCategory>>ok(statusCategoryManager.getUserVisibleStatusCategories());
    }

    public ServiceOutcome<StatusCategory> getStatusCategoryById(User user, String id)
    {
        final Long longId;

        if (!isStatusAsLozengeEnabled())
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.id", id));
        }

        try
        {
            longId = Long.parseLong(id);
        }
        catch (NumberFormatException e)
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.id", id));
        }

        final StatusCategory statusCategory = statusCategoryManager.getStatusCategory(longId);

        if (statusCategory != null)
        {
            return ServiceOutcomeImpl.ok(statusCategory);
        }
        else
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.id", id));
        }
    }

    public ServiceOutcome<StatusCategory> getStatusCategoryByKey(User user, String key)
    {
        if (!isStatusAsLozengeEnabled())
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.key", key));
        }

        final StatusCategory statusCategory = statusCategoryManager.getStatusCategoryByKey(key);

        if (statusCategory != null)
        {
            return ServiceOutcomeImpl.ok(statusCategory);
        }
        else
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.key", key));
        }
    }

    public ServiceOutcome<StatusCategory> getDefaultStatusCategory(User user)
    {
        final StatusCategory statusCategory = statusCategoryManager.getDefaultStatusCategory();

        if (statusCategory != null)
        {
            return ServiceOutcomeImpl.ok(statusCategory);
        }
        else
        {
            return ServiceOutcomeImpl.error(i18n.getInstance(user).getText("constants.service.status.category.not.found.default"));
        }
    }

    public boolean isStatusAsLozengeEnabled()
    {
        return statusCategoryManager.isStatusAsLozengeEnabled();
    }
}
