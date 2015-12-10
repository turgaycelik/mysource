package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.DraftWorkflowCreatedEvent;
import com.atlassian.jira.event.DraftWorkflowDeletedEvent;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.jira.event.WorkflowCopiedEvent;
import com.atlassian.jira.event.WorkflowCreatedEvent;
import com.atlassian.jira.event.WorkflowDeletedEvent;
import com.atlassian.jira.event.WorkflowRenamedEvent;
import com.atlassian.jira.event.WorkflowUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;

import com.google.common.collect.Iterables;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.InvalidEntryStateException;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.InvalidRoleException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.basic.BasicWorkflow;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import webwork.action.ActionContext;

import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang.StringUtils.stripToNull;

public class OSWorkflowManager implements WorkflowManager
{
    private static final Logger log = Logger.getLogger(OSWorkflowManager.class);

    private volatile Configuration configuration;
    private final DraftWorkflowStore draftWorkflowStore;
    private final EventPublisher eventPublisher;
    private WorkflowsRepository workflowsRepository;
    private WorkflowCopyNameFactory workflowCopyNameFactory;
    private final JiraAuthenticationContext context;

    public OSWorkflowManager(Configuration configuration, DraftWorkflowStore draftWorkflowStore,
            EventPublisher eventPublisher, WorkflowsRepository workflowsRepository,
            WorkflowCopyNameFactory workflowCopyNameFactory, JiraAuthenticationContext context)
    {
        this.workflowsRepository = workflowsRepository;
        this.workflowCopyNameFactory = workflowCopyNameFactory;
        this.context = context;
        setConfiguration(configuration);
        this.draftWorkflowStore = draftWorkflowStore;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Retrieve all of the workflows in the system
     *
     * @return A collection of JiraWorkflow objects.
     */
    public Collection<JiraWorkflow> getWorkflows()
    {
        List<JiraWorkflow> workflows = new ArrayList<JiraWorkflow>();

        try
        {
            String[] workflowNames = getConfiguration().getWorkflowNames();

            for (String workflowName : workflowNames)
            {
                workflows.add(getWorkflow(workflowName));
            }
        }
        catch (FactoryException e)
        {
            log.error("Could not get workflow names: " + e, e);
        }

        Collections.sort(workflows);
        return workflows;
    }

    @Override
    public List<JiraWorkflow> getWorkflowsIncludingDrafts()
    {
        List<JiraWorkflow> ret = new ArrayList<JiraWorkflow>();
        for (JiraWorkflow jiraWorkflow : getWorkflows())
        {
            ret.add(jiraWorkflow);
            JiraWorkflow draftWorkflow = getDraftWorkflow(jiraWorkflow.getName());
            if (draftWorkflow != null)
            {
                ret.add(draftWorkflow);
            }
        }
        return ret;
    }

    /**
     * This method returns the (unique) name of the workflow which should be used for the provided projectId and
     * issueType
     *
     * @return the name of the workflow that should be used for the issue
     */
    protected String getWorkflowName(Long projectId, String issueType)
    {
        // first we need to get the workflow scheme for this project
        Project project = getProjectManager().getProjectObj(projectId);
        return getWorkflowSchemeManager().getWorkflowName(project, issueType);
    }

    private ProjectManager getProjectManager() {return ComponentAccessor.getProjectManager();}

    public Collection<JiraWorkflow> getActiveWorkflows() throws WorkflowException
    {
        return getSchemeActiveWorkflows();
    }

    public boolean isActive(JiraWorkflow workflow) throws WorkflowException
    {
        return getSchemeActiveWorkflows().contains(workflow);
    }

    // Check for a system or XML based workflow - can not be edited
    public boolean isSystemWorkflow(JiraWorkflow workflow)
    {
        return !getConfiguration().isModifiable(workflow.getName());
    }

    private Collection<JiraWorkflow> getSchemeActiveWorkflows() throws WorkflowException
    {
        try
        {
            Collection<String> names = getWorkflowSchemeManager().getActiveWorkflowNames();
            Set<JiraWorkflow> workflows = new HashSet<JiraWorkflow>();
            for (String name : names)
            {
                workflows.add(getWorkflow(name));
            }
            return workflows;
        }
        catch (GenericEntityException e)
        {
            throw new WorkflowException(e);
        }
    }

    public JiraWorkflow getWorkflow(String name)
    {
        try
        {
            WorkflowDescriptor workflowDescriptor = getConfiguration().getWorkflow(name);
            //TODO: We should check here if the returned workflowDescriptor is non null.
            if (JiraWorkflow.DEFAULT_WORKFLOW_NAME.equals(name))
            {
                return new DefaultJiraWorkflow(workflowDescriptor, this, context);
            }
            else
            {
                return new ConfigurableJiraWorkflow(name, workflowDescriptor, this);
            }
        }
        catch (FactoryException e)
        {
            log.debug("Could not get workflow called: " + name + ": " + e, e);
            return null;
        }
    }

    public JiraWorkflow getWorkflowClone(String name)
    {
        try
        {
            WorkflowDescriptor workflowDescriptor = getConfiguration().getWorkflow(name);
            if (JiraWorkflow.DEFAULT_WORKFLOW_NAME.equals(name))
            {
                return new DefaultJiraWorkflow(workflowDescriptor, this, context);
            }
            WorkflowDescriptor mutableDescriptor = cloneDescriptor(workflowDescriptor);
            return new ConfigurableJiraWorkflow(name, mutableDescriptor, this);
        }
        catch (FactoryException e)
        {
            log.error("Could not get workflow called: " + name + ": " + e, e);
            return null;
        }
    }

    //package level protected for testing.
    WorkflowDescriptor cloneDescriptor(WorkflowDescriptor workflowDescriptor)
            throws FactoryException
    {
        return WorkflowUtil.convertXMLtoWorkflowDescriptor(WorkflowUtil.convertDescriptorToXML(workflowDescriptor));
    }

    public JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        final JiraWorkflow parentWorkflow = getWorkflow(parentWorkflowName);
        if (parentWorkflow == null)
        {
            throw new IllegalArgumentException("Draft workflow could not be retrieved, since the parent workflow with name '" +
                    parentWorkflowName + "' does not exist.");
        }
        return draftWorkflowStore.getDraftWorkflow(parentWorkflowName);
    }

