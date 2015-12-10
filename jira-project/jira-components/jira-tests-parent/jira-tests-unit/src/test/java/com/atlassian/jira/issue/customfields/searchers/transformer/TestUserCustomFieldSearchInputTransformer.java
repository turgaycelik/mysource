package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserCustomFieldSearchInputTransformer
{
    private final String id = "cf[100]";

    @Mock private UserConverter userConverter;
    @Mock private UserFitsNavigatorHelper userFitsNavigatorHelper;
    @Mock private CustomField customField;
    @Mock private SearchContext searchContext;
    @Mock private CustomFieldInputHelper customFieldInputHelper;

    private final User searcher = null;
    private ClauseNames clauseNames;
    private I18nHelper i18nHelper;

    @Before
    public void setUp() throws Exception
    {
        when(customField.getId()).thenReturn(id);
        clauseNames = new ClauseNames(id);
        i18nHelper = new MockI18nHelper();
    }

    @Test
    public void testGetParamsFromSearchRequest() throws Exception
    {
        final String userValue = "userValue";
        final String userName = "userName";

        when(userFitsNavigatorHelper.checkUser(userValue)).thenReturn(userName);

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand(userValue));
            }
        };

        final CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField, Collections.singleton(userName));
        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetParamsFromSearchRequestUserNameInvalid() throws Exception
    {
        final String userValue = "userValue";
        when(userFitsNavigatorHelper.checkUser(userValue)).thenReturn(null);

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand(userValue));
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext);
        assertNull(result);
    }

    @Test
    public void testGetParamsFromSearchRequestInvalidStructure() throws Exception
    {
        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, null);
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext);
        assertNull(result);
    }

    @Test
    public void testGetSearch() throws Exception
    {
        when(customField.getUntranslatedName()).thenReturn("ABC");
        when(customFieldInputHelper.getUniqueClauseName(searcher, clauseNames.getPrimaryName(), "ABC")).thenReturn(clauseNames.getPrimaryName());

        final String value = "value";
        _testGetSearchClause(new TerminalClauseImpl(id, Operator.EQUALS, value), new CustomFieldParamsImpl(customField, value));
        _testGetSearchClause(null, null);
        _testGetSearchClause(null, new CustomFieldParamsImpl(customField));
        _testGetSearchClause(null, new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value, value+"1").asList()));
    }

    @Test
    public void testdoRelevantClausesFitFilterForm() throws Exception
    {
        final String value = "value";
        _testDoRelevantClausesFitFilterForm(false, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, new MultiValueOperand(value, value))), value, value);
        _testDoRelevantClausesFitFilterForm(true, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, value)), value, value);
        _testDoRelevantClausesFitFilterForm(false, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, value)), value, null);
        _testDoRelevantClausesFitFilterForm(true, null, value, value);
        _testDoRelevantClausesFitFilterForm(true,  new QueryImpl(new TerminalClauseImpl("blarg", Operator.EQUALS, value)), value, value);
        _testDoRelevantClausesFitFilterForm(false, new QueryImpl(new OrClause(
                new TerminalClauseImpl(id, Operator.EQUALS, value), new TerminalClauseImpl(id, Operator.EQUALS, value))), value, value);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testValidateParamsNoUser() throws Exception
    {
        final String value = "value";

        when(userConverter.getUser(value)).thenThrow(new FieldValidationException("blarg!"));

        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value).asList());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);

        final I18nHelper mockI18nHelper = mock(I18nHelper.class);
        final String error = "whoops";
        when(mockI18nHelper.getText("admin.errors.could.not.find.username", value)).thenReturn(error);
        final ErrorCollection mockErrorCollection = mock(ErrorCollection.class);

        // Invoke
        transformer.validateParams(null, searchContext, holder, mockI18nHelper, mockErrorCollection);

        // Check
        verify(mockErrorCollection).addError(id, error);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testValidateParamsHappyPath() throws Exception
    {
        final String value = "value";
        when(userConverter.getUser(value)).thenReturn(null);

        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value).asList());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();

        transformer.validateParams(null, searchContext, holder, i18nHelper, errorCollection);

        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateParamsNoParams() throws Exception
    {
        final CustomFieldParamsImpl params = null;
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        transformer.validateParams(null, searchContext, holder, i18nHelper, errorCollection);

        assertFalse(errorCollection.hasAnyErrors());
    }

    private void _testGetSearchClause(Clause expected, CustomFieldParams params)
    {
        final FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);
        final Clause result = transformer.getSearchClause(null, holder);
        assertEquals(expected, result);
    }

    private void _testDoRelevantClausesFitFilterForm(boolean expected, Query query, final String name, final String checkResult)
    {
        when(userFitsNavigatorHelper.checkUser(name)).thenReturn(checkResult);

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(
                id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);

        boolean result = transformer.doRelevantClausesFitFilterForm(null, query, searchContext);

        assertEquals(expected, result);
    }
}
