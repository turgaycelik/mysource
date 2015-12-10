package com.atlassian.jira.web.bean;

import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkEditAction;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comparator.BeanComparatorIgnoreCase;
import com.atlassian.jira.issue.comparator.KeyComparator;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.BulkTransitionIssueOperation;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.security.IssueSecurityHelper;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.mail.MailFactory;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of BulkEditBean.
 * <p/>
 * BulkEditBean was originally a concrete class, but it has been made abstract in order to separate API and implementation in JIRA.
 *
 * @since v4.3
 */
public class BulkEditBeanImpl implements BulkEditBean
{

    // ------------------------------------------------------------------------------------------------- Type Properties

    private int currentStep = 1;
    private final Set<Integer> availablePreviousSteps = new HashSet<Integer>();
    private Map<String, BulkEditAction> actions;
    private GenericValue project;
    private Set<Long> projectIds;
    private Set<GenericValue> projects;
    private Set<Project> projectObjects;
    private Set<String> issueTypeIds;

    private String operationName;

    // the original list of issues retrieved from the navigator search request
    private List<Issue> issuesFromSearchRequest;

    // currently selected issues (subset of the original set)
    private List<Issue> selectedIssues;
    private List<Issue> subTaskOfSelectedIssues;
    private List<Long> selectedIssueIds;
    private List<Issue> selectedIssuesIncSubTasks;

    private BulkEditBean subTaskBulkEditBean;

    // BulkEditBean can either work with the ALL the issues selected from IssueNavigator
    // or a subset of them as chosen by the user in the first step of bulkedit
    private Collection<?> issuesInUse;
    private final Map<String, Object> fieldValues = new HashMap<String, Object>();
    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private Map<String, String> statusMapHolder = new HashMap<String, String>();

    private Map<String, Map<Long, Long>> fieldSubstitutionMap = new LinkedHashMap<String, Map<Long, Long>>();

    private Map<String, ?> params;

    private Set<FieldLayout> fieldLayouts;

    ///// ------------------------ Move Variables ------------------------ /////
    private Set<String> invalidSubTaskTypes;
    private Map<String, Set<String>> invalidSubTaskStatusesByType;
    private Collection<String> subTaskStatusHolder;

    // Collection of statuses associated with parent issues that are invalid in the target context
    private Set<Status> invalidStatuses;
    private Set<Issue> invalidIssues;
    private Set<?> removedFields;
    private FieldLayout targetFieldLayout;
    private Map<Issue, Issue> targetIssueObjects;
    private JiraWorkflow targetWorkflow;
    private Collection<?> moveFieldLayoutItems;
    private int subTaskCount = -1;
    private int invalidSubTaskCount = -1;

    private Set<String> retainValues;

    private Map<?, ?> messagedFieldLayoutItems;

    // send mail for this bulk operation
    private boolean sendBulkNotification = false;

    // Bulk Workflow Transition
    private MultiMap workflowTransitionMap;
    private WorkflowTransitionKey selectedWFTransitionKey;
    private FieldScreenRenderer fieldScreenRenderer;

    private MultiBulkMoveBean relatedMultiBulkMoveBean;
    private BulkEditBean parentBulkEditBean;
    private final IssueManager issueManager;
    private Map<String, Collection<String>> transitionErrors = new LinkedHashMap<String, Collection<String>>();

