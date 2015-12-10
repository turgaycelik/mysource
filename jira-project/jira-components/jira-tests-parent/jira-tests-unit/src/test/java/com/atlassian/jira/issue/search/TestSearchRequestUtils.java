package com.atlassian.jira.issue.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.search.util.SearchSortUtilImpl;
import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.query.order.SortOrder.ASC;
import static com.atlassian.query.order.SortOrder.DESC;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v3.13.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSearchRequestUtils
{
    private static final String FIELD_1 = "goodField1";
    private static final String FIELD_2 = "goodField2";
    private static final String FIELD_3 = "goodField3";
    private static final String BAD_FIELD = "badField";

    @Mock private FieldManager fieldManager;
    @Mock private SearchHandlerManager searchHandlerManager;

    private SearchSortUtil searchSortUtil;

    private I18nHelper i18n = new MockI18nHelper();

    @Before
    public void setUp()
    {
        final ArgumentCaptor<String> jqlClauseNameCaptor = ArgumentCaptor.forClass(String.class);
        final Answer<Collection<String>> collectionFromClauseName = new Answer<Collection<String>>()
        {
            @Override
            public Collection<String> answer(final InvocationOnMock invocation) throws Throwable
            {
                return Collections.singleton(jqlClauseNameCaptor.getValue());
            }
        };
        when(searchHandlerManager.getFieldIds(any(User.class), jqlClauseNameCaptor.capture())).thenAnswer(collectionFromClauseName);
        when(searchHandlerManager.getFieldIds(jqlClauseNameCaptor.capture())).thenAnswer(collectionFromClauseName);

        searchSortUtil = new SearchSortUtilImpl(searchHandlerManager, fieldManager);

        new MockComponentWorker()
                .addMock(SearchSortUtil.class, searchSortUtil)
                .init();
    }

    @After
    public void tearDown()
    {
        fieldManager = null;
        searchHandlerManager = null;
        searchSortUtil = null;
        i18n = null;
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGetSearchSortDescriptionsNoSorts() throws Exception
    {
        createMockNavigableField("key");
        final SearchRequest sr = new SearchRequest();

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains("key navigator.hidden.sortby.descending"));
    }

    @Test
    public void testGetSearchSortDescriptionsOneNonNavigableSort() throws Exception
    {
        createMockField(FIELD_1);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(FIELD_1, DESC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(FIELD_1));
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodSortDesc() throws Exception
    {
        createMockNavigableField(FIELD_1);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(FIELD_1, DESC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(FIELD_1 + " navigator.hidden.sortby.descending"));
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodSortAsc() throws Exception
    {
        createMockNavigableField(FIELD_1);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(FIELD_1, ASC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(FIELD_1 + " navigator.hidden.sortby.ascending"));
    }

    @Test
    public void testGetSearchSortDescriptionsOneBadSort() throws Exception
    {
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(BAD_FIELD, DESC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, hasSize(0));
    }

    @Test
    public void testGetSearchSortDescriptionsTwoGoodSorts() throws Exception
    {
        createMockNavigableField(FIELD_1);
        createMockNavigableField(FIELD_2);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(new SearchSort(FIELD_2, DESC), new SearchSort(FIELD_1, ASC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(
                "goodField2 navigator.hidden.sortby.descending, navigator.hidden.sortby.then",
                "goodField1 navigator.hidden.sortby.ascending"));
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodSortOneBadSort() throws Exception
    {
        createMockNavigableField(FIELD_1);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(
                new SearchSort(BAD_FIELD, DESC),
                new SearchSort(FIELD_1, ASC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(FIELD_1 + " navigator.hidden.sortby.ascending"));
    }

    @Test
    public void testGetSearchSortDescriptionsOneGoodOneBadOneGoodSort() throws Exception
    {
        createMockNavigableField(FIELD_1);
        createMockNavigableField(FIELD_3);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(
                new SearchSort(FIELD_3, DESC),
                new SearchSort(BAD_FIELD, DESC),
                new SearchSort(FIELD_1, ASC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(
                FIELD_3 + " navigator.hidden.sortby.descending, navigator.hidden.sortby.then",
                FIELD_1 + " navigator.hidden.sortby.ascending"));
    }

    @Test
    public void testGetSearchSortDescriptionsOneBadTwoGoodSort() throws Exception
    {
        createMockNavigableField(FIELD_2);
        createMockNavigableField(FIELD_3);
        final SearchRequest sr = new SearchRequest();
        sr.setQuery(new QueryImpl(null, new OrderByImpl(
                new SearchSort(FIELD_3, DESC),
                new SearchSort(FIELD_2, DESC),
                new SearchSort(BAD_FIELD, ASC)), null));

        final List<String> list = getSearchSortDescriptions(sr);
        assertThat(list, contains(
                FIELD_3 + " navigator.hidden.sortby.descending, navigator.hidden.sortby.then",
                FIELD_2 + " navigator.hidden.sortby.descending"));
    }

    private NavigableField createMockNavigableField(final String nameKey)
    {
        final NavigableField field = mock(NavigableField.class, new Strict());
        doReturn(nameKey).when(field).getNameKey();
        when(fieldManager.getField(nameKey)).thenReturn(field);
        return field;
    }

    private Field createMockField(final String nameKey)
    {
        final Field field = mock(Field.class, new Strict());
        doReturn(nameKey).when(field).getNameKey();
        when(fieldManager.getField(nameKey)).thenReturn(field);
        return field;
    }

    private List<String> getSearchSortDescriptions(final SearchRequest sr)
    {
        return SearchRequestUtils.getSearchSortDescriptions(sr, fieldManager, searchHandlerManager, searchSortUtil, i18n, null);
    }
}
