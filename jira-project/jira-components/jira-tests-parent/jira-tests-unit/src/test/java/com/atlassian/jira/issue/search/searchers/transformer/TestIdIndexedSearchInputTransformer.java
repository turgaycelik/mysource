package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestIdIndexedSearchInputTransformer extends MockControllerTestCase
{
    private static final String FIELD_NAME = "field";

    private JiraAuthenticationContext authenticationContext = null;
    private User theUser = null;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        authenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        authenticationContext.getLoggedInUser();
        mockController.setDefaultReturnValue(theUser);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @After
    public void tearDown() throws Exception
    {
        authenticationContext = null;
    }

    @Test
    public void testPopulateFromParamsOneId() throws Exception
    {
        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testId"), mockIndexInfoResolver, mockJqlOperandResolver, mockFieldFlagOperandRegistry);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "val1", "val2" };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build("testId", values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(Arrays.asList(values), valuesHolder.get("testId"));

        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequest() throws Exception
    {
        final QueryImpl query = new QueryImpl();

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexedInputHelper indexedInputHelper = mockController.getMock(IndexedInputHelper.class);
        indexedInputHelper.getAllNavigatorValuesForMatchingClauses(null, new ClauseNames("testId"), query);
        mockController.setReturnValue(Collections.singleton("stuff"));

        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new IdIndexedSearchInputTransformer(new ClauseNames("testId"), mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry)
        {
            @Override
            IndexedInputHelper createIndexedInputHelper()
            {
                return indexedInputHelper;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, query, searchContext);

        @SuppressWarnings ({ "unchecked" }) final Collection<String> values = (Collection<String>) valuesHolder.get("testId");
        assertNotNull(values);
        assertTrue(values.contains("stuff"));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseSingleValueStringOneId() throws Exception
    {
        Map values = EasyMap.build("testId", EasyList.build("123"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testId", "123");
        mockController.setReturnValue(null);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testId"), mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);
        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testId", Operator.EQUALS, 123L);

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseSingleValueLongOneId() throws Exception
    {
        Map values = EasyMap.build("testId", EasyList.build(123L));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testId"), mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);

        try
        {
            transformer.getSearchClause(null, valuesHolder);
            fail("This can not handle long inputs.");
        }
        catch (IllegalArgumentException e)
        {
            // expeceted
        }

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseMultiValueStringOneId() throws Exception
    {
        Map values = EasyMap.build("testId", EasyList.build("123", "456"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testId", "123");
        mockController.setReturnValue(null);
        mockFieldFlagOperandRegistry.getOperandForFlag("testId", "456");
        mockController.setReturnValue(null);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testId"), mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);
        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testId", Operator.IN, new MultiValueOperand(123L, 456L));

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseNoValuesOneId() throws Exception
    {
        Map values = EasyMap.build("testId", Collections.emptyList());
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testId"), mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);

        assertNull(clause);

        mockController.verify();
    }

    @Test
    public void testPopulateFromParamsTwoIds() throws Exception
    {
        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testFieldName"), "testSearcherId", mockIndexInfoResolver, mockJqlOperandResolver, mockFieldFlagOperandRegistry);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "val1", "val2" };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build("testSearcherId", values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(Arrays.asList(values), valuesHolder.get("testSearcherId"));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseSingleValueStringTwoIds() throws Exception
    {
        Map values = EasyMap.build("testSearcherId", EasyList.build("123"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testFieldName", "123");
        mockController.setReturnValue(null);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testFieldName"), "testSearcherId", mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);
        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testFieldName", Operator.EQUALS, 123L);

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseSingleValueLongTwoIds() throws Exception
    {
        Map values = EasyMap.build("testSearcherId", EasyList.build(123L));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testFieldName"), "testSearcherId", mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);

        try
        {
            transformer.getSearchClause(null, valuesHolder);
            fail("This can not handle long inputs.");
        }
        catch (IllegalArgumentException e)
        {
            // expeceted
        }

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseMultiValueStringTwoIds() throws Exception
    {
        Map values = EasyMap.build("testSearcherId", EasyList.build("123", "456"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testFieldName", "123");
        mockController.setReturnValue(null);
        mockFieldFlagOperandRegistry.getOperandForFlag("testFieldName", "456");
        mockController.setReturnValue(null);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testFieldName"), "testSearcherId", mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);
        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testFieldName", Operator.IN, new MultiValueOperand(123L, 456L));

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseNoValuesTwoIds() throws Exception
    {
        Map values = EasyMap.build("testSearcherId", Collections.emptyList());
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testFieldName"), "testSearcherId", mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);

        assertNull(clause);

        mockController.verify();
    }

    @Test
    public void testValidateForNavigatorHappyPath() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                                            new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        final IndexInfoResolver<Version> vIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry oFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        IdIndexedSearchInputTransformer<Version> transformer = new MockIdIndexedSearchInputTransformer<Version>(new ClauseNames(FIELD_NAME), vIndexInfoResolver, mockJqlOperandResolver, oFieldFlagOperandRegistry)
        {
            @Override
            NavigatorStructureChecker<Version> createNavigatorStructureChecker()
            {
                return new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, operandResolver)
                {
                    @Override
                    public boolean checkSearchRequest(final Query query)
                    {
                        return true;
                    }
                };
            }
        };

        assertValidate(andClause, transformer, true);

        mockController.verify();
    }

    @Test
    public void testValidateForNavigatorSadPath() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                                            new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        final IndexInfoResolver<Version> indexInfoResolver = mockController.getMock(IndexInfoResolver.class);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer<Version>(new ClauseNames(FIELD_NAME), indexInfoResolver, mockJqlOperandResolver, fieldFlagOperandRegistry)
        {
            @Override
            NavigatorStructureChecker<Version> createNavigatorStructureChecker()
            {
                return new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME),  true, fieldFlagOperandRegistry, operandResolver)
                {
                    @Override
                    public boolean checkSearchRequest(final Query query)
                    {
                        return false;
                    }
                };
            }
        };

        assertValidate(andClause, transformer, false);
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseFlagMultiValues() throws Exception
    {
        Map values = EasyMap.build("testdid", EasyList.build("123", "456"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        FunctionOperand functionOperand = new FunctionOperand("function");

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testdid", "123");
        mockController.setReturnValue(null);
        mockFieldFlagOperandRegistry.getOperandForFlag("testdid", "456");
        mockController.setReturnValue(functionOperand);

        final OperandHandler handler = mockController.getMock(OperandHandler.class);
        handler.isList();
        mockController.setReturnValue(true);

        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport().addHandler("function", handler);

        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testdid"), mockIndexInfoResolver, jqlOperandResolver, mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);

        final List<Operand> operands = new ArrayList<Operand>();
        operands.add(new SingleValueOperand(123L));
        operands.add(new FunctionOperand("function"));
        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testdid", Operator.IN, new MultiValueOperand(operands));

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseFlagOneOnlyWithList() throws Exception
    {
        Map values = EasyMap.build("testdid", EasyList.build("456"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        FunctionOperand functionOperand = new FunctionOperand("function");

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testdid", "456");
        mockController.setReturnValue(functionOperand);

        final OperandHandler handler = mockController.getMock(OperandHandler.class);
        handler.isList();
        mockController.setReturnValue(true);

        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport().addHandler("function", handler);

        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testdid"), mockIndexInfoResolver, jqlOperandResolver, mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);

        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testdid", Operator.IN, functionOperand);

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseFlagOneOnlyWithNotList() throws Exception
    {
        Map values = EasyMap.build("testdid", EasyList.build("456"));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        FunctionOperand functionOperand = new FunctionOperand("function");

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockFieldFlagOperandRegistry.getOperandForFlag("testdid", "456");
        mockController.setReturnValue(functionOperand);

        final OperandHandler handler = mockController.getMock(OperandHandler.class);
        handler.isList();
        mockController.setReturnValue(false);

        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport().addHandler("function", handler);

        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testdid"), mockIndexInfoResolver, jqlOperandResolver, mockFieldFlagOperandRegistry);
        final Clause clause = transformer.getSearchClause(null, valuesHolder);

        final TerminalClauseImpl expectedClause = new TerminalClauseImpl("testdid", Operator.EQUALS, functionOperand);

        assertEquals(expectedClause, clause);

        mockController.verify();
    }

    @Test
    public void testGetValuesFromHolderThrowsException() throws Exception
    {
        Map values = EasyMap.build("testdid", EasyList.build(1L));
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl(values);

        final IndexInfoResolver mockIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final IdIndexedSearchInputTransformer transformer = new MockIdIndexedSearchInputTransformer(new ClauseNames("testdid"), mockIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry);

        try
        {
            transformer.getValuesFromHolder(valuesHolder);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e) {}

        mockController.verify();
    }

    private void assertValidate(final AndClause andClause, final IdIndexedSearchInputTransformer transformer, final boolean isClauseValid)
    {
        final QueryImpl query = new QueryImpl(andClause);
        assertEquals(isClauseValid, transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
    }

    private static class MockIdIndexedSearchInputTransformer<T> extends IdIndexedSearchInputTransformer<T>
    {
        private MockIdIndexedSearchInputTransformer(ClauseNames id, IndexInfoResolver<T> indexInfoResolver, JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry)
        {
            super(id, indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        }

        private MockIdIndexedSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, IndexInfoResolver<T> indexInfoResolver, JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry)
        {
            super(clauseNames, urlParameterName, indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        }

        IndexedInputHelper createIndexedInputHelper()
        {
            return getDefaultIndexedInputHelper();
        }
    }
}