    private int maxIssues = -1;
    private String singleIssueKey;
    private String redirectUrl;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BulkEditBeanImpl(final IssueManager issueManager)
    {
        this.issueManager = issueManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public BulkEditBean getParentBulkEditBean()
    {
        return parentBulkEditBean;
    }

    /**
     * Initialises the {@link BulkEditBean} with the selected issues
     *
     * @param selectedIssues Required selected Issues.
     */
    public void initSelectedIssues(final Collection<Issue> selectedIssues)
    {
        this.selectedIssues = null;
        setSelectedIssueIds(selectedIssues);
    }

    public void addIssues(final Collection<Issue> issues)
    {
        selectedIssues = null;
        addIssueIds(issues);
        resetMoveData();
    }

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
    public List<Issue> getSelectedIssues()
    {
        if (selectedIssues == null)
        {
            initSelectedIssues();
        }

        return selectedIssues;
    }

    public List<Issue> getSelectedIssuesIncludingSubTasks()
    {
        if (selectedIssuesIncSubTasks == null)
        {
            initSelectedIssuesIncSubTasks();
        }
        return selectedIssuesIncSubTasks;
    }

    // ------------------------------------------------------------------------------------------- Informational Methods
    public boolean isChecked(final Issue issue)
    {
        return getSelectedIssues().contains(issue);
    }

    public boolean isMultipleProjects()
    {
        return (getProjectIds().size() > 1);
    }

    public boolean isMutipleIssueTypes()
    {
        return (getIssueTypes().size() > 1);
    }

    public GenericValue getProject()
    {
        if (project == null)
        {
            if (!getProjectIds().isEmpty())
            {
                project = getProject(getProjectIds().iterator().next());
            }
        }
        return project;
    }

    public Project getSingleProject()
    {
        Collection<Project> allProjects = getProjectObjects();
        if (allProjects.size() == 1)
        {
            return allProjects.iterator().next();
        }
        else if (allProjects.size() > 1)
        {
            throw new IllegalStateException("Too many Projects only one is allowed");
        }
        else
        {
            throw new IllegalStateException("No projects found.");
        }
    }

    public GenericValue getIssueType()
    {
        if (!getIssueTypes().isEmpty())
        {
            return ComponentAccessor.getConstantsManager().getIssueType(getIssueTypes().iterator().next());
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns all the unique field layouts of the selected issues
     *
     * @return Collection of the FieldLayouts.
     */
    public Collection<FieldLayout> getFieldLayouts()
    {
        if (fieldLayouts == null)
        {
            fieldLayouts = new HashSet<FieldLayout>();

            final FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
            // Iterate over all the selected issues
            for (final Issue issue : getSelectedIssues())
            {
                // For each issue pull out the field layout and add it to the set
                fieldLayouts.add(fieldLayoutManager
                        .getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()));
            }
        }
        return fieldLayouts;
    }

    /**
     * Returns a string that represents a "unique" identifier for this bulke edit bean
     *
     * @return unique key formed from projects, issue types, target project if a subtask only bulkeditbean and size of the bean
     */
    public String getKey()
    {
        final StringBuilder key = new StringBuilder();
        for (final Long projectId : getProjectIds())
        {
            key.append(projectId);
        }

        key.append("_");
        for (final IssueType issueType : getIssueTypeObjects())
        {
            key.append(issueType.getId());
        }
        key.append("_");

        if (isSubTaskOnly())
        {
            key.append(getTargetPid()).append("_");
        }

        return key.toString();
    }

    /**
     * returns a list of project ids for projects which the currently selected issues belong to.
     *
     * @return A list of project ids for projects which the currently selected issues belong to.
     */
    public Collection<Long> getProjectIds()
    {
        if (projectIds == null)
        {
            initIssueProperties();
        }
        return projectIds;
    }

    @Override
    public Collection<GenericValue> getProjects()
    {
        if (projects == null)
        {
            initIssueProperties();
        }
        return projects;
    }

    @Override
    public Collection<Project> getProjectObjects()
    {
        if (projectObjects == null)
        {
            initIssueProperties();
        }
        return projectObjects;
    }

    public Collection<String> getIssueTypes()
    {
        if (issueTypeIds == null)
        {
            initIssueProperties();
        }
        return issueTypeIds;
    }

    public Collection<IssueType> getIssueTypeObjects()
    {
        return Transformed.collection(getIssueTypes(), new Function<String, IssueType>()
        {
            public IssueType get(final String input)
            {
                return ComponentAccessor.getConstantsManager().getIssueTypeObject(input);
            }
        });
    }

    private void initIssueProperties()
    {
        projectIds = new HashSet<Long>();
        projects = new HashSet<GenericValue>();
        projectObjects = new HashSet<Project>();
        issueTypeIds = new HashSet<String>();
        for (final Issue issue : getSelectedIssues())
        {
            final Project project = issue.getProjectObject();
            projectObjects.add(project);
            projects.add(project.getGenericValue());
            projectIds.add(project.getId());
            issueTypeIds.add(issue.getIssueTypeObject().getId());
        }
    }

    private void initInvalidStatuses() throws WorkflowException
    {
        invalidStatuses = Sets.newHashSet();
        invalidIssues = new HashSet<Issue>();

        final JiraWorkflow workflow = getWorkflowManager().getWorkflow(getTargetPid(), getTargetIssueTypeId());
        final List<Status> validStatuses = workflow.getLinkedStatusObjects();

        for (final Issue issue : getSelectedIssues())
        {
            final Status status = issue.getStatusObject();

            if (status == null)
            {
                // Add it to the really screwed list
                invalidIssues.add(issue);
            }

            if (!validStatuses.contains(status))
            {
                invalidStatuses.add(status);
            }
        }
    }

    public String getCheckboxName(final Issue issue)
    {
        return BulkEditBean.BULKEDIT_PREFIX + issue.getId();
    }

    public CustomField getCustomField(final String customFieldKey) throws GenericEntityException
    {
        return ComponentAccessor.getCustomFieldManager().getCustomFieldObject(customFieldKey);
    }

    public String getCustomFieldView(final CustomField customField) throws FieldValidationException
    {
        final Object value = getFieldValues().get(customField.getId());

        // There is a validation that will not allow an edit to occur on a field that has different renderer types
        // defined in the field layout item so if we get here then we know it is safe to grab the first layout
        // item we can find for the field and that this will imply the correct renderer type.
        FieldLayoutItem layoutItem = null;
        if (!getFieldLayouts().isEmpty())
        {
            layoutItem = (getFieldLayouts().iterator().next()).getFieldLayoutItem(customField);
        }
        return customField.getCustomFieldType().getDescriptor().getViewHtml(customField, value, null, layoutItem);
    }

    private void refresh()
    {
        selectedIssueIds = null;
        selectedIssues = null;
        projectIds = null;
        project = null;
    }

    public void setParams(final Map<String, ?> params)
    {
        this.params = params;
        refresh();
    }

    public Map<String, ?> getParams()
    {
        return params;
    }

    public void setIssuesInUse(final Collection<?> issuesInUse)
    {
        this.issuesInUse = issuesInUse;

        // reset all lazy loaded variables
        project = null;
        projectIds = null;
        issueTypeIds = null;
    }

    public void addAvailablePreviousStep(final int stepId)
    {
        availablePreviousSteps.add(new Integer(stepId));
    }

    public void clearAvailablePreviousSteps()
    {
        availablePreviousSteps.clear();
    }

    public boolean isAvailablePreviousStep(final int stepId)
    {
        return availablePreviousSteps.contains(new Integer(stepId));
    }

    /**
     * @return MutableIssue
     * @deprecated Use issue objects directly
     */
    @Deprecated
    private static MutableIssue getIssueObject(final GenericValue genericValue)
    {
        return IssueImpl.getIssueObject(genericValue);
    }

    /**
     * Check if a mail server has been specified.
     *
     * @return boolean  true if a mail server has been specified
     */
    public boolean isHasMailServer()
    {
        try
        {
            if (MailFactory.getServerManager().getDefaultSMTPMailServer() != null)
            {
                return true;
            }
        }
        catch (final Exception e)
        {
            // This isn't the place to die if anything is wrong
        }
        return false;
    }

    ///// ------------------------ Move Issues ------------------------ /////
    public Collection<?> getMoveFieldLayoutItems()
    {
        return moveFieldLayoutItems;
    }

    public void setMoveFieldLayoutItems(final Collection<?> moveFieldLayoutItems)
    {
        this.moveFieldLayoutItems = moveFieldLayoutItems;
    }

    public Long getTargetPid()
    {
        return (Long) getFieldValuesHolder().get(IssueFieldConstants.PROJECT);
    }

    @Override
    public void setTargetProject(final GenericValue project)
    {
        if (project != null)
        {
            getFieldValuesHolder().put(IssueFieldConstants.PROJECT, project.getLong("id"));
        }
        else
        {
            getFieldValuesHolder().put(IssueFieldConstants.PROJECT, null);
        }
    }

    @Override
    public void setTargetProject(final Project project)
    {
        if (project != null)
        {
            getFieldValuesHolder().put(IssueFieldConstants.PROJECT, project.getId());
        }
        else
        {
            getFieldValuesHolder().put(IssueFieldConstants.PROJECT, null);
        }
    }

    public Project getTargetProject()
    {
        return ComponentAccessor.getProjectManager().getProjectObj(getTargetPid());
    }

    public GenericValue getTargetProjectGV()
    {
        return ComponentAccessor.getProjectManager().getProject(getTargetPid());
    }

    public void setTargetIssueTypeId(final String id)
    {
        getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, id);
    }

    public String getTargetIssueTypeId()
    {
        return (String) getFieldValuesHolder().get(IssueFieldConstants.ISSUE_TYPE);
    }

    public GenericValue getTargetIssueTypeGV()
    {
        return ComponentAccessor.getConstantsManager().getIssueType(getTargetIssueTypeId());
    }

    public IssueType getTargetIssueTypeObject()
    {
        return ComponentAccessor.getConstantsManager().getIssueTypeObject(getTargetIssueTypeId());
    }

    // Retrieve the target statuses from the params
    public void populateStatusHolder() throws WorkflowException
    {
        @SuppressWarnings("unchecked")
        final Map<String, String[]> actionParameters = ActionContext.getParameters();
        for (final GenericValue status : getInvalidStatuses())
        {
            final String statusKey = status.getString("id");
            statusMapHolder.put(statusKey, ParameterUtils.getStringParam(actionParameters, statusKey));
        }
    }

    /**
     * Gets a set of invalid statuses that are not valid in the destination workflow
     *
     * @return Set of {@link GenericValue} objects
     * @throws WorkflowException
     */
    public Collection<GenericValue> getInvalidStatuses() throws WorkflowException
    {
        if (invalidStatuses == null)
        {
            initInvalidStatuses();
        }

        return Collections2.transform(invalidStatuses, new StatusToStatusGV());
    }

    /**
     * Gets issues whose status is null
     *
     * @return Set of {@link Issue} objects. Emoty Set if no invalid issues
     * @throws WorkflowException
     */
    public Set<Issue> getInvalidIssues() throws WorkflowException
    {
        if (invalidIssues == null)
        {
            initInvalidStatuses();
        }

        return invalidIssues;
    }

    // Retrieve a collection of sub-task types that are associated with an invalid status in the target context
    public Set<String> getInvalidSubTaskTypes() throws WorkflowException
    {
        if (invalidSubTaskTypes == null)
        {
            invalidSubTaskTypes = new HashSet<String>();
            invalidSubTaskCount = 0;

            // Examine each subtask associated with selected issues - rather than pulling all subtasks into memory
            final Collection<Issue> selectedIssues = getSelectedIssues();

            final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();

            for (final Issue issue : selectedIssues)
            {
                final Collection<GenericValue> subTasks = issue.getSubTasks();

                for (final GenericValue subTask : subTasks)
                {
                    final String subTaskTypeId = subTask.getString("type");
                    final String subTaskStatusId = subTask.getString("status");

                    final GenericValue subTaskStatusGV = constantsManager.getStatus(subTaskStatusId);

                    final JiraWorkflow targetSubTaskWorkflow = getWorkflowForType(getTargetPid(), subTaskTypeId);

                    if (!targetSubTaskWorkflow.getLinkedStatuses().contains(subTaskStatusGV))
                    {
                        invalidSubTaskTypes.add(subTaskTypeId);
                        ++invalidSubTaskCount;

                        // Add the status of this issue to a hashset in our map
                        if ((invalidSubTaskStatusesByType != null) && !invalidSubTaskStatusesByType.isEmpty())
                        {
                            if (invalidSubTaskStatusesByType.containsKey(subTaskTypeId))
                            {
                                final Set<String> invalidStatuses =
                                        new HashSet<String>(invalidSubTaskStatusesByType.get(subTaskTypeId));
                                invalidStatuses.add(subTaskStatusId);
                                invalidSubTaskStatusesByType.put(subTaskTypeId, invalidStatuses);
                            }
                            else
                            {
                                final Set<String> invalidStatuses = new HashSet<String>();
                                invalidStatuses.add(subTaskStatusGV.getString("id"));
                                invalidSubTaskStatusesByType.put(subTaskTypeId, invalidStatuses);
                            }
                        }
                        else
                        {
                            invalidSubTaskStatusesByType = new HashMap<String, Set<String>>();

                            final Set<String> invalidStatuses = new HashSet<String>();
                            invalidStatuses.add(subTaskStatusGV.getString("id"));
                            invalidSubTaskStatusesByType.put(subTaskTypeId, invalidStatuses);
                        }
                    }
                }
            }
        }
        return invalidSubTaskTypes;
    }

    // Retrieve collection of invalid statuses associated with the specified subtask type.
    // Collection retireved from Map: (Key) SubTask Type ID -> (Value) Collection of Invalid SubTask Status IDs
    public Set<String> getInvalidSubTaskStatusesByType(final String subTaskTypeId) throws WorkflowException
    {
        if (invalidSubTaskStatusesByType == null)
        {
            getInvalidSubTaskTypes();
        }

        return invalidSubTaskStatusesByType.get(subTaskTypeId);
    }

    /**
     * Sets the targetFieldLayout to the appropriate FieldLayout (aka "Field Configuration") for the target Project and
     * Issue Type.
     */
    public void setTargetFieldLayout()
    {
        final FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        targetFieldLayout = fieldLayoutManager.getFieldLayout(getTargetProjectGV(), getTargetIssueTypeId());
    }

    public FieldLayout getTargetFieldLayout()
    {
        return targetFieldLayout;
    }

    public FieldLayout getTargetFieldLayoutForType(final String targetTypeId)
    {
        final FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        return fieldLayoutManager.getFieldLayout(getTargetProjectGV(), targetTypeId);
    }

    public JiraWorkflow getTargetWorkflow() throws WorkflowException
    {
        if (targetWorkflow == null)
        {
            targetWorkflow = getWorkflowForType(getTargetPid(), getTargetIssueTypeId());
        }

        return targetWorkflow;
    }

    /**
     * This method is used to get a target issue that will provide the correct context (i.e. project and issue type),
     * for where you are moving to. The object returned is not mapped to a specific selected issue.
     *
     * @return an issue whose project and issue type are of where the you are moving to.
     */
    public Issue getFirstTargetIssueObject()
    {
        if (getTargetIssueObjects().isEmpty())
        {
            return null;
        }
        return getTargetIssueObjects().values().iterator().next();
    }

    public Map<Issue, Issue> getTargetIssueObjects()
    {
        if (targetIssueObjects == null)
        {
            targetIssueObjects = new HashMap<Issue, Issue>();

            for (final Issue selectedIssue : getSelectedIssues())
            {
                // create a clone of the original issue
                final MutableIssue cloneIssue =
                        issueManager.getIssueObject(selectedIssue.getGenericValue().getLong("id"));

                // Setup the new targets context (i.e. project and issue type)
                // Sub-Tasks only change issue type - target project will be same as original
                if (getTargetPid() != null)
                {
                    cloneIssue.setProjectId(getTargetPid());
                }
                cloneIssue.setIssueTypeId(getTargetIssueTypeId());
                // JRA-13937 Now if this BulkEditBean is linked to a parent BulkEditBean,
                // use this parent to try to set the parent of subtasks.
                if ((parentBulkEditBean != null) && cloneIssue.isSubTask())
                {
                    // TODO Unit test this blowing up.
                    final Issue parent = getParentTargetIssue(cloneIssue.getParentId());
                    if (parent == null)
                    {
                        // This really shouldn't happen, but the rest of the code is so complicated that it is better
                        // to fail early just in case.
                        throw new IllegalStateException(
                                "Could not find parent [" + cloneIssue.getParentId() + "] for subtask [" +
                                        cloneIssue.getId() + "] in the parent BulkEditBean.");
                    }
                    cloneIssue.setParentObject(parent);
                }

                // JRA-13990 We should set the security level to null, if the security level field
                // requires a change.  This will enable us to edit the security level via the UI rather than
                // moving to the default target security level.
                final IssueSecurityHelper issueSecurityHelper = getIssueSecurityHelper();
                if (issueSecurityHelper.securityLevelNeedsMove(selectedIssue, cloneIssue))
                {
                    cloneIssue.setSecurityLevel(null);
                }

                targetIssueObjects.put(selectedIssue, cloneIssue);
            }
        }
        return targetIssueObjects;
    }

    /**
     * Attempts to find the Target (edited) Issue with the given ID in the parent BulkEditBean.
     *
     * @param issueId ID of the issue we want.
     * @return Edited Issue object with the given ID.
     */
    private Issue getParentTargetIssue(final Long issueId)
    {
        if (issueId == null)
        {
            return null;
        }
        // loop through the target issues until we find the one with the required ID:
        for (final Issue issue : parentBulkEditBean.getTargetIssueObjects().values())
        {
            if (issueId.equals(issue.getId()))
            {
                return issue;
            }
        }
        return null;
    }

    public void setTargetIssueObjects(final Map<Issue, Issue> targetIssueObjects)
    {
        this.targetIssueObjects = targetIssueObjects;
    }

    /**
     * This is a convinience method for converting the list of objects to a list of GenericValues
     *
     * @return list of GenericValue issue objects
     */
    public List<GenericValue> getTargetIssueGVs()
    {
        final List<GenericValue> gvs = new ArrayList<GenericValue>();
        for (final Issue issue : getTargetIssueObjects().values())
        {
            gvs.add(issue.getGenericValue());
        }
        return gvs;
    }

    @Override
    public Status getTargetStatusObject(Issue issue)
    {
        // call getTargetStatus in case someone has overriden it in a subclass (should be inlined later)
        GenericValue targetStatusGV = getTargetStatus(issue);

        return ComponentAccessor.getConstantsManager()
                .getStatusObject(targetStatusGV != null ? targetStatusGV.getString("id") : null);
    }

    public GenericValue getTargetStatus(final Issue issue)
    {
        final String statusId = statusMapHolder.get(issue.getStatusObject().getId());
        if (statusId != null)
        {
            return ComponentAccessor.getConstantsManager().getStatus(statusId);
        }
        else
        {
            return null;
        }
    }

    private JiraWorkflow getWorkflowForType(final Long projectId, final String issueTypeId) throws WorkflowException
    {
        return getWorkflowManager().getWorkflow(projectId, issueTypeId);
    }

    public Map<String, String> getStatusMapHolder()
    {
        return statusMapHolder;
    }

    public Collection<?> getRemovedFields()
    {
        return removedFields;
    }

    public void setRemovedFields(final Set<?> removedFields)
    {
        this.removedFields = removedFields;
    }

    public void resetMoveData()
    {
        // Reset the issue collection if the 'movefieldlayout' items have been determined
        // as the issue collection will have been updated.
        if ((getMoveFieldLayoutItems() != null) && !getMoveFieldLayoutItems().isEmpty())
        {
            setSelectedIssueIds(getSelectedIssues());
            // TODO: Why set selectedIssues = null when we just relied on this value?
            selectedIssues = null;
        }

        setRemovedFields(null);
        setMoveFieldLayoutItems(null);
        setInvalidSubTaskTypes(null);
        setInvalidSubTaskStatusesByType(null);
        setRetainValues(null);
        setTargetIssueObjects(null);
        setInvalidStatuses(null);
        invalidIssues = null;
        setSubTaskBulkEditBean(null);
        setRelatedMultiBulkMoveBean(null);
        statusMapHolder = new HashMap<String, String>();

        projectIds = null;
        projects = null;
        projectObjects = null;
        issueTypeIds = null;
    }

    // Retrieve the sub task target statuses from the params
    // The subtaskStatusHolder contains a collection of strings constructd as follows:
    // subtaskstatusinfo_subtasktype_originalstatusid_targetstatusid
    public void populateSubTaskStatusHolder() throws WorkflowException
    {
        @SuppressWarnings("unchecked")
        final Map<String, String[]> actionParameters = ActionContext.getParameters();

        subTaskStatusHolder = new ArrayList<String>();

        for (final String actionKey : actionParameters.keySet())
        {
            if (actionKey.indexOf(SUBTASK_STATUS_INFO) != -1)
            {
                final String[] value = actionParameters.get(actionKey);
                if ((value != null) && (value.length != 0))
                {
                    subTaskStatusHolder.add(actionKey + "_" + value[0]);
                }
            }
        }
    }

    // Retrieve the workflow associated with the sub-task in the target project
    public JiraWorkflow getTargetWorkflowByType(final String issueTypeId) throws WorkflowException
    {
        return getWorkflowForType(getTargetPid(), issueTypeId);
    }

    private WorkflowManager getWorkflowManager()
    {
        return ComponentAccessor.getWorkflowManager();
    }

    // ------------------------------------------------------------------------------------------ Private Helper Methods
    private void initSelectedIssuesIncSubTasks()
    {
        final Set<Issue> issueSet = new HashSet<Issue>();
        issueSet.addAll(getSelectedIssues());
        issueSet.addAll(getSubTaskOfSelectedIssues());
        // Sort the list by key
        selectedIssuesIncSubTasks = new ArrayList<Issue>(issueSet);
        //noinspection unchecked
        Collections.sort(selectedIssuesIncSubTasks, new BeanComparatorIgnoreCase("key"));
    }

    private void initSelectedIssues()
    {
        subTaskCount = 0;
        selectedIssues = new ArrayList<Issue>();
        subTaskOfSelectedIssues = new ArrayList<Issue>();
        for (final Long issueId : getSelectedIds())
        {
            final Issue issue = issueManager.getIssueObject(issueId);
            if (issue != null)
            {
                if (issue.isSubTask())
                {
                    subTaskCount++;
                }

                selectedIssues.add(issue);

                subTaskOfSelectedIssues.addAll(issue.getSubTaskObjects());
            }
        }
    }

    private GenericValue getProject(final Long projectId)
    {
        return ComponentAccessor.getProjectManager().getProject(projectId);
    }

    /**
     * Get a list of issue id's corresponding to the issues that have been selected in the first step of bulk edit
     */
    private List<Long> getSelectedIds()
    {
        if (selectedIssueIds == null)
        {
            selectedIssueIds = new ArrayList<Long>();

            if (getParams() == null)
            {
                return selectedIssueIds;
            }

            for (final String issueKey : getParams().keySet())
            {
                if (issueKey.startsWith(BULKEDIT_PREFIX))
                {
                    selectedIssueIds.add(new Long(issueKey.substring(BULKEDIT_PREFIX.length())));
                }
            }
        }
        return selectedIssueIds;
    }

    private void setSelectedIssueIds(final Collection<Issue> issues)
    {
        if (selectedIssueIds == null)
        {
            selectedIssueIds = new ArrayList<Long>();
        }
        else
        {
            selectedIssueIds.clear();
        }
        addIssueIds(issues);
    }

    private void addIssueIds(final Collection<Issue> issues)
    {
        for (final Issue issue : issues)
        {
            selectedIssueIds.add(issue.getId());
        }
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public Collection<String> getSubTaskStatusHolder()
    {
        return subTaskStatusHolder;
    }

    public boolean isRetainChecked(final String fieldId)
    {
        return (retainValues == null) ? false : retainValues.contains(fieldId);
    }

    public boolean isSubTaskCollection()
    {
        return getSubTaskCount() > 0;
    }

    public boolean isSubTaskOnly()
    {
        return getSubTaskCount() == getSelectedIds().size();
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName(final String operationName)
    {
        this.operationName = operationName;
    }

    public void setInvalidSubTaskStatusesByType(final Map<String, Set<String>> invalidSubTaskStatusesByType)
    {
        this.invalidSubTaskStatusesByType = invalidSubTaskStatusesByType;
    }

    public void setInvalidSubTaskTypes(final Set<String> invalidSubTaskTypes)
    {
        this.invalidSubTaskTypes = invalidSubTaskTypes;
    }

    private int getSubTaskCount()
    {
        // The selected issues should be inited before isSubTaskCollection is accurate
        if (selectedIssues == null)
        {
            initSelectedIssues();
        }

        return subTaskCount;
    }

    public int getInvalidSubTaskCount()
    {
        return invalidSubTaskCount;
    }

    public Set<String> getRetainValues()
    {
        return retainValues;
    }

    public void setRetainValues(final Set<String> retainValues)
    {
        this.retainValues = retainValues;
    }

    public void addRetainValue(final String fieldId)
    {
        if (retainValues == null)
        {
            retainValues = new HashSet<String>();
        }

        retainValues.add(fieldId);
    }

    private void setInvalidStatuses(final Set<Status> invalidStatuses)
    {
        this.invalidStatuses = invalidStatuses;
    }

    public List<Issue> getSubTaskOfSelectedIssues()
    {
        return subTaskOfSelectedIssues;
    }

    public void setSubTaskOfSelectedIssues(final List<Issue> subTaskOfSelectedIssues)
    {
        this.subTaskOfSelectedIssues = subTaskOfSelectedIssues;
    }

    public List<Issue> getIssuesFromSearchRequest()
    {
        return issuesFromSearchRequest;
    }

    public void setIssuesFromSearchRequest(final List<Issue> issuesFromSearchRequest)
    {
        this.issuesFromSearchRequest = issuesFromSearchRequest;
    }

    public int getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(final int currentStep)
    {
        this.currentStep = currentStep;
    }

    public Map<String, BulkEditAction> getActions()
    {
        return actions;
    }

    public void setActions(final Map<String, BulkEditAction> actions)
    {
        this.actions = actions;
    }

    public Map<String, Object> getFieldValues()
    {
        return fieldValues;
    }

    public Map<String, Object> getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        final BulkOperationManager bulkOperationManager = ComponentAccessor.getBulkOperationManager();
        if (BulkWorkflowTransitionOperation.NAME.equals(getOperationName()))
        {
            final JiraWorkflow workflow = ComponentAccessor.getWorkflowManager().getWorkflow(
                    getSelectedWFTransitionKey().getWorkflowName());
            final ActionDescriptor action = workflow.getDescriptor()
                    .getAction(Integer.parseInt(getSelectedWFTransitionKey().getActionDescriptorId()));
            // Need to construct a new object as we need to pass in the Action Descriptor of the workflow action that is
            // being executed. Cannot get away with a singleton here.
            return new BulkTransitionIssueOperation(bulkOperationManager.getProgressAwareOperation(getOperationName()),
                    action);
        }
        else
        {
            return bulkOperationManager.getOperation(getOperationName());
        }
    }

    public Collection<?> getIssuesInUse()
    {
        return issuesInUse;
    }

    public BulkEditBean getSubTaskBulkEditBean()
    {
        return subTaskBulkEditBean;
    }

    public void setSubTaskBulkEditBean(final BulkEditBean subTaskBulkEditBean)
    {
        this.subTaskBulkEditBean = subTaskBulkEditBean;
    }

    public MultiBulkMoveBean getRelatedMultiBulkMoveBean()
    {
        return relatedMultiBulkMoveBean;
    }

    public void setRelatedMultiBulkMoveBean(final MultiBulkMoveBean relatedMultiBulkMoveBean)
    {
        this.relatedMultiBulkMoveBean = relatedMultiBulkMoveBean;
    }

    public boolean isSendBulkNotification()
    {
        return sendBulkNotification;
    }

    public void setSendBulkNotification(final boolean sendBulkNotification)
    {
        this.sendBulkNotification = sendBulkNotification;
    }

    // -------------------------------------------------------------------------------- Bulk Workflow Tranistion Methods

    public MultiMap getWorkflowTransitionMap()
    {
        return workflowTransitionMap;
    }

    public void setWorkflowTransitionMap(final MultiMap workflowTransitionMap)
    {
        this.workflowTransitionMap = workflowTransitionMap;
    }

    public Set<String> getWorkflowsInUse()
    {
        @SuppressWarnings("unchecked")
        final Set<WorkflowTransitionKey> workflowTransitionMapKeys = workflowTransitionMap.keySet();
        return CollectionUtil.transformSet(workflowTransitionMapKeys, new Function<WorkflowTransitionKey, String>()
        {
            public String get(final WorkflowTransitionKey input)
            {
                return input.getWorkflowName();
            }
        });
    }

    public List<WorkflowTransitionKey> getTransitionIdsForWorkflow(final String workflowName)
    {
        @SuppressWarnings("unchecked")
        final Collection<WorkflowTransitionKey> workflowTransitionMaps = workflowTransitionMap.keySet();
        final List<WorkflowTransitionKey> transitionIdsForWorkflow = new ArrayList<WorkflowTransitionKey>();

        for (final WorkflowTransitionKey wfTransitionKey : workflowTransitionMaps)
        {
            if (wfTransitionKey.getWorkflowName().equals(workflowName))
            {
                transitionIdsForWorkflow.add(wfTransitionKey);
            }
        }

        //Sorting the list by actiondescriptor, to ensure that if there's two lines
        //for an action descriptor they'll be grouped (see JRA-12017 about how this may happen).
        Collections.sort(transitionIdsForWorkflow, new Comparator<WorkflowTransitionKey>()
        {
            public int compare(final WorkflowTransitionKey o1, final WorkflowTransitionKey o2)
            {
                return o1.getActionDescriptorId().compareTo(o2.getActionDescriptorId());
            }
        });
        return transitionIdsForWorkflow;
    }

    public String getTransitionName(final String workflowName, final String actionDescriptorId)
    {
        final JiraWorkflow workflow = ComponentAccessor.getWorkflowManager().getWorkflow(workflowName);
        final ActionDescriptor descriptor = workflow.getDescriptor().getAction(Integer.parseInt(actionDescriptorId));

        @SuppressWarnings("unchecked")
        final Map<String, String> metadata = descriptor.getMetaAttributes();
        if (metadata.containsKey(JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED))
        {
            final String key = metadata.get(JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED);
            final I18nBean i18nBean = new I18nBean();
            final String value = i18nBean.getText(key);
            if ((value != null) && !"".equals(value.trim()) && !value.trim().equals(key.trim()))
            {
                return value;
            }
        }
        return descriptor.getName();
    }

    public List<String> getTransitionIssueKeys(final WorkflowTransitionKey workflowTransitionKey)
    {
        @SuppressWarnings("unchecked")
        final List<String> workflowIssues =
                new ArrayList<String>((List<String>) workflowTransitionMap.get(workflowTransitionKey));

        // Sort the list
        Collections.sort(workflowIssues, KeyComparator.COMPARATOR);

        return workflowIssues;
    }

    public void setSelectedWFTransitionKey(final WorkflowTransitionKey workflowTransitionKey)
    {
        selectedWFTransitionKey = workflowTransitionKey;
    }

    public WorkflowTransitionKey getSelectedWFTransitionKey()
    {
        return selectedWFTransitionKey;
    }

    public void resetWorkflowTransitionSelection()
    {
        selectedWFTransitionKey = null;
    }

    public boolean isTransitionChecked(final WorkflowTransitionKey workflowTransitionKey)
    {
        return (selectedWFTransitionKey != null) && selectedWFTransitionKey.equals(workflowTransitionKey);
    }

    public String getSelectedTransitionName()
    {
        return getTransitionName(selectedWFTransitionKey.getWorkflowName(),
                selectedWFTransitionKey.getActionDescriptorId());
    }

    public void setFieldScreenRenderer(final FieldScreenRenderer fieldScreenRenderer)
    {
        this.fieldScreenRenderer = fieldScreenRenderer;
    }

    public FieldScreenRenderer getFieldScreenRenderer()
    {
        return fieldScreenRenderer;
    }

    // ------------------------------------------------------------------------------------------- Bullshit test methods

    /**
     * Use for testing ONLY
     *
     * @param issues
     * @deprecated
     */
    @Deprecated
    public void _setSelectedIssueGVsForTesting(final List<GenericValue> issues)
    {
        if (issues != null)
        {
            selectedIssues = new ArrayList<Issue>(issues.size());
            for (final GenericValue issueGV : issues)
            {
                selectedIssues.add(getIssueObject(issueGV));
            }
        }
        else
        {
            selectedIssues = null;
        }
    }

    public Map<?, ?> getMessagedFieldLayoutItems()
    {
        return messagedFieldLayoutItems;
    }

    public void setMessagedFieldLayoutItems(final Map<?, ?> messagedFieldLayoutItems)
    {
        this.messagedFieldLayoutItems = messagedFieldLayoutItems;
    }

    public void initMultiBulkBean()
    {
        final MultiBulkMoveBean multiBulkMoveBean = new MultiBulkMoveBeanImpl(getOperationName(), issueManager);
        multiBulkMoveBean.initFromIssues(getSelectedIssues(), null);
        initSingleIssueMultiBulkMoveBean(multiBulkMoveBean);
        setRelatedMultiBulkMoveBean(multiBulkMoveBean);
    }

    public void initMultiBulkBeanWithSubTasks()
    {
        // Get list of subtasks that need to also be moved.
        final List<Issue> selectedIssues = getSubTaskBulkEditBean().getSelectedIssues();
        final MultiBulkMoveBean multiBulkMoveBean = new MultiBulkMoveBeanImpl(getOperationName(), issueManager);
        multiBulkMoveBean.initFromIssues(selectedIssues, this);
        initSingleIssueMultiBulkMoveBean(multiBulkMoveBean);

        // Set all the BEBs with the current target project
        multiBulkMoveBean.setTargetProject(getTargetProjectGV());
        setRelatedMultiBulkMoveBean(multiBulkMoveBean);
    }

    /**
     * Initializes related beans of <code>multiBulkMoveBean</code> with
     * {@link #singleIssueKey}. This is necessary when rendering i.e. view for subtasks
     * and we need also information regarding related parent issue.
     *
     * @see #getSingleIssueKey()
     * @see MultiBulkMoveBean#getBulkEditBeans()
     */
    private void initSingleIssueMultiBulkMoveBean(final MultiBulkMoveBean multiBulkMoveBean)
    {
        ListOrderedMap bulkEditBeans = multiBulkMoveBean.getBulkEditBeans();
        List valueList = bulkEditBeans.valueList();
        for (Object editBean : valueList)
        {
            ((BulkEditBean) editBean).setSingleIssueKey(singleIssueKey);
        }
    }

    public boolean isOnlyContainsSubTasks()
    {
        return getSubTaskCount() == getSelectedIssues().size();
    }

    /**
     * If this BulkEditBean contains subtasks of another BulkEditBean, then we can set a pointer back to
     * the BulkEditBean containing the parent issues.
     * This is used so that the subtask issues have access to the <em>new</em> values in their parent issues.
     * See JRA-13937 where we had to ensure that the subtasks in a Bulk Move could get to the new Security Level of
     * their parents.
     *
     * @param parentBulkEditBean The BulkEditBean that contains parent issues of the issues (subtasks) in this BulkEditBean.
     */
    public void setParentBulkEditBean(final BulkEditBean parentBulkEditBean)
    {
        this.parentBulkEditBean = parentBulkEditBean;
    }

    IssueSecurityHelper getIssueSecurityHelper()
    {
        return ComponentAccessor.getComponent(IssueSecurityHelper.class);
    }

    /**
     * If there is a limit on the number of issues that can be bulk edited, this will return that number,
     * otherwise -1.
     *
     * @return -1 to indicate no limit on bulk editing issues, otherwise the number of the limit.
     */
    public int getMaxIssues()
    {
        return maxIssues;
    }

    /**
     * Sets the maximum number of issues allowed to be bulk edited at once. Use -1 to indicate no limit.
     *
     * @param maxIssues either -1 or a positive integer representing the maximum number of issues allowed for bulk edit.
     */
    public void setMaxIssues(final int maxIssues)
    {
        if (maxIssues < -1)
        {
            throw new IllegalArgumentException("max issues for bulk edit cannot be " + maxIssues);
        }
        this.maxIssues = maxIssues;
    }

    public Map<String, Map<Long, Long>> getFieldSubstitutionMap()
    {
        return fieldSubstitutionMap;
    }

    @Override
    public Map<String, Collection<String>> getTransitionErrors(@Nullable Integer maxCount)
    {
        if (maxCount != null && isTranisitionErrorsLimited(maxCount))
        {
            final Set<String> limitedKeys = Sets.newLinkedHashSet(Iterables.limit(transitionErrors.keySet(), maxCount));
            return Maps.filterKeys(transitionErrors, new Predicate<String>()
            {
                @Override
                public boolean apply(String key)
                {
                    return limitedKeys.contains(key);
                }
            });
        }
        return transitionErrors;
    }

    @Override
    public void addTransitionErrors(@Nonnull String issueKey, @Nonnull Collection<String> errors)
    {
        transitionErrors.put(issueKey, errors);
    }

    public boolean isTranisitionErrorsLimited(@Nullable Integer maxCount)
    {
        return maxCount != null && transitionErrors.size() > maxCount;
    }

    private static class StatusToStatusGV implements com.google.common.base.Function<Status, GenericValue>
    {
        @Override
        public GenericValue apply(@Nullable Status status)
        {
            return status != null ? status.getGenericValue() : null;
        }
    }

    @Override
    public boolean isSingleMode()
    {
        return singleIssueKey != null;
    }

    @Override
    public void setSingleIssueKey(String singleIssueKey)
    {
        this.singleIssueKey = singleIssueKey;
    }

    @Override
    public String getSingleIssueKey()
    {
        return this.singleIssueKey;
    }

    @Override
    public String getRedirectUrl()
    {
        return this.redirectUrl;
    }

    @Override
    public void setRedirectUrl(String redirectUrl)
    {
        this.redirectUrl = redirectUrl;
    }
}
