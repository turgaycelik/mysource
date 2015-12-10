package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;

import com.google.common.collect.ImmutableList;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultCustomFieldInputHelper
{
    @Mock private SearchHandlerManager searchHandlerManager;

    private User searcher;
    private DefaultCustomFieldInputHelper helper;

    @After
    public void tearDown()
    {
        searchHandlerManager = null;
        searcher = null;
        helper = null;
    }

    @Test
    public void testGetClauseNameNameIsUnique() throws Exception
    {
        final String fieldName = "ABC";
        final String primaryName = "cf[10000]";
        final ClauseHandler clauseHandler = mock(ClauseHandler.class);
        CustomField customField = mock(CustomField.class);

        when(searchHandlerManager.getClauseHandler(searcher, fieldName)).thenReturn(ImmutableList.of(clauseHandler));
        when(customField.getName()).thenReturn(fieldName);

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(fieldName, result);
    }

    @Test
    public void testGetClauseNameNameIsNotUnique() throws Exception
    {
        final String fieldName = "ABC";
        final String primaryName = "cf[10000]";
        final ClauseHandler clauseHandler1 = EasyMock.createMock(ClauseHandler.class);
        final ClauseHandler clauseHandler2 = EasyMock.createMock(ClauseHandler.class);
        CustomField customField = mock(CustomField.class);

        when(searchHandlerManager.getClauseHandler(searcher, fieldName)).thenReturn(ImmutableList.of(clauseHandler1, clauseHandler2));
        when(customField.getName()).thenReturn(fieldName);

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(primaryName, result);
    }

    @Test
    public void testGetClauseNameNameIsSystemFieldName() throws Exception
    {
        final String fieldName = "project";
        final String primaryName = "cf[10000]";
        CustomField customField = mock(CustomField.class);

        when(customField.getName()).thenReturn(fieldName);

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(primaryName, result);
    }

    @Test
    public void testGetClauseNameNameIsCustomFieldId() throws Exception
    {
        final String fieldName = "cf[12345]";
        final String primaryName = "cf[10000]";
        CustomField customField = mock(CustomField.class);

        when(customField.getName()).thenReturn(fieldName);

        helper = new DefaultCustomFieldInputHelper(searchHandlerManager);

        final String result = helper.getUniqueClauseName(searcher, primaryName, fieldName);
        assertEquals(primaryName, result);
    }
}
