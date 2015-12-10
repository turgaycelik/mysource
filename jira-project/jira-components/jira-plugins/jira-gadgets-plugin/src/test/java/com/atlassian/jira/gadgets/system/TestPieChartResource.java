package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test for {@link com.atlassian.jira.gadgets.system.PieChartResource}.
 *
 * @since v4.0
 */
public class TestPieChartResource extends TestCase
{
    public void testGetData()
    {
        final CategoryURLGenerator mockUrlGenerator = EasyMock.createMock(CategoryURLGenerator.class);
        final CategoryDataset mockDataset = EasyMock.createMock(CategoryDataset.class);
        final PieChartResource.DataRow[] mockRows = new PieChartResource.DataRow[] { EasyMock.createMock(PieChartResource.DataRow.class) };
        final AtomicBoolean sorted = new AtomicBoolean(false);
        PieChartResource pcr = new PieChartResource(null, null, null, null, null, null, null)
        {
            @Override
            DataRow[] generateDataSet(final CategoryDataset dataset, final CategoryURLGenerator urlGenerator)
            {
                assertEquals(mockDataset, dataset);
                assertEquals(mockUrlGenerator, urlGenerator);
                return mockRows;
            }

            @Override
            void sort(final String statType, final DataRow[] data)
            {
                sorted.set(true);
                assertEquals("statType", statType);
                assertEquals(mockRows, data);
            }
        };
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PieChartResource.KEY_DATASET, mockDataset);
        params.put(PieChartResource.KEY_URL_GENERATOR, mockUrlGenerator);
        final PieChartResource.DataRow[] returnedRows = pcr.getData("statType", params);
        assertTrue(Arrays.equals(mockRows, returnedRows));
        assertTrue(sorted.get());
    }

    public void testCreateChart()
    {
        ChartFactory mockChartFactory = EasyMock.createMock(ChartFactory.class);
        Chart mockChart = EasyMock.createMock(Chart.class);
        EasyMock.expect(mockChartFactory.generatePieChart(EasyMock.isA(ChartFactory.ChartContext.class), EasyMock.eq("statType"))).andReturn(mockChart);
        PieChartResource pcr = new PieChartResource(mockChartFactory, null, null, null, null, null, null);
        final User barney = new MockUser("barney");
        final SearchRequest mockSearchRequest = EasyMock.createMock(SearchRequest.class);
        EasyMock.replay(mockSearchRequest, mockChartFactory, mockChart);

        final Chart returnedChart = pcr.createChart("statType", 300, 2, barney, mockSearchRequest, true);
        assertEquals(mockChart, returnedChart);

        EasyMock.verify(mockSearchRequest, mockChartFactory, mockChart);
    }

    public void testValidateStatType()
    {
        StatisticTypesResource mockStatisticTypesResource = EasyMock.createMock(StatisticTypesResource.class);
        EasyMock.expect(mockStatisticTypesResource.getDisplayName("someStatType")).andReturn("stat display name");
        EasyMock.replay(mockStatisticTypesResource);
        PieChartResource pcr = new PieChartResource(null, null, null, null, mockStatisticTypesResource, null, null);
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();

        pcr.validateStatType(errors, "someStatType");
        assertTrue(errors.isEmpty());
        EasyMock.verify(mockStatisticTypesResource);

        EasyMock.reset(mockStatisticTypesResource);
        EasyMock.expect(mockStatisticTypesResource.getDisplayName("someStatType")).andReturn("");
        EasyMock.replay(mockStatisticTypesResource);
        pcr.validateStatType(errors, "someStatType");
        assertEquals(1, errors.size());
        final ValidationError expectedError = new ValidationError(PieChartResource.STAT_TYPE, "gadget.common.invalid.stat.type", "someStatType");
        assertEquals(expectedError, errors.get(0));
        EasyMock.verify(mockStatisticTypesResource);
    }

    public void testGetPieChartError()
    {
        final String query = "theQueryString";

        JiraAuthenticationContext mockJiraAuthenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        EasyMock.expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney);

        PieChartResource pcr = new PieChartResource(null, null, mockJiraAuthenticationContext, null, null, null, null)
        {
            @Override
            protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
            {
                assertEquals(query, queryString);
                errors.add(new ValidationError("searchRequest", "error Message"));
                return null;
            }

            @Override
            String validateStatType(final Collection<ValidationError> errors, final String statType)
            {
                assertEquals("statType", statType);
                errors.add(new ValidationError("statType", "stat type Error"));
                return null;
            }
        };

        final Response response = pcr.getPieChart(query, "statType", false, 747, 22, true);
        assertEquals(400, response.getStatus());
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        errors.add(new ValidationError("searchRequest", "error Message"));
        errors.add(new ValidationError("statType", "stat type Error"));
        assertEquals(errors, ((ErrorCollection) response.getEntity()).getErrors());
    }

    public void testGenerateDataset()
    {
        PieChartResource pcr = new PieChartResource(null, null, null, null, null, null, null);
        DefaultCategoryDataset catDataset = new DefaultCategoryDataset();
        catDataset.addValue((Number) 2, 0, 0);
        catDataset.addValue((Number) 8, 0, 1);
        catDataset.addValue((Number) 20, 1, 0);
        catDataset.addValue((Number) 80, 1, 1);
        CategoryURLGenerator urlGenerator = new CategoryURLGenerator()
        {
            public String generateURL(final CategoryDataset data, final int series, final int category)
            {
                return "http://" + series + "," + category;
            }
        };
        final PieChartResource.DataRow[] dataRows = pcr.generateDataSet(catDataset, urlGenerator);
        PieChartResource.DataRow[] expectedDataRows = new PieChartResource.DataRow[] {
                new PieChartResource.DataRow(0, "http://0,0", 2, 20),
                new PieChartResource.DataRow(1, "http://0,1", 8, 80),
        };
        assertTrue(Arrays.equals(expectedDataRows, dataRows));
    }
}
