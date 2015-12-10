package com.atlassian.jira.web.bean;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bulkedit.operation.BulkEditAction;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used in the BulkEdit Wizard
 * Stores in session:
 * currentStep
 * action selected and values associated with that action
 * issues selected
 */
@PublicApi
public interface BulkEditBean extends OperationContext, SingleIssueModeEditBean
{
    public static final String SUBTASK_STATUS_INFO = "subtaskstatusinfo_";
    public static final String BULK_MOVE_OP = "bulk_move_op";
    public static final String BULK_DELETE_OP = "bulk_delete_op";
    public static final String BULK_EDIT_OP = "bulk_edit_op";
    public static final String BULK_DELETE = "delete";
    public static final String BULKEDIT_PREFIX = "bulkedit_";

    public BulkEditBean getParentBulkEditBean();

    /**
     * Initialises the {@link BulkEditBean} with the selected issues
     *
     * @param selectedIssues Required selected Issues.
     */
    public void initSelectedIssues(final Collection<Issue> selectedIssues);

    public void addIssues(final Collection<Issue> issues);

    /**
     * Returns a list of "selected" issues.
     * For the "top level" BulkEditBean this should be the actual issues chosen by the user for the bulk operation.
     * <p>
     * The Bulk Move operation will then break this list up in groups of project and issue type and store each of these
     * types in its own BulkEditBean, so for these nested BulkEditBeans this list may be a subset of the original
     * selected issues. Furthermore when moving parent issues to a new Project, we will have to move any subtasks as
     * well. In this case a third level of BulkEditBean is created and these ones will have subtasks that may not have
     * been explicitly selected by the user at all.
     * </p>
     *
     * @return List of the Selected Issues
     */
    public List<Issue> getSelectedIssues();

    public List<Issue> getSelectedIssuesIncludingSubTasks();

    // ------------------------------------------------------------------------------------------- Informational Methods
    public boolean isChecked(final Issue issue);

    public boolean isMultipleProjects();

    public boolean isMutipleIssueTypes();

    /**
     * @deprecated Use {@link #getSingleProject()} instead. Since v5.2.
     */
    public GenericValue getProject();

    /**
     * Returns the single Project if there is only one Project in this BulkEditBean, otherwise throws an IllegalStateException.
     *
     * @return the single Project if there is only one Project in this BulkEditBean, otherwise throws an IllegalStateException.
     * @see #isMultipleProjects()
     */
    public Project getSingleProject();

    public GenericValue getIssueType();

    /**
     * Returns all the unique field layouts of the selected issues
     *
     * @return Collection of the FieldLayouts.
     */
    public Collection<FieldLayout> getFieldLayouts();

    /**
     * Returns a string that represents a "unique" identifier for this bulke edit bean
     *
     * @return unique key formed from projects, issue types, target project if a subtask only bulkeditbean and size of the bean
     */
    public String getKey();

    /**
     * returns a list of project ids for projects which the currently selected issues belong to.
     *
     * @return A list of project ids for projects which the currently selected issues belong to.
     */
    public Collection<Long> getProjectIds();

    /**
     * @return
     * @deprecated Use {@link #getProjectObjects()} instead. Since v5.2.
     */
    public Collection<GenericValue> getProjects();

    public Collection<Project> getProjectObjects();

    public Collection<String> getIssueTypes();

    public Collection<IssueType> getIssueTypeObjects();

    public String getCheckboxName(final Issue issue);

    public CustomField getCustomField(final String customFieldKey) throws GenericEntityException;

    public String getCustomFieldView(final CustomField customField) throws FieldValidationException;

    public void setParams(final Map<String, ?> params);

    public Map<String, ?> getParams();

    public void setIssuesInUse(final Collection<?> issuesInUse);

    public void addAvailablePreviousStep(final int stepId);

    public void clearAvailablePreviousSteps();

    public boolean isAvailablePreviousStep(final int stepId);

    /**
     * Check if a mail server has been specified.
     *
     * @return boolean  true if a mail server has been specified
     */
    public boolean isHasMailServer();

    ///// ------------------------ Move Issues ------------------------ /////
    public Collection<?> getMoveFieldLayoutItems();

    public void setMoveFieldLayoutItems(final Collection<?> moveFieldLayoutItems);

    public Long getTargetPid();

