package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Collection;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWorkRatioSearchInputTransformer
{
    private static final User ANONYMOUS = null;
    private static final SimpleFieldSearchConstants CONSTANTS = SystemSearchConstants.forWorkRatio();
    private static final WorkRatioSearcherConfig SEARCHER_CONFIG = new WorkRatioSearcherConfig(CONSTANTS.getSearcherId());

    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @Mock private SearchContext searchContext;
    @Mock private JqlOperandResolver operandResolver;

    @After
    public void tearDown()
    {
        searchContext = null;
        operandResolver = null;
    }


    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullConstants()
    {
        new WorkRatioSearchInputTransformer(null, SEARCHER_CONFIG, operandResolver);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullConfig()
    {
        new WorkRatioSearchInputTransformer(CONSTANTS, null, operandResolver);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOperandResolver()
    {
        new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, null);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test
    public void testConstructor()
    {
        new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver);
    }

    @Test
    public void testValidateParamsEmptyValues() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = fixture();

        final FieldValuesHolder values = new FieldValuesHolderImpl();
        final I18nHelper i18n = new NoopI18nHelper();
        final ErrorCollection errors = new SimpleErrorCollection();

        transformer.validateParams(null, null, values, i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsHappyPath() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = fixture();

        final FieldValuesHolder values = new FieldValuesHolderImpl();
        final I18nHelper i18n = new NoopI18nHelper();
        final ErrorCollection errors = new SimpleErrorCollection();

        // just minimum
        values.put(SEARCHER_CONFIG.getMinField(), "45");
        transformer.validateParams(null, null, values, i18n, errors);
        assertNoErrors(errors);

        // minimum and maximum
        values.put(SEARCHER_CONFIG.getMaxField(), "45");
        transformer.validateParams(null, null, values, i18n, errors);
        assertNoErrors(errors);

        // just maximum
        values.remove(SEARCHER_CONFIG.getMinField());
        transformer.validateParams(null, null, values, i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsSadPath() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = fixture();
        final I18nHelper i18n = new NoopI18nHelper();

        // just minimum
        FieldValuesHolder values = new FieldValuesHolderImpl();
        ErrorCollection errors = new SimpleErrorCollection();
        values.put(SEARCHER_CONFIG.getMinField(), "xx");
        transformer.validateParams(null, null, values, i18n, errors);
        assert1FieldError(errors, SEARCHER_CONFIG.getMinField(), "navigator.filter.workratio.min.error{[]}");

        // just maximum
        values = new FieldValuesHolderImpl();
        errors = new SimpleErrorCollection();
        values.put(SEARCHER_CONFIG.getMaxField(), "zzz");
        transformer.validateParams(null, null, values, i18n, errors);
        assert1FieldError(errors, SEARCHER_CONFIG.getMaxField(), "navigator.filter.workratio.max.error{[]}");

        // maximum < minimum
        values = new FieldValuesHolderImpl();
        errors = new SimpleErrorCollection();
        values.put(SEARCHER_CONFIG.getMinField(), "999");
        values.put(SEARCHER_CONFIG.getMaxField(), "1");
        transformer.validateParams(null, null, values, i18n, errors);
        assert1FieldError(errors, SEARCHER_CONFIG.getMinField(), "navigator.filter.workratio.limits.error{[]}");
    }

    @Test
    public void testPopulateFromSearchRequestNoWhereClause() throws Exception
    {
        final FieldValuesHolder values = new FieldValuesHolderImpl();
        final WorkRatioSearchInputTransformer transformer = fixture();

        transformer.populateFromQuery(null, values, new QueryImpl(), searchContext);
        assertThat(values.entrySet(), noEntries());
    }

    @Test
    public void testPopulateFromSearchRequestHelperReturnsNull() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");

        final FieldValuesHolder values = new FieldValuesHolderImpl();
        Query query = mock(Query.class);
        when(query.getWhereClause()).thenReturn(theWhereClause);

        final WorkRatioSearcherInputHelper helper = mock(WorkRatioSearcherInputHelper.class);
        when(helper.convertClause(theWhereClause, ANONYMOUS)).thenReturn(null);

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        transformer.populateFromQuery(null, values, query, searchContext);
        assertThat(values.entrySet(), noEntries());
    }

    @Test
    public void testPopulateFromSearchRequestHappyPath() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");

        FieldValuesHolder values = new FieldValuesHolderImpl();
        Query query = mock(Query.class);
        when(query.getWhereClause()).thenReturn(theWhereClause);

        final Map<String, String> result = ImmutableMap.of("field", "value");
        final WorkRatioSearcherInputHelper helper = mock(WorkRatioSearcherInputHelper.class);
        when(helper.convertClause(theWhereClause, ANONYMOUS)).thenReturn(result);

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        transformer.populateFromQuery(null, values, query, searchContext);

        assertFalse(values.isEmpty());
        assertEquals("value", values.get("field"));
    }

    @Test
    public void testFitsNoWhereClause() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = fixture();
        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
    }

    @Test
    public void testFitsHelperReturnsNull() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");

        Query query = mock(Query.class);
        when(query.getWhereClause()).thenReturn(theWhereClause);

        final WorkRatioSearcherInputHelper helper = mock(WorkRatioSearcherInputHelper.class);
        when(helper.convertClause(theWhereClause, ANONYMOUS)).thenReturn(null);

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        assertFalse(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
    }

    @Test
    public void testFitsHappyPath() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");

        Query query = mock(Query.class);
        when(query.getWhereClause()).thenReturn(theWhereClause);

        final Map<String, String> result = MapBuilder.<String, String>newBuilder().add("field", "value").toMap();
        final WorkRatioSearcherInputHelper helper = mock(WorkRatioSearcherInputHelper.class);
        when(helper.convertClause(theWhereClause, ANONYMOUS)).thenReturn(result);

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        assertTrue(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
    }

    @Test
    public void testSearchClauseEmptyValues() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();

        final WorkRatioSearchInputTransformer transformer = fixture();
        assertNull(transformer.getSearchClause(null, values));
    }

    @Test
    public void testSearchClauseOnlyMinimum() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        values.put(SEARCHER_CONFIG.getMinField(), "50");

        final WorkRatioSearchInputTransformer transformer = fixture();
        final TerminalClause clause = (TerminalClause) transformer.getSearchClause(null, values);
        assertEquals(CONSTANTS.getJqlClauseNames().getPrimaryName(), clause.getName());
        assertEquals(Operator.GREATER_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand actual = (SingleValueOperand) clause.getOperand();
        assertEquals("50", actual.getStringValue());
    }

    @Test
    public void testSearchClauseOnlyMaximum() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        values.put(SEARCHER_CONFIG.getMaxField(), "50");

        final WorkRatioSearchInputTransformer transformer = fixture();
        final TerminalClause clause = (TerminalClause) transformer.getSearchClause(null, values);
        assertEquals(CONSTANTS.getJqlClauseNames().getPrimaryName(), clause.getName());
        assertEquals(Operator.LESS_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand actual = (SingleValueOperand) clause.getOperand();
        assertEquals("50", actual.getStringValue());
    }

    @Test
    public void testSearchClauseBothMinAndMax() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        values.put(SEARCHER_CONFIG.getMinField(), "50");
        values.put(SEARCHER_CONFIG.getMaxField(), "50");

        final WorkRatioSearchInputTransformer transformer = fixture();
        final AndClause clause = (AndClause) transformer.getSearchClause(null, values);

        final TerminalClause maxClause = new TerminalClauseImpl(CONSTANTS.getJqlClauseNames().getPrimaryName(), Operator.LESS_THAN_EQUALS, "50");
        final TerminalClause minClause = new TerminalClauseImpl(CONSTANTS.getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, "50");
        assertThat(clause.getClauses(), Matchers.<Clause>containsInAnyOrder(maxClause, minClause));
    }

    private WorkRatioSearchInputTransformer fixture()
    {
        return new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver);
    }

    private static Matcher<Collection<Map.Entry<String,Object>>> noEntries()
    {
        return Matchers.empty();
    }
}
