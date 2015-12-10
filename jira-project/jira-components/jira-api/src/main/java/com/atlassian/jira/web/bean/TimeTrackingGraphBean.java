package com.atlassian.jira.web.bean;

import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents some tracking information so that it may be rendered.
 */
public class TimeTrackingGraphBean
{
    static final class Colors
    {
        static final String DEFAULT_BACKGROUND = "#cccccc";
        static final String REMAINING_TIME = "#ec8e00";
        static final String ORIGINAL_ESTIMATE = "#89afd7";
        static final String TIME_SPENT = "#51a825";
        static final String LEFT_OVER = "transparent";
    }

    private PercentageGraphModel timeSpentGraph;
    private PercentageGraphModel originalEstimateGraph;
    private PercentageGraphModel remainingEstimateGraph;
    private PercentageGraphModel progressGraph;
    private PercentageGraphModel originalProgressGraph;
    private final I18nHelper i18nHelper;
    private final String timeSpentStr;
    private final Tooltip timeSpentTooltip;
    private final String originalEstimateStr;
    private final Tooltip originalEstimateTooltip;
    private final String remainingEstimateStr;
    private final Tooltip remainingEstimateTooltip;

    private final long timeSpent;
    private final long originalEstimate;
    private final long remainingEstimate;
    private final long totalTime;

    /**
     * Bean that holds three {@link PercentageGraphModel} objects, one for Original Estimate, one for Remaining Estimate and one for Time Spent.
     *
     * @param params the parameters used to create the object.
     */
    public TimeTrackingGraphBean(final Parameters params)
    {
        notNull("params", params);

        this.i18nHelper = params.getI18nHelper();

        this.originalEstimate = nullOrNegative(params.getOriginalEstimate());
        this.originalEstimateStr = params.getOriginalEstimateStr();
        this.originalEstimateTooltip = createTooltip(i18nHelper, "common.concepts.original.estimate",
                params.getOriginalEstimateTooltip(), params.getOriginalEstimateStr()) ;

        this.remainingEstimate = nullOrNegative(params.getRemainingEstimate());
        this.remainingEstimateStr = params.getRemainingEstimateStr();
        this.remainingEstimateTooltip = createTooltip(i18nHelper, "common.concepts.remaining.estimate",
                params.getRemainingEstimateTooltip(), params.getRemainingEstimateStr());

        this.timeSpent = nullOrNegative(params.getTimeSpent());
        this.timeSpentStr = params.getTimeSpentStr();
        this.timeSpentTooltip = createTooltip(i18nHelper, "common.concepts.time.spent",
                params.getTimeSpentTooltip(), params.getTimeSpentStr());

        this.totalTime = this.timeSpent + this.remainingEstimate;
    }

    public boolean hasData()
    {
        return !getOriginalProgressGraph().isTotalZero() && !getTimeSpentGraph().isTotalZero() && !getRemainingEstimateGraph().isTotalZero();
    }

    private PercentageGraphModel createRemainingEstimateGraph()
    {
        final PercentageGraphModel gm = new PercentageGraphModel();

        gm.addRow(Colors.DEFAULT_BACKGROUND, timeSpent, remainingEstimate == 0 ? getRemainingEstimateTooltip() : getTimeSpentTooltip());
        gm.addRow(Colors.REMAINING_TIME, remainingEstimate, getRemainingEstimateTooltip());
        if (originalEstimate > totalTime)
        {
            gm.addRow(Colors.LEFT_OVER, originalEstimate - totalTime, "");
        }
        return gm;
    }

    private PercentageGraphModel createOriginalEstimateGraph(final String bgColour)
    {
        String tooltipToUse = originalEstimateTooltip.geText();
        final PercentageGraphModel gm = new PercentageGraphModel();
        gm.addRow(Colors.ORIGINAL_ESTIMATE, originalEstimate, tooltipToUse);
        if (originalEstimate < totalTime)
        {
            gm.addRow(bgColour, totalTime - originalEstimate, tooltipToUse);
        }
        return gm;
    }

    private PercentageGraphModel createTimeSpentGraph()
    {
        final PercentageGraphModel gm = new PercentageGraphModel();

        gm.addRow(Colors.TIME_SPENT, timeSpent, getTimeSpentTooltip());
        gm.addRow(Colors.DEFAULT_BACKGROUND, remainingEstimate, timeSpent == 0 ? getTimeSpentTooltip() : getRemainingEstimateTooltip());
        if (originalEstimate > totalTime)
        {
            gm.addRow(Colors.LEFT_OVER, originalEstimate - totalTime, i18nHelper.getText("common.concepts.time.not.required"));
        }
        return gm;
    }

    private PercentageGraphModel createProgressGraph()
    {
        final PercentageGraphModel gm = new PercentageGraphModel();
        gm.addRow(Colors.TIME_SPENT, timeSpent, getTimeSpentTooltip());
        gm.addRow(Colors.REMAINING_TIME, remainingEstimate, getRemainingEstimateTooltip());
        if (originalEstimate > totalTime)
        {
            gm.addRow(Colors.LEFT_OVER, originalEstimate - totalTime, i18nHelper.getText("common.concepts.time.not.required"));
        }
        return gm;
    }

    public PercentageGraphModel getTimeSpentGraph()
    {
        if (timeSpentGraph == null)
        {
            timeSpentGraph = createTimeSpentGraph();
        }
        return timeSpentGraph;
    }

    public PercentageGraphModel getOriginalEstimateGraph()
    {
        if (originalEstimateGraph == null)
        {
            originalEstimateGraph = createOriginalEstimateGraph(Colors.DEFAULT_BACKGROUND);
        }
        return originalEstimateGraph;
    }

