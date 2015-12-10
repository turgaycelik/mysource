package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.ProgressJsonBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Base class for Progress Bar system fields.
 *
 * @since v3.11
 */
public abstract class AbstractProgressBarSystemField extends NavigableFieldImpl implements RestAwareField
{
    private static final Logger log = Logger.getLogger(AbstractProgressBarSystemField.class);

    public AbstractProgressBarSystemField(final String id, final String nameKey, final String columnHeadingKey, final VelocityTemplatingEngine templatingEngine, final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext)
    {
        super(id, nameKey, columnHeadingKey, ORDER_ASCENDING, templatingEngine, applicationProperties, authenticationContext);
    }

    @Override
    public String getHiddenFieldId()
    {
        return IssueFieldConstants.TIMETRACKING;
    }

    public String getColumnViewHtml(final FieldLayoutItem fieldLayoutItem, final Map displayParams, final Issue issue)
    {
        final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);

        final I18nHelper i18nBean = authenticationContext.getI18nHelper();
        final TimeTrackingParameters params = getTimeTrackingGraphBeanParameters(issue, i18nBean);

        final Long spent = params.getSpent();
        final Long originalEstimate = params.getOriginalEstimate();
        final Long remainingEstimate = params.getRemainingEstimate();

        // work out our percentages before hand
        final AggregateTimeTrackingBean aggregateTTBean = (AggregateTimeTrackingBean) displayParams.get("aggTTBean");
        if (aggregateTTBean == null)
        {
            // from the issue navigator
            velocityParams.put("innerGraphWidth", "100%");
        }
        else
        {
            velocityParams.put("innerGraphWidth", "100%");
            final Long greatestEstimate = aggregateTTBean.getGreastestSubTaskEstimate();
            final Long subTaskEstimate = AggregateTimeTrackingBean.getTheGreaterOfEstimates(originalEstimate, remainingEstimate, spent);
            if ((greatestEstimate != null) && (subTaskEstimate != null) && (greatestEstimate > 0))
            {
                final int width = (int) (((float) subTaskEstimate.longValue() / (float) greatestEstimate) * 100F);
                velocityParams.put("innerGraphWidth", width + "%");
            }
        }

        Long percentage;
        try
        {
            percentage = calculateProgressPercentage(spent, remainingEstimate);
        }
        catch (final IllegalArgumentException probablyNegative)
        {
            // can't do anything with negative numbers - make it zero
            percentage = 0L;
            log.error("Issue: '" + issue.getKey() + "' has an uncalculable percentage", probablyNegative);
        }
        velocityParams.put("percentComplete", decorateProgressPercentage(percentage));
        velocityParams.put("graphBean", params.getTimeTrackingGraphBean());
        velocityParams.put("graphDisplayId", getDisplayId());

        return renderTemplate("progress-bar.vm", velocityParams);
    }

    /**
     * Calculates the progress as spent / (spent + remainingEstimate) if parameters are not null. Returns 0 if spent
     * was null, 100 if remainingEstimate was null, or null if both parameters were null or zeros.
     * <p/>
     * Throws an IllegalArgumentException if a negative number is passed in.
     *
     * @param spent             time spent
     * @param remainingEstimate remaining estimate
     * @return progress as a percentage, can return null
     */
    public static Long calculateProgressPercentage(final Long spent, final Long remainingEstimate)
    {
        if ((spent != null) && (remainingEstimate != null))
        {
            if (0 > spent)
            {
                throw new IllegalArgumentException("Time spent must be a non-negative number: '" + spent + "'");
            }

            if (0 > remainingEstimate)
            {
                throw new IllegalArgumentException("Remaining estimate must be a non-negative number: '" + remainingEstimate + "'");
            }

            // to avoid division by zero
            if ((spent == 0) && (remainingEstimate == 0))
            {
                return null;
            }

            return (long) ((float)spent / (spent + remainingEstimate) * 100f);
        }
        else if (spent != null)
        {
            return 100L;
        }
        else if (remainingEstimate != null)
        {
            return 0L;
        }
        else
        {
            return null;
        }
    }

    /**
     * Appends '%' to the value given if not null. Returns empty string for null parameter.
     *
     * @param percentage percentage, can be null
     * @return percentage string
     */
    private String decorateProgressPercentage(final Number percentage)
    {
        return percentage == null ? "" : percentage.toString() + "%";
    }

    protected abstract TimeTrackingParameters getTimeTrackingGraphBeanParameters(Issue issue, I18nHelper helper);

    /**
     * Returns the display id which is used by the progress-bar.vm template to generate ids for HTML tags.
     *
     * @return id unique for each field
     */
    protected abstract String getDisplayId();

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.PROGRESS_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        TimeTrackingParameters ttParams = getTimeTrackingGraphBeanParameters(issue, authenticationContext.getI18nHelper());
        final long spent = ttParams.getSpent() == null ? 0 : ttParams.getSpent();
        final long remainingEstimate = ttParams.getRemainingEstimate() == null ? 0 : ttParams.getRemainingEstimate();
        Long percentage;
        try
        {
            percentage = calculateProgressPercentage(spent, remainingEstimate);
        }
        catch (IllegalArgumentException ex)
        {
            percentage = null;
            log.debug("Failed to calculate progess percentage.", ex);
        }
        final long total = spent + remainingEstimate;
        return new FieldJsonRepresentation(new JsonData(ProgressJsonBean.shortBean(spent, total, percentage)));
    }

    protected static class TimeTrackingParameters
    {
        private final Long spent;
        private final Long originalEstimate;
        private final Long remainingEstimate;
        private final TimeTrackingGraphBean timeTrackingGraphBean;

        public TimeTrackingParameters(Long spent, Long originalEstimate, Long remainingEstimate, final TimeTrackingGraphBean timeTrackingGraphBean)
        {
            this.spent = spent;
            this.originalEstimate = originalEstimate;
            this.remainingEstimate = remainingEstimate;
            this.timeTrackingGraphBean = timeTrackingGraphBean;
        }

        public Long getSpent()
        {
            return spent;
        }

        public Long getOriginalEstimate()
        {
            return originalEstimate;
        }

        public Long getRemainingEstimate()
        {
            return remainingEstimate;
        }

        public TimeTrackingGraphBean getTimeTrackingGraphBean()
        {
            return timeTrackingGraphBean;
        }
    }
}
