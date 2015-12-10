package com.atlassian.jira.jql.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.AlwaysValidOperatorUsageValidator;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCascadingSelectCustomFieldClauseContextFactory
{
    @Mock private CustomField customField;
    @Mock private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    @Mock private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    @Mock private JqlOperandResolver jqlOperandResolver;
    @Mock private ContextSetUtil contextSetUtil;
    @Mock private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;

    private OperatorUsageValidator operatorUsageValidator = new AlwaysValidOperatorUsageValidator();
    private User theUser = null;

    @After
    public void tearDown() throws Exception
    {
        customField = null;
        jqlSelectOptionsUtil = null;
        fieldConfigSchemeClauseContextUtil = null;
        jqlOperandResolver = null;
        contextSetUtil = null;
        jqlCascadingSelectLiteralUtil = null;
        operatorUsageValidator = null;

    }

    @Test
    public void testNoSchemes() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");

        when(customField.getConfigurationSchemes()).thenReturn(Collections.<FieldConfigScheme>emptyList());

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);
        final ClauseContext clauseContext = factory.getClauseContext(theUser, clause);

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), clauseContext);
    }

    @Test
    public void testNullSchemes() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");

        when(customField.getConfigurationSchemes()).thenReturn(null);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);
        final ClauseContext clauseContext = factory.getClauseContext(theUser, clause);

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), clauseContext);
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.LIKE, "fine");
        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(5);
        final ClauseContext context3 = createContextForProjects(1292);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1, context2);
        when(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).thenReturn(context3);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);
        
        assertEquals(context3, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testBadOperatorUsage() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(5);
        final ClauseContext context3 = createContextForProjects(1292);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));

        operatorUsageValidator = mock(OperatorUsageValidator.class);
        when(operatorUsageValidator.check(theUser, testClause)).thenReturn(false);
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme))
                .thenReturn(context1, context2);
        when(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).thenReturn(context3);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        assertEquals(context3, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testPostiveQueryWithPositiveOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final ClauseContext context1 = createContextForProjects(1, 2);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));
        //The second scheme should not be included because it has no options.
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme))
                .thenReturn(Arrays.<Option>asList(option1, option2))
                .thenReturn(Arrays.<Option>asList());
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1);


        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        assertEquals(context1, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testPositiveQueryWithPositiveAndNegativeOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context1 = createContextForProjects(1, 56);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));

        //The second scheme has no options in the scheme not in the exclude list.
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme))
                .thenReturn(Arrays.<Option>asList(option1, option2))
                .thenReturn(Arrays.<Option>asList(option2, option2child1, option2child2));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1);


        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
                negativeOption.add(option2);
            }
        };

        assertEquals(context1, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testPositiveQueryWithPositiveAndEmpty() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final ClauseContext context1 = createContextForProjects(1, 56);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));
        when(scheme.isGlobal()).thenReturn(false, true);
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(null);
            }
        };

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testPositiveQueryWithNoPositive() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));
        when(scheme.isGlobal()).thenReturn(true);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                negativeOption.add(option2);
                negativeOption.add(option1);
            }
        };

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testNegativeQueryWithPositiveOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final ClauseContext context1 = createContextForProjects(1, 2);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(scheme.isGlobal()).thenReturn(false);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));

        //This second scheme should not be included because it only has options that we are to exclude.
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme))
                .thenReturn(Arrays.<Option>asList(option1, option2))
                .thenReturn(Arrays.<Option>asList(option1));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1);


        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        assertEquals(context1, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testNegativeQueryWithPositiveAndNegativeOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(3);
        final ClauseContext context3 = createContextForProjects(3);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(scheme.isGlobal()).thenReturn(false);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));

        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1, context2);

        //The second scheme should not be included because it only has options that we are to exclude.
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme))
                .thenReturn(Arrays.<Option>asList(option1, option2))
                .thenReturn(Arrays.<Option>asList(option2, option2child1, option2child2));

        when(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).thenReturn(context3);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option2);
                negativeOption.add(option2child1);
            }
        };

        assertEquals(context3, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testNegativeQueryWithNegativeOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_IN, "fine");
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context2 = createContextForProjects(3);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(scheme.isGlobal()).thenReturn(false);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));

        //Will not be included because here are no options.
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).thenReturn(Collections.<Option>emptyList());

        //This second scheme should not be included because it only has options that we are to exclude.
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).thenReturn(Arrays.<Option>asList(option2, option2child1, option2child2));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context2);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                negativeOption.add(option2child1);
            }
        };

        assertEquals(context2, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testNegativeQueryWithNoOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_IN, "fine");
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context2 = createContextForProjects(3);
        final ClauseContext context3 = createContextForProjects(64);
        final ClauseContext context4 = createContextForProjects(34938);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(scheme.isGlobal()).thenReturn(false);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));

        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context3, context2);
        when(contextSetUtil.union(CollectionBuilder.newBuilder(context2, context3).asSet())).thenReturn(context4);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);
            }
        };

        assertEquals(context4, factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testGlobalSchemeConfig() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.LIKE, "fine");
        final ClauseContext context3 = createContextForProjects(64);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme, scheme));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context3);
        when(scheme.isGlobal()).thenReturn(false, true);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testNoGeneratedContext() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(5);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme, scheme));
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).thenReturn(Arrays.<Option>asList(option1, option2));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).thenReturn(context1, context2);
        when(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).thenReturn(new ClauseContextImpl());

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testNoApplicableConfigurations() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);

        final FieldConfigScheme scheme = mock(FieldConfigScheme.class);
        when(customField.getConfigurationSchemes()).thenReturn(Arrays.asList(scheme));
        when(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).thenReturn(Arrays.<Option>asList(option2));

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
    }

    @Test
    public void testFillOptions() throws Exception
    {
        final Operand operand = new SingleValueOperand("one");
        final TerminalClause testClause = new TerminalClauseImpl("test", Operator.EQUALS, operand);
        final QueryLiteral literal1 = new QueryLiteral(operand, 1L);
        final QueryLiteral literal2 = new QueryLiteral(operand, 2L);
        final QueryLiteral literal3 = new QueryLiteral(operand, 3L);
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option3 = new MockOption(null, null, null, null, null, 242L);
        final MockOption option4 = new MockOption(null, null, null, null, null, 27L);

        final List<QueryLiteral> testLiterals = Arrays.asList(literal1, literal2, literal3);

        when(jqlOperandResolver.getValues(theUser, operand, testClause)).thenReturn(testLiterals);

        jqlCascadingSelectLiteralUtil = new JqlCascadingSelectLiteralUtil(jqlSelectOptionsUtil)
        {
            @Override
            public void processPositiveNegativeOptionLiterals(final List<QueryLiteral> inputLiterals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                assertEquals(testLiterals, inputLiterals);
                positiveLiterals.add(literal1);
                positiveLiterals.add(literal2);
                negativeLiterals.add(literal3);
            }
        };

        when(jqlSelectOptionsUtil.getOptions(customField, literal1, true)).thenReturn(Collections.<Option>singletonList(option1));
        when(jqlSelectOptionsUtil.getOptions(customField, literal2, true)).thenReturn(Arrays.<Option>asList(option2, option3));
        when(jqlSelectOptionsUtil.getOptions(customField, literal3, true)).thenReturn(Arrays.<Option>asList(option4));

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        Set<Option> posOpts = new HashSet<Option>();
        Set<Option> negOpts = new HashSet<Option>();

        factory.fillOptions(theUser, testClause, posOpts, negOpts);

        assertEquals(CollectionBuilder.<Option>newBuilder(option1, option2, option3).asSet(), posOpts);
        assertEquals(CollectionBuilder.<Option>newBuilder(option4).asSet(), negOpts);
    }

    @Test
    public void testFillOptionsNullLiterals() throws Exception
    {
        final Operand operand = new SingleValueOperand("one");
        final TerminalClause testClause = new TerminalClauseImpl("test", Operator.EQUALS, operand);

        when(jqlOperandResolver.getValues(theUser, operand, testClause)).thenReturn(null);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        Set<Option> posOpts = new HashSet<Option>();
        Set<Option> negOpts = new HashSet<Option>();

        factory.fillOptions(theUser, testClause, posOpts, negOpts);

        assertTrue(posOpts.isEmpty());
        assertTrue(negOpts.isEmpty());
    }

    private static ClauseContext createContextForProjects(int... projects)
    {
        Set<ProjectIssueTypeContext> ctxs = Sets.newHashSetWithExpectedSize(projects.length);
        for (int project : projects)
        {
            ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl((long)project), AllIssueTypesContext.getInstance()));
        }
        return new ClauseContextImpl(ctxs);
    }
}
