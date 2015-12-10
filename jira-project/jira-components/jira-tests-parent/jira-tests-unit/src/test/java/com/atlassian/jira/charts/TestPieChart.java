package com.atlassian.jira.charts;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.data.general.PieDataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestPieChart
{
    @Mock
    private CustomFieldManager customFieldManager;

    @Mock (extraInterfaces = { CustomFieldStattable.class })
    private CustomFieldSearcher customFieldSearcher;

    @Mock
    private CustomField customField;

    @Mock
    private I18nHelper i18nHelper;

    private PieChart pieChart;

    @Before
    public void setUp() throws Exception
    {
        when(customField.getCustomFieldSearcher()).thenReturn(customFieldSearcher);
        when(customField.getName()).thenReturn("Custom Field Name");

        when(customFieldManager.getCustomFieldObject("customfield_10001")).thenReturn(customField);

        when(i18nHelper.getText("gadget.filterstats.field.statistictype.type")).thenReturn("Issue Type");
        when(i18nHelper.getText("gadget.filterstats.field.statistictype.priorities")).thenReturn("Priorities");

        pieChart = new PieChart(null, customFieldManager, null, null);
    }

    @Test
    public void testGetStatisticsTypeI18nName()
    {
        assertThat(pieChart.getStatisticsTypeI18nName(i18nHelper, "type"), is("Issue Type"));
        assertThat(pieChart.getStatisticsTypeI18nName(i18nHelper, "priorities"), is("Priorities"));
        assertThat(pieChart.getStatisticsTypeI18nName(i18nHelper, "customfield_10001"), is("Custom Field Name"));
    }

    @Test // For JRA-29993
    public void pieSegmentToolTipsShouldNotEscapeForeignCharacters()
    {
        // Set up
        final I18nBean mockI18nBean = mock(I18nBean.class);
        final long numIssues = 200;
        final PieToolTipGenerator toolTipGenerator =
                PieChart.createPieToolTipGenerator(mockI18nBean, numIssues);
        final PieDataset mockPieDataSet = mock(PieDataset.class);
        final Comparable<?> key = "\uC2DC\uD5D8"; // Korean characters
        when(mockPieDataSet.getValue(key)).thenReturn(100);
        when(mockI18nBean.getText(PieChart.ISSUES_MESSAGE_CODE)).thenReturn("Bugs");

        // Invoke
        final String toolTip = toolTipGenerator.generateToolTip(mockPieDataSet, key);

        // Check
        assertEquals(key + ": 100 Bugs (50%)", toolTip);
    }
}