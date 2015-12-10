package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.issueview.IssueViewFieldParams;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Purpose of this class is to enable custom XML issue views.
 * This class is a wrapper of {@link com.atlassian.jira.web.FieldVisibilityManager}
 * If field parameters for issue view were defined correctly they are evaluated first
 * and only for requested fields visibiliy is checked with {@link com.atlassian.jira.web.FieldVisibilityManager}.
 *
 * For non orderable fields checking with {@link com.atlassian.jira.web.FieldVisibilityManager} is skipped and true value is returned.
 * Another exception are time tracking fields, which are evaluated based on {@link com.atlassian.jira.issue.IssueFieldConstants#TIMETRACKING}
 * field.
 */
public class CustomIssueXMLViewFieldsBean
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final IssueViewFieldParams issueViewFieldParams;
    private final Long projectId;
    private final String issueTypeId;
    private final Set<String> timetrackingFieldIds;

    public CustomIssueXMLViewFieldsBean(final FieldVisibilityManager fieldVisibilityManager,
            final IssueViewFieldParams issueViewFieldParams, final Long projectId,
            final String issueTypeId)
    {
        this.projectId = projectId;
        this.issueTypeId = issueTypeId;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.issueViewFieldParams = issueViewFieldParams;

        Set<String> fieldIds = new HashSet<String>();
        fieldIds.add(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE);
        fieldIds.add(IssueFieldConstants.TIME_ESTIMATE);
        fieldIds.add(IssueFieldConstants.TIME_SPENT);
        fieldIds.add(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE);
        fieldIds.add(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE);
        fieldIds.add(IssueFieldConstants.AGGREGATE_TIME_SPENT);
        timetrackingFieldIds = Collections.unmodifiableSet(fieldIds);
    }

    /**
     *
     * @param  fieldId
     * @return true if field is defiend in custom view parameters and not hidden,
     *          or just not hidden if no view parameters were specified
     *          or is non orderable field
     */
    public boolean isFieldRequestedAndVisible(String fieldId)
    {
        boolean visible = false;

        if (issueViewFieldParams.isCustomViewRequested())
        {
            if (issueViewFieldParams.getFieldIds().contains(fieldId))
            {
                visible = checkFieldVisible(fieldId, projectId, issueTypeId);
            }
        }
        else
        {
            visible = checkFieldVisible(fieldId, projectId, issueTypeId);
        }
        return visible;
    }

    private boolean checkFieldVisible(final String fieldId, final Long projectId, final String issueTypeId)
    {
        // JRA-16508 : we always want to display the attachments regardless of whether they are "hidden" or not
        if (IssueFieldConstants.ATTACHMENT.equals(fieldId))
        {
            return true;
        }

        if (timetrackingFieldIds.contains(fieldId))
        {
            return fieldVisibilityManager.isFieldVisible(projectId, IssueFieldConstants.TIMETRACKING, issueTypeId);
        }
        else
        {
            if (issueViewFieldParams.getOrderableFieldIds().contains(fieldId))
            {
                return fieldVisibilityManager.isFieldVisible(projectId, fieldId, issueTypeId);
            }
            else
            {
                return true;
            }
        }
    }
}