    public JiraWorkflow createDraftWorkflow(String username, String parentWorkflowName)
    {
        if (username == null)
        {
            throw new IllegalArgumentException("You can not create a draft workflow with a null username.");
        }
        ApplicationUser user = getUserManager().getUserByName(username);
        return createDraftWorkflow(user, parentWorkflowName);
    }

    public JiraWorkflow createDraftWorkflow(ApplicationUser user, String parentWorkflowName)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("You can not create a draft workflow with a null user.");
        }
        final JiraWorkflow parentWorkflow = getWorkflow(parentWorkflowName);
        if (parentWorkflow == null)
        {
            throw new IllegalArgumentException("You can not create a draft workflow from a parent that does not exist.");
        }
        if (!parentWorkflow.isActive())
        {
            throw new IllegalStateException("You can not create a draft workflow from a parent workflow that is not active.");
        }

        JiraWorkflow draftWorkflow = draftWorkflowStore.createDraftWorkflow(user, parentWorkflow);
        eventPublisher.publish(new DraftWorkflowCreatedEvent(draftWorkflow));

        return draftWorkflow;
    }

    public boolean deleteDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        if (StringUtils.isBlank(parentWorkflowName))
        {
            throw new IllegalArgumentException("Can not delete a draft workflow for a parent workflow name of null.");
        }

        JiraWorkflow draftWorkflow = getDraftWorkflow(parentWorkflowName);
        boolean deleted = draftWorkflowStore.deleteDraftWorkflow(parentWorkflowName);

        if (deleted)
        {
            eventPublisher.publish(new DraftWorkflowDeletedEvent(draftWorkflow));
        }

        return deleted;
    }

    public boolean workflowExists(String name) throws WorkflowException
    {
        return workflowsRepository.contains(name);
    }

    public JiraWorkflow getWorkflow(Issue issue) throws WorkflowException
    {
        GenericValue project = issue.getProject();
        if (project == null)
        {
            throw new IllegalArgumentException("Project for issue with id '" + issue.getId() + "' is null.");
        }

        IssueType issueType = issue.getIssueTypeObject();
        if (issueType == null)
        {
            throw new IllegalArgumentException("Issue Type for issue with id '" + issue.getId() + "' is null.");
        }

        return getWorkflow(project.getLong("id"), issueType.getId());
    }

    public JiraWorkflow getWorkflow(Long projectId, String issueTypeId) throws WorkflowException
    {
        return getWorkflow(getWorkflowName(projectId, issueTypeId));
    }

    public JiraWorkflow getWorkflowFromScheme(GenericValue scheme, String issueTypeId)
    {
        return getWorkflow(getWorkflowSchemeManager().getWorkflowName(scheme, issueTypeId));
    }

    public JiraWorkflow getWorkflowFromScheme(WorkflowScheme scheme, String issueTypeId)
    {
        return getWorkflow(scheme.getActualWorkflow(issueTypeId));
    }

    public Collection<JiraWorkflow> getWorkflowsFromScheme(GenericValue scheme) throws WorkflowException
    {
        if (scheme != null)
        {
            // now, check if we have a workflow configured for this issue type
            try
            {
                Collection<GenericValue> schemeEntities = getWorkflowSchemeManager().getEntities(scheme);
                if (schemeEntities != null)
                {
                    List<JiraWorkflow> result = new ArrayList<JiraWorkflow>(schemeEntities.size());
                    for (GenericValue schemeEntity : schemeEntities)
                    {
                        result.add(getWorkflow(schemeEntity.getString("workflow")));
                    }
                    return result;
                }
            }
            catch (GenericEntityException e)
            {
                throw new WorkflowException(e);
            }
        }

        // Always return the default if nothing else is found
        return CollectionBuilder.newBuilder(getWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME)).asMutableList();
    }

    @Override
    public Iterable<JiraWorkflow> getWorkflowsFromScheme(Scheme workflowScheme) throws WorkflowException
    {
        try
        {
            if (workflowScheme == null)
            {
                return getWorkflowsFromScheme((GenericValue) null);
            }
            return getWorkflowsFromScheme(getWorkflowSchemeManager().getScheme(workflowScheme.getId()));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void copyAndDeleteDraftWorkflows(User user, Set<JiraWorkflow> workflows)
    {
        copyAndDeleteDraftsForInactiveWorkflowsIn(user, workflows);
    }

    @Override
    public void copyAndDeleteDraftsForInactiveWorkflowsIn(User user, Iterable<JiraWorkflow> workflows)
    {
        if (workflows == null || isEmpty(workflows))
        {
            return;
        }
        for (final JiraWorkflow workflow : workflows)
        {
            final String parentWorkflowName = workflow.getName();
            final JiraWorkflow draftWorkflow = getDraftWorkflow(parentWorkflowName);

            //We should only create a copy and delete the draft, if the parentworkflow
            //is not active, and the draft actually exists.  For a workflow that's still
            //active, we want to keep the draft around.
            if (!workflow.isActive() && draftWorkflow != null)
            {
                String username = user == null ? null : user.getName();
                StringBuilder builder = new StringBuilder();
                final String parentDescription = stripToNull(draftWorkflow.getDescription());
                if (parentDescription != null)
                {
                    builder.append(parentDescription).append(" ");
                }
                builder.append(getI18nBean(user).getText("admin.workflows.manager.draft.auto.generated", parentWorkflowName));
                copyWorkflow(username, getClonedWorkflowName(parentWorkflowName, user), builder.toString(), draftWorkflow);
                deleteDraftWorkflow(parentWorkflowName);
            }
        }
    }

    @Nonnull
    @Override
    public String getNextStatusIdForAction(@Nonnull final Issue issue, final int actionId)
    {
        JiraWorkflow workflow = getWorkflow(issue);
        if (workflow == null)
        {
            throw new IllegalStateException("There is no workflow for the issue with key " + issue.getKey());
        }

        ActionDescriptor action = workflow.getDescriptor().getAction(actionId);
        if (action == null)
        {
            throw new IllegalStateException("There is no action descriptor for an action with id " + actionId);
        }

        int stepId = action.getUnconditionalResult().getStep();
        if (stepId == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
        {
            return issue.getStatusObject().getId();
        }

        StepDescriptor step = workflow.getDescriptor().getStep(stepId);
        return workflow.getLinkedStatusId(step);
    }

    private void createActualWorkflow(ApplicationUser user, JiraWorkflow workflow) throws WorkflowException
    {
        // Store the last edit author and updated date in the workflow descriptor
        addAuditInfo(user, workflow);

        saveWorkflowWithoutAudit(workflow);
    }

    public void createWorkflow(String username, JiraWorkflow workflow) throws WorkflowException
    {
        ApplicationUser user = getUserManager().getUserByName(username);
        createWorkflow(user, workflow);
    }

    public void createWorkflow(User creator, JiraWorkflow workflow) throws WorkflowException
    {
        createWorkflow(ApplicationUsers.from(creator), workflow);
    }

    public void createWorkflow(ApplicationUser user, JiraWorkflow workflow) throws WorkflowException
    {
        createActualWorkflow(user, workflow);

        eventPublisher.publish(new WorkflowCreatedEvent(workflow));
    }


    private void addAuditInfo(ApplicationUser user, JiraWorkflow workflow)
    {
        if (workflow == null)
        {
            return;
        }
        WorkflowDescriptor descriptor = workflow.getDescriptor();
        log.info("User '" + user + "' updated workflow '" + workflow.getName() + "' at '" + new Date() + "'");

        // If a non-logged in user is storing the workflow then we will store it as an empty string
        if (user != null)
        {
            descriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY, user.getKey());
        }
        else
        {
            descriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY, "");
        }
        descriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(System.currentTimeMillis()));
    }

    public void saveWorkflowWithoutAudit(JiraWorkflow workflow) throws WorkflowException
    {
        if (workflow.isDraftWorkflow())
        {
            // Save the passed workflow over the existing draft
            draftWorkflowStore.updateDraftWorkflowWithoutAudit(workflow.getName(), workflow);
        }
        try
        {
            getConfiguration().saveWorkflow(workflow.getName(), workflow.getDescriptor(), true);
            workflow.reset();
        }
        catch (FactoryException e)
        {
            throw new WorkflowException(e);
        }
    }

    protected WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return ComponentAccessor.getWorkflowSchemeManager();
    }

    public void deleteWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
        if (!Iterables.isEmpty(getWorkflowSchemeManager().getSchemesForWorkflowIncludingDrafts(workflow)))
        {
            throw new WorkflowException("The workflow is assigned to workflow schemes");
        }

        try
        {
            //we need to delete the draft first since it needs a reference to the parent
            deleteDraftWorkflow(workflow.getName());

            //TODO: Should we move this to the store?
            getConfiguration().removeWorkflow(workflow.getName());
            eventPublisher.publish(new WorkflowDeletedEvent(workflow));

        }
        catch (FactoryException e)
        {
            throw new WorkflowException("Error deleting workflow: " + e, e);
        }
    }

    public JiraWorkflow getWorkflow(GenericValue issue) throws WorkflowException
    {
        String workflowName = getWorkflowName(issue.getLong("project"), issue.getString("type"));
        return getWorkflow(workflowName);
    }

    public void migrateIssueToWorkflow(GenericValue issue, JiraWorkflow newWorkflow, GenericValue newStatus)
            throws WorkflowException
    {
        if (migrateIssueToWorkflowNoReindex(issue, newWorkflow, newStatus))
        {
            try
            {
                getIssueIndexManager().reIndex(issue);
            }
            catch (IndexException e)
            {
                log.error("Error indexing issue during workflow migration: " + e, e);
            }
        }
    }

    @Override
    public boolean migrateIssueToWorkflowNoReindex(GenericValue issue, JiraWorkflow newWorkflow, GenericValue newStatus)
            throws WorkflowException
    {
        try
        {
            WorkflowStore store = getStore();

            // find the current step for the current entry
            long wfid = issue.getLong("workflowId");
            List currentSteps = store.findCurrentSteps(wfid);
            SimpleStep currentStep = null;

            if (!currentSteps.isEmpty())
            {
                currentStep = (SimpleStep) currentSteps.get(0);
            }

            // create a new workflow entry
            WorkflowEntry newEntry = store.createEntry(newWorkflow.getName());
            store.setEntryState(newEntry.getId(), WorkflowEntry.ACTIVATED);
            store.setEntryState(wfid, WorkflowEntry.KILLED);

            // now create a current step matching the old current step, but with the new workflow
            StepDescriptor stepInNewWorkflow = newWorkflow.getLinkedStep(newStatus);
            if (stepInNewWorkflow == null)
            {
                throw new RuntimeException("No step associated with status " + (newStatus == null ? "null" : newStatus.getString("name")) + " in new workflow " + newWorkflow.getName());
            }

            // Check if the workflow entry had a corresponding step
            if (currentStep != null)
            {
                store.createCurrentStep(newEntry.getId(), stepInNewWorkflow.getId(), currentStep.getOwner(), currentStep.getStartDate(), currentStep.getDueDate(), currentStep.getStatus(), null);
                // Move the original step to the OS_HISTORYSTEP table
                store.moveToHistory(currentStep);
            }
            else
            {
                // Create a new step - set the start date and status
                Date startDate = issue.getTimestamp("created");
                store.createCurrentStep(newEntry.getId(), stepInNewWorkflow.getId(), null, startDate, null, newStatus.getString("id"), null);
            }

            boolean result = updateIssueStatusAndUpdatedDate(issue, newStatus);

            issue.set("workflowId", newEntry.getId());
            issue.store();

            return result;
        }
        catch (StoreException e)
        {
            throw new WorkflowException(e);
        }
        catch (GenericEntityException e)
        {
            throw new WorkflowException(e);
        }
    }

    public void overwriteActiveWorkflow(String username, String workflowName)
    {
        overwriteActiveWorkflow(getUserManager().getUserByName(username), workflowName);
    }

    public void overwriteActiveWorkflow(ApplicationUser user, String workflowName)
    {
        // Get the draft workflow from the Store
        final JiraWorkflow draftWorkflow = draftWorkflowStore.getDraftWorkflow(workflowName);
        if (draftWorkflow == null)
        {
            throw new WorkflowException("No draft workflow named '" + workflowName + "'");
        }
        final JiraWorkflow originalWorkflow = getWorkflow(workflowName);

        boolean saved;
        // save the draft over the active workflow
        try
        {
            // Add Audit info
            addAuditInfo(user, draftWorkflow);
            saved = getConfiguration().saveWorkflow(workflowName, draftWorkflow.getDescriptor(), true);
            eventPublisher.publish(new DraftWorkflowPublishedEvent(draftWorkflow, originalWorkflow));
        }
        catch (FactoryException e)
        {
            throw new WorkflowException(e);
        }

        if (!saved)
        {
            throw new WorkflowException("Workflow '" + workflowName + "' could not be overwritten!");
        }
        else
        {
            // Now remove the "Draft" copy
            draftWorkflowStore.deleteDraftWorkflow(workflowName);
        }
    }

    protected boolean updateIssueStatusAndUpdatedDate(GenericValue issue, GenericValue newStatus)
    {
        if (!issue.getString("status").equals(newStatus.getString("id")))
        {
            issue.set("updated", UtilDateTime.nowTimestamp());
            issue.set("status", newStatus.getString("id"));

            return true;
        }
        else
        {
            return false;
        }
    }

    public void updateWorkflow(String username, JiraWorkflow workflow){
        if (username == null)
        {
            throw new IllegalArgumentException("Can not update a workflow with a null username.");
        }
        updateWorkflow(getUserManager().getUserByName(username), workflow);
    }

    public void updateWorkflow(ApplicationUser user, JiraWorkflow workflow)
    {
        final JiraWorkflow originalWorkflow;
        if (user == null)
        {
            throw new IllegalArgumentException("Can not update a workflow with a null user.");
        }
        if (workflow == null || workflow.getDescriptor() == null)
        {
            throw new IllegalArgumentException("Can not update a workflow with a null workflow/descriptor.");
        }
        if (workflow.isDraftWorkflow())
        {
            final JiraWorkflow parentWorkflow = getWorkflow(workflow.getName());
            if (parentWorkflow == null)
            {
                throw new IllegalStateException("You can not update a draft workflow for a parent that does not exist.");
            }
            originalWorkflow = getDraftWorkflow(workflow.getName());

            draftWorkflowStore.updateDraftWorkflow(user, workflow.getName(), workflow);
        }
        else
        {
            originalWorkflow = getWorkflow(workflow.getName());
            if (workflow.isActive())
            {
                throw new WorkflowException("Cannot save an active workflow.");
            }
            if (workflow.isSystemWorkflow())
            {
                throw new WorkflowException("Cannot change the system workflow.");
            }
            createActualWorkflow(user, workflow);
        }

        eventPublisher.publish(new WorkflowUpdatedEvent(workflow, originalWorkflow));
    }

    public JiraWorkflow copyWorkflow(String username, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone)
    {
        return copyWorkflow(getUserManager().getUserByName(username), clonedWorkflowName, clonedWorkflowDescription, workflowToClone);
    }

    public JiraWorkflow copyWorkflow(ApplicationUser user, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone)
    {
        final WorkflowDescriptor workflowDescriptor;
        try
        {
            workflowDescriptor = cloneDescriptor(workflowToClone.getDescriptor());
        }
        catch (FactoryException e)
        {
            throw new WorkflowException("Unexpected exception copying a workflowDescriptor for workflow '" + clonedWorkflowName + "'!", e);
        }
        ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(clonedWorkflowName, workflowDescriptor, this);

        // Set description if we have one, otherwise set it to an empty string
        if (StringUtils.isNotEmpty(clonedWorkflowDescription))
        {
            newWorkflow.setDescription(clonedWorkflowDescription);
        }
        else
        {
            newWorkflow.setDescription("");
        }

        createActualWorkflow(user, newWorkflow);
        eventPublisher.publish(new WorkflowCopiedEvent(workflowToClone, newWorkflow));
        return newWorkflow;
    }

    public void updateWorkflowNameAndDescription(String username, JiraWorkflow currentWorkflow, String newName, String newDescription)
    {
        updateWorkflowNameAndDescription(getUserManager().getUserByName(username), currentWorkflow, newName, newDescription);
    }

    public void updateWorkflowNameAndDescription(ApplicationUser user, JiraWorkflow currentWorkflow, String newName, String newDescription)
    {
        Null.not("currentWorkflow", currentWorkflow);
        final String currentWorkflowName = currentWorkflow.getName();

        //get an editable copy of the workflow.  If the workflow passed in is not
        //a draft workflow, we need to get an editable clone of the real workflow.
        JiraWorkflow workflow = currentWorkflow;
        if (!currentWorkflow.isDraftWorkflow())
        {
            workflow = getWorkflowClone(currentWorkflowName);
        }

        //update the description in the database
        if (newDescription != null && !newDescription.equals(workflow.getDescription()))
        {
            WorkflowDescriptor descriptor = workflow.getDescriptor();
            descriptor.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, newDescription);
            updateWorkflow(user, workflow);
        }

        //update the name in the database only if needed, and if the workflow is not a draft workflow!
        if (!currentWorkflowName.equals(newName) && !currentWorkflow.isDraftWorkflow())
        {
            try
            {
                getConfiguration().removeWorkflow(currentWorkflowName);
                getConfiguration().saveWorkflow(newName, workflow.getDescriptor(), true);

                //update the associated schemes foreign key reference the new workflow name
                getWorkflowSchemeManager().updateSchemesForRenamedWorkflow(currentWorkflowName, newName);

                //check if there's a draft hanging around.  This should never be the case, as an in-active workflow
                //should never have a draft, but just in case we should change its name too to make sure
                //we don't end up with an orphaned draft!
                JiraWorkflow draftWorkflow = draftWorkflowStore.getDraftWorkflow(currentWorkflowName);
                if (draftWorkflow != null)
                {
                    log.warn("Inactive workflow '" + newName + "' has a draft workflow. Please remove this draft!");
                    JiraDraftWorkflow newWorkflow = new JiraDraftWorkflow(newName, this, draftWorkflow.getDescriptor());
                    draftWorkflowStore.createDraftWorkflow(user, newWorkflow);
                    draftWorkflowStore.deleteDraftWorkflow(currentWorkflowName);
                }

                eventPublisher.publish(new WorkflowRenamedEvent(workflow, currentWorkflowName, newName));
            }
            catch (FactoryException e)
            {
                throw new WorkflowException("Error renaming workflow '" + currentWorkflow + "' to '" + newName + "' " + e, e);
            }
        }
    }

    //a couple of helper methods mainly to make the code more testable!
    I18nHelper getI18nBean(User user)
    {
        return new I18nBean(user);
    }

    String getClonedWorkflowName(String parentWorkflowName, User user)
    {
        return workflowCopyNameFactory.createFrom(parentWorkflowName, getI18nBean(user).getLocale());
    }

    /*
     * needed for tests
     */
    void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }


    public JiraWorkflow getDefaultWorkflow() throws WorkflowException
    {
        return getWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
    }

    public GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws WorkflowException
    {
        // As per JRADEV-7958 and JRA-25914  transactions around create issue can cause either deadlock
        // or duplicate keys.  Do not put this back in a transaction, unless you significantly refactor the mechanism
        // for obtaining the project counter

        try
        {
            // Determine the workflow for the issue to use
            Issue issue = (Issue) fields.get("issue");
            final Long projectId = issue.getProjectObject().getId();
            final String issueTypeId = issue.getIssueTypeObject().getId();
            final JiraWorkflow jiraWorkflow = getWorkflow(projectId, issueTypeId);
            if (jiraWorkflow == null)
            {
                throw new IllegalArgumentException("Cannot find workflow for project with id '" + projectId + "' and issue type with id '" + issueTypeId + "'.");
            }

            final Workflow workflow = makeWorkflowWithUserName(remoteUserName);

            // Get the initial action for the
            final WorkflowDescriptor workflowDescriptor = jiraWorkflow.getDescriptor();
            final List initialActions = workflowDescriptor.getInitialActions();

            if (initialActions == null || initialActions.isEmpty())
            {
                throw new WorkflowException("No initial actions exist for workflow with name '" + jiraWorkflow.getName() + ".");
            }

            // NOTE: Only one initial action is supported.
            // REMEMBER: THERE CAN BE ONLY ONE!
            final ActionDescriptor actionDescriptor = (ActionDescriptor) initialActions.get(0);

            long wfId = workflow.initialize(jiraWorkflow.getName(), actionDescriptor.getId(), fields);

            final GenericValue issueGV = getIssueManager().getIssueByWorkflow(wfId);
            // https://support.atlassian.com/browse/JST-15144  Looks like IssueCreateFunction can screw up if you configure your post functions incorrectly.
            if (issueGV == null)
            {
                throw new WorkflowException("Issue workflow initialization error: unable to find Issue created with workflowId '" +
                        wfId + "'. Did the IssueCreateFunction run successfully on workflow.initialize() ?");
            }
            return issueGV;

        }
        catch (InvalidRoleException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (InvalidInputException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (GenericEntityException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (InvalidEntryStateException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (com.opensymphony.workflow.WorkflowException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (ClassCastException e)
        {
            String message = "Error occurred while creating issue. This could be due to a plugin being incompatible with this version of JIRA. For more details please consult the logs, and see: " +
                    "http://confluence.atlassian.com/x/3McB";

            throw new WorkflowException(message, e);
        }
        catch (Exception e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public void removeWorkflowEntries(GenericValue issue)
    {
        getOfBizDelegator().removeByAnd("OSWorkflowEntry", EasyMap.build("id", issue.getLong("workflowId")));
        getOfBizDelegator().removeByAnd("OSCurrentStep", EasyMap.build("entryId", issue.getLong("workflowId")));
        getOfBizDelegator().removeByAnd("OSHistoryStep", EasyMap.build("entryId", issue.getLong("workflowId")));
    }

    private OfBizDelegator getOfBizDelegator()
    {
        return ComponentAccessor.getOfBizDelegator();
    }

    public void doWorkflowAction(WorkflowProgressAware from)
    {
        disableIndexingForThisThread();

        Transaction txn = Txn.begin();
        try
        {
            doWorkflowActionInsideTxn(txn, from);
        }
        finally
        {
            try
            {
                enableIndexingForThisThread();
            }
            catch (IndexException e)
            {
                throw new WorkflowException(e.getMessage(), e);
            }
            txn.finallyRollbackIfNotCommitted();
        }
    }

    private void doWorkflowActionInsideTxn(final Transaction txn, final WorkflowProgressAware from)
    {
        Issue issue = null;
        Long wfid = null;

        try
        {
            issue = from.getIssue();
            wfid = issue.getLong("workflowId");

            // if these are available at the time an exception is thrown, they will be logged
            final Workflow wf = getWorkflowObject(from);

            // get inputs
            Map<Object, Object> inputs = new HashMap<Object, Object>();
            inputs.put("issue", issue);
            GenericValue originalIssueGV = getIssueManager().getIssue(issue.getId());
            MutableIssue originalIssue = ComponentAccessor.getIssueFactory().getIssue(originalIssueGV);
            inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
            inputs.put("proj", from.getProject());
            inputs.put("project", from.getProject().getGenericValue());
            inputs.put("pkey", from.getProject().getKey()); // Allows ${pkey} in condition args
            if (from.getAdditionalInputs() != null)
            {
                inputs.putAll(from.getAdditionalInputs());
            }

            try
            {
                wf.doAction(wfid, from.getAction(), inputs);
            }
            finally
            {
                // For backwards compatability with old plugins that may have turned indexing off during a transition,
                // we turn indexing back on more or less unconditionality.
                if (ImportUtils.isIndexIssues())
                {
                    ImportUtils.setIndexIssues(true);
                }
            }

            // save the issue updates - the generate change history function stores the issue as well, but if it has been modified by some post functions
            // after that we need to store the issue again.
            issue.store();

            // commit the TX
            txn.commit();

            // Now re-index the transitioned index.  We do this before releasing the queue so the queue can combine multiple requests
            // to re-index this issue if they occur.
            getIssueIndexManager().reIndex(issue);
            if (!ObjectUtils.equals(originalIssue.getSecurityLevelId(), issue.getSecurityLevelId()))
            {
                getIssueIndexManager().reIndexIssueObjects(issue.getSubTaskObjects());
            }
        }
        catch (InvalidInputException e)
        {
            // This is an expected message from a Workflow Validator upon bad user input.
            // We retrieve the error messages from the InvalidInputException and put them in the WorkflowProgressAware
            // for passing back to the caller (eg the Webwork Action) such that they can be displayed to the user.
            for (final Object o1 : e.getGenericErrors())
            {
                String error = (String) o1;
                from.addErrorMessage(error);
            }

            for (final Object o : e.getErrors().entrySet())
            {
                Map.Entry entry = (Map.Entry) o;
                from.addError((String) entry.getKey(), (String) entry.getValue());
            }

            // JDEV-25244: Log this at debug because it is an expected error and spams the log files with useless garbage
            log.debug(String.format("InvalidInputException while attempting to perform action %d from workflow %d on issue '%s'", from.getAction(), wfid, issue), e);
        }
        catch (ClassCastException e)
        {
            String message = "Error occurred while creating issue. This could be due to a plugin being incompatible with this version of JIRA. For more details please consult the logs, and see: " +
                    "http://confluence.atlassian.com/x/3McB";
            log.error(String.format("Caught exception while attempting to perform action %d from workflow %d on issue '%s'", from.getAction(), wfid, issue), e);
            from.addErrorMessage(message + " " + e.getMessage());
        }
        catch (Exception e)
        {
            from.addErrorMessage(e.getMessage());
            log.error(String.format("Caught exception while attempting to perform action %d from workflow %d on issue '%s'", from.getAction(), wfid, issue), e);
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }
    }

    private IssueIndexManager getIssueIndexManager()
    {
        return ComponentAccessor.getIssueIndexManager();
    }

    private void disableIndexingForThisThread()
    {
        getIssueIndexManager().hold();
    }

    private void enableIndexingForThisThread() throws IndexException
    {
        getIssueIndexManager().release();
    }

    private Workflow getWorkflowObject(WorkflowProgressAware from)
    {
        // Allows actions to be run as other users.
        String userkey = WorkflowFunctionUtils.getCallerKey(null, from.getAdditionalInputs());

        if (userkey == null && ActionContext.getPrincipal() != null)
        {
            ApplicationUser principal = getUserManager().getUserByName(ActionContext.getPrincipal().getName());
            userkey = principal == null ? null : principal.getKey();
        }

        if (userkey == null)
        {
            userkey = ApplicationUsers.getKeyFor(from.getRemoteApplicationUser());
        }

        return makeWorkflow(ApplicationUsers.byKey(userkey));
    }

    public User getRemoteUser(Map transientVars)
    {
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        String username = context.getCaller();

        if (username != null)
        {
            return getUserManager().getUserObject(username);
        }
        else
        {
            return null;
        }
    }

    private UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }

    /**
     * This is used for unit testing so we can return our own 'mock store' instead of the static
     * StoreFactory.getPersistence(ctx);
     * <p/>
     * Also used in the migrateIssueToWorkflow method.
     */
    public WorkflowStore getStore() throws StoreException
    {
        return getConfiguration().getWorkflowStore();
    }


    public ActionDescriptor getActionDescriptor(WorkflowProgressAware workflowProgressAware) throws Exception
    {
        JiraWorkflow workflow = getWorkflow(workflowProgressAware.getIssue().getGenericValue());
        return workflow.getDescriptor().getAction(workflowProgressAware.getAction());
    }

    /**
     * Migrates given issue to new workflow and sets new status on it.
     *
     * @param issue issue to migrate
     * @param newWorkflow new workflow
     * @param status new status
     * @throws WorkflowException if migration fails
     */
    public void migrateIssueToWorkflow(MutableIssue issue, JiraWorkflow newWorkflow, Status status)
            throws WorkflowException
    {
        final GenericValue issueGV = issue.getGenericValue();
        migrateIssueToWorkflow(issueGV, newWorkflow, status.getGenericValue());
        issue.setWorkflowId(issueGV.getLong("workflowId"));
        issue.setStatusId(issueGV.getString("status"));
    }

    public Workflow makeWorkflow(String userName)
    {
        return makeWorkflowWithUserName(userName);
    }

    public Workflow makeWorkflowWithUserName(String userName)
    {
        ApplicationUser appUser = getUserManager().getUserByName(userName);
        return makeWorkflowWithUserKey(appUser != null ? appUser.getKey() : null);
    }

    public Workflow makeWorkflowWithUserKey(String userKey)
    {
        Workflow workflow = new BasicWorkflow(userKey);
        workflow.setConfiguration(getConfiguration());
        return workflow;
    }

    public Workflow makeWorkflow(User user)
    {
        if (user == null)
        {
            return makeWorkflowWithUserKey(null);
        }
        return makeWorkflowWithUserName(user.getName());
    }

    public Workflow makeWorkflow(ApplicationUser applicationUser)
    {
        return makeWorkflowWithUserKey(ApplicationUsers.getKeyFor(applicationUser));
    }

    public boolean isEditable(Issue issue)
    {
        if (issue == null || issue.getProjectObject() == null || issue.getIssueTypeObject() == null ||
                issue.getStatusObject() == null)
        {
            return false;
        }

        try
        {
            final JiraWorkflow workflow = getWorkflow(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
            final String statusId = issue.getStatusObject().getId();
            if (statusId != null) //this should never really be null - but it saves hassle of setting up hundreds of tests with a statusId
            {
                final StepDescriptor currentStep = workflow.getLinkedStep(getConstantsManager().getStatusObject(statusId));
                if (currentStep == null || !"false".equals(currentStep.getMetaAttributes().get(JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED)))
                {
                    return true;
                }
            }

            return false;
        }
        catch (WorkflowException e)
        {
            throw new RuntimeException(e + " when trying to access workflow for issue " + issue, e);
        }
    }

    private ConstantsManager getConstantsManager() {return ComponentAccessor.getConstantsManager();}

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    public Map<ActionDescriptor, Collection<FunctionDescriptor>> getPostFunctionsForWorkflow(JiraWorkflow workflow)
    {
        Map<ActionDescriptor, Collection<FunctionDescriptor>> transitionPostFunctionMap = new HashMap<ActionDescriptor, Collection<FunctionDescriptor>>();

        Collection<ActionDescriptor> actions = workflow.getAllActions();
        for (final ActionDescriptor actionDescriptor : actions)
        {
            Collection<FunctionDescriptor> postFunctions = workflow.getPostFunctionsForTransition(actionDescriptor);

            transitionPostFunctionMap.put(actionDescriptor, postFunctions);
        }

        return transitionPostFunctionMap;
    }

    public String getStepId(long actionDescriptorId, String workflowName)
    {
        int actionDescId = new Long(actionDescriptorId).intValue();

        String stepId = null;

        JiraWorkflow workflow = getWorkflow(workflowName);
        ActionDescriptor actionDescriptor = workflow.getDescriptor().getAction(actionDescId);

        if (actionDescriptor != null)
        {
            Collection<StepDescriptor> stepsForTransition = workflow.getStepsForTransition(actionDescriptor);

            for (final StepDescriptor stepDescriptor : stepsForTransition)
            {
                stepId = String.valueOf(stepDescriptor.getId());
                break;
            }
        }

        return stepId;
    }

    /**
     * Returns the IssueManager. Needed to avoid circular dependency.
     *
     * @return IssueManager
     */
    private IssueManager getIssueManager()
    {
        return ComponentAccessor.getIssueManager();
    }

}
