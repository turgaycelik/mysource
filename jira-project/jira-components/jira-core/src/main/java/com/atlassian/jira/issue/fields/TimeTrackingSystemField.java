package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.TimeTrackingRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.TimeTrackingJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import webwork.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The TimeTrackingSystemField is responsible for "maintaining" the 2 estimates values, namely Original Estimate and
 * Remaining Estimate.
 * <p/>
 * 
 * Since 4.2 it has two modes of operation, the older legacy mode that keeps original and remaining estimate tied
 * together while work is not logged and the more modern mode that allows the 2 values to be edited and set
 * independently.
 *
 * NOTES: Whenever you see the targetSubField mentioned its because the {@link com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction}
 * tries to jam 1 value into this field.  in the past this kinda worked but in the post separation world, it becomes more complicated.
 *
 * So we keep track of the "target" that wants to be updated and then we spend lots of effort making sure only that sub field gets updated.
 */

// we are not read for Java 5 quite yet in this class so stop bugging me IDEA
@SuppressWarnings ({ "unchecked" })
public class TimeTrackingSystemField extends AbstractOrderableField implements HideableField, RequirableField, RestAwareField, RestFieldOperations
{
    private static final Logger log = Logger.getLogger(TimeTrackingSystemField.class);

    private static final String TIME_TRACKING_NAME_KEY = "issue.field.timetracking";

    /**
     * When this input parameter is specified then we are only wanted to set a specific sub field of time tracking. This
     * parameter will say which one.
     * <p/>
     * This is used by the {@link com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction} so that it can
     * set say original estimate only for example.
     */
    public static final String TIMETRACKING_TARGETSUBFIELD = "timetracking_targetsubfield";

    public static final String TIMETRACKING_ORIGINALESTIMATE = "timetracking_originalestimate";
    public static final String TIMETRACKING_REMAININGESTIMATE = "timetracking_remainingestimate";

    private static final String ISCREATEISSUE = "isCreateIssue";

    private final IssueManager issueManager;
    private final JiraDurationUtils jiraDurationUtils;

