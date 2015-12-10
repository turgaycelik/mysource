package com.atlassian.jira.charts;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.PieChartGenerator;
import com.atlassian.jira.charts.piechart.PieChartDataSetFactory;
import com.atlassian.jira.charts.piechart.PieChartUrlGeneratorFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;

import org.apache.log4j.Logger;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Produces a pie chart based on all issues for a certain field.
 *
 * @since v4.0
 */
public class PieChart
{
    // Constants
    private static final Logger log = Logger.getLogger(PieChart.class);
    static final String ISSUES_MESSAGE_CODE = "datacollector.tooltip.issues";

    // Fields
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final Comparable otherSegmentKey = "Other";

    public PieChart(ConstantsManager constantsManager, CustomFieldManager customFieldManager,
            SearchService searchService, ApplicationProperties applicationProperties)
    {
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
    }

    public Chart generateChart(final User remoteUser, final SearchRequest searchRequest, final String statisticType,
            final int desiredChartWidth, final int desiredChartHeight)
    {
        return generateChartInternal(remoteUser, searchRequest, statisticType, desiredChartWidth, desiredChartHeight, false);
    }

    public Chart generateInlineChart(final User remoteUser, final SearchRequest searchRequest,
            final String statisticType, final int desiredChartWidth, final int desiredChartHeight)
    {
        return generateChartInternal(remoteUser, searchRequest, statisticType, desiredChartWidth, desiredChartHeight, true);
    }

    private Chart generateChartInternal(final User remoteUser, final SearchRequest searchRequest,
            final String statisticType, final int desiredChartWidth, final int desiredChartHeight, boolean inline)
    {
        Assertions.notNull("searchRequest", searchRequest);
        Assertions.notNull("statisticType", statisticType);

        final I18nBean i18nBean = getI18nBean(remoteUser);

        try
        {
            log.debug("DataCollector.populatePieChart: Cloning the SearchRequest.");
            final SearchRequest clonedSearchRequest = new SearchRequest(searchRequest.getQuery());

            final StatisticAccessorBean statisticAccessor = new StatisticAccessorBean(remoteUser, clonedSearchRequest);
            final StatisticMapWrapper statisticMapWrapper = statisticAccessor.getAllFilterBy(statisticType);
            final SearchRequestAppender searchRequestAppender = statisticAccessor.getSearchRequestAppender(statisticType);

            PieChartDataSetFactory dataSetFactory = new PieChartDataSetFactory(constantsManager, customFieldManager, statisticMapWrapper, i18nBean, statisticType);

            final long numIssues = statisticMapWrapper.getTotalCount();

            final PieDataset sortedDataset = dataSetFactory.createSortedPieDataset();
            final PieDataset consolidatedDataset = dataSetFactory.createConsolidatedPieDataset(sortedDataset, otherSegmentKey);
            final CategoryDataset categoryDataset = dataSetFactory.createCategoryDataset(numIssues, sortedDataset);

            log.debug("DataCollector.populatePieChart: Generate the ChartHelper.");
            final ChartHelper helper = new PieChartGenerator(consolidatedDataset, i18nBean).generateChart();
            log.debug("DataCollector.populatePieChart: ChartHelper generated. Add Tooltips and URL Generators.");

            PieChartUrlGeneratorFactory urlGeneratorFactory = new PieChartUrlGeneratorFactory(searchService, applicationProperties, searchRequestAppender, remoteUser, clonedSearchRequest);

            final PiePlot plot = (PiePlot) helper.getChart().getPlot();

            plot.setToolTipGenerator(createPieToolTipGenerator(i18nBean, numIssues));
            plot.setURLGenerator(urlGeneratorFactory.getPieUrlGenerator(otherSegmentKey));

            final int chartHeight = numIssues == 0 ? 35 : desiredChartHeight;
            final int chartWidth = desiredChartWidth;

            log.debug("DataCollector.populatePieChart: Have the ChartHelper generate the image.");
            if (inline)
            {
                helper.generateInline(chartWidth, chartHeight);
            }
            else
            {
                helper.generate(chartWidth, chartHeight);
            }
            log.debug("DataCollector.populatePieChart: ChartHelper finished generating.");

            final Map<String, Object> params = createChartParameters(
                    statisticType,
                    i18nBean,
                    numIssues,
                    consolidatedDataset,
                    categoryDataset,
                    helper,
                    urlGeneratorFactory.getCategoryUrlGenerator(),
                    chartHeight,
                    chartWidth
            );

            if (inline)
            {
                String base64Image = ComponentAccessor.getComponent(ChartUtils.class).renderBase64Chart(helper.getImage(), "Pie Chart");
                params.put("base64Image", base64Image);
            }
            return new Chart(
                    helper.getLocation(),
                    helper.getImageMapHtml(),
                    helper.getImageMapName(),
                    params
            );
        }
        catch (SearchException e)
        {
            throw new RuntimeException("Error generating pie chart", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error generating pie chart", e);
        }
    }

    private Map<String, Object> createChartParameters(String statisticType, I18nBean i18nBean, long numIssues, PieDataset consolidatedDataset, CategoryDataset completeDataset, ChartHelper helper, CategoryURLGenerator completeUrlGenerator, int chartHeight, int chartWidth)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("chart", helper.getLocation());
        params.put("chartDataset", consolidatedDataset);
        params.put("completeDataset", completeDataset);
        params.put("completeDatasetUrlGenerator", completeUrlGenerator);
        params.put("numIssues", numIssues);
        params.put("statisticType", statisticType);
        params.put("statisticTypeI18nName", getStatisticsTypeI18nName(i18nBean, statisticType));
        params.put("imagemap_html", helper.getImageMapHtml());
        params.put("imagemapName", helper.getImageMapName());
        params.put("width", chartWidth);
        params.put("height", chartHeight);
        return params;
    }

    static PieToolTipGenerator createPieToolTipGenerator(final I18nBean i18nBean, final long numIssues)
    {
        return new PieToolTipGenerator()
        {
            public String generateToolTip(final PieDataset dataset, final Comparable key)
            {
                final Number number = dataset.getValue(key);
                return key + ": " + number + " " + i18nBean.getText(ISSUES_MESSAGE_CODE) + " (" + (100 * number.intValue() / numIssues) + "%)";
            }
        };
    }

    String getStatisticsTypeI18nName(final I18nHelper i18nBean, final String statisticType)
    {
        //check if it's a custom field and look up the custom field name if that's the case
        if (statisticType.startsWith(FieldManager.CUSTOM_FIELD_PREFIX))
        {
            final CustomField customField = customFieldManager.getCustomFieldObject(statisticType);
            if (customField == null)
            {
                throw new RuntimeException("No custom field with id '" + statisticType + "'");
            }
            if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
            {
                return customField.getName();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return i18nBean.getText("gadget.filterstats.field.statistictype." + statisticType.toLowerCase());
        }
    }

    private static I18nBean getI18nBean(final User remoteUser)
    {
        return new I18nBean(remoteUser);
    }
}