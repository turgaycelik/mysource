package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractSingleValueCustomFieldSearchInputTransformer
{
    private static final String ID = "cf[1090]";

    @Mock private CustomField mockCustomField;
    @Mock private CustomFieldInputHelper mockCustomFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        when(mockCustomField.getName()).thenReturn("ABC");
        when(mockCustomField.getUntranslatedName()).thenReturn("ABC");
        when(mockCustomFieldInputHelper.getUniqueClauseName(null, ID, "ABC")).thenReturn(ID);
    }

    @Test
    public void testGetSearchClause() throws Exception
    {
        final String value = "value";

        _testGetSearchClause(null, new CustomFieldParamsImpl());
        _testGetSearchClause(new TerminalClauseImpl(ID, Operator.EQUALS, value),
                new CustomFieldParamsImpl(mockCustomField, value));
        _testGetSearchClause(null, null);
        _testGetSearchClause(null, new CustomFieldParamsImpl(mockCustomField));
        _testGetSearchClause(null,
                new CustomFieldParamsImpl(mockCustomField, CollectionBuilder.newBuilder(value, value+"1").asList()));
    }

    @Test
    public void testdoRelevantClausesFitFilterForm() throws Exception
    {
        final String value = "value";
        _testDoRelevantClausesFitFilterForm(false, null,
                new QueryImpl(new TerminalClauseImpl(ID, Operator.EQUALS, new MultiValueOperand(value, value))));
        _testDoRelevantClausesFitFilterForm(true, new SingleValueOperand(value),
                new QueryImpl(new TerminalClauseImpl(ID, Operator.EQUALS, value)));
        _testDoRelevantClausesFitFilterForm(false, null,
                new QueryImpl(new TerminalClauseImpl(ID, Operator.NOT_EQUALS, value)));
        _testDoRelevantClausesFitFilterForm(true, null, null);
        _testDoRelevantClausesFitFilterForm(true, null, new QueryImpl());
        _testDoRelevantClausesFitFilterForm(true, null,
                new QueryImpl(new TerminalClauseImpl("blarg", Operator.EQUALS, value)));
        _testDoRelevantClausesFitFilterForm(false, null,
                new QueryImpl(new OrClause(new TerminalClauseImpl(ID, Operator.EQUALS, value),
                        new TerminalClauseImpl(ID, Operator.EQUALS, value))));
    }

    private void _testDoRelevantClausesFitFilterForm(final boolean isValid, final SingleValueOperand value, Query query)
    {
        final AbstractSingleValueCustomFieldSearchInputTransformer transformer =
                new AbstractSingleValueCustomFieldSearchInputTransformer(
                        mockCustomField, new ClauseNames(ID), ID, mockCustomFieldInputHelper)
        {
            public boolean doRelevantClausesFitFilterForm(
                    final User user, final Query query, final SearchContext searchContext)
            {
                return false;
            }

            protected CustomFieldParams getParamsFromSearchRequest(
                    final User user, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        final NavigatorConversionResult result = transformer.convertForNavigator(query);

        assertTrue(result.fitsNavigator() == isValid);
        assertEquals(value, result.getValue());
    }
    
    private void _testGetSearchClause(Clause expected, CustomFieldParams params)
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(ID, params).toHashMap());

        final AbstractSingleValueCustomFieldSearchInputTransformer transformer =
                new AbstractSingleValueCustomFieldSearchInputTransformer(
                        mockCustomField, new ClauseNames(ID), ID, mockCustomFieldInputHelper)
        {
            public boolean doRelevantClausesFitFilterForm(
                    final User user, final Query query, final SearchContext searchContext)
            {
                return false;
            }

            protected CustomFieldParams getParamsFromSearchRequest(
                    final User user, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        final Clause result = transformer.getSearchClause(null, holder);
        assertEquals(expected, result);
    }
}
