package com.atlassian.jira.issue.search.searchers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestDefaultIndexedInputHelper extends MockControllerTestCase
{
    private User theUser = null;
    private SearchRequest searchRequest;

    @Before
    public void setUp() throws Exception
    {
        searchRequest = mockController.getMock(SearchRequest.class);
    }

    @Test
    public void testGetClauseForNavigatorValuesEmptySet() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertNull(helper.getClauseForNavigatorValues(fieldName, Collections.<String>emptySet()));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesOneValueNotFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id = "45";
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, 45L);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, Collections.singleton(id)));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesTwoValuesNotFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id1 = "45";
        final String id2 = "888";
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, new MultiValueOperand(45L, 888L));
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id1);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id2);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, CollectionBuilder.newBuilder(id1, id2).asListOrderedSet()));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesTwoValuesOneNotNumber() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id1 = "45";
        final String id2 = "notanumber";
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, new MultiValueOperand(new SingleValueOperand(45L), new SingleValueOperand("notanumber")));
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id1);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id2);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, CollectionBuilder.newBuilder(id1, id2).asListOrderedSet()));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesOneValueIsListFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id = "-3";
        final Operand flagOperand = new MultiValueOperand(45L);
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, flagOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id);
        mockController.setReturnValue(flagOperand);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, Collections.singleton(id)));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesOneValueIsFlagNotList() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id = "-1";
        final Operand flagOperand = EmptyOperand.EMPTY;
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, flagOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id);
        mockController.setReturnValue(flagOperand);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, Collections.singleton(id)));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesTwoValuesOneIsListFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id1 = "-3";
        final String id2 = "888";
        final Operand flagOperand = new MultiValueOperand(45L);
        final Operand singleOperand = new SingleValueOperand(888L);
        final Operand expectedMultiOperand = new MultiValueOperand(CollectionBuilder.newBuilder(flagOperand, singleOperand).asList());
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, expectedMultiOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id1);
        mockController.setReturnValue(flagOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id2);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, CollectionBuilder.newBuilder(id1, id2).asListOrderedSet()));
        mockController.verify();
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsNoWhereClause() throws Exception
    {
        DefaultIndexedInputHelper helper = mockController.instantiate(DefaultIndexedInputHelper.class);
        final Set<String> strings = helper.getAllNavigatorValuesForMatchingClauses(theUser, new ClauseNames("balrg"), new QueryImpl());
        assertEquals(0, strings.size());
    }

    @Test
    public void testGetAllIndexValuesAsStringsNoWhereClause() throws Exception
    {
        DefaultIndexedInputHelper helper = mockController.instantiate(DefaultIndexedInputHelper.class);
        final Set<String> strings = helper.getAllIndexValuesForMatchingClauses(theUser, new ClauseNames("balrg"), new QueryImpl());
        assertEquals(0, strings.size());
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsFunctionalOperandHasFlag() throws Exception
    {
        final String fieldName = "testfield";
        final FunctionOperand opFunc = new FunctionOperand("func");

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunc);
        mockController.setReturnValue(Collections.singleton("-2"));
        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("func", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, opFunc);

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry);
        assertNavigatorValidForSingleClause(helper, terminalClause, "-2");
        mockController.verify();
    }

    @Test
    public void testNavigatorValuesWithMultiValueOperandsContainsFlaggedFuncsAndOperands() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("ImAFlag");
        final FunctionOperand opFunction = new FunctionOperand("myFunction");
        final MultiValueOperand opMulti = new MultiValueOperand(CollectionBuilder.newBuilder(opAnything, opFunction).asList());

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opMulti);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(Collections.singleton("-1"));
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunction);
        mockController.setReturnValue(Collections.singleton("-2"));

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        jqlOperandSupport.addHandler("myFunction", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);

        final QueryImpl searchQuery = new QueryImpl(new OrClause(new TerminalClauseImpl(fieldName, Operator.IN, opMulti)));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry);

        assertNavigatorValidForSearchRequest(fieldName, helper, "-1", "-2");
    }

    private void assertNavigatorValidForSingleClause(final DefaultIndexedInputHelper<?> helper, final TerminalClause clause, final String... contains)
    {
        final Set<String> strings = helper.getAllNavigatorValues(theUser, clause.getName(), clause.getOperand(), clause);
        final List<String> expected = Arrays.asList(contains);
        assertEquals(expected.size(), strings.size());
        assertTrue(expected.containsAll(strings));
    }

    private void assertNavigatorValidForSearchRequest(final String fieldName, final DefaultIndexedInputHelper<?> helper, final String... contains)
    {
        final Set<String> strings = helper.getAllNavigatorValuesForMatchingClauses(theUser, new ClauseNames(fieldName), searchRequest.getQuery());

        final List<String> expected = Arrays.asList(contains);
        assertEquals(expected.size(), strings.size());
        assertTrue(expected.containsAll(strings));

    }

    private List<QueryLiteral> createQueryLiterals(String... value)
    {
        List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        for (String s : value)
        {
            literals.add(createLiteral(s));
        }
        return literals;
    }

    private List<QueryLiteral> createQueryLiterals(Long... value)
    {
        List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        for (Long s : value)
        {
            literals.add(createLiteral(s));
        }
        return literals;
    }
}
