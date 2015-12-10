package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestMultiSelectCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private CustomField customField;
    private JqlOperandResolver jqlOperandResolver;

    private final String url = "cf_100";
    private final ClauseNames clauseNames = new ClauseNames("cf[100]");
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private SearchContext searchContext;
    private QueryContextConverter queryContextConverter;
    private User theUser = null;
    private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setDefaultReturnValue(url);

        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        searchContext = mockController.getMock(SearchContext.class);
        queryContextConverter = mockController.getMock(QueryContextConverter.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testGetSearchClauseNoValues() throws Exception
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseEmptyParams() throws Exception
    {
        final CustomFieldParamsImpl params = new CustomFieldParamsImpl();
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(url, params).toMap());
        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseParamsHasOnlyInvalidValues() throws Exception
    {
        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder("").asCollection());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(url, params).toMap());
        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseParamsHappyPath() throws Exception
    {
        EasyMock.expect(customField.getUntranslatedName()).andReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, clauseNames.getPrimaryName(), "ABC")).andReturn(clauseNames.getPrimaryName());
        EasyMock.expect(jqlSelectOptionsUtil.getOptionById(1000L)).andReturn(MockOption._getMockParentOption());
        EasyMock.expect(jqlSelectOptionsUtil.getOptionById(1001L)).andReturn(MockOption._getMockParent2Option());

        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder("-1", "", "1000", "1001").asCollection());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(url, params).toMap());
        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        final Clause result = transformer.getSearchClause(null, holder);
        final TerminalClause expectedResult = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand("cars", "2"));
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoWhereClause() throws Exception
    {
        Query query = new QueryImpl();

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }    

    @Test
    public void testGetParamsFromSearchRequestNoValues() throws Exception
    {
        Query query = new QueryImpl(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);
        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestEmptyLiteral() throws Exception
    {
        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IS, EmptyOperand.EMPTY));

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper)
        {
        };
                
        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestUnsupportedOperators() throws Exception
    {
        Query query1 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IS_NOT, "x"));
        Query query2 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.NOT_EQUALS, "x"));
        Query query3 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, "x"));
        Query query4 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.NOT_IN, new MultiValueOperand("x", "y")));

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);

        assertNull(transformer.getParamsFromSearchRequest(null, query1, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query2, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query3, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query4, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestHappyPath() throws Exception
    {
        final QueryLiteral literal1 = createLiteral("value1");
        final QueryLiteral literal2 = createLiteral("value2");
        final Option option1 = new MockOption(null, null, null, "Value 1", null, 10L);
        final Option option2 = new MockOption(null, null, null, "Value 2", null, 20L);

        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand(literal1, literal2)));

        jqlSelectOptionsUtil.getOptions(customField, literal1, false);
        mockController.setReturnValue(Collections.singletonList(option1));

        jqlSelectOptionsUtil.getOptions(customField, literal2, false);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option1, option2).asList());

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder("20", "10").asList());
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNullLiterals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("value1");
        final TerminalClauseImpl clause = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, operand);
        Query query = new QueryImpl(clause);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(null);

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper)
        {
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoOptions() throws Exception
    {
        final Option option1 = new MockOption(null, null, null, "Value 1", null, 10L);

        final QueryLiteral literal1 = createLiteral("value1");
        final QueryLiteral literal2 = createLiteral("value2");

        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand(literal1, literal2)));

        jqlSelectOptionsUtil.getOptions(customField, literal1, false);
        mockController.setReturnValue(Collections.singletonList(option1));

        jqlSelectOptionsUtil.getOptions(customField, literal2, false);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        final MultiSelectCustomFieldSearchInputTransformer transformer = new MultiSelectCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper)
        {
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

}
