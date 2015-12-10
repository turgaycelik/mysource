package com.atlassian.jira.jql.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.AlwaysValidOperatorUsageValidator;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestSelectCustomFieldClauseContextFactory extends MockControllerTestCase
{
    private CustomField customField;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private JqlOperandResolver jqlOperandResolver;
    private ContextSetUtil contextSetUtil;
    private User theUser = null;
    private OperatorUsageValidator operatorUsageValidator;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        fieldConfigSchemeClauseContextUtil = mockController.getMock(FieldConfigSchemeClauseContextUtil.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        contextSetUtil = mockController.getMock(ContextSetUtil.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        operatorUsageValidator = new AlwaysValidOperatorUsageValidator();
    }

    @After
    public void tearDown() throws Exception
    {

        customField = null;
        jqlSelectOptionsUtil = null;
        fieldConfigSchemeClauseContextUtil = null;
        jqlOperandResolver = null;
        contextSetUtil = null;
        jqlOperandResolver = null;
        operatorUsageValidator = null;
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final Operand operand = new SingleValueOperand("one");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, operand);

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(6);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(scheme.isGlobal()).andReturn(false);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext3);
        expect(scheme.isGlobal()).andReturn(false);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext3, clauseContext3).asSet())).andReturn(clauseContext3);

        mockController.replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);
        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertEquals(clauseContext3, result);

        mockController.verify();
    }

    @Test
    public void testGetInvalidUsage() throws Exception
    {
        final Operand operand = new SingleValueOperand("one");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(6);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        operatorUsageValidator = getMock(OperatorUsageValidator.class);
        expect(operatorUsageValidator.check(theUser, clause)).andReturn(false);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(scheme.isGlobal()).andReturn(false);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext3);
        expect(scheme.isGlobal()).andReturn(false);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext3, clauseContext3).asSet())).andReturn(clauseContext3);

        mockController.replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);
        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertEquals(clauseContext3, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextNoSchemes() throws Exception
    {
        final QueryLiteral literal = createLiteral(10L);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, new MultiValueOperand(literal));

        expect(customField.getConfigurationSchemes()).andReturn(Collections.<FieldConfigScheme>emptyList());

        mockController.replay();
        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);

        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertGlobalContext(result);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextNullSchemes() throws Exception
    {
        final QueryLiteral literal = createLiteral(10L);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, new MultiValueOperand(literal));

        expect(customField.getConfigurationSchemes()).andReturn(null);

        mockController.replay();
        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);
        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertGlobalContext(result);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextOneOptionEmptyOneNotEmpty() throws Exception
    {
        final QueryLiteral literalPositive = createLiteral(10L);
        final QueryLiteral literalEmpty = new QueryLiteral(EmptyOperand.EMPTY);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, new MultiValueOperand(literalPositive, literalEmpty));
        final MockOption option = new MockOption(null, null, null, null, null, 10L);

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(2);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme).asList());

        expect(jqlSelectOptionsUtil.getOptions(customField, literalPositive, false)).andReturn(Collections.<Option>singletonList(option));
        expect(jqlSelectOptionsUtil.getOptions(customField, literalEmpty, false)).andReturn(Collections.<Option>singletonList(null));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Collections.<Option>singletonList(option));

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);

        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertEquals(clauseContext3, result);

        verify();
    }

    @Test
    public void testGetClauseContextNoLiterals() throws Exception
    {
        final QueryLiteral literalPositive = createLiteral(10L);
        final QueryLiteral literalEmpty = new QueryLiteral();
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, new MultiValueOperand(literalPositive, literalEmpty));

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(2);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false);

        jqlOperandResolver = getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(theUser, clause.getOperand(), clause)).andReturn(Collections.<QueryLiteral>emptyList());

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme).asList());
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);

        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertEquals(clauseContext3, result);

        verify();
    }


    @Test
    public void testGetClauseContextOptionsEquals() throws Exception
    {
        final String value = "one";
        final SingleValueOperand operand = new SingleValueOperand(value);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = new QueryLiteral(operand, value);

        final MockOption option1 = new MockOption(null, null, null, null, null, 10L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 20L);
        final MockOption option3 = new MockOption(null, null, null, null, null, 30L);

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(6);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());

        expect(jqlSelectOptionsUtil.getOptions(customField, literal, false)).andReturn(Arrays.<Option>asList(option1, option2));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(scheme.isGlobal()).andReturn(false);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        //This scheme should be ignored.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option3));

        mockController.replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);
        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertEquals(clauseContext3, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextOptionsNotEquals() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand("one", "two");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.NOT_EQUALS, operand);
        final QueryLiteral literal1 = new QueryLiteral(operand, "one");
        final QueryLiteral literal2 = new QueryLiteral(operand, "two");

        final MockOption option1 = new MockOption(null, null, null, null, null, 10L);
        final MockOption option3 = new MockOption(null, null, null, null, null, 30L);

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(6);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        //This should be ignored.
        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());
        expect(jqlSelectOptionsUtil.getOptions(customField, literal1, false)).andReturn(Arrays.<Option>asList(option1));
        expect(jqlSelectOptionsUtil.getOptions(customField, literal2, false)).andReturn(Arrays.<Option>asList(option1));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option3));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(scheme.isGlobal()).andReturn(false);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        mockController.replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);
        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertEquals(clauseContext3, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextOptionsGlobalContext() throws Exception
    {
        final String value = "one";
        final SingleValueOperand operand = new SingleValueOperand(value);
        final QueryLiteral literal = new QueryLiteral(operand, value);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.NOT_EQUALS, operand);
        final MockOption option1 = new MockOption(null, null, null, null, null, 10L);
        final MockOption option3 = new MockOption(null, null, null, null, null, 30L);

        ClauseContext clauseContext1 = new ClauseContextImpl();
        ClauseContext clauseContext2 = createProjectContext(1);
        ClauseContext clauseContext3 = createProjectContext(6);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());
        expect(jqlSelectOptionsUtil.getOptions(customField, literal, false)).andReturn(Arrays.<Option>asList(option1));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option3));
        expect(scheme.isGlobal()).andReturn(false);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(clauseContext2);
        expect(contextSetUtil.union(CollectionBuilder.<ClauseContext>newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option3));
        expect(scheme.isGlobal()).andReturn(true);

        mockController.replay();

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil, fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);
        final ClauseContext result = factory.getClauseContext(theUser, clause);

        assertGlobalContext(result);

        mockController.verify();
    }

    @Test
    public void testNoLiterals() throws Exception
    {
        final String value = "one";
        final SingleValueOperand operand = new SingleValueOperand(value);
        final QueryLiteral literal = new QueryLiteral(operand, value);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.NOT_EQUALS, operand);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());
        expect(jqlSelectOptionsUtil.getOptions(customField, literal, false)).andReturn(Collections.<Option>emptyList());
        expect(scheme.isGlobal()).andReturn(true);

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);

        replay();
        assertGlobalContext(factory.getClauseContext(theUser, clause));
        verify();
    }

    @Test
    public void testNullLiterals() throws Exception
    {
        final String value = "one";
        final SingleValueOperand operand = new SingleValueOperand(value);
        final QueryLiteral literal = new QueryLiteral(operand, value);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.NOT_EQUALS, operand);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(CollectionBuilder.<FieldConfigScheme>newBuilder(scheme, scheme).asList());
        expect(jqlSelectOptionsUtil.getOptions(customField, literal, false)).andReturn(CollectionBuilder.<Option>newBuilder((Option)null).asMutableList());
        expect(scheme.isGlobal()).andReturn(true);

        final SelectCustomFieldClauseContextFactory factory = new SelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, operatorUsageValidator);

        replay();
        assertGlobalContext(factory.getClauseContext(theUser, clause));
        verify();
    }

    private static void assertGlobalContext(final ClauseContext result)
    {
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), result);
    }

    private ClauseContext createProjectContext(int... projects)
    {
        Set<ProjectIssueTypeContext> itc = new HashSet<ProjectIssueTypeContext>();
        for (int project : projects)
        {
            itc.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl((long) project), AllIssueTypesContext.getInstance()));
        }
        return new ClauseContextImpl(itc);
    }
}
