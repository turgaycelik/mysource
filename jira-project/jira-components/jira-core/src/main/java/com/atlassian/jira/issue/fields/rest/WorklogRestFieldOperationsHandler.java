package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.WorklogSystemField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v5.0
 */
public class WorklogRestFieldOperationsHandler implements RestFieldOperationsHandler
{
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final ProjectRoleManager projectRoleManager;
    private final I18nHelper i18n;

    private static final String TIME_SPENT_INPUT = "timeSpent";
    private static final String STARTED_INPUT = "started";
    public static final String ADJUST_REMAINING_ESTIMATE_INPUT = "adjustEstimate";
    public static final String NEW_ESTIMATE_INPUT = "newEstimate";
    public static final String REDUCE_BY_INPUT = "reduceBy";
    public static final String INCREASE_BY_INPUT = "increaseBy";

    public WorklogRestFieldOperationsHandler(DateTimeFormatterFactory dateTimeFormatterFactory, ProjectRoleManager projectRoleManager, I18nHelper i18n)
    {
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.projectRoleManager = projectRoleManager;
        this.i18n = i18n;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName());
    }

    @Override
    public ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations)
    {
        ErrorCollection errors = new SimpleErrorCollection();

        if (operations.size() > 1)
        {
            errors.addError(IssueFieldConstants.WORKLOG, i18n.getText("rest.worklog.error.tomanyoperations"));
            return errors;
        }

        for (FieldOperationHolder op : operations)
        {
            StandardOperation standardOperation = StandardOperation.valueOf(op.getOperation().toUpperCase());

            final JsonData data = op.getData();
            if (!data.isObject())
            {
                errors.addError(IssueFieldConstants.WORKLOG, i18n.getText("rest.worklog.error.requiredObject"));
                continue;
            }

            switch (standardOperation)
            {
                case ADD:
                    performAddOperation(issue, data, inputParameters, errors);
                    break;
                default:
                    errors.addError(IssueFieldConstants.WORKLOG, i18n.getText("rest.worklog.error.operationsSupported"));

            }
        }
        return errors;
    }

    private void performAddOperation(Issue issue, JsonData data, IssueInputParameters inputParameters, ErrorCollection errors)
    {
        String timeSpent = data.getObjectStringProperty("timeSpent", "timeSpent", errors);
        Date start = data.getObjectDateProperty("started", errors);
        if (!errors.hasAnyErrors())
        {
            final Map<String, String[]> actionParameters = inputParameters.getActionParameters();
            actionParameters.put(IssueFieldConstants.WORKLOG, new String[] { });
            actionParameters.put(WorklogSystemField.WORKLOG_ACTIVATE, new String[] { "true" });
            if (issue == null)
            {
                actionParameters.put(WorklogSystemField.PARAM_ISCREATEISSUE, new String[] { "true" });
            }
            if (timeSpent != null)
            {
                actionParameters.put(WorklogSystemField.WORKLOG_TIMELOGGED, new String[] { timeSpent });
            }
            
            DateTimeFormatter userDateTimeFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.DATE_TIME_PICKER);
            actionParameters.put(WorklogSystemField.WORKLOG_STARTDATE, new String[] { userDateTimeFormatter.format(start != null ? start : new Date()) });
            addAdjustmentParams(data, actionParameters, errors);
            addCommentParams(data, actionParameters, errors);
            addVisibilityParams(data, actionParameters, errors);
        }
    }

    private void addCommentParams(JsonData data, final Map<String, String[]> actionParameters, com.atlassian.jira.util.ErrorCollection errors)
    {
        String comment = data.getObjectStringProperty("comment", "comment", errors);
        if (StringUtils.isNotBlank(comment))
        {
            actionParameters.put(WorklogSystemField.WORKLOG_DESCRIPTION, new String[]{comment});
        }
    }

    private void addVisibilityParams(JsonData data, final Map<String, String[]> actionParameters, ErrorCollection errors)
    {
        Map<String, Object> stringObjectMap = data.asObject();
        Map<String, Object> visibilityData = (Map<String, Object>) stringObjectMap.get("visibility");

        if (visibilityData != null)
        {
            String visibilityType = (String) visibilityData.get("type");
            if (visibilityType != null)
            {
                VisibilityJsonBean.VisibilityType type = VisibilityJsonBean.VisibilityType.valueOf(visibilityType.toLowerCase());
                if (type != null)
                {
                    String securityLevel = null;
                    if (type.equals(VisibilityJsonBean.VisibilityType.group))
                    {
                        String group = (String) visibilityData.get("value");
                        if (StringUtils.isNotEmpty(group))
                        {
                            securityLevel = CommentVisibility.getCommentLevelFromLevels(group, null);
                        }
                        else
                        {
                            errors.addError(IssueFieldConstants.COMMENT, i18n.getText("rest.worklog.error.visibility.group.missing"));
                        }
                    }
                    else
                    {
                        String roleLevel = (String) visibilityData.get("value");
                        if (StringUtils.isNotEmpty(roleLevel))
                        {
                            ProjectRole projectRole = projectRoleManager.getProjectRole(roleLevel);
                            Long roleLevelId;
                            if (projectRole != null)
                            {
                                roleLevelId = projectRole.getId();
                            }
                            else
                            {
                                try
                                {
                                    roleLevelId = Long.valueOf(roleLevel);
                                }
                                catch (NumberFormatException ex)
                                {
                                    errors.addError(IssueFieldConstants.COMMENT, i18n.getText("rest.worklog.error.visibility.role.invalid"));
                                    return;
                                }
                            }
                            securityLevel = CommentVisibility.getCommentLevelFromLevels(null, roleLevelId);
                        }
                        else
                        {
                            errors.addError(IssueFieldConstants.COMMENT, i18n.getText("rest.worklog.error.visibility.role.missing"));
                        }
                    }

                    final List<String> commentSecurity = new ArrayList<String>();
                    commentSecurity.add(securityLevel);
                    actionParameters.put(WorklogSystemField.WORKLOG_VISIBILITY_LEVEL, commentSecurity.toArray(new String[commentSecurity.size()]));
                }
                else
                {
                    errors.addError("visibility", i18n.getText("rest.worklog.error.visibility.type.invalid", visibilityType));
                }
            }
        }
    }

    private void addAdjustmentParams(JsonData data, final Map<String, String[]> actionParameters, com.atlassian.jira.util.ErrorCollection errors)
    {
        String adjustmentString = data.getObjectStringProperty(ADJUST_REMAINING_ESTIMATE_INPUT, ADJUST_REMAINING_ESTIMATE_INPUT, errors);
        if (adjustmentString == null)
        {
            actionParameters.put(WorklogSystemField.WORKLOG_ADJUSTESTIMATE, new String[] { WorklogSystemField.WorklogValue.AdjustEstimate.AUTO.name() });
            return;
        }

        WorklogSystemField.WorklogValue.AdjustEstimate mode = WorklogSystemField.WorklogValue.AdjustEstimate.valueOf(adjustmentString.toUpperCase());
        if (mode == null)
        {
            errors.addError(ADJUST_REMAINING_ESTIMATE_INPUT, i18n.getText("rest.worklog.error.adjustEstimate.invalid", Arrays.toString(WorklogSystemField.WorklogValue.AdjustEstimate.values())));
            return;
        }

        actionParameters.put(WorklogSystemField.WORKLOG_ADJUSTESTIMATE, new String[] { mode.name() });
        switch (mode)
        {
            case NEW:
                String newEstimate = data.getObjectStringProperty(NEW_ESTIMATE_INPUT, NEW_ESTIMATE_INPUT, errors);
                if (newEstimate == null)
                {
                    errors.addError("newEstimate", i18n.getText("rest.worklog.error.adjustEstimate.new.newEstimate.missing"));
                }
                else
                {
                    actionParameters.put(WorklogSystemField.WORKLOG_NEWESTIMATE, new String[] { newEstimate });
                }
                break;
            case MANUAL:
                String reduceBy = data.getObjectStringProperty(REDUCE_BY_INPUT, REDUCE_BY_INPUT, errors);
                if (reduceBy == null)
                {
                    errors.addError("reduceBy", i18n.getText("rest.worklog.error.adjustEstimate.manual.reduceBy.missing"));
                }
                else
                {
                    actionParameters.put(WorklogSystemField.WORKLOG_ADJUSTMENTAMOUNT, new String[] { reduceBy });
                }
                break;
        }
    }

}
