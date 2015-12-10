package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.DocumentIssueImpl;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.mock.issue.MockIssue;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertTrue;

public class TestAggregateTimeTrackingCalculatorFactory
{

    @Test
    public void testDocumentIssue()
    {
        AggregateTimeTrackingCalculatorFactory factory = getFactory();

        AggregateTimeTrackingCalculator calc = factory.getCalculator(new DocumentIssueImpl(null, null, null, null, null, null, null, null));

        assertTrue(calc instanceof DocumentIssueAggregateTimeTrackingCalculator);

    }

    @Test
    public void testIssueImpl()
    {
        AggregateTimeTrackingCalculatorFactory factory = getFactory();

        AggregateTimeTrackingCalculator calc = factory.getCalculator(new IssueImpl((GenericValue) null, null, null, null, null, null, null, null, null, null, null, null));

        assertTrue(calc instanceof IssueImplAggregateTimeTrackingCalculator);

    }

    @Test
    public void testNullIssue()
    {
        AggregateTimeTrackingCalculatorFactory factory = getFactory();

        AggregateTimeTrackingCalculator calc = factory.getCalculator(null);

        assertTrue(calc instanceof IssueImplAggregateTimeTrackingCalculator);

    }

    @Test
    public void testMockIssue()
    {
        AggregateTimeTrackingCalculatorFactory factory = getFactory();

        AggregateTimeTrackingCalculator calc = factory.getCalculator(new MockIssue());

        assertTrue(calc instanceof IssueImplAggregateTimeTrackingCalculator);

    }

    private AggregateTimeTrackingCalculatorFactory getFactory()
    {
        Mock navigableFieldMock = new Mock(NavigableField.class);
        navigableFieldMock.expectAndReturn("getSorter", null);
        NavigableField navField = (NavigableField) navigableFieldMock.proxy();
        Mock fieldManagerMock = new Mock(FieldManager.class);
        fieldManagerMock.expectAndReturn("getNavigableField", P.ANY_ARGS, navField);
        FieldManager fieldManager = (FieldManager) fieldManagerMock.proxy();

        Mock searchProviderMock = new Mock(SearchProvider.class);
        SearchProvider searchProvider = (SearchProvider) searchProviderMock.proxy();

        AggregateTimeTrackingCalculatorFactoryImpl factory = new AggregateTimeTrackingCalculatorFactoryImpl(null, null, null);
        factory.setFieldManager(fieldManager);
        factory.setSearchProvider(searchProvider);
        return factory;
    }
}
