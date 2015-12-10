package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
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
public class TestMultiGroupCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private CustomField customField;
    private JqlOperandResolver jqlOperandResolver;

    private final String url = "cf_100";
    private final ClauseNames clauseNames = new ClauseNames("cf[100]");
    private GroupConverter groupConverter;
    private SearchContext searchContext;
    private User theUser = null;
    private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setDefaultReturnValue(url);

        groupConverter = mockController.createNiceMock(GroupConverter.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        searchContext = mockController.getMock(SearchContext.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testGetSearchClauseNoValues() throws Exception
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);
        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseEmptyParams() throws Exception
    {
        final CustomFieldParamsImpl params = new CustomFieldParamsImpl();
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(url, params).toMap());
        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);
        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseParamsHasOnlyInvalidValues() throws Exception
    {
        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder("").asCollection());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(url, params).toMap());
        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);
        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseParamsHappyPath() throws Exception
    {
        EasyMock.expect(customField.getUntranslatedName()).andReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, clauseNames.getPrimaryName(), "ABC")).andReturn(clauseNames.getPrimaryName());

        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder("-1", "", "nick", "ross").asCollection());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(url, params).toMap());
        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);
        final Clause result = transformer.getSearchClause(null, holder);
        final TerminalClause expectedResult = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand("nick", "ross"));
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoWhereClause() throws Exception
    {
        Query query = new QueryImpl();

        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);
        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoContext() throws Exception
    {
        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.EQUALS, "blah"));
        EasyMock.expect(groupConverter.getGroup("blah")).andReturn(createGroup("blah"));


        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);
        final CustomFieldParams paramsFromSearchRequest = transformer.getParamsFromSearchRequest(null, query, searchContext);
        assertEquals(1, paramsFromSearchRequest.getAllValues().size());
        assertEquals(CollectionBuilder.list("blah"), paramsFromSearchRequest.getAllValues());
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestUnsupportedOperators() throws Exception
    {
        Query query0 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IS, "x"));
        Query query1 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IS_NOT, "x"));
        Query query2 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.NOT_EQUALS, "x"));
        Query query3 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, "x"));
        Query query4 = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.NOT_IN, new MultiValueOperand("x", "y")));

        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);

        assertNull(transformer.getParamsFromSearchRequest(null, query0, searchContext));
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

        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand(literal1, literal2)));

        EasyMock.expect(groupConverter.getGroup("value1")).andReturn(createGroup("value1"));
        EasyMock.expect(groupConverter.getGroup("value2")).andReturn(createGroup("value2"));

        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder("value1", "value2").asList());
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
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoGroups() throws Exception
    {
        final QueryLiteral literal1 = createLiteral("value1");
        final QueryLiteral literal2 = createLiteral("value2");

        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand(literal1, literal2)));
        final QueryContext context = mockController.getMock(QueryContext.class);

        EasyMock.expect(groupConverter.getGroup("value1")).andReturn(null);
        EasyMock.expect(groupConverter.getGroup("value2")).andReturn(null);

        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestErrorGroups() throws Exception
    {
        final QueryLiteral literal1 = createLiteral("value1");
        final QueryLiteral literal2 = createLiteral("value2");

        Query query = new QueryImpl(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IN, new MultiValueOperand(literal1, literal2)));
        final QueryContext context = mockController.getMock(QueryContext.class);

        EasyMock.expect(groupConverter.getGroup("value1")).andThrow(new FieldValidationException("bad"));
        EasyMock.expect(groupConverter.getGroup("value2")).andThrow(new FieldValidationException("bad"));

        mockController.replay();
        final MultiGroupCustomFieldSearchInputTransformer transformer = new MultiGroupCustomFieldSearchInputTransformer(url, clauseNames, customField, jqlOperandResolver, customFieldInputHelper, groupConverter);

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    private Group createGroup(final String groupName)
    {
        return new Group()
        {
            @Override
            public String getName()
            {
                return groupName;
            }

            @Override
            public boolean equals(Object o)
            {
                if (o != null && o instanceof Group)
                {
                    return groupName.equals(((Group) o).getName());
                }

                return false;
            }

            @Override
            public int hashCode()
            {
                return groupName.hashCode();
            }

            @Override
            public int compareTo(Group o)
            {
                if (o == null)
                {
                    return 1;
                }
                return groupName.compareTo(o.getName());
            }
        };
    }

}
