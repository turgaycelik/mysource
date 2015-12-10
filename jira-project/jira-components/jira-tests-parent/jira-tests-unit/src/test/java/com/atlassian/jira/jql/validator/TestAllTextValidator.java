package com.atlassian.jira.jql.validator;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestAllTextValidator
{
    private static final User ANONYMOUS = null;

    private CommentValidator commentValidator;
    private AllTextValidator validator;

    @Before
    public void setUp()
    {
        commentValidator = new CommentValidator(MockJqlOperandResolver.createSimpleSupport());
    }

    @After
    public void tearDown()
    {
        commentValidator = null;
        validator = null;
    }

    @Test
    public void testValidateBadOperator()
    {
        final TerminalClause clause = new TerminalClauseImpl("text", Operator.LIKE, "test");
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("something");

        final SupportedOperatorsValidator supportedOperatorsValidator = mock(SupportedOperatorsValidator.class);
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause))
                .thenReturn(messageSet);

        validator = new AllTextValidator(commentValidator)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }
        };

        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertEquals(messageSet, result);
    }

    @Test
    public void testValidateDelegateCalled()
    {
        final AtomicBoolean validatedCalled = new AtomicBoolean(false);
        final TerminalClause clause = new TerminalClauseImpl("text", Operator.LIKE, "test");
        final MessageSetImpl messageSet = new MessageSetImpl();

        final SupportedOperatorsValidator supportedOperatorsValidator = mock(SupportedOperatorsValidator.class);
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause))
                .thenReturn(messageSet);

        commentValidator = new CommentValidator(MockJqlOperandResolver.createSimpleSupport())
        {
            @Nonnull
            @Override
            public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
            {
                validatedCalled.set(true);
                return messageSet;
            }
        };

        validator = new AllTextValidator(commentValidator)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }
        };

        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertSame(messageSet, result);
        assertTrue("validated", validatedCalled.get());
    }
}
