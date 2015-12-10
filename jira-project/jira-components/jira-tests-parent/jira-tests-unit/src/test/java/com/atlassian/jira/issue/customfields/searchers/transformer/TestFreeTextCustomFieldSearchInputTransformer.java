package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestFreeTextCustomFieldSearchInputTransformer
{
    private static final User ANONYMOUS = null;
    private static final String ID = "id";
    private static final String URL = "url";
    private static final ClauseNames NAMES = new ClauseNames("name");

    @Mock private CustomField field;
    @Mock private CustomFieldType<?,?> customFieldType;
    @Mock private FieldConfig fieldConfig;
    @Mock private SearchContext searchContext;
    @Mock private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        when(field.getId()).thenReturn(ID);
        when(field.getFieldName()).thenReturn(ID);
    }

    @After
    public void tearDown()
    {
        field = null;
        fieldConfig = null;
        searchContext = null;
        customFieldInputHelper = null;
    }


    @Test
    public void testCreateSearchClause() throws Exception
    {
        when(field.getUntranslatedName()).thenReturn(ID);
        when(customFieldInputHelper.getUniqueClauseName(ANONYMOUS, NAMES.getPrimaryName(), ID)).thenReturn(ID);

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper);
        final Clause result = transformer.createSearchClause(ANONYMOUS, "value");
        final Clause expectedResult = new TerminalClauseImpl(ID, Operator.LIKE, "value");

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetParamsFromSearchRequestDoesntFit() throws Exception
    {
        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, null);
            }
        };

        assertThat(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext), nullValue());
    }

    @Test
    public void testGetParamsFromSearchRequestNoValue() throws Exception
    {
        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, null);
            }
        };

        assertThat(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext), nullValue());
    }

    @Test
    public void testGetParamsFromSearchRequestHappyPath() throws Exception
    {
        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand("blah"));
            }
        };

        final CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(field, ImmutableList.of("blah"));

        assertEquals(expectedResult, transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));
    }

    @Test
    public void testValidateParamsDoesNotContainKey() throws Exception
    {
        final SearchContext searchContext = mock(SearchContext.class);
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsNullParam() throws Exception
    {
        final CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", ImmutableList.of("blah")).toHashMap());
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(ID, params).toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsFieldValidationException() throws Exception
    {
        final CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", ImmutableList.of("blah")).toHashMap());
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(ID, params).toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getValueFromCustomFieldParams(params)).thenThrow(new FieldValidationException("blarg!"));

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assert1FieldError(errors, ID, "blarg!");
    }

    @Test
    public void testValidateParamsClassCastException() throws Exception
    {
        final CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", ImmutableList.of("blah")).toHashMap());
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(ID, params).toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getValueFromCustomFieldParams(params)).thenThrow(new ClassCastException());

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assert1FieldError(errors, ID, "Internal error attempting to validate the search term.");
    }

    // JRA-27018
    @Test
    public void testValidateParamsInValidQueryRuntimeException() throws Exception
    {
        final CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", ImmutableList.of("blah")).toHashMap());
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(ID, params).toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        final QueryParser queryParser = mock(QueryParser.class);
        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getValueFromCustomFieldParams(params)).thenReturn("value");
        when(queryParser.parse("value")).thenThrow(new IllegalArgumentException());

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            QueryParser getQueryParser(final CustomField customField)
            {
                return queryParser;
            }
        };

        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assert1FieldError(errors, ID, "navigator.error.parse");
    }


    @Test
    public void testValidateParamsInValidQuery() throws Exception
    {
        final CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", ImmutableList.of("blah")).toHashMap());
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(ID, params).toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        final QueryParser queryParser = mock(QueryParser.class);
        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getValueFromCustomFieldParams(params)).thenReturn("value");
        when(queryParser.parse("value")).thenThrow(new ParseException());

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            QueryParser getQueryParser(final CustomField customField)
            {
                return queryParser;
            }
        };
        
        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assert1FieldError(errors, ID, "navigator.error.parse");
    }

    @Test
    public void testValidateParamsHappyPath() throws Exception
    {
        final CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", ImmutableList.of("blah")).toHashMap());
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(ID, params).toHashMap());
        final ErrorCollection errors = new SimpleErrorCollection();

        final QueryParser queryParser = mock(QueryParser.class);

        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getValueFromCustomFieldParams(params)).thenReturn("value");

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            QueryParser getQueryParser(final CustomField customField)
            {
                return queryParser;
            }
        };

        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errors);
        assertNoErrors(errors);
    }

    @Test
    public void testFitsNavigator() throws Exception
    {
        final AtomicInteger called = new AtomicInteger(0);

        final FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, NAMES, URL, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(called.incrementAndGet() == 1, null);
            }
        };

        assertThat("first result", transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext), is(true));
        assertThat("second result", transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext), is(false));
        assertEquals("called", 2, called.get());
    }
}