    public PercentageGraphModel getRemainingEstimateGraph()
    {
        if (remainingEstimateGraph == null)
        {
            remainingEstimateGraph = createRemainingEstimateGraph();
        }
        return remainingEstimateGraph;
    }

    public PercentageGraphModel getOriginalProgressGraph()
    {
        if (originalProgressGraph == null)
        {
            originalProgressGraph = createOriginalEstimateGraph(Colors.LEFT_OVER);
        }
        return originalProgressGraph;
    }

    public PercentageGraphModel getProgressGraph()
    {
        if (progressGraph == null)
        {
            progressGraph = createProgressGraph();
        }
        return progressGraph;
    }

    public long getTimeSpent()
    {
        return timeSpent;
    }

    public long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public long getRemainingEstimate()
    {
        return remainingEstimate;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public String getTimeSpentStr()
    {
        return timeSpentStr;
    }

    public String getOriginalEstimateStr()
    {
        return originalEstimateStr;
    }

    public String getRemainingEstimateStr()
    {
        return remainingEstimateStr;
    }

    public String getTimeSpentTooltip()
    {
        return timeSpentTooltip.geText();
    }

    public String getOriginalEstimateTooltip()
    {
        return originalEstimateTooltip.geText();
    }

    public String getRemainingEstimateTooltip()
    {
        return remainingEstimateTooltip.geText();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * Create a tooltip for the estimate. The tooltip is made up from a fixed string followed by a duration.
     *
     * @param helper the object that handles i18n.
     * @param key the i18n key of the fixed part of the tooltip.
     * @param tooltip the the section of the tooltip after the fixed part.
     * @param backup the section of the tooltip after the fixed part if the tooltip argument is blank or empty.
     * @return the constructed tooltip.
     */
    private static Tooltip createTooltip(I18nHelper helper, String key, String tooltip, String backup)
    {
        boolean known = true;
        final String actualTip;
        if (StringUtils.isBlank(tooltip))
        {
            if (StringUtils.isBlank(backup))
            {
                actualTip = helper.getText("viewissue.timetracking.unknown");
                known = false;
            }
            else
            {
                actualTip = backup;
            }
        }
        else
        {
            actualTip =  tooltip;
        }

        return new Tooltip(String.format("%s - %s", helper.getText(key), actualTip), known);
    }

    /**
     * Returns a primitive long value of the given positive Long object. Returns zero if value is null or negative.
     *
     * @param value value as Long or null
     * @return primitive long value
     */
    private static long nullOrNegative(final Long value)
    {
        return value == null ? 0 : (value < 0) ? 0 : value;
    }

    /**
     * This is a values class to return both a string and a boolean state of whether the values is known
     * or not.
     */
    private static class Tooltip
    {
        private final String text;
        private final boolean isKnown;

        private Tooltip(final String text, final boolean known)
        {
            this.text = text;
            isKnown = known;
        }

        public String geText()
        {
            return text;
        }

        public boolean isKnown()
        {
            return isKnown;
        }
    }

    /**
     * A simple parameter object to keep the number of constructor arguments down.
     */
    public static class Parameters
    {
        private final I18nHelper helper;

        private Long timeSpent;
        private String timeSpentStr;
        private String timeSpentTooltip;
        private Long originalEstimate;
        private String originalEstimateStr;
        private String originalEstimateTooltip;
        private Long remainingEstimate;
        private String remainingEstimateStr;
        private String remainingEstimateTooltip;

        public Parameters(final I18nHelper i18nBean)
        {
            this.helper = notNull("i18nBean", i18nBean);
        }

        public I18nHelper getI18nHelper()
        {
            return helper;
        }

        public Long getTimeSpent()
        {
            return timeSpent;
        }

        public String getTimeSpentStr()
        {
            return timeSpentStr;
        }

        public Long getOriginalEstimate()
        {
            return originalEstimate;
        }

        public String getOriginalEstimateStr()
        {
            return originalEstimateStr;
        }

        public Long getRemainingEstimate()
        {
            return remainingEstimate;
        }

        public String getRemainingEstimateStr()
        {
            return remainingEstimateStr;
        }

        public Parameters setTimeSpent(final Long timeSpent)
        {
            this.timeSpent = timeSpent;
            return this;
        }

        public Parameters setTimeSpentStr(final String timeSpentStr)
        {
            this.timeSpentStr = timeSpentStr;
            return this;
        }

        public Parameters setOriginalEstimate(final Long originalEstimate)
        {
            this.originalEstimate = originalEstimate;
            return this;
        }

        public Parameters setOriginalEstimateStr(final String originalEstimateStr)
        {
            this.originalEstimateStr = originalEstimateStr;
            return this;
        }

        public Parameters setRemainingEstimate(final Long remainingEstimate)
        {
            this.remainingEstimate = remainingEstimate;
            return this;
        }

        public Parameters setRemainingEstimateStr(final String remainingEstimateStr)
        {
            this.remainingEstimateStr = remainingEstimateStr;
            return this;
        }

        public String getTimeSpentTooltip()
        {
            return timeSpentTooltip;
        }

        public Parameters setTimeSpentTooltip(final String timeSpentTooltip)
        {
            this.timeSpentTooltip = timeSpentTooltip;
            return this;
        }

        public String getOriginalEstimateTooltip()
        {
            return originalEstimateTooltip;
        }

        public Parameters setOriginalEstimateTooltip(final String originalEstimateTooltip)
        {
            this.originalEstimateTooltip = originalEstimateTooltip;
            return this;
        }

        public String getRemainingEstimateTooltip()
        {
            return remainingEstimateTooltip;
        }

        public Parameters setRemainingEstimateTooltip(final String remainingEstimateTooltip)
        {
            this.remainingEstimateTooltip = remainingEstimateTooltip;
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
