package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSelectCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private CustomField customField;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private ClauseNames clauseNames = new ClauseNames("cf[100]");
    private SearchContext searchContext;
    private QueryContextConverter queryContextConverter;
    private User searcher = null;
    private CustomFieldInputHelper customFieldInputHelper;
    private JqlOperandResolver jqlOperandResolver;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        searchContext = mockController.getMock(SearchContext.class);
        queryContextConverter = mockController.getMock(QueryContextConverter.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
    }

    @Test
    public void testCreateClauseIsAll() throws Exception
    {
        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper);
        assertNull(transformer.createSearchClause(searcher, SelectConverter.ALL_STRING));
        mockController.verify();
    }

    @Test
    public void testCreateClause() throws Exception
    {
        final Option option1 = new MockOption(null, null, null, "option", null, 12L);

        clauseNames = new ClauseNames("blah");
        EasyMock.expect(customField.getUntranslatedName()).andStubReturn("ABC");
        EasyMock.expect(jqlSelectOptionsUtil.getOptionById(Long.valueOf(12))).andStubReturn(option1);
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(searcher, clauseNames.getPrimaryName(), "ABC")).andStubReturn("ABC");

        replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper);
        final Clause result = transformer.createSearchClause(searcher, "12");
        TerminalClause expectedResult = new TerminalClauseImpl("ABC", Operator.EQUALS, "option");
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoRelevantClauseFitNavigatorMoreThanOneOption() throws Exception
    {
        Query query = new QueryImpl();
        final Option option1 = new MockOption(null, null, null, null, null, 10L);
        final Option option2 = new MockOption(null, null, null, null, null, 20L);

        final QueryContext context = mockController.getMock(QueryContext.class);

        jqlSelectOptionsUtil.getOptions(customField, context, createLiteral("value"), true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option1, option2).asList());

        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(context);

        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand("value"));
            }

        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClauseFitNavigatorHappyPath() throws Exception
    {
        Query query = new QueryImpl();
        final Option option1 = new MockOption(null, null, null, null, null, 10L);

        final QueryContext context = mockController.getMock(QueryContext.class);

        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(context);

        jqlSelectOptionsUtil.getOptions(customField, context, createLiteral("value"), true);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option1).asList());

        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand("value"));
            }

        };

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClauseFitNavigatorDoesntFit() throws Exception
    {
        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, new SingleValueOperand("value"));
            }

        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClauseFitNavigatorDoesntFitNoValue() throws Exception
    {
        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, null);
            }

        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsHappyPath() throws Exception
    {
        TerminalClauseImpl whereClause = new TerminalClauseImpl("cf[100]", Operator.EQUALS, "12");
        Query query = new QueryImpl(whereClause);

        jqlOperandResolver.getValues((User) null, whereClause.getOperand(), whereClause);
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral(new SingleValueOperand("value"), Long.valueOf(12))));

        final QueryContext context = mockController.getMock(QueryContext.class);
        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(context);

        final Option option1 = new MockOption(null, null, null, "option1", null, 12L);
        jqlSelectOptionsUtil.getOptions(customField, context, createLiteral(12L), true);
        mockController.setReturnValue(Collections.singletonList(option1));


        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper);

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        final CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField, Collections.singleton("12"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetParamsDoesntFit() throws Exception
    {
        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, new SingleValueOperand("value"));
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsDoesntFitNoValue() throws Exception
    {
        mockController.replay();
        final SelectCustomFieldSearchInputTransformer transformer = new SelectCustomFieldSearchInputTransformer(customField, clauseNames, "blah", jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, null);
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));
        mockController.verify();
    }
}