    /**
     * @deprecated Use {@link #setTargetProject(com.atlassian.jira.project.Project)} instead. Since v5.2.
     */
    public void setTargetProject(final GenericValue project);

    public void setTargetProject(final Project project);

    public Project getTargetProject();

    public GenericValue getTargetProjectGV();

    public void setTargetIssueTypeId(final String id);

    public String getTargetIssueTypeId();

    public GenericValue getTargetIssueTypeGV();

    public IssueType getTargetIssueTypeObject();

    // Retrieve the target statuses from the params
    public void populateStatusHolder() throws WorkflowException;

    /**
     * Gets a set of invalid statuses that are not valid in the destination workflow
     *
     * @return Set of {@link GenericValue} objects
     * @throws WorkflowException
     */
    public Collection<GenericValue> getInvalidStatuses() throws WorkflowException;

    /**
     * Gets issues whose status is null
     *
     * @return Set of {@link Issue} objects. Emoty Set if no invalid issues
     * @throws WorkflowException
     */
    public Set<Issue> getInvalidIssues() throws WorkflowException;

    // Retrieve a collection of sub-task types that are associated with an invalid status in the target context
    public Set<String> getInvalidSubTaskTypes() throws WorkflowException;

    // Retrieve collection of invalid statuses associated with the specified subtask type.
    // Collection retireved from Map: (Key); SubTask Type ID -> (Value); Collection of Invalid SubTask Status IDs
    public Set<String> getInvalidSubTaskStatusesByType(final String subTaskTypeId) throws WorkflowException;

    /**
     * Sets the targetFieldLayout to the appropriate FieldLayout (aka "Field Configuration"); for the target Project and
     * Issue Type.
     */
    public void setTargetFieldLayout();

    public FieldLayout getTargetFieldLayout();

    public FieldLayout getTargetFieldLayoutForType(final String targetTypeId);

    public JiraWorkflow getTargetWorkflow() throws WorkflowException;

    /**
     * This method is used to get a target issue that will provide the correct context (i.e. project and issue type);,
     * for where you are moving to. The object returned is not mapped to a specific selected issue.
     *
     * @return an issue whose project and issue type are of where the you are moving to.
     */
    public Issue getFirstTargetIssueObject();

    public Map<Issue, Issue> getTargetIssueObjects();

    public void setTargetIssueObjects(final Map<Issue, Issue> targetIssueObjects);

    /**
     * This is a convinience method for converting the list of objects to a list of GenericValues
     *
     * @return list of GenericValue issue objects
     */
    public List<GenericValue> getTargetIssueGVs();

    public Status getTargetStatusObject(final Issue issue);

