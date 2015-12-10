package com.atlassian.jira.charts.piechart;

import com.atlassian.jira.charts.PieChart;
import com.atlassian.jira.charts.PieSegmentWrapper;
import com.atlassian.jira.charts.jfreechart.util.PieDatasetUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.StatisticMap;
import org.apache.log4j.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.util.Map;

import static com.atlassian.jira.charts.jfreechart.util.PieDatasetUtil.createConsolidatedSortedPieDataset;
import static com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator.IRRELEVANT;

/**
 * This class generates JFreeChart datasets for pie charts
 *
 * @since v6.0
 */
public class PieChartDataSetFactory
{
    private static final double DEFAULT_CONSOLIDATION_MINIMUM_PERCENTAGE_THRESHOLD = 0.02;
    private static final int DEFAULT_CONSOLIDATION_MINIMUM_COUNT_THRESHOLD = 10;

    private static final Logger log = Logger.getLogger(PieChart.class);

    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final StatisticMap<?, Number> statisticMap;
    private final I18nHelper i18nHelper;
    private final String statisticType;

    private final double consolidationMinimumPercentThreshold = DEFAULT_CONSOLIDATION_MINIMUM_PERCENTAGE_THRESHOLD;
    private final int consolidationMinimumCountThreshold = DEFAULT_CONSOLIDATION_MINIMUM_COUNT_THRESHOLD;

    public PieChartDataSetFactory(ConstantsManager constantsManager, CustomFieldManager customFieldManager, StatisticMap<?, Number> statisticMap, I18nHelper i18nHelper, String statisticType)
    {
        this.constantsManager = Assertions.notNull(constantsManager);
        this.customFieldManager = Assertions.notNull(customFieldManager);
        this.statisticMap = Assertions.notNull(statisticMap);
        this.i18nHelper = Assertions.notNull(i18nHelper);
        this.statisticType = Assertions.notNull(statisticType);
    }

    public PieDataset createSortedPieDataset()
    {
        log.debug("DataCollector.populatePieChart: Creating initial PieDataset.");
        final PieDataset dataset = createRawPieDataset();
        log.debug("DataCollector.populatePieChart: Processing the PieDataset.");
        // sort the dataset in order of lowest to highest
        return PieDatasetUtil.createSortedPieDataset(dataset);
    }

    PieDataset createRawPieDataset()
    {
        final DefaultPieDataset dataset = new DefaultPieDataset();
        for (final Map.Entry<?, Number> statistic : statisticMap.entrySet())
        {
            final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(statistic.getKey(), i18nHelper, statisticType, constantsManager, customFieldManager);
            dataset.setValue(pieSegmentWrapper, statistic.getValue());
        }

        if (statisticMap.getIrrelevantCount() > 0)
        {
            // Add a pie slice for Irrelevant
            final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(IRRELEVANT, i18nHelper, statisticType, constantsManager, customFieldManager);
            final Number number = statisticMap.getIrrelevantCount();

            dataset.setValue(pieSegmentWrapper, number);
        }
        return dataset;
    }

    public CategoryDataset createCategoryDataset(final long numIssues, final PieDataset pieDataset)
    {
        // now create the complete dataset
        final DefaultCategoryDataset completeDataset = new DefaultCategoryDataset();
        for (final Object o : pieDataset.getKeys())
        {
            final Comparable key = (Comparable) o;
            final Number value = pieDataset.getValue(key);
            final long percentage = 100 * value.intValue() / numIssues;

            completeDataset.addValue(value, i18nHelper.getText("common.concepts.issues"), key);
            completeDataset.addValue(percentage, "%", key);
        }
        return completeDataset;
    }

    public PieDataset createConsolidatedPieDataset(PieDataset sortedPieDataset, Comparable otherSegmentKey)
    {
        return createConsolidatedSortedPieDataset(sortedPieDataset, otherSegmentKey, false, consolidationMinimumPercentThreshold, consolidationMinimumCountThreshold);
    }
}