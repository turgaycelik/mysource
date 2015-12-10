package com.atlassian.jira.jql.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.MockPriorityResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link RawValuesExistValidator}.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestRawValuesExistValidator
{
    private static final User ANONYMOUS = null;

    @Mock I18nHelper.BeanFactory beanFactory;
    @Mock OperandHandler<SingleValueOperand> operandHandler;

    @After
    public void tearDown()
    {
        beanFactory = null;
        operandHandler = null;
    }


    @Test
    public void testLookupFailureStringValue()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("major");
        final NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);

        final RawValuesExistValidator clauseValidator = new Fixture(priorityIndexInfoResolver);
        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "The value 'major' does not exist for the field 'priority'.");
    }

    @Test
    public void testLookupFailureStringValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("major");
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);

        when(operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause)))
                .thenReturn(ImmutableList.of(createLiteral("major")));
        when(operandHandler.isFunction()).thenReturn(true);

        final MockJqlOperandResolver mockJqlOperandSupport = new MockJqlOperandResolver().addHandler("SingleValueOperand", operandHandler);
        final NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);

        final RawValuesExistValidator clauseValidator = new Fixture(mockJqlOperandSupport, priorityIndexInfoResolver);
        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "A value provided by the function 'SingleValueOperand' is invalid for the field 'priority'.");
    }

    @Test
    public void testLookupFailureLongValue()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(12345L);
        final NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);

        final RawValuesExistValidator clauseValidator = new Fixture(priorityIndexInfoResolver);
        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "A value with ID '12345' does not exist for the field 'priority'.");
    }

    @Test
    public void testLookupFailureLongValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(12345L);
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);

        when(operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause)))
                .thenReturn(ImmutableList.of(createLiteral(12345L)));
        when(operandHandler.isFunction()).thenReturn(true);

        final MockJqlOperandResolver mockJqlOperandSupport = new MockJqlOperandResolver().addHandler("SingleValueOperand", operandHandler);
        final NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);

        final RawValuesExistValidator clauseValidator = new Fixture(mockJqlOperandSupport, priorityIndexInfoResolver);
        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "A value provided by the function 'SingleValueOperand' is invalid for the field 'priority'.");
    }

    @Test
    public void testLookupLongAsName()
    {
        final MultiValueOperand operand = new MultiValueOperand(111L, 123L);
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, operand);
        final JqlOperandResolver jqlOperandResolver = mock(JqlOperandResolver.class);
        when(jqlOperandResolver.isValidOperand(operand)).thenReturn(true);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, priorityClause)).thenReturn(ImmutableList.of(
                createLiteral(111L),
                createLiteral(123L)));
        final LinkedHashMap<String, List<Long>> priorityConfig = new LinkedHashMap<String, List<Long>>();
        priorityConfig.put("111", Lists.newArrayList(1L));
        priorityConfig.put("123", Lists.newArrayList(2L));

        final NameResolver<Priority> priorityResolver = new MockPriorityResolver(priorityConfig);
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);
        final RawValuesExistValidator clauseValidator = new Fixture(jqlOperandResolver, priorityIndexInfoResolver);

        final MessageSet errorCollection = clauseValidator.validate(ANONYMOUS, priorityClause);
        assertNoMessages(errorCollection);
    }

    @Test
    public void testNoOperandHandler()
    {
        final JqlOperandResolver jqlOperandResolver = mock(JqlOperandResolver.class);
        when(jqlOperandResolver.isValidOperand(isA(Operand.class))).thenReturn(false);
        final LinkedHashMap<String, List<Long>> priorityConfig = new LinkedHashMap<String, List<Long>>();
        priorityConfig.put("major", Lists.newArrayList(1L));
        priorityConfig.put("minor", Lists.newArrayList(2L));

        final NameResolver<Priority> priorityResolver = new MockPriorityResolver(priorityConfig);
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, new MultiValueOperand("major", "minor"));

        final RawValuesExistValidator clauseValidator = new Fixture(jqlOperandResolver, priorityIndexInfoResolver);
        final MessageSet errorCollection = clauseValidator.validate(ANONYMOUS, priorityClause);
        assertNoMessages(errorCollection);
    }

    @Test
    public void testHappyPath()
    {
        final MultiValueOperand operand = new MultiValueOperand("major", "minor");
        final TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, operand);
        final JqlOperandResolver jqlOperandResolver = mock(JqlOperandResolver.class);
        when(jqlOperandResolver.isValidOperand(operand)).thenReturn(true);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, priorityClause)).thenReturn(ImmutableList.of(
                createLiteral("major"),
                createLiteral("minor")));

        final LinkedHashMap<String, List<Long>> priorityConfig = new LinkedHashMap<String, List<Long>>();
        priorityConfig.put("major", Lists.newArrayList(1L));
        priorityConfig.put("minor", Lists.newArrayList(2L));
        final NameResolver<Priority> priorityResolver = new MockPriorityResolver(priorityConfig);
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);

        final RawValuesExistValidator clauseValidator = new Fixture(jqlOperandResolver, priorityIndexInfoResolver);
        final MessageSet errorCollection = clauseValidator.validate(ANONYMOUS, priorityClause);
        assertNoMessages(errorCollection);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testNullJqlOperandResolver()
    {
        new RawValuesExistValidator(null, mock(IndexInfoResolver.class), beanFactory);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testNullIndexInfoResolver()
    {
        new RawValuesExistValidator(mock(JqlOperandResolver.class), null, beanFactory);
    }


    class Fixture extends RawValuesExistValidator
    {
        Fixture(final IndexInfoResolver<?> indexInfoResolver)
        {
            this(MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver);
        }

        Fixture(final JqlOperandResolver operandResolver, final IndexInfoResolver<?> indexInfoResolver)
        {
            super(operandResolver, indexInfoResolver, beanFactory);
        }

        @Override
        protected I18nHelper getI18n(final User user)
        {
            return new MockI18nBean();
        }
    }


    /**
     * A NameResolver<Priority> which always fails lookups.
     */
    private static class FailingPriorityResolver implements NameResolver<Priority>
    {
        public List<String> getIdsFromName(final String name)
        {
            return Collections.emptyList();
        }

        public boolean nameExists(final String name)
        {
            return false;
        }

        public boolean idExists(final Long id)
        {
            return false;
        }

        public Priority get(final Long id)
        {
            return null;
        }

        public Collection<Priority> getAll()
        {
            return Collections.emptyList();
        }

    }
}
