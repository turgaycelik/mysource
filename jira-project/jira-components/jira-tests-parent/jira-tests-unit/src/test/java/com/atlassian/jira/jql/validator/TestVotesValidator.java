package com.atlassian.jira.jql.validator;

import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.vote.DefaultVoteManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.VotesIndexValueConverter;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoWarnings;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVotesValidator
{
    private static final Function<String,String> TO_VALUE_ERROR = new Function<String,String>()
    {
        @Override
        public String apply(final String errorValue)
        {
            return "jira.jql.clause.invalid.votes.value [votes] [" + errorValue + ']';
        }
    };

    @Mock private DefaultVoteManager mockDefaultVoteManager;
    @Mock private JqlOperandResolver mockJqlOperandResolver;
    @Mock private SupportedOperatorsValidator mockSupportedOperatorsValidator;

    @Test
    public void testVotingDisabled() throws Exception
    {
        // Set up
        TerminalClause clause = new TerminalClauseImpl("votes", EQUALS, 10L);
        when(mockDefaultVoteManager.isVotingEnabled()).thenReturn(false);

        final VotesValidator votesValidator =
                new VotesValidator(mockJqlOperandResolver, new VotesIndexValueConverter(), mockDefaultVoteManager)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        final MessageSet messageSet = votesValidator.validate(null, clause);
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.votes.disabled [votes]");
    }

    @Test
    public void testInvalidOperand() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("votes", EQUALS, 10L);

        MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("error!");
        when(mockSupportedOperatorsValidator.validate(null, clause)).thenReturn(messageSet);
        when(mockDefaultVoteManager.isVotingEnabled()).thenReturn(true);

        final VotesValidator votesValidator =
                new VotesValidator(mockJqlOperandResolver, new VotesIndexValueConverter(), mockDefaultVoteManager)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }
        };

        final MessageSet result = votesValidator.validate(null, clause);
        assert1ErrorNoWarnings(result, "error!");
    }

    @Test
    public void testValidValue() throws Exception
    {
        checkValidation(values(10L), false);
        checkValidation(values(0L), false);
        checkValidation(values("10"), false);
        checkValidation(values("0"), false);
        checkValidation(values("0100"), false);
        checkValidation(values("10", 10L), false);
    }

    @Test
    public void testNotValidValue() throws Exception
    {
        checkValidation(new Object[] { null }, false, "EMPTY");
        checkValidation(values(-1L), false, "-1");
        checkValidation(values("-1"), false, "-1");
        checkValidation(values("ab"), false, "ab");
        checkValidation(new Object[] { null }, true, "EMPTY");
        checkValidation(values(-1L), true, "-1");
        checkValidation(values("-1"), true, "-1");
        checkValidation(values("ab"), true, "ab");
        checkValidation(values(10L, "ab"), false, "ab");
        checkValidation(values(-1L, "10"), false, "-1");
        checkValidation(values("ab", "-1", -2L), false, "ab", "-1", "-2");
    }

    private MessageSet validate(final Object[] values, boolean isFunction)
    {
        final Operand operand = new SingleValueOperand("");
        TerminalClause clause = new TerminalClauseImpl("votes", EQUALS, operand);

        when(mockJqlOperandResolver.getValues((User) null, operand, clause)).thenReturn(mapValues(values, operand));
        when(mockJqlOperandResolver.isFunctionOperand(operand)).thenReturn(isFunction);
        when(mockDefaultVoteManager.isVotingEnabled()).thenReturn(true);

        final VotesValidator votesValidator = new VotesValidator(mockJqlOperandResolver,
                new VotesIndexValueConverter(), mockDefaultVoteManager)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };
        return votesValidator.validate(null, clause);
    }

    private void checkValidation(final Object[] values, boolean isFunction, final String... errorValues)
    {
        final MessageSet messageSet = validate(values, isFunction);
        if (errorValues.length == 0)
        {
            assertNoMessages(messageSet);
        }
        else if (isFunction)
        {
            assert1ErrorNoWarnings(messageSet, "jira.jql.clause.invalid.votes.value.function [SingleValueOperand] [votes]");
        }
        else
        {
            assertValueErrors(messageSet, errorValues);
        }
    }

    private static void assertValueErrors(final MessageSet messageSet, final String... errorValues)
    {
        final Collection<String> expectedErrors = newHashSet(transform(asList(errorValues), TO_VALUE_ERROR));
        assertThat("getErrorMessages", messageSet.getErrorMessages(), equalTo(expectedErrors));
        assertNoWarnings(messageSet);
    }

    private static List<QueryLiteral> mapValues(Object[] values, Operand operand)
    {
        final ImmutableList.Builder<QueryLiteral> list = ImmutableList.builder();
        for (Object value : values)
        {
            list.add(mapValue(value, operand));
        }
        return list.build();
    }

    private static QueryLiteral mapValue(Object value, final Operand operand)
    {
        if (value instanceof String)
        {
            return new QueryLiteral(operand, (String)value);
        }
        return new QueryLiteral(operand, (Long)value);
    }

    private static Object[] values(final Object... values)
    {
        return values;
    }
}
