package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;

import com.google.common.collect.ImmutableList;

import org.apache.lucene.search.SortField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultOrderByValidator
{
    private static final User ANONYMOUS = null;

    @Mock SearchHandlerManager searchHandlerManager;
    @Mock FieldManager fieldManager;
    @Mock I18nHelper.BeanFactory i18nBeanFactory;

    private DefaultOrderByValidator defaultOrderByValidator;

    @Before
    public void setUp()
    {
        when(i18nBeanFactory.getInstance(ANONYMOUS)).thenReturn(new MockI18nBean());
        defaultOrderByValidator = new DefaultOrderByValidator(searchHandlerManager, fieldManager, i18nBeanFactory);
    }

    @After
    public void tearDown()
    {
        searchHandlerManager = null;
        fieldManager = null;
        i18nBeanFactory = null;
        defaultOrderByValidator = null;
    }

    @Test
    public void testNoSearchSorts() throws Exception
    {
        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl());
        assertNoMessages(messageSet);
    }

    @Test
    public void testSearchSortNoFieldIds() throws Exception
    {
        when(searchHandlerManager.getFieldIds(null, "test")).thenReturn(ImmutableList.of("testField"));

        final NavigableField field = mock(NavigableField.class);
        when(field.getSortFields(false)).thenReturn(ImmutableList.of(new SortField("notfound", SortField.STRING, false)));

        when(fieldManager.isNavigableField("testField")).thenReturn(true);
        when(fieldManager.getNavigableField("testField")).thenReturn(field);
        when(searchHandlerManager.getFieldIds(null, "notfound")).thenReturn(ImmutableList.<String>of());

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("notfound")));
        assert1ErrorNoWarnings(messageSet, "Not able to sort using field 'notfound'.");
    }

    @Test
    public void testSearchSortNotNavigableField() throws Exception
    {
        when(searchHandlerManager.getFieldIds(null, "notfound")).thenReturn(ImmutableList.of("customfield10"));

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("notfound")));
        assert1ErrorNoWarnings(messageSet, "Field 'notfound' does not support sorting.");
    }

    @Test
    public void testSearchSortNavigableFieldHasNoSorter() throws Exception
    {
        when(searchHandlerManager.getFieldIds(null, "notfound")).thenReturn(ImmutableList.of("customfield10"));

        final NavigableField field = mock(NavigableField.class);
        when(field.getSortFields(false)).thenReturn(ImmutableList.<SortField>of());

        when(fieldManager.isNavigableField("customfield10")).thenReturn(true);
        when(fieldManager.getNavigableField("customfield10")).thenReturn(field);

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("notfound")));
        assert1ErrorNoWarnings(messageSet, "Field 'notfound' does not support sorting.");
    }

    @Test
    public void testSearchSortDuplicateSorts() throws Exception
    {
        when(searchHandlerManager.getFieldIds(null, "test")).thenReturn(ImmutableList.of("testField"));

        final NavigableField field = mock(NavigableField.class);
        when(field.getSortFields(false)).thenReturn(ImmutableList.of(new SortField("notfound", SortField.STRING, false)));

        when(fieldManager.isNavigableField("testField")).thenReturn(true);
        when(fieldManager.getNavigableField("testField")).thenReturn(field);

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("test")));
        assert1ErrorNoWarnings(messageSet, "The sort field 'test' is referenced multiple times in the JQL sort.");
    }

    @Test
    public void testSearchSortAliasedSorts() throws Exception
    {
        final NavigableField field = mock(NavigableField.class);
        when(field.getSortFields(false)).thenReturn(ImmutableList.of(new SortField("notfound", SortField.STRING, false)));

        when(searchHandlerManager.getFieldIds(null, "test")).thenReturn(ImmutableList.of("testField"));
        when(searchHandlerManager.getFieldIds(null, "anothertest")).thenReturn(ImmutableList.of("testField"));

        when(fieldManager.isNavigableField("testField")).thenReturn(true);
        when(fieldManager.getNavigableField("testField")).thenReturn(field);

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("anothertest")));
        assert1ErrorNoWarnings(messageSet, "The sort field 'anothertest' is referenced multiple times in the JQL sort. Field 'anothertest' is an alias for field 'test'.");
    }

    @Test
    public void testSearchSortHappyPath() throws Exception
    {
        final NavigableField field = mock(NavigableField.class);
        when(field.getSortFields(false)).thenReturn(ImmutableList.of(new SortField("notfound", SortField.STRING, false)));

        when(searchHandlerManager.getFieldIds(null, "test")).thenReturn(ImmutableList.of("testField"));
        when(searchHandlerManager.getFieldIds(null, "anothertest")).thenReturn(ImmutableList.of("anotherField"));

        when(fieldManager.isNavigableField("testField")).thenReturn(true);
        when(fieldManager.getNavigableField("testField")).thenReturn(field);
        when(fieldManager.isNavigableField("anotherField")).thenReturn(true);
        when(fieldManager.getNavigableField("anotherField")).thenReturn(field);

        final MessageSet messageSet = defaultOrderByValidator.validate(null, new OrderByImpl(new SearchSort("test"), new SearchSort("anothertest")));
        assertNoMessages(messageSet);
    }

}
