package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.plugin.jql.function.CascadeOptionFunction;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCascadingSelectCustomFieldSearchInputTransformer
{
    private static final String URL = "cf_100";
    private static final ClauseNames NAMES = new ClauseNames("cf[100]");

    @Mock private CustomField customField;
    @Mock private SelectConverter selectConverter;
    @Mock private JqlOperandResolver jqlOperandResolver;
    @Mock private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    @Mock private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    @Mock private SearchContext searchContext;
    @Mock private QueryContextConverter queryContextConverter;
    @Mock private CustomFieldInputHelper customFieldInputHelper;

    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        when(customField.getName()).thenReturn("ABC");
        when(customField.getUntranslatedName()).thenReturn("ABC");
        when(customFieldInputHelper.getUniqueClauseName(theUser, NAMES.getPrimaryName(), "ABC")).thenReturn(NAMES.getPrimaryName());
    }

    @After
    public void tearDown()
    {
        customField = null;
        selectConverter = null;
        jqlOperandResolver = null;
        jqlSelectOptionsUtil = null;
        jqlCascadingSelectLiteralUtil = null;
        searchContext = null;
        queryContextConverter = null;
        customFieldInputHelper = null;
    }

    @Test
    public void testGetParamsFromSearchRequestNotValid() throws Exception
    {
        final TerminalClauseImpl whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(false, null);
        
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }
        };
        
        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleClauses() throws Exception
    {
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause, whereClause).asList());

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }
        };

        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestUnsupportedOperators() throws Exception
    {
        final String name = "cf[100]";
        Query query1 = new QueryImpl(new TerminalClauseImpl(name, Operator.NOT_EQUALS, "blah"));
        Query query3 = new QueryImpl(new TerminalClauseImpl(name, Operator.NOT_IN, "blah"));
        Query query4 = new QueryImpl(new TerminalClauseImpl(name, Operator.IS_NOT, "blah"));
        Query query5 = new QueryImpl(new TerminalClauseImpl(name, Operator.LESS_THAN_EQUALS, "blah"));
        Query query6 = new QueryImpl(new TerminalClauseImpl(name, Operator.GREATER_THAN_EQUALS, "blah"));
        Query query7 = new QueryImpl(new TerminalClauseImpl(name, Operator.LIKE, "blah"));
        Query query8 = new QueryImpl(new TerminalClauseImpl(name, Operator.NOT_LIKE, "blah"));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertNull(transformer.getParamsFromSearchRequest(null, query1, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query3, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query4, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query5, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query6, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query7, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query8, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleLiterals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.of(createLiteral("10"), createLiteral("10")));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestNullLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query searchQuery = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, searchQuery, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestEmptyLiteral() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        final QueryLiteral literal = new QueryLiteral();
        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.of(literal));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleOptions() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());
        final Option option = new MockOption(null, null, null, null, null, null);

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.of(literal));
        when(queryContextConverter.getQueryContext(searchContext)).thenReturn(queryContext);
        when(jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true)).thenReturn(ImmutableList.of(option, option));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestNoOption() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());
        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.of(literal));
        when(queryContextConverter.getQueryContext(searchContext)).thenReturn(queryContext);
        when(jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true)).thenReturn(ImmutableList.<Option>of());

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("10"));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetParamsFromSearchRequestParentOption() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.of(literal));
        when(queryContextConverter.getQueryContext(searchContext)).thenReturn(queryContext);
        final Option option = new MockOption(null, null, null, null, null, 20L);
        when(jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true)).thenReturn(ImmutableList.of(option));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(option.getOptionId().toString()));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetParamsFromSearchRequestChildOption() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());
        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.of(literal));
        when(queryContextConverter.getQueryContext(searchContext)).thenReturn(queryContext);

        final Option parentOption = new MockOption(null, null, null, null, null, 10L);
        final Option option = new MockOption(parentOption, null, null, null, null, 20L);
        when(jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true)).thenReturn(ImmutableList.of(option));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(parentOption.getOptionId().toString()));
        expectedResult.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton(option.getOptionId().toString()));

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetParamsFromSearchRequestFunctionNoValues() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.<QueryLiteral>of());

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestCascadeFunctionOneArg() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "parent");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.<QueryLiteral>of());
        when(jqlSelectOptionsUtil.getOptions(customField, new QueryLiteral(whereClause.getOperand(), "parent"), true))
                .thenReturn(ImmutableList.<Option>of(new MockOption(null, Collections.emptyList(), 0L, "parent", null, 123L)));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("parent"));

        assertEquals(expectedResult, transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestCascadeFunctionTwoArgs() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "123", "456");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.<QueryLiteral>of());
        when(jqlSelectOptionsUtil.getOptions(customField, new QueryLiteral(whereClause.getOperand(), "123"), true))
                .thenReturn(ImmutableList.<Option>of(new MockOption(null, Collections.emptyList(), 0L, "123", null, 123L)));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("123"));
        expectedResult.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton("456"));

        assertEquals(expectedResult, transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }
    
    @Test
    public void testGetParamsFromSearchRequestCascadeFunctionNoArgs() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION);
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        when(jqlOperandResolver.getValues(theUser, operand, whereClause)).thenReturn(ImmutableList.<QueryLiteral>of());

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
    }

    @Test
    public void testGetClauseFromParamsNoValues() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertNull(transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testGetClauseFromParamsParentSpecified() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("20"));
        final MockOption option = new MockOption(null, null, null, "parent", null, 20L);

        final TerminalClause expectedClause = new TerminalClauseImpl(NAMES.getPrimaryName(), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "20"));

        when(jqlSelectOptionsUtil.getOptionById(20L)).thenReturn(option);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testGetClauseFromParamsParentSpecifiedButDoesntExist() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("20"));

        final TerminalClause expectedClause = new TerminalClauseImpl(NAMES.getPrimaryName(), Operator.EQUALS, 20);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testGetClauseFromParamsChildSpecified() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

        final MockOption parentOption = new MockOption(null, null, null, "parent", null, 20L);
        final MockOption childOption = new MockOption(parentOption, null, null, "child", null, 40L);
        parentOption.setChildOptions(Collections.singletonList(childOption));

        customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("20"));
        customFieldParams.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton("40"));

        final TerminalClause expectedClause = new TerminalClauseImpl(NAMES.getPrimaryName(), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "20", "40"));

        when(jqlSelectOptionsUtil.getOptionById(40L)).thenReturn(childOption);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testGetClauseFromParamsChildSpecifiedNoParent() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

        final MockOption parentOption = new MockOption(null, null, null, "parent", null, 20L);
        final MockOption childOption = new MockOption(null, null, null, "child", null, 40L);
        parentOption.setChildOptions(Collections.singletonList(childOption));

        customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("20"));
        customFieldParams.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton("40"));

        when(jqlSelectOptionsUtil.getOptionById(40L)).thenReturn(childOption);

        final TerminalClause expectedClause = new TerminalClauseImpl(NAMES.getPrimaryName(), Operator.EQUALS, 40);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testGetClauseFromParamsParentInvalid() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("INVALID"));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        final TerminalClause expectedResult = new TerminalClauseImpl(NAMES.getPrimaryName(), Operator.EQUALS, "INVALID");
        assertEquals(expectedResult, transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testGetClauseFromParamsChildInvalid() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("INVALID"));
        customFieldParams.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton("INVALID CHILD"));

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        final TerminalClause expectedResult = new TerminalClauseImpl(NAMES.getPrimaryName(), Operator.EQUALS, "INVALID CHILD");
        assertEquals(expectedResult, transformer.getClauseFromParams(theUser, customFieldParams));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormTheyDo() throws Exception
    {
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
            {
                return new CustomFieldParamsImpl();
            }
        };

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormTheyDont() throws Exception
    {
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(NAMES, customField, URL, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
    }

    static class MySimpleNavigatorCollectingVistor extends SimpleNavigatorCollectorVisitor
    {
        private final boolean valid;
        private final List<TerminalClause> clauses;

        public MySimpleNavigatorCollectingVistor(boolean isValid, List<TerminalClause> clauses)
        {
            super("blah");
            valid = isValid;
            this.clauses = clauses;
        }

        @Override
        public List<TerminalClause> getClauses()
        {
            return clauses;
        }

        @Override
        public boolean isValid()
        {
            return valid;
        }

        @Override
        public Void visit(final AndClause andClause)
        {
            return super.visit(andClause);
        }

        @Override
        public Void visit(final NotClause notClause)
        {
            return super.visit(notClause);
        }

        @Override
        public Void visit(final OrClause orClause)
        {
            return super.visit(orClause);
        }

        @Override
        public Void visit(final TerminalClause terminalClause)
        {
            return super.visit(terminalClause);
        }
    }
}