    /**
     * @deprecated Use {@link #getTargetStatusObject(com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    @Deprecated
    public GenericValue getTargetStatus(final Issue issue);

    public Map<String, String> getStatusMapHolder();

    public Collection<?> getRemovedFields();

    public void setRemovedFields(final Set<?> removedFields);

    public void resetMoveData();

    // Retrieve the sub task target statuses from the params
    // The subtaskStatusHolder contains a collection of strings constructd as follows:
    // subtaskstatusinfo_subtasktype_originalstatusid_targetstatusid
    public void populateSubTaskStatusHolder() throws WorkflowException;

    // Retrieve the workflow associated with the sub-task in the target project
    public JiraWorkflow getTargetWorkflowByType(final String issueTypeId) throws WorkflowException;

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public Collection<String> getSubTaskStatusHolder();

    public boolean isRetainChecked(final String fieldId);

    public boolean isSubTaskCollection();

    public boolean isSubTaskOnly();

    public String getOperationName();

    public void setOperationName(final String operationName);

    public void setInvalidSubTaskStatusesByType(final Map<String, Set<String>> invalidSubTaskStatusesByType);

    public void setInvalidSubTaskTypes(final Set<String> invalidSubTaskTypes);

    public int getInvalidSubTaskCount();

    public Set<String> getRetainValues();

    public void setRetainValues(final Set<String> retainValues);

    public void addRetainValue(final String fieldId);

    public List<Issue> getSubTaskOfSelectedIssues();

    public void setSubTaskOfSelectedIssues(final List<Issue> subTaskOfSelectedIssues);

    public List<Issue> getIssuesFromSearchRequest();

    public void setIssuesFromSearchRequest(final List<Issue> issuesFromSearchRequest);

    public int getCurrentStep();

    public void setCurrentStep(final int currentStep);

    public Map<String, BulkEditAction> getActions();

    public void setActions(final Map<String, BulkEditAction> actions);

    public Map<String, Object> getFieldValues();

    public Map<String, Object> getFieldValuesHolder();

    public IssueOperation getIssueOperation();

    public Collection<?> getIssuesInUse();

    public BulkEditBean getSubTaskBulkEditBean();

    public void setSubTaskBulkEditBean(final BulkEditBean subTaskBulkEditBean);

    public MultiBulkMoveBean getRelatedMultiBulkMoveBean();

    public void setRelatedMultiBulkMoveBean(final MultiBulkMoveBean relatedMultiBulkMoveBean);

    public boolean isSendBulkNotification();

    public void setSendBulkNotification(final boolean sendBulkNotification);

    // -------------------------------------------------------------------------------- Bulk Workflow Tranistion Methods

    public MultiMap getWorkflowTransitionMap();

    public void setWorkflowTransitionMap(final MultiMap workflowTransitionMap);

    public Set<String> getWorkflowsInUse();

    public List<WorkflowTransitionKey> getTransitionIdsForWorkflow(final String workflowName);

    public String getTransitionName(final String workflowName, final String actionDescriptorId);

    public List<String> getTransitionIssueKeys(final WorkflowTransitionKey workflowTransitionKey);

    public void setSelectedWFTransitionKey(final WorkflowTransitionKey workflowTransitionKey);

    public WorkflowTransitionKey getSelectedWFTransitionKey();

    public void resetWorkflowTransitionSelection();

    public boolean isTransitionChecked(final WorkflowTransitionKey workflowTransitionKey);

    public String getSelectedTransitionName();

    public void setFieldScreenRenderer(final FieldScreenRenderer fieldScreenRenderer);

    public FieldScreenRenderer getFieldScreenRenderer();

    public Map<?, ?> getMessagedFieldLayoutItems();

    public void setMessagedFieldLayoutItems(final Map<?, ?> messagedFieldLayoutItems);

    public void initMultiBulkBean();

    public void initMultiBulkBeanWithSubTasks();

    public boolean isOnlyContainsSubTasks();

    /**
     * Get the transition errors after bulk issue transition
     *
     * @param maxCount max count of errors to return or <code>null</code> for no limits.
     * @return transition errors after bulk issue transition
     */
    public Map<String, Collection<String>> getTransitionErrors(@Nullable Integer maxCount);

    public void addTransitionErrors(@Nonnull String issueKey, @Nonnull Collection<String> errors);

    /**
     * @return <code>true</code> if there is more errors on transitioned issues than <code>maxCount</code>.
     */
    public boolean isTranisitionErrorsLimited(@Nullable Integer maxCount);

    /**
     * If this BulkEditBean contains subtasks of another BulkEditBean, then we can set a pointer back to
     * the BulkEditBean containing the parent issues.
     * This is used so that the subtask issues have access to the <em>new</em> values in their parent issues.
     * See JRA-13937 where we had to ensure that the subtasks in a Bulk Move could get to the new Security Level of
     * their parents.
     *
     * @param parentBulkEditBean The BulkEditBean that contains parent issues of the issues (subtasks); in this BulkEditBean.
     */
    public void setParentBulkEditBean(final BulkEditBean parentBulkEditBean);

    /**
     * If there is a limit on the number of issues that can be bulk edited, this will return that number,
     * otherwise -1.
     *
     * @return -1 to indicate no limit on bulk editing issues, otherwise the number of the limit.
     */
    public int getMaxIssues();

    /**
     * Sets the maximum number of issues allowed to be bulk edited at once. Use -1 to indicate no limit.
     *
     * @param maxIssues either -1 or a positive integer representing the maximum number of issues allowed for bulk edit.
     */
    public void setMaxIssues(final int maxIssues);

    public Map<String, Map<Long, Long>> getFieldSubstitutionMap();

    /**
     * Returns the redirect URL. See {@link #setRedirectUrl(String)}.
     */
    public String getRedirectUrl();

    /**
     * Sets the URL to which user should be redirected once the bulk operation is finished. This is used only if a
     * progress indicator is displayed to the user while the operation is being executed.
     */
    public void setRedirectUrl(String redirectUrl);
}
