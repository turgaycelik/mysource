package com.atlassian.jira.web.action.issue.util;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;
import com.atlassian.jira.issue.fields.WorklogSystemField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;

/**
 * Helper class which contains the logic required to resolve which Field Screen Tabs contain errors, based on the error
 * collection supplied.
 *
 * @see com.atlassian.jira.web.action.issue.EditIssue
 * @see com.atlassian.jira.web.action.issue.CreateIssue
 * @see com.atlassian.jira.web.action.issue.CommentAssignIssue
 * @see com.atlassian.jira.web.action.issue.bulkedit.BulkWorkflowTransition
 * @since v4.2
 */
public class ScreenTabErrorHelper implements Serializable
{
    private static final Logger log = Logger.getLogger(ScreenTabErrorHelper.class);

    /**
     * Populates the set of field screen tabs with the tabs which contain errors. This is based on the error collection
     * supplied, which is a mapping from field names to error messages. Will return the index of the tab to be selected,
     * which is either the first tab with an error, or simply the first tab.
     *
     * @param tabsWithErrors the set of tabs to populate
     * @param errors the error collection
     * @param fieldScreenRenderer the field screen renderer to resolve {@link FieldScreenRenderTab} from field ids.
     * @param webParameters the Webwork parameters
     * @return the (1-based) index of the tab to be selected.
     */
    public int initialiseTabsWithErrors(final SortedSet<FieldScreenRenderTab> tabsWithErrors, final Map<String, ?> errors, final FieldScreenRenderer fieldScreenRenderer, final Map webParameters)
    {
        if (errors != null && !errors.isEmpty())
        {
            // Record the tabs which have fields with errors on them
            for (String fieldId : errors.keySet())
            {
                final FieldScreenRenderTab position;

                // TimeTracking and Worklog play differently when together
                if (fieldId.startsWith(IssueFieldConstants.TIMETRACKING) || fieldId.startsWith(IssueFieldConstants.WORKLOG))
                {
                    position = resolvePositionForTimeTrackingAndWorklog(fieldId, fieldScreenRenderer, webParameters);
                }
                else
                {
                    position = fieldScreenRenderer.getFieldScreenRenderTabPosition(fieldId);
                }

                // Why do we bother checking null here? Because we're trying to be nice to a customer: JRA-21260.
                // I'm sure this will come back and haunt me someday. No good deed goes unpunished.
                if (position != null)
                {
                    tabsWithErrors.add(position);
                }
            }

            // Add 1 as the status' counts in WW iterators start at 1 (not 0)
            return tabsWithErrors.first().getPosition() + 1;
        }
        else
        {
            return 1;
        }
    }

    /**
     * Resolves which tab will be displaying an error for either the {@link WorklogSystemField} or {@link
     * TimeTrackingSystemField}.
     *
     * @param fieldId the field id of the error
     * @param fieldScreenRenderer the field screen renderer
     * @param webParameters the Webwork parameters
     * @return the tab which will display the error; null if neither field was found in any tabs.
     */
    private FieldScreenRenderTab resolvePositionForTimeTrackingAndWorklog(final String fieldId, final FieldScreenRenderer fieldScreenRenderer, final Map webParameters)
    {
        final FieldScreenRenderTab timeTrackingTab = fieldScreenRenderer.getFieldScreenRenderTabPosition(IssueFieldConstants.TIMETRACKING);
        final FieldScreenRenderTab worklogTab = fieldScreenRenderer.getFieldScreenRenderTabPosition(IssueFieldConstants.WORKLOG);

        if (timeTrackingTab != null && worklogTab != null)
        {
            if (timeTrackingTab.getPosition() != worklogTab.getPosition())
            {
                if (fieldId.startsWith(IssueFieldConstants.WORKLOG) || TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE.equals(fieldId))
                {
                    return worklogTab;
                }
                else if (TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE.equals(fieldId))
                {
                    return timeTrackingTab;
                }
                else if (IssueFieldConstants.TIMETRACKING.equals(fieldId))
                {
                    // detect if the error is about remaining or original estimate -- this is done by checking
                    // for the 'hasWorkStarted' hidden input
                    boolean hasWorkStarted = extractBooleanWebParameter(webParameters, "hasWorkStarted");
                    if (hasWorkStarted)
                    {
                        // 'timetracking' is Remaining Estimate and appears with the Worklog field
                        return worklogTab;
                    }
                    else
                    {
                        // 'timetracking' is Original Estimate and appears with the Time Tracking field
                        return timeTrackingTab;
                    }
                }
            }
            else
            {
                // doesn't matter which - return either
                return timeTrackingTab;
            }
        }
        else if (timeTrackingTab != null)
        {
            return timeTrackingTab;
        }
        else if (worklogTab != null)
        {
            return worklogTab;
        }

        if (log.isInfoEnabled())
        {
            log.info("Got an error for field '" + fieldId + "' however that field doesn't appear to be on any screen tab.");
        }
        return null;
    }

    private boolean extractBooleanWebParameter(final Map webParameters, final String paramName)
    {
        final String[] values = (String[]) webParameters.get(paramName);
        if (values != null && values.length > 0)
        {
            return Boolean.valueOf(values[0]);
        }
        return false;
    }
}