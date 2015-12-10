package com.atlassian.jira.web.bean;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTimeTrackingGraphBean
{
    private static final String REMAINING = "remaining";
    private static final String REMAINING_TOOLTIP = "remainingTooltip";
    private static final String TIMESPENT = "timespent";
    private static final String TIMESPENT_TOOLTIP = "timespentTooltip";
    private static final String ORIGINAL = "original";
    private static final String ORIGINAL_TOOLTIP = "originalTooltip";

    private static final String KEY_TIME_SPENT = "common.concepts.time.spent";
    private static final String KEY_REMAINING_ESTIMATE = "common.concepts.remaining.estimate";
    private static final String KEY_ORIGINAL_ESTIMATE = "common.concepts.original.estimate";

    @Test
    public void testCreateRemainingEstimateGraph() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(20L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getRemainingEstimateGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 10L, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.REMAINING_TIME, 10L, createTimeRemainingToolTip(REMAINING), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateRemainingEstimateGraphNoRemaining() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(0L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getRemainingEstimateGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 10L, createTimeRemainingToolTip(REMAINING), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.REMAINING_TIME, 0L, createTimeRemainingToolTip(REMAINING), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateRemainingEstimateGraphLargeOriginal() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(30L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getRemainingEstimateGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 10L, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.REMAINING_TIME, 10L, createTimeRemainingToolTip(REMAINING), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.LEFT_OVER, 10L, "", null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateOriginalEstimateGraph() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(20L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getOriginalEstimateGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.ORIGINAL_ESTIMATE, 20L, createOriginalToolTip(ORIGINAL), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateOriginalEstimateGraphLargeTotal() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(50L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(20L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getOriginalEstimateGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.ORIGINAL_ESTIMATE, 20L, createOriginalToolTip(ORIGINAL), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 40L, createOriginalToolTip(ORIGINAL), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateOriginalProgressGraph() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(20L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getOriginalProgressGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.ORIGINAL_ESTIMATE, 20L, createOriginalToolTip(ORIGINAL), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateOriginalProgressGraphLargeTotal() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(50L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(20L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getOriginalProgressGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.ORIGINAL_ESTIMATE, 20L, createOriginalToolTip(ORIGINAL), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.LEFT_OVER, 40L, createOriginalToolTip(ORIGINAL), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testTimeSpentGraph() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(20L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getTimeSpentGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.TIME_SPENT, 10L, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 10L, createTimeRemainingToolTip(REMAINING), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testTimeSpentGraphNoSpent() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpentStr(TIMESPENT);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getTimeSpentGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.TIME_SPENT, 0, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 10L, createTimeSpentToolTip(TIMESPENT), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testTimeSpentGraphLargeOrig() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(100L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getTimeSpentGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.TIME_SPENT, 10L, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.DEFAULT_BACKGROUND, 10L, createTimeRemainingToolTip(REMAINING), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.LEFT_OVER, 80L, NoopI18nHelper.makeTranslation("common.concepts.time.not.required"), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testCreateProgressGraph() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(5L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getProgressGraph();
        final List<?> list = model.getRows();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.TIME_SPENT, 10L, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.REMAINING_TIME, 10L, createTimeRemainingToolTip(REMAINING), null));

        assertEquals(rows, list);
    }

    @Test
    public void testCreateProgressGraphLargeOriginal() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(10L).setRemainingEstimateStr(REMAINING);
        parameters.setTimeSpent(10L).setTimeSpentStr(TIMESPENT);
        parameters.setOriginalEstimate(90L).setOriginalEstimateStr(ORIGINAL);

        final TimeTrackingGraphBean bean = new TimeTrackingGraphBean(parameters);
        final PercentageGraphModel model = bean.getProgressGraph();

        List<PercentageGraphRow> rows = new ArrayList<PercentageGraphRow>();
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.TIME_SPENT, 10L, createTimeSpentToolTip(TIMESPENT), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.REMAINING_TIME, 10L, createTimeRemainingToolTip(REMAINING), null));
        rows.add(new PercentageGraphRow(TimeTrackingGraphBean.Colors.LEFT_OVER, 70L, NoopI18nHelper.makeTranslation("common.concepts.time.not.required"), null));

        assertEquals(rows, model.getRows());
    }

    @Test
    public void testNegativeTimeSpent()
    {
        final String spentStr = "negativeTimeSpent";

        TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setTimeSpent(-1L).setTimeSpentStr(spentStr);

        TimeTrackingGraphBean graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(spentStr, graphBean.getTimeSpentStr());

        List list = graphBean.getTimeSpentGraph().getRows();
        assertEquals("should not be a time remaining row", 2, list.size());
        PercentageGraphRow timeSpent = (PercentageGraphRow) list.get(0);  // first row is time spent
        assertEquals(0, timeSpent.getNumber());
    }

    @Test
    public void testNegativeOriginalEstimate()
    {
        final String estimateStr = ORIGINAL;

        TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setOriginalEstimate(-19L).setOriginalEstimateStr(estimateStr);

        TimeTrackingGraphBean graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(estimateStr, graphBean.getOriginalEstimateStr());

        List list = graphBean.getOriginalEstimateGraph().getRows();
        assertEquals("should not be a time remaining row", 1, list.size());
        PercentageGraphRow originalEstimate = (PercentageGraphRow) list.get(0);  // first row is original estimate
        assertEquals(0, originalEstimate.getNumber());
    }

    @Test
    public void testNegativeTimeRemaining()
    {
        final String estimateStr = "remainingEstimate";

        TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        parameters.setRemainingEstimate(-19L).setRemainingEstimateStr(estimateStr);

        TimeTrackingGraphBean graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(estimateStr, graphBean.getRemainingEstimateStr());

        List list = graphBean.getRemainingEstimateGraph().getRows();
        assertEquals("should not be a time remaining row", 2, list.size());
        PercentageGraphRow remaining = (PercentageGraphRow) list.get(0);  // first row is remaining estimate
        assertEquals(0, remaining.getNumber());
    }

    @Test
    public void testOriginalTooltip() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        TimeTrackingGraphBean graphBean = new TimeTrackingGraphBean(parameters);

        assertEquals(createOriginalToolTip(null), graphBean.getOriginalEstimateTooltip());

        parameters.setOriginalEstimateStr(ORIGINAL);
        graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(createOriginalToolTip(ORIGINAL), graphBean.getOriginalEstimateTooltip());

        parameters.setOriginalEstimateTooltip(ORIGINAL_TOOLTIP);
        graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(createOriginalToolTip(ORIGINAL_TOOLTIP), graphBean.getOriginalEstimateTooltip());
    }

    @Test
    public void testRemainingTooltip() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        TimeTrackingGraphBean graphBean = new TimeTrackingGraphBean(parameters);

        assertEquals(createTimeRemainingToolTip(null), graphBean.getRemainingEstimateTooltip());

        parameters.setRemainingEstimateStr(REMAINING);
        graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(createTimeRemainingToolTip(REMAINING), graphBean.getRemainingEstimateTooltip());

        parameters.setRemainingEstimateStr(REMAINING_TOOLTIP);
        graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(createTimeRemainingToolTip(REMAINING_TOOLTIP), graphBean.getRemainingEstimateTooltip());
    }

    @Test
    public void testTimeSpentTooltip() throws Exception
    {
        final TimeTrackingGraphBean.Parameters parameters = new TimeTrackingGraphBean.Parameters(createI18nHelper());
        TimeTrackingGraphBean graphBean = new TimeTrackingGraphBean(parameters);

        assertEquals(createTimeSpentToolTip(null), graphBean.getTimeSpentTooltip());

        parameters.setTimeSpentStr(TIMESPENT);
        graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(createTimeSpentToolTip(TIMESPENT), graphBean.getTimeSpentTooltip());

        parameters.setTimeSpentTooltip(TIMESPENT_TOOLTIP);
        graphBean = new TimeTrackingGraphBean(parameters);
        assertEquals(createTimeSpentToolTip(TIMESPENT_TOOLTIP), graphBean.getTimeSpentTooltip());
    }

    private String createTimeSpentToolTip(String value)
    {
        return value == null ? createUnknownTooltip(KEY_TIME_SPENT) : createTooltip(KEY_TIME_SPENT, value);
    }

    private String createTimeRemainingToolTip(String value)
    {
        return value == null ? createUnknownTooltip(KEY_REMAINING_ESTIMATE) : createTooltip(KEY_REMAINING_ESTIMATE, value);
    }

    private String createOriginalToolTip(String value)
    {
        return value == null ? createUnknownTooltip(KEY_ORIGINAL_ESTIMATE) : createTooltip(KEY_ORIGINAL_ESTIMATE, value);
    }

    private String createTooltip(String key, String value)
    {
        return String.format("%s - %s", NoopI18nHelper.makeTranslation(key), value);
    }

    private String createUnknownTooltip(String key)
    {
        return createTooltip(key, NoopI18nHelper.makeTranslation("viewissue.timetracking.unknown"));
    }

    private I18nHelper createI18nHelper()
    {
        return new NoopI18nHelper();
    }
}
