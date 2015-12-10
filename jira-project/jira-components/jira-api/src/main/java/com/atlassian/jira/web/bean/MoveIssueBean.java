package com.atlassian.jira.web.bean;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.ProjectManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bean recording new data for issue
 */
public class MoveIssueBean implements OperationContext
{
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;

    private int currentStep;
    private Long issueId;
    private final Set<Integer> availablePreviousSteps;
    private String targetStatusId;
    private final Map<String, Object> fieldValuesHolder;
    private Map fieldValues;
    // This map consists of a key (in the form SUBTASK_TARGET_PREFIX + Task Type Id + Status Type Id) and value (target value for this task type/status pair)
    private Map taskTargetStatusHolder;
    private Collection moveFieldLayoutItems;
    private Collection removedFields;
    private MutableIssue updatedIssue;
    private String sourceIssueKey;

    public MoveIssueBean(ConstantsManager constantsManager, ProjectManager projectManager)
    {
        this.currentStep = 1;
        this.issueId = null;
        this.availablePreviousSteps = new HashSet<Integer>();
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
        this.fieldValuesHolder = new HashMap<String, Object>();
        // These collections should not be modified, just reset if necessary
        this.moveFieldLayoutItems = Collections.EMPTY_LIST;
        this.removedFields = Collections.EMPTY_LIST;
    }

    public int getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(int currentStep)
    {
        this.currentStep = currentStep;
    }

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
    }

    public void addAvailablePreviousStep(int stepId)
    {
        availablePreviousSteps.add(Integer.valueOf(stepId));
    }

    public void clearAvailablePreviousSteps()
    {
        availablePreviousSteps.clear();
    }

    public boolean isAvailablePreviousStep(int stepId)
    {
        return availablePreviousSteps.contains(Integer.valueOf(stepId));
    }

    public Long getTargetPid()
    {
        return (Long) getFieldValuesHolder().get(IssueFieldConstants.PROJECT);
    }

    // Used in the summary pane of the move issue wizard
    public String getTargetProjectName()
    {
        return projectManager.getProjectObj(getTargetPid()).getName();
    }


    public String getTargetIssueType()
    {
        return (String) getFieldValuesHolder().get(IssueFieldConstants.ISSUE_TYPE);
    }

    // Used in the summary pane of the move issue wizard
    public String getTargetTypeName()
    {
        IssueType issueType = constantsManager.getIssueTypeObject(getTargetIssueType());
        return issueType.getNameTranslation();
    }

    public String getTargetStatusId()
    {
        return targetStatusId;
    }

    public void setTargetStatusId(String targetStatusId)
    {
        this.targetStatusId = targetStatusId;
    }

    public String getTargetStatusName()
    {
        Status status = constantsManager.getStatusObject(getTargetStatusId());
        return status.getNameTranslation();
    }

    public void reset()
    {
        setTargetStatusId(null);
        setTaskTargetStatusHolder(null);
        getFieldValuesHolder().clear();
        setFieldValues(new HashMap());
        setMoveFieldLayoutItems(Collections.EMPTY_LIST);
        setRemovedFields(Collections.EMPTY_LIST);
        setUpdatedIssue(null);
    }

    public Map<String, Object> getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        return IssueOperations.MOVE_ISSUE_OPERATION;
    }

    public Map getFieldValues()
    {
        return fieldValues;
    }

    public void setFieldValues(Map fieldValues)
    {
        this.fieldValues = fieldValues;
    }

    public Map getTaskTargetStatusHolder()
    {
        return taskTargetStatusHolder;
    }

    public void setTaskTargetStatusHolder(Map taskTargetStatusHolder)
    {
        this.taskTargetStatusHolder = taskTargetStatusHolder;
    }

    public Collection getMoveFieldLayoutItems()
    {
        return moveFieldLayoutItems;
    }

    public void setMoveFieldLayoutItems(Collection moveFieldLayoutItems)
    {
        this.moveFieldLayoutItems = moveFieldLayoutItems;
    }

    public void setUpdatedIssue(MutableIssue targetIssue)
    {
        this.updatedIssue = targetIssue;
    }

    public MutableIssue getUpdatedIssue()
    {
        return updatedIssue;
    }

    public Collection getRemovedFields()
    {
        return removedFields;
    }

    public void setRemovedFields(Collection removeds)
    {
        this.removedFields = removeds;
    }

    public String getSourceIssueKey()
    {
        return sourceIssueKey;
    }

    public void setSourceIssueKey(final String sourceIssueKey)
    {
        this.sourceIssueKey = sourceIssueKey;
    }
}
