package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;
import com.atlassian.jira.issue.fields.rest.json.beans.TimeTrackingJsonBean;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v5.0
 */
public class TimeTrackingRestFieldOperationsHandler implements RestFieldOperationsHandler
{

    private final TimeTrackingSystemField field;
    private final ApplicationProperties applicationProperties;
    private final JiraDurationUtils jiraDurationUtils;
    private final I18nHelper i18nHelper;

    public TimeTrackingRestFieldOperationsHandler(TimeTrackingSystemField field, ApplicationProperties applicationProperties, JiraDurationUtils jiraDurationUtils, I18nHelper i18nHelper)
    {
        this.field = field;
        this.applicationProperties = applicationProperties;
        this.jiraDurationUtils = jiraDurationUtils;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName(), StandardOperation.EDIT.getName());
    }

    @Override
    public ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        if (operations.isEmpty())
        {
            return errors;
        }

        if (operations.size() > 1)
        {
            errors.addError(IssueFieldConstants.TIMETRACKING, i18nHelper.getText("rest.operations.morethanone", String.valueOf(operations.size()), fieldId));
            return errors;
        }
        FieldOperationHolder fieldOperationHolder = operations.get(0);
        StandardOperation standardOperation = StandardOperation.valueOf(fieldOperationHolder.getOperation().toUpperCase());
        TimeTrackingJsonBean timeTrackingJsonBean = fieldOperationHolder.getData().convertValue(IssueFieldConstants.TIMETRACKING, TimeTrackingJsonBean.class, errors);
        if (errors.hasAnyErrors())
        {
            return errors;
        }
        switch (standardOperation)
        {
            case SET:
                performSetOperation(timeTrackingJsonBean, inputParameters, issue, errors);
                break;
            case EDIT:
                performEditOperation(timeTrackingJsonBean, inputParameters, issue, errors);
                break;
        }
        return errors;
    }

    private void performSetOperation(TimeTrackingJsonBean timeTrackingJsonBean, IssueInputParameters inputParameters, Issue issue, ErrorCollection errors)
    {
        String estimate = timeTrackingJsonBean.getRemainingEstimate();
        String originalEstimate = timeTrackingJsonBean.getOriginalEstimate();
        final Map<String, String[]> actionParameters = inputParameters.getActionParameters();

        if (timeTrackingJsonBean.getTimeSpent() != null)
        {
            errors.addError(IssueFieldConstants.TIMETRACKING, i18nHelper.getText("rest.timetracking.cannot.set.timespent"), ErrorCollection.Reason.VALIDATION_FAILED);
            return;
        }

        if (applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR))
        {
            if (originalEstimate != null)
            {
                if (issue != null && field.hasWorkStarted(issue))
                {
                    errors.addError(IssueFieldConstants.TIMETRACKING, i18nHelper.getText("rest.timetracking.cannot.set.original.work.started"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
                else if (estimate != null)
                {
                    errors.addError(IssueFieldConstants.TIMETRACKING, i18nHelper.getText("rest.timetracking.cannot.set.both.legacy"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
                else
                {
                    actionParameters.put(IssueFieldConstants.TIMETRACKING, new String[] {originalEstimate});
                }
            }
            else
            {
                actionParameters.put(IssueFieldConstants.TIMETRACKING, new String[] {estimate});
            }
        }
        else
        {
            actionParameters.put(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, new String[] { originalEstimate });
            actionParameters.put(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, new String[] { estimate });
            // Need to add a placeholder with the system field name, just so the field is found in the imput params.
            actionParameters.put(IssueFieldConstants.TIMETRACKING, new String[] {});
        }
    }

    private void performEditOperation(TimeTrackingJsonBean timeTrackingJsonBean, IssueInputParameters inputParameters, Issue issue, ErrorCollection errors)
    {
        // If the individual values are not supplied on edit, we will just copy the current values forward.
        String currentEstimate = issue.getEstimate() == null ? null : jiraDurationUtils.getShortFormattedDuration(issue.getEstimate());
        String currentOriginalEstimate = issue.getOriginalEstimate() == null ? null : jiraDurationUtils.getShortFormattedDuration(issue.getOriginalEstimate());

        String estimate = timeTrackingJsonBean.getRemainingEstimate();
        String originalEstimate = timeTrackingJsonBean.getOriginalEstimate();

        final Map<String, String[]> actionParameters = inputParameters.getActionParameters();

        if (timeTrackingJsonBean.getTimeSpent() != null)
        {
            errors.addError(IssueFieldConstants.TIMETRACKING, i18nHelper.getText("rest.timetracking.cannot.set.timespent"), ErrorCollection.Reason.VALIDATION_FAILED);
            return;
        }

        if (applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR))
        {
            actionParameters.put(IssueFieldConstants.TIMETRACKING, new String[] {estimate});
            if (originalEstimate != null)
            {
                 errors.addError(IssueFieldConstants.TIMETRACKING, i18nHelper.getText("rest.timetracking.cannot.set.original.work.started"), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        else
        {
            if (originalEstimate != null)
            {
                actionParameters.put(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, new String[] { originalEstimate });
            }
            else
            {
                actionParameters.put(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, new String[] { currentOriginalEstimate });
            }
            if (estimate != null)
            {
                actionParameters.put(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, new String[] { estimate });
            }
            else
            {
                actionParameters.put(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, new String[] { currentEstimate});
            }
            // Need to add a placeholder with the system field name, just so the field is found in the imput params.
            actionParameters.put(IssueFieldConstants.TIMETRACKING, new String[] {});
        }
    }

}
