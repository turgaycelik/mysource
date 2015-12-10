package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.searchers.NumberRangeSearcher;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
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
public class TestNumberRangeCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private final ClauseNames clauseNames = new ClauseNames("cf[100]");
    private String url = "cf_100";
    private String id = "cf_100";
    private MockI18nHelper i18nHelper;
    private CustomField customField;
    private DoubleConverter doubleConverter;
    private JqlOperandResolver jqlOperandResolver;
    private NumberRangeCustomFieldInputHelper inputHelper;
    private SearchContext searchContext;
    private User theUser = null;
    private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        i18nHelper = new MockI18nHelper();
        doubleConverter = mockController.getMock(DoubleConverter.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setDefaultReturnValue(id);
        inputHelper = mockController.getMock(NumberRangeCustomFieldInputHelper.class);
        searchContext = mockController.getMock(SearchContext.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testValidateParamsNullParams() throws Exception
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        final SimpleErrorCollection errors = new SimpleErrorCollection();

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        transformer.validateParams(null, null, holder, i18nHelper, errors);

        assertFalse(errors.hasAnyErrors());
        mockController.verify();
    }

    @Test
    public void testValidateParamsNoParams() throws Exception
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());
        final SimpleErrorCollection errors = new SimpleErrorCollection();

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        transformer.validateParams(null, null, holder, i18nHelper, errors);

        assertFalse(errors.hasAnyErrors());
        mockController.verify();
    }

    @Test
    public void testValidateParamsInvalidNumber() throws Exception
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder().add(NumberRangeSearcher.GREATER_THAN_PARAM, Collections.singleton("blah")).toMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());
        final SimpleErrorCollection errors = new SimpleErrorCollection();

        doubleConverter.getDouble("blah");
        mockController.setThrowable(new FieldValidationException("blah"));

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        transformer.validateParams(null, null, holder, i18nHelper, errors);

        assertTrue(errors.hasAnyErrors());
        assertEquals("blah", errors.getErrors().get(id));
        mockController.verify();
    }

    @Test
    public void testValidateParamsValid() throws Exception
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder().add(NumberRangeSearcher.GREATER_THAN_PARAM, Collections.singleton("blah")).toMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());
        final SimpleErrorCollection errors = new SimpleErrorCollection();

        expect(doubleConverter.getDouble("blah")).andReturn(10D);

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        transformer.validateParams(null, null, holder, i18nHelper, errors);

        assertFalse(errors.hasAnyErrors());
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoWhereClause() throws Exception
    {
        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        assertNull(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));
    }

    @Test
    public void testGetParamsFromSearchRequestHelperReturnsNull() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah");
        final Query query = new QueryImpl(clause);
        inputHelper.getValuesFromQuery(query);
        mockController.setReturnValue(null);

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };                

        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestHelperReturnsNoClauses() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah");
        final Query query = new QueryImpl(clause);

        inputHelper.getValuesFromQuery(query);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleLiteralsResolved() throws Exception
    {
        final Query query = new QueryImpl( new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));

        inputHelper.getValuesFromQuery(query);
        final FunctionOperand operand = new FunctionOperand("blah");
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, operand);
        mockController.setReturnValue(CollectionBuilder.newBuilder(clause1).asList());

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause1);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("1"), createLiteral("2")).asList());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestEmptyLiteralResolved() throws Exception
    {
        final Query query = new QueryImpl( new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));

        inputHelper.getValuesFromQuery(query);
        final FunctionOperand operand = new FunctionOperand("blah");
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, operand);
        mockController.setReturnValue(CollectionBuilder.newBuilder(clause1).asList());

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause1);
        mockController.setReturnValue(CollectionBuilder.newBuilder(new QueryLiteral()).asList());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestHelperClauses() throws Exception
    {
        final Query query = new QueryImpl( new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));

        inputHelper.getValuesFromQuery(query);
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, "10");
        final TerminalClauseImpl clause2 = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, "15");
        mockController.setReturnValue(CollectionBuilder.newBuilder(clause1, clause2).asList());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, query, searchContext);
        final CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder()
                .add(NumberRangeSearcher.GREATER_THAN_PARAM, Collections.singleton("15"))
                .add(NumberRangeSearcher.LESS_THAN_PARAM, Collections.singleton("10"))
                .toMap());

        assertEquals(result, expectedResult);
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNoWhereClause() throws Exception
    {
        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormInputHelperReturnsNull() throws Exception
    {
        final Query query = new QueryImpl(new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));

        inputHelper.getValuesFromQuery(query);
        mockController.setReturnValue(null);

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormInputHelperReturnsNotNull() throws Exception
    {
        final Query query = new QueryImpl(new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));

        inputHelper.getValuesFromQuery(query);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper)
        {
            @Override
            NumberRangeCustomFieldInputHelper createInputHelper(final ClauseNames clauseNames, final JqlOperandResolver jqlOperandResolver)
            {
                return inputHelper;
            }
        };

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
        mockController.verify();
    }

     @Test
     public void testGetSearchClauseNullParams() throws Exception
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseNoParams() throws Exception
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseLessThanParam() throws Exception
    {
        EasyMock.expect(customField.getUntranslatedName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, clauseNames.getPrimaryName(), "ABC")).andStubReturn(clauseNames.getPrimaryName());

        CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder()
                .add(NumberRangeSearcher.LESS_THAN_PARAM, Collections.singleton("10"))
                .toMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        final Clause result = transformer.getSearchClause(null, holder);
        final TerminalClauseImpl expectedResult = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, "10");
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseGreaterThanParam() throws Exception
    {
        EasyMock.expect(customField.getUntranslatedName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, clauseNames.getPrimaryName(), "ABC")).andStubReturn(clauseNames.getPrimaryName());

        CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder()
                .add(NumberRangeSearcher.GREATER_THAN_PARAM, Collections.singleton("10"))
                .toMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        final Clause result = transformer.getSearchClause(null, holder);
        final TerminalClauseImpl expectedResult = new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, "10");
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseBothParams() throws Exception
    {
        EasyMock.expect(customField.getUntranslatedName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, clauseNames.getPrimaryName(), "ABC")).andStubReturn(clauseNames.getPrimaryName());

        CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder()
                .add(NumberRangeSearcher.GREATER_THAN_PARAM, Collections.singleton("10"))
                .add(NumberRangeSearcher.LESS_THAN_PARAM, Collections.singleton("15"))
                .toMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        final Clause result = transformer.getSearchClause(null, holder);
        final Clause expectedResult = new AndClause(
                new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.LESS_THAN_EQUALS, "15"),
                new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.GREATER_THAN_EQUALS, "10"));
        assertEquals(expectedResult, result);
        mockController.verify();
    }
    
    @Test
    public void testGetSearchClauseNoRelevantParams() throws Exception
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField, MapBuilder.newBuilder().toMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder(id, customFieldParams).toMap());

        mockController.replay();
        NumberRangeCustomFieldSearchInputTransformer transformer = new NumberRangeCustomFieldSearchInputTransformer(clauseNames, customField, url, doubleConverter, jqlOperandResolver, customFieldInputHelper);

        assertNull(transformer.getSearchClause(null, holder));
        mockController.verify();
    }
}