    public TimeTrackingSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, IssueManager issueManager, JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, final JiraDurationUtils jiraDurationUtils)
    {
        super(IssueFieldConstants.TIMETRACKING, TIME_TRACKING_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, null);
        this.issueManager = issueManager;
        this.jiraDurationUtils = jiraDurationUtils;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        velocityParams.put(ISCREATEISSUE, Boolean.TRUE);
        getOurSpecificVelocityParams(issue, velocityParams, operationContext.getFieldValuesHolder());
        return renderTemplate("timetracking-edit.vm", velocityParams);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        getOurSpecificVelocityParams(issue, velocityParams, operationContext.getFieldValuesHolder());
        return renderTemplate("timetracking-edit.vm", velocityParams);
    }

    @Override
    public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        notNull("bulkEditBean", bulkEditBean);
        notEmpty("selectedIssues", bulkEditBean.getSelectedIssues());

        // Time Tracking must be rendered slightly differently on Bulk Move due to the rendering of table cells and so on.
        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            return getBulkMoveHtml(operationContext, action, bulkEditBean, displayParameters);
        }
        return super.getBulkEditHtml(operationContext, action, bulkEditBean, displayParameters);
    }

    private String getBulkMoveHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        final FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);
        final Issue issue = bulkEditBean.getFirstTargetIssueObject();
        final Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        getOurSpecificVelocityParams(issue, velocityParams, operationContext.getFieldValuesHolder());
        return renderTemplate("timetracking-bulkmove.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        getOurSpecificVelocityParams(issue, velocityParams, null);
        populateFromIssue(velocityParams, issue);
        return getViewVelocityTemplate(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put(getId(), value);
        return getViewVelocityTemplate(velocityParams);
    }

    private String getViewVelocityTemplate(Map velocityParams)
    {
        return renderTemplate("timetracking-view.vm", velocityParams);
    }

    private void getOurSpecificVelocityParams(final Issue issue, final Map velocityParams, final Map<String, Object> fieldValuesHolder)
    {
        final boolean legacyBehaviorEnabled = isLegacyBehaviorEnabled();
        velocityParams.put("legacyBehaviour", legacyBehaviorEnabled);
        if (legacyBehaviorEnabled)
        {
            final boolean hasWorkStarted = hasWorkStarted(issue);
            velocityParams.put("hasWorkStarted", hasWorkStarted);
            if (hasWorkStarted)
            {
                velocityParams.put("fieldName", "common.concepts.remaining.estimate");
            }
            else
            {
                velocityParams.put("fieldName", "common.concepts.original.estimate");
            }
        }

        velocityParams.put("isWorklogPresent", fieldValuesHolder != null && fieldValuesHolder.containsKey(IssueFieldConstants.WORKLOG));
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }


    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    /**
     * We don't return any default for the TimeTracking field.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param issue             The issue in play.
     */
    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), new TimeTrackingValue.Builder().setInLegacyMode(isLegacyBehaviorEnabled()).build());
    }

    /**
     * This is called by Jelly code to map a value into a field values holder.
     *
     * @param fieldValuesHolder Map of field Values.
     * @param stringValue       user friendly string value.
     * @param issue             the issue in play.
     *
     * @throws FieldValidationException
     */
    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), new TimeTrackingValue.Builder().setInLegacyMode(isLegacyBehaviorEnabled()).setEstimate(stringValue).build());
    }


    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // If the field need to be moved then it does not have a current value, so populate the default value
        // which is null in our case
    }

    @Override
    public boolean hasParam(Map parameters)
    {
        // For time tracking we must peek into the parameters to see if any of the following are set.
        if (parameters.get(getId()) != null)
        {
            return true;
        }
        if (parameters.get(TIMETRACKING_TARGETSUBFIELD) != null)
        {
            return true;
        }
        if (parameters.get(TIMETRACKING_ORIGINALESTIMATE) != null)
        {
            return true;
        }
        if (parameters.get(TIMETRACKING_REMAININGESTIMATE) != null)
        {
            return true;
        }
        return false;
    }

    /**
     * This will populate the field values holder map with the time tracking value contained within the map of
     * (typically but not always web) parameters.
     * <p/>
     * We override this so we can see this happen.  This helps for debugging reasons.  Damn you class hierarchies, you
     * are confusing me!
     * <p/>
     * This will end up calling {@link #getRelevantParams} by the way.
     *
     * @param fieldValuesHolder the writable map of field values in play.
     * @param inputParameters   the input parameters.
     */
    @Override
    public void populateFromParams(Map<String, Object> fieldValuesHolder, Map<String, String[]> inputParameters)
    {
        super.populateFromParams(fieldValuesHolder, inputParameters);
    }

    /**
     * This is implemented in response to use being an AbstractOrderableField.  It is actually called via
     * populateFromParams so that we can place our relevant value object into the field values holder map.  See above
     * for the code entry point.
     *
     * @param inputParameters the input parameters.
     *
     * @return the object to be placed into a field values holder map under our id.
     */
    protected Object getRelevantParams(Map<String, String[]> inputParameters)
    {
        TimeTrackingValue.Builder builder = new TimeTrackingValue.Builder();
        if (isLegacyBehaviorEnabled(inputParameters))
        {
            builder.setInLegacyMode(true).setEstimate(inputParameters.get(getId()));
        }
        else
        {
            // handle the special case of targeting only one field
            final String[] targetField = inputParameters.get(TIMETRACKING_TARGETSUBFIELD);
            final String targetSubField = (targetField == null) ? null : fromArray(targetField);

            builder.setTargetSubField(targetSubField);
            if (TIMETRACKING_ORIGINALESTIMATE.equals(targetSubField))
            {
                builder.setOriginalEstimate(inputParameters.get(TIMETRACKING_ORIGINALESTIMATE));
            }
            else if (TIMETRACKING_REMAININGESTIMATE.equals(targetSubField))
            {
                builder.setRemainingEstimate(inputParameters.get(TIMETRACKING_REMAININGESTIMATE));
            }
            else
            {
                builder.setOriginalEstimate(inputParameters.get(TIMETRACKING_ORIGINALESTIMATE))
                       .setRemainingEstimate(inputParameters.get(TIMETRACKING_REMAININGESTIMATE));
            }
            builder.setInLegacyMode(false);
        }
        builder.setCreateIssue(inputParameters.get(ISCREATEISSUE));

        // sneak a look at the parameters supplied for the WorklogSystemField
        // to identify if the Log Work checkbox has been checked.
        builder.setLogWorkActivated(inputParameters.get(WorklogSystemField.WORKLOG_ACTIVATE));
        return builder.build();
    }

    private String fromArray(final String[] value)
    {
        return value != null && value.length > 0 ? value[0] : null;
    }

    /**
     * This is called to populate the field values holder with the current state of the Issue object.  For example this
     * will be called when the issue is edited.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param issue             The issue in play.
     */
    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        TimeTrackingValue.Builder valueBuilder = new TimeTrackingValue.Builder();

        if (isLegacyBehaviorEnabled())
        {
            valueBuilder.setInLegacyMode(true);
            Long estimate;
            if (hasWorkStarted(issue))
            {
                estimate = issue.getEstimate();
            }
            else
            {
                estimate = issue.getOriginalEstimate();
            }
            if (estimate != null)
            {
                valueBuilder.setEstimate(formatMillisIntoDisplayFormat(estimate));
            }
        }
        else
        {
            valueBuilder
                    .setInLegacyMode(false)
                    .setOriginalEstimate(
                            formatMillisIntoDisplayFormat(issue.getOriginalEstimate()))
                    .setRemainingEstimate(
                            formatMillisIntoDisplayFormat(issue.getEstimate()));
        }

        fieldValuesHolder.put(getId(), valueBuilder.build());
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        TimeTrackingValue value = (TimeTrackingValue) operationContext.getFieldValuesHolder().get(getId());

        if (value != null)
        {
            boolean hasWorkStarted = hasWorkStarted(issue);
            TimeTrackingValue newValue;
            if (value.isInLegacyMode())
            {
                newValue = validateParamsInLegacyMode(errorCollection, i18n, fieldScreenRenderLayoutItem.isRequired(), issue, value, value.isLogWorkActivated(), hasWorkStarted);
            }
            else
            {
                newValue = validateParamsInModernMode(errorCollection, i18n, fieldScreenRenderLayoutItem.isRequired(), issue, value, value.isLogWorkActivated());
            }

            // if we were returned an updated value, that signifies that we must update the TimeTrackingValue in the FieldValuesHolder
            if (newValue != null)
            {
                operationContext.getFieldValuesHolder().put(getId(), newValue);
            }
        }
    }

    private TimeTrackingValue validateParamsInModernMode(final ErrorCollection errorCollection, final I18nHelper i18n, final boolean fieldIsRequired, final Issue issue, TimeTrackingValue value, final boolean isLogWorkActivated)
    {
        if (!isTimeTrackingEnabled())
        {
            errorCollection.addError(getId(), i18n.getText("createissue.error.timetracking.disabled"));
            return null;
        }
        final String originalEstimate = value.getOriginalEstimateDisplayValue();
        String remainingEstimate = value.getRemainingEstimateDisplayValue();
        if (fieldIsRequired)
        {
            if (isLogWorkActivated)
            {
                // If Log Work is activated, then anything entered in the Remaining Estimate field is ignored;
                // Hence for Field Required, we just check the Original Estimate.
                if (StringUtils.isBlank(originalEstimate))
                {
                    errorCollection.addError(TIMETRACKING_ORIGINALESTIMATE, i18n.getText("issue.field.required", i18n.getText("common.concepts.original.estimate")));

                    // reset the Remaining Estimate entered to nothing so that input is not passed back to user
                    return resetEstimateForValue(value, issue);
                }
            }
            if (StringUtils.isBlank(originalEstimate) && StringUtils.isBlank(remainingEstimate))
            {
                errorCollection.addError(TIMETRACKING_ORIGINALESTIMATE, i18n.getText("issue.field.required", i18n.getText("common.concepts.original.estimate")));
                errorCollection.addError(TIMETRACKING_REMAININGESTIMATE, i18n.getText("issue.field.required", i18n.getText("common.concepts.remaining.estimate")));
                return null;
            }
        }

        boolean originalEstimateValid = false;
        boolean remainingEstimateValid = false;
        if (isDurationInvalid(originalEstimate))
        {
            errorCollection.addError(TIMETRACKING_ORIGINALESTIMATE, i18n.getText("createissue.error.original.estimate.invalid"));
        }
        else
        {
            originalEstimateValid = true;
        }

        if (isLogWorkActivated)
        {
            // if Log Work is activated then we ignore Remaining Estimate and reset
            // the entered value to what is currently in the issue.
            remainingEstimateValid = true;
            value = resetEstimateForValue(value, issue);
            remainingEstimate = value.getRemainingEstimateDisplayValue();
        }
        else
        {
            if (isDurationInvalid(remainingEstimate))
            {
                errorCollection.addError(TIMETRACKING_REMAININGESTIMATE, i18n.getText("createissue.error.remaining.estimate.invalid"));
            }
            else
            {
                remainingEstimateValid = true;
            }
        }

        if (!originalEstimateValid || !remainingEstimateValid)
        {
            return value;
        }
        else
        {
            // generally, if either Original or Remaining estimate is blank, we copy the non-blank value to the blank field.
            if (StringUtils.isNotBlank(originalEstimate) && StringUtils.isBlank(remainingEstimate))
            {
                // set remaining to original
                // if Log Work is activated, we ignore Remaining Estimate so overwrite it with the Original Estimate
                TimeTrackingValue.Builder b = new TimeTrackingValue.Builder(value);
                b.setRemainingEstimate(originalEstimate);
                return b.build();
            }
            else if (StringUtils.isBlank(originalEstimate) && StringUtils.isNotBlank(remainingEstimate))
            {
                // set original to remaining
                TimeTrackingValue.Builder b = new TimeTrackingValue.Builder(value);
                b.setOriginalEstimate(remainingEstimate);
                return b.build();
            }
        }

        // no additional changes required
        return value;
    }

    /**
     * @param duration the input duration
     * @return true only if the input duration is not blank and is not valid as deemed by {@link JiraDurationUtils#parseDuration(String, Locale)}
     */
    boolean isDurationInvalid(final String duration)
    {
        if (StringUtils.isNotBlank(duration))
        {
            try
            {
                jiraDurationUtils.parseDuration(duration, authenticationContext.getLocale());
            }
            catch (InvalidDurationException e)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This was the original validation.  I can pick some logic holes in it BUT I don't want to break old behavior so it
     * is repeated here verbatim without any changes.
     *
     * @param errorCollectionToAddTo the ErrorCollection in play.
     * @param i18n                   the I18nHelper in play.
     * @param fieldIsRequired        is the field required to be there.
     * @param issue                  the issue being validated against
     * @param value                  the value from FieldValuesHolder
     * @param isLogWorkActivated     has "Log Work" been activated/checked
     * @param hasWorkStarted         has work started on this issue
     * @return the time tracking values.
     */
    private TimeTrackingValue validateParamsInLegacyMode(final ErrorCollection errorCollectionToAddTo, final I18nHelper i18n, final boolean fieldIsRequired, final Issue issue, final TimeTrackingValue value, final boolean isLogWorkActivated, final boolean hasWorkStarted)
    {
        // when Log Work is activated, we should not validate and reset the value for remaining estimate to what is on the issue
        if (isLogWorkActivated && hasWorkStarted)
        {
            return resetEstimateForValue(value, issue);
        }

        final String estimate = value.getEstimateDisplayValue();
        if (StringUtils.isNotBlank(estimate))
        {
            if (isTimeTrackingEnabled())
            {
                if (isDurationInvalid(estimate))
                {
                    final String key = hasWorkStarted ? "createissue.error.remaining.estimate.invalid" : "createissue.error.original.estimate.invalid";
                    errorCollectionToAddTo.addError(getId(), i18n.getText(key));
                    return null;
                }
            }
            else
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.timetracking.disabled"));
                return null;
            }
        }

        if (fieldIsRequired && StringUtils.isBlank(estimate))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            return null;
        }

        return null;
    }

    /**
     * Takes in a {@link TimeTrackingValue} and resets the Remaining Estimate on it to the current value from the issue.
     *
     * @param oldValue the value to reset
     * @param issue the issue
     * @return a copy of the TimeTrackingValue with the estimate reset.
     */
    private TimeTrackingValue resetEstimateForValue(final TimeTrackingValue oldValue, final Issue issue)
    {
        final TimeTrackingValue.Builder builder = new TimeTrackingValue.Builder(oldValue);
        Long currentEstimate = issue.getEstimate();
        String currentEstimateString;
        if (currentEstimate != null)
        {
            currentEstimateString = formatMillisIntoDisplayFormat(currentEstimate);
        }
        else
        {
            currentEstimateString = null;
        }
        if (oldValue.isInLegacyMode())
        {
            builder.setEstimate(currentEstimateString);
        }
        else
        {
            builder.setRemainingEstimate(currentEstimateString);
        }
        return builder.build();
    }

    /**
     * This is called from BulkEdit/BulkWorkflowTransition to get an value object from a input set of values.
     *
     * @param fieldValueHolder the map of parameters.
     * @return a parsed long or null if not in the input.
     */
    public Object getValueFromParams(Map fieldValueHolder)
    {
        TimeTrackingValue value = (TimeTrackingValue) fieldValueHolder.get(getId());
        if (value != null)
        {
            if (value.isInLegacyMode())
            {
                return convertToIssueFormInLegacyMode(value);
            }
            else
            {
                return convertToIssueFormInModernMode(value);
            }
        }
        return null;
    }

    private Object convertToIssueFormInModernMode(final TimeTrackingValue value)
    {
        TimeTrackingValue.Builder builder = new TimeTrackingValue.Builder();
        final String oeDisplayValue = value.getOriginalEstimateDisplayValue();
        final String reDisplayValue = value.getRemainingEstimateDisplayValue();
        final String targetedSubField = value.getTargetSubField();
        builder.setTargetSubField(targetedSubField);
        if (TIMETRACKING_ORIGINALESTIMATE.equals(targetedSubField))
        {
            if (StringUtils.isNotBlank(oeDisplayValue))
            {
                builder.setOriginalEstimate(convertDurationIntoSeconds(oeDisplayValue));
            }
        }
        else if (TIMETRACKING_REMAININGESTIMATE.equals(targetedSubField))
        {
            if (StringUtils.isNotBlank(reDisplayValue))
            {
                builder.setRemainingEstimate(convertDurationIntoSeconds(reDisplayValue));
            }
        }
        else
        {
            if (StringUtils.isNotBlank(oeDisplayValue))
            {
                builder.setOriginalEstimate(convertDurationIntoSeconds(oeDisplayValue));
            }
            if (StringUtils.isNotBlank(reDisplayValue))
            {
                builder.setRemainingEstimate(convertDurationIntoSeconds(reDisplayValue));
            }
        }
        return builder.build();
    }

    private Object convertToIssueFormInLegacyMode(final TimeTrackingValue value)
    {
        TimeTrackingValue.Builder builder = new TimeTrackingValue.Builder(value);

        String estimate = value.getEstimateDisplayValue();
        if (StringUtils.isNotBlank(estimate))
        {
            builder.setEstimate(convertDurationIntoSeconds(estimate));
        }
        return builder.build();
    }

    private Long convertDurationIntoSeconds(final String estimate)
    {
        try
        {
            return jiraDurationUtils.parseDuration(estimate, authenticationContext.getLocale());
        }
        catch (InvalidDurationException e)
        {
            log.error("Error occurred while converting time estimates.");
            throw new IllegalArgumentException("Error occurred while converting time estimates:" + e.getMessage());
        }
    }

    /**
     * <p>
     * This is called to back update the MutableIssue with the value object we previously stuffed into the field values
     * holder.
     * <p/>
     * <p>This is called prior to the issue being stored on disk.</p>
     *
     * @param fieldLayoutItem  FieldLayoutItem in play.
     * @param issue            MutableIssue in play.
     * @param fieldValueHolder Field Value Holder Map.
     */
    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        TimeTrackingValue newValue = (TimeTrackingValue) getValueFromParams(fieldValueHolder);
        if (newValue == null)
        {
            return; // belts and braces.  We don't ever expect this
        }
        if (newValue.isInLegacyMode())
        {
            final Long estimate = newValue.getEstimate();
            if (hasWorkStarted(issue))
            {
                issue.setEstimate(estimate);
            }
            else
            {
                issue.setOriginalEstimate(estimate);
                issue.setEstimate(estimate);
            }
        }
        else
        {
            final TimeTrackingValue oldValue = new TimeTrackingValue.Builder()
                    .setInLegacyMode(false)
                    .setTargetSubField(newValue.getTargetSubField())
                    .setOriginalEstimate(issue.getOriginalEstimate())
                    .setRemainingEstimate(issue.getEstimate())
                    .build();

            final String targetSubField = newValue.getTargetSubField();
            if (TIMETRACKING_ORIGINALESTIMATE.equals(targetSubField))
            {
                issue.setOriginalEstimate(newValue.getOriginalEstimate());
            }
            else if (TIMETRACKING_REMAININGESTIMATE.equals(targetSubField))
            {
                issue.setEstimate(newValue.getRemainingEstimate());
            }
            else
            {
                issue.setOriginalEstimate(newValue.getOriginalEstimate());
                issue.setEstimate(newValue.getRemainingEstimate());
            }

            // in the modern world we throw in a value object into the mutable issue under our name so we can create change history with it later
            issue.setExternalFieldValue(getId(), oldValue, newValue);
        }
    }


    /**
     * This is called after the issue has been stored on disk and allows us a chance to create change records for the
     * update.
     *
     * @param fieldLayoutItem   for this field within this context.
     * @param issue             Issue this field is part of.
     * @param modifiedValue     new value to set field to. Cannot be null.
     * @param issueChangeHolder an object to record any changes made to the issue by this method.
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Object newValue = modifiedValue.getNewValue();
        if (!(newValue instanceof TimeTrackingValue))
        {
            updateChangeHistoryInLegacyMode(issue, modifiedValue, issueChangeHolder);
        }
        else
        {
            updateChangeHistoryInModernMode(modifiedValue, issueChangeHolder);
        }
    }

    private void updateChangeHistoryInModernMode(final ModifiedValue modifiedValue, final IssueChangeHolder issueChangeHolder)
    {
        TimeTrackingValue oldValue = (TimeTrackingValue) modifiedValue.getOldValue();
        TimeTrackingValue newValue = (TimeTrackingValue) modifiedValue.getNewValue();

        final String targetSubField = newValue.getTargetSubField();
        if (TIMETRACKING_ORIGINALESTIMATE.equals(targetSubField))
        {
            updateChangeHistoryForFieldInModernMode(issueChangeHolder, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, oldValue.getOriginalEstimate(), newValue.getOriginalEstimate());
        }
        else if (TIMETRACKING_REMAININGESTIMATE.equals(newValue.getTargetSubField()))
        {
            updateChangeHistoryForFieldInModernMode(issueChangeHolder, IssueFieldConstants.TIME_ESTIMATE, oldValue.getRemainingEstimate(), newValue.getRemainingEstimate());
        }
        else
        {
            updateChangeHistoryForFieldInModernMode(issueChangeHolder, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, oldValue.getOriginalEstimate(), newValue.getOriginalEstimate());
            updateChangeHistoryForFieldInModernMode(issueChangeHolder, IssueFieldConstants.TIME_ESTIMATE, oldValue.getRemainingEstimate(), newValue.getRemainingEstimate());
        }
    }

    private void updateChangeHistoryForFieldInModernMode(final IssueChangeHolder issueChangeHolder, final String fieldName, final Long oldValue, final Long newValue)
    {
        final String oldChangeLogValue = getChangelogValue(oldValue);
        final String newChangeLogValue = getChangelogValue(newValue);
        if (oldValue == null)
        {
            if (newValue != null)
            {
                issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, fieldName, oldChangeLogValue, oldChangeLogValue, newChangeLogValue, newChangeLogValue));
            }
        }
        else
        {
            if (!valuesEqual(newValue, oldValue))
            {
                issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, fieldName, oldChangeLogValue, oldChangeLogValue, newChangeLogValue, newChangeLogValue));
            }
        }
    }

    private void updateChangeHistoryInLegacyMode(final Issue issue, final ModifiedValue modifiedValue, final IssueChangeHolder issueChangeHolder)
    {
        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();

        if (currentValue == null)
        {
            if (value != null)
            {
                if (hasWorkStarted(issue))
                {
                    issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE, null, null, getChangelogValue(value), getChangelogValue(value)));
                }
                else
                {
                    issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, null, null, getChangelogValue(value), getChangelogValue(value)));
                    issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE, null, null, getChangelogValue(value), getChangelogValue(value)));
                }
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                if (hasWorkStarted(issue))
                {
                    issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timeestimate", getChangelogValue(currentValue), getChangelogValue(currentValue), getChangelogValue(value), getChangelogValue(value)));
                }
                else
                {
                    issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timeoriginalestimate", getChangelogValue(currentValue), getChangelogValue(currentValue), getChangelogValue(value), getChangelogValue(value)));
                    issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timeestimate", getChangelogValue(currentValue), getChangelogValue(currentValue), getChangelogValue(value), getChangelogValue(value)));
                }
            }
        }
    }


    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        Collection<Issue> issues = (Collection<Issue>) originalIssues;
        for (final Issue originalIssue : issues)
        {
            // The estimate is always set, even during issue creation when the original estimate is set.
            if ((originalIssue.getOriginalEstimate() == null || originalIssue.getEstimate() == null) && targetFieldLayoutItem.isRequired())
            {
                return new MessagedResult(true);
            }
        }
        return new MessagedResult(false);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setEstimate(null);
        issue.setOriginalEstimate(null);
    }

    public boolean isShown(Issue issue)
    {
        return isTimeTrackingEnabled();
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getOriginalEstimate() != null || issue.getEstimate() != null);
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    public boolean hasWorkStarted(Issue issue)
    {
        if (issue.getGenericValue() == null)
        {
            return false;
        }

        try
        {
            return !issueManager.getEntitiesByIssueObject(IssueRelationConstants.TYPE_WORKLOG, issue).isEmpty();
        }
        catch (Exception e)
        {
            return true;
        }
    }

    private boolean isTimeTrackingEnabled()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    /**
     * If the caller ONLY provides a "timetracking" single value, then we switch into legacy mode even if we are in
     * modern mode according to the app property.  This is because old clients like SOAP don't know about the new modes
     * and will be still sending the old single value. So we degrade back into old mode for them based on that input.
     *
     * @param inputParameters the Map of input parameters.
     *
     * @return true if we need to act in legacy mode.
     */
    private boolean isLegacyBehaviorEnabled(Map inputParameters)
    {
        if (!isLegacyBehaviorEnabled())
        {
            // it still might be legacy if they only provide the old "timetracking" field value only
            if (inputParameters.containsKey(getId()))
            {
                if (!inputParameters.containsKey(TIMETRACKING_ORIGINALESTIMATE) && !inputParameters.containsKey(TIMETRACKING_REMAININGESTIMATE))
                {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean isLegacyBehaviorEnabled()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR);
    }

    private String getChangelogValue(Object value)
    {
        if (value != null)
        {
            return value.toString();
        }
        else
        {
            return null;
        }
    }

    private String formatMillisIntoDisplayFormat(final Long estimate)
    {
        if (estimate == null)
        {
            return null;
        }
        //Note: we want English locale to format to w d h m correctly
        return jiraDurationUtils.getShortFormattedDuration(estimate, new Locale("en_UK"));
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.TIME_TRACKING_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        JiraDurationUtils jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);

        String originalEstimate = issue.getOriginalEstimate() == null ? null : jiraDurationUtils.getShortFormattedDuration(issue.getOriginalEstimate());
        String estimate = issue.getEstimate() == null ? null : jiraDurationUtils.getShortFormattedDuration(issue.getEstimate());
        String timeSpent = issue.getTimeSpent() == null ? null : jiraDurationUtils.getShortFormattedDuration(issue.getTimeSpent());

        TimeTrackingJsonBean timeTrackingJsonBean = TimeTrackingJsonBean.shortBean(originalEstimate, estimate, timeSpent, issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());

        if (renderedVersionRequested)
        {
            String renderedOriginalEstimate = issue.getOriginalEstimate() == null ? null : jiraDurationUtils.getFormattedDuration(issue.getOriginalEstimate(), authenticationContext.getLocale());
            String renderedEstimate = issue.getEstimate() == null ? null : jiraDurationUtils.getFormattedDuration(issue.getEstimate(), authenticationContext.getLocale());
            String renderedTimeSpent = issue.getTimeSpent() == null ? null : jiraDurationUtils.getFormattedDuration(issue.getTimeSpent(), authenticationContext.getLocale());

            TimeTrackingJsonBean renderedTimeTrackingJsonBean = TimeTrackingJsonBean.shortBean(renderedOriginalEstimate, renderedEstimate, renderedTimeSpent, issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());
            return new FieldJsonRepresentation(new JsonData(timeTrackingJsonBean), new JsonData(renderedTimeTrackingJsonBean));
        }
        else
        {
            return new FieldJsonRepresentation(new JsonData(timeTrackingJsonBean));
        }
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new TimeTrackingRestFieldOperationsHandler(this, getApplicationProperties(), jiraDurationUtils, authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }

    /**
     * <p>This interface is used as a value object for TimeTracking information.<p/>
     * <p>
     * It lurks around inside the field values holder maps while JIRA does its thang.  It's referenced by the velocity
     * views and also by the TimeTrackingSystemField itself.
     * <p/>
     * <p>
     * While the class is PUBLIC, it is only so that the Velocity template can get to it.  Please do not consider this
     * part of the JIRA API.  It's for the TimeTrackingSystemField only.  You have been warned :)
     * <p/>
     * <p>It exists so that we can cater for the 2 modes that this field can work in since 4.2.</p>
     */
    public static interface TimeTrackingValue
    {
        /**
         * This allows the input to drive what mode we work in.  This is their to allows old SOAP clients to work, in
         * that they may only provide 1 value even if the system as the new mode turned on.
         *
         * @return true if the values in the time tracking value object need to be treated in the mode legacy mode
         */
        public boolean isInLegacyMode();

        public boolean isCreateIssue();

        /**
         * Signifies if we will be ignoring the Remaining Estimate from the input parameters as a result of the Log Work
         * form being activated. The checkbox is only ever rendered when both TimeTracking and Worklog system fields are
         * rendered on the same screen.
         *
         * @return true if the "Log Work" checkbox from the WorklogSystemField was checked.
         */
        boolean isLogWorkActivated();

        /**
         * This exists to allow one bit of JIRA code {@link com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction}
         * which is a workflow post function to set 1 specific sub field value.  This allows us to work in modern mode
         * but still only set one value.
         *
         * @return either null or TIMETRACKING_ORIGINALESTIMATE or TIMETRACKING_REMAININGESTIMATE
         */
        public String getTargetSubField();

        public Long getOriginalEstimate();

        public Long getRemainingEstimate();

        public Long getEstimate();

        public String getEstimateDisplayValue();

        public String getOriginalEstimateDisplayValue();

        public String getRemainingEstimateDisplayValue();

        public static class Builder
        {
            Long estimate = null;
            Long originalEstimate = null;
            Long remainingEstimate = null;

            String estimateDisplayValue = null;
            String originalEstimateDisplayValue = null;
            String remainingEstimateDisplayValue = null;

            String targetSubField = null;

            boolean inLegacyMode;

            boolean isCreateIssue;

            boolean isLogWorkActivated;

            Builder()
            {

            }

            Builder(TimeTrackingValue value)
            {
                this.estimate = value.getEstimate();
                this.originalEstimate = value.getOriginalEstimate();
                this.remainingEstimate = value.getRemainingEstimate();

                this.estimateDisplayValue = value.getEstimateDisplayValue();
                this.originalEstimateDisplayValue = value.getOriginalEstimateDisplayValue();
                this.remainingEstimateDisplayValue = value.getRemainingEstimateDisplayValue();

                this.targetSubField = value.getTargetSubField();
                this.inLegacyMode = value.isInLegacyMode();
                this.isCreateIssue = value.isCreateIssue();
                this.isLogWorkActivated = value.isLogWorkActivated();
            }

            Builder setCreateIssue(String[] createIssue)
            {
                final String s = fromArray(createIssue);
                this.isCreateIssue = StringUtils.isNotBlank(s) ? Boolean.valueOf(s) : false;
                return this;
            }

            Builder setLogWorkActivated(String[] logWorkActivated)
            {
                final String s = fromArray(logWorkActivated);
                this.isLogWorkActivated = StringUtils.isNotBlank(s) ? Boolean.valueOf(s) : false;
                return this;
            }

            Builder setInLegacyMode(boolean flag)
            {
                inLegacyMode = flag;
                return this;
            }

            Builder setTargetSubField(String value)
            {
                targetSubField = value;
                return this;
            }

            Builder setEstimate(Long value)
            {
                estimate = value;
                return this;
            }

            Builder setEstimate(String value)
            {
                estimateDisplayValue = value;
                return this;
            }

            Builder setEstimate(String[] value)
            {
                estimateDisplayValue = fromArray(value);
                return this;
            }

            Builder setOriginalEstimate(Long value)
            {
                originalEstimate = value;
                return this;
            }

            Builder setOriginalEstimate(String value)
            {
                originalEstimateDisplayValue = value;
                return this;
            }

            Builder setOriginalEstimate(String[] value)
            {
                originalEstimateDisplayValue = fromArray(value);
                return this;
            }

            Builder setRemainingEstimate(Long value)
            {
                remainingEstimate = value;
                return this;
            }

            Builder setRemainingEstimate(String value)
            {
                remainingEstimateDisplayValue = value;
                return this;
            }

            Builder setRemainingEstimate(String[] value)
            {
                remainingEstimateDisplayValue = fromArray(value);
                return this;
            }

            private String fromArray(final String[] value)
            {
                return value != null && value.length > 0 ? value[0] : null;
            }

            TimeTrackingValue build()
            {
                final boolean inLegacyMode = this.inLegacyMode;
                final boolean isCreateIssue = this.isCreateIssue;
                final boolean isLogWorkActivated = this.isLogWorkActivated;
                final Long estimate = this.estimate;
                final Long originalEstimate = this.originalEstimate;
                final Long remainingEstimate = this.remainingEstimate;
                final String estimateDisplayValue = this.estimateDisplayValue;
                final String originalEstimateDisplayValue = this.originalEstimateDisplayValue;
                final String remainingEstimateDisplayValue = this.remainingEstimateDisplayValue;
                final String targetSubField = this.targetSubField;

                return new TimeTrackingValue()
                {

                    public boolean isInLegacyMode()
                    {
                        return inLegacyMode;
                    }

                    public boolean isCreateIssue()
                    {
                        return isCreateIssue;
                    }

                    public boolean isLogWorkActivated()
                    {
                        return isLogWorkActivated;
                    }

                    public Long getOriginalEstimate()
                    {
                        return originalEstimate;
                    }

                    public Long getRemainingEstimate()
                    {
                        return remainingEstimate;
                    }

                    public Long getEstimate()
                    {
                        return estimate;
                    }

                    public String getEstimateDisplayValue()
                    {
                        return estimateDisplayValue;
                    }

                    public String getOriginalEstimateDisplayValue()
                    {
                        return originalEstimateDisplayValue;
                    }

                    public String getRemainingEstimateDisplayValue()
                    {
                        return remainingEstimateDisplayValue;
                    }

                    public String getTargetSubField()
                    {
                        return targetSubField;
                    }

                    @Override
                    public boolean equals(final Object obj)
                    {
                        if (this == obj) { return true; }

                        if (!(obj instanceof TimeTrackingValue)) { return false; }

                        TimeTrackingValue rhs = (TimeTrackingValue ) obj;

                        return new EqualsBuilder().
                                append(inLegacyMode, rhs.isInLegacyMode()).
                                append(isCreateIssue, rhs.isCreateIssue()).
                                append(isLogWorkActivated, rhs.isLogWorkActivated()).
                                append(originalEstimate, rhs.getOriginalEstimate()).
                                append(remainingEstimate, rhs.getRemainingEstimate()).
                                append(estimate, rhs.getEstimate()).
                                append(estimateDisplayValue, rhs.getEstimateDisplayValue()).
                                append(originalEstimateDisplayValue, rhs.getOriginalEstimateDisplayValue()).
                                append(remainingEstimateDisplayValue, rhs.getRemainingEstimateDisplayValue()).
                                append(targetSubField, rhs.getTargetSubField()).
                                isEquals();
                    }

                    @Override
                    public int hashCode()
                    {
                        return new HashCodeBuilder(17,31).
                                append(inLegacyMode).
                                append(isCreateIssue).
                                append(isLogWorkActivated).
                                append(originalEstimate).
                                append(remainingEstimate).
                                append(estimate).
                                append(estimateDisplayValue).
                                append(originalEstimateDisplayValue).
                                append(remainingEstimateDisplayValue).
                                append(targetSubField).
                                toHashCode();
                    }

                    @Override
                    public String toString()
                    {
                        return new ToStringBuilder(this)
                                .append("legacy", inLegacyMode)
                                .append("createIssue", isCreateIssue)
                                .append("logWorkActivated", isLogWorkActivated)
                                .append("target", targetSubField)
                                .append("estimate", estimate)
                                .append("estimateDV", estimateDisplayValue)
                                .append("originalEstimate", originalEstimate)
                                .append("originalEstimateDV", originalEstimateDisplayValue)
                                .append("remainingEstimate", remainingEstimate)
                                .append("remainingEstimateDV", remainingEstimateDisplayValue)
                                .toString();
                    }
                };
            }
        }

    }
}
