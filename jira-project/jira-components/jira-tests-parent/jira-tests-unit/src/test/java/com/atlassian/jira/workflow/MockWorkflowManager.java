package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.apache.commons.lang.NotImplementedException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Mock WorkflowManager.
 *
 * @since v3.13
 */
public class MockWorkflowManager implements WorkflowManager
{
    private Map workflowMap = new HashMap();
    private Map draftWorkflowMap = new HashMap();
    private Collection<JiraWorkflow> activeWorkflows = new ArrayList();

    public Collection getWorkflows()
    {
        return null;
    }

    public List getWorkflowsIncludingDrafts()
    {
        return null;
    }

    public boolean isActive(JiraWorkflow workflow) throws WorkflowException
    {
        return false;
    }

    public boolean isSystemWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
        return false;
    }

    public Collection getActiveWorkflows() throws WorkflowException
    {
        return activeWorkflows;
    }

    public MockWorkflowManager addActiveWorkflows(JiraWorkflow workflow) throws WorkflowException
    {
        activeWorkflows.add(workflow);
        return this;
    }

    public JiraWorkflow getWorkflow(String name)
    {
        return (JiraWorkflow) workflowMap.get(name);
    }

    public JiraWorkflow getWorkflowClone(String name)
    {
        return (JiraWorkflow) workflowMap.get(name);
    }

    public JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        return (JiraWorkflow) draftWorkflowMap.get(parentWorkflowName);
    }

    public JiraWorkflow createDraftWorkflow(String username, String parentWorkflowName)
            throws IllegalStateException, IllegalArgumentException
    {
        return null;
    }

    public JiraWorkflow createDraftWorkflow(ApplicationUser user, String parentWorkflowName)
            throws IllegalStateException, IllegalArgumentException
    {
        return null;
    }

    public boolean deleteDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        return false;
    }

    public JiraWorkflow updateDraftWorkflow(String username, String parentWorkflowName, JiraWorkflow workflow)
    {
        draftWorkflowMap.put(parentWorkflowName, workflow);
        return workflow;
    }

    public JiraWorkflow getWorkflow(GenericValue issue) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflow(Issue issue) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflow(Long projectId, String issueTypeId) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflowFromScheme(GenericValue scheme, String issueTypeId) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflowFromScheme(WorkflowScheme scheme, String issueTypeId) throws WorkflowException
    {
        return null;
    }

    public Collection getWorkflowsFromScheme(GenericValue workflowScheme) throws WorkflowException
    {
        return null;
    }

    @Override
    public Collection<JiraWorkflow> getWorkflowsFromScheme(Scheme workflowScheme) throws WorkflowException
    {
        throw null;
    }

    public JiraWorkflow getDefaultWorkflow() throws WorkflowException
    {
        return null;
    }

    public GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws WorkflowException
    {
        return null;
    }

    public void removeWorkflowEntries(GenericValue issue)
    {
    }

    public void doWorkflowAction(WorkflowProgressAware from)
    {
    }

    public User getRemoteUser(Map transientVars)
    {
        return null;
    }

    public WorkflowStore getStore() throws StoreException
    {
        return null;
    }

    public void createWorkflow(String username, JiraWorkflow workflow) throws WorkflowException
    {
    }

    public void createWorkflow(User creator, JiraWorkflow workflow) throws WorkflowException
    {
    }

    public void createWorkflow(ApplicationUser creator, JiraWorkflow workflow) throws WorkflowException
    {
    }

    public void saveWorkflow(User user, JiraWorkflow workflow) throws WorkflowException
    {
        workflowMap.put(workflow.getName(), workflow);
    }

    public void saveWorkflowWithoutAudit(JiraWorkflow workflow) throws WorkflowException
    {
    }

    public void deleteWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
    }

    public ActionDescriptor getActionDescriptor(WorkflowProgressAware workflowProgressAware) throws Exception
    {
        return null;
    }

    public void migrateIssueToWorkflow(MutableIssue issue, JiraWorkflow newWorkflow, Status status)
            throws WorkflowException
    {
    }

    public void migrateIssueToWorkflow(GenericValue issue, JiraWorkflow newWorkflow, GenericValue status)
            throws WorkflowException
    {
    }

    @Override
    public boolean migrateIssueToWorkflowNoReindex(GenericValue issue, JiraWorkflow newWorkflow, GenericValue status)
            throws WorkflowException
    {
        throw new NotImplementedException("Not implemented in this mock.");
    }

    public Workflow makeWorkflow(String userName)
    {
        return null;
    }

    public Workflow makeWorkflow(User user)
    {
        return null;
    }

    @Override
    public Workflow makeWorkflow(ApplicationUser user)
    {
        return null;
    }

    @Override
    public Workflow makeWorkflowWithUserName(String userName)
    {
        return null;
    }

    @Override
    public Workflow makeWorkflowWithUserKey(String userKey)
    {
        return null;
    }

    public boolean workflowExists(String name) throws WorkflowException
    {
        return false;
    }

    public boolean isEditable(Issue issue)
    {
        return false;
    }

    public Map<ActionDescriptor, Collection<FunctionDescriptor>> getPostFunctionsForWorkflow(JiraWorkflow workflow)
    {
        return null;
    }

    public String getStepId(long actionDescriptorId, String workflowName)
    {
        return null;
    }

    public void overwriteActiveWorkflow(String username, String workflowName)
    {
    }

    public void overwriteActiveWorkflow(ApplicationUser user, String workflowName)
    {
    }

    public void updateWorkflow(String username, JiraWorkflow workflow)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    public void updateWorkflow(ApplicationUser user, JiraWorkflow workflow)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    public JiraWorkflow copyWorkflow(String username, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone)
    {
        return null;
    }

    public JiraWorkflow copyWorkflow(ApplicationUser user, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone)
    {
        return null;
    }

    public void updateWorkflowNameAndDescription(String username, JiraWorkflow currentWorkflow, String newName, String newDescription)
    {
    }
    public void updateWorkflowNameAndDescription(ApplicationUser user, JiraWorkflow currentWorkflow, String newName, String newDescription)
    {
    }

    @Override
    public void copyAndDeleteDraftsForInactiveWorkflowsIn(User user, Iterable<JiraWorkflow> workflows)
    {
    }

    @Nonnull
    @Override
    public String getNextStatusIdForAction(@Nonnull final Issue issue, final int actionId)
    {
        return "";
    }

    public void copyAndDeleteDraftWorkflows(User user, Set workflows)
    {
    }

}
