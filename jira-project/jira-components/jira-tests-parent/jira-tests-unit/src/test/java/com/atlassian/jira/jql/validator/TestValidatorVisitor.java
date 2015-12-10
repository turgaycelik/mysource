package com.atlassian.jira.jql.validator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestValidatorVisitor
{
    @Mock private ValidatorRegistry validatorRegistry;
    @Mock private OperatorUsageValidator usageValidator;
    @Mock private ClauseValidator clauseValidator;
    private JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

    private final User user = new MockUser("admin");
    private ValidatorVisitor validatorVisitor;

    @Before
    public void setup()
    {
        validatorVisitor = new MockValidatorVisitor(validatorRegistry, jqlOperandResolver, usageValidator, user, null);
    }

    @Test
    public void testVisitAndClauseHappyPath() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        TerminalClause clause2 = new TerminalClauseImpl("clause2", Operator.EQUALS, 2);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        when(validatorRegistry.getClauseValidator(user, clause2)).thenReturn(singletonList(clauseValidator));

        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));
        when(clauseValidator.validate(user, clause2)).thenReturn(getErrorMessageSet("Error 2"));

        final MessageSet messageSet = validatorVisitor.visit(new AndClause(clause1, clause2));

        assertMessageSetErrors(messageSet, "Error 1", "Error 2");
        assertMessageSetWarnings(messageSet);

    }

    @Test
    public void testVisitAndClauseOneNullQuery() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        TerminalClause clause2 = new TerminalClauseImpl("clause2", Operator.EQUALS, 2);

        final ClauseValidator clauseValidator = Mockito.mock(ClauseValidator.class);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        when(validatorRegistry.getClauseValidator(user, clause2)).thenReturn(singletonList(clauseValidator));

        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));

        final MessageSet messageSet = validatorVisitor.visit(new AndClause(clause1, clause2));

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);
    }

    @Test
    public void testVisitOrClauseHappyPath() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        TerminalClause clause2 = new TerminalClauseImpl("clause2", Operator.EQUALS, 2);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        when(validatorRegistry.getClauseValidator(user, clause2)).thenReturn(singletonList(clauseValidator));

        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));
        when(clauseValidator.validate(user, clause2)).thenReturn(getErrorMessageSet("Error 2"));

        final MessageSet messageSet = validatorVisitor.visit(new OrClause(clause1, clause2));

        assertMessageSetErrors(messageSet, "Error 1", "Error 2");
        assertMessageSetWarnings(messageSet);
    }

    @Test
    public void testVisitOrClauseOneNullQuery() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        TerminalClause clause2 = new TerminalClauseImpl("clause2", Operator.EQUALS, 2);

        final ClauseValidator clauseValidator = Mockito.mock(ClauseValidator.class);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        when(validatorRegistry.getClauseValidator(user, clause2)).thenReturn(singletonList(clauseValidator));

        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));

        final MessageSet messageSet = validatorVisitor.visit(new OrClause(clause1, clause2));

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);
    }

    @Test
    public void testVisitNotClauseHappyPath() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));

        final MessageSet messageSet = validatorVisitor.visit(new NotClause(clause1));

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);
    }

    @Test
    public void testVisitTerminalClauseOperatorValidationFailure() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));
        when(usageValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 2"));

        final MessageSet messageSet = validatorVisitor.visit(clause1);

        assertMessageSetErrors(messageSet, "Error 2");
        assertMessageSetWarnings(messageSet);
    }

    @Test
    public void testVisitTerminalClauseHappyPathWithOperatorWarning() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));

        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));
        when(usageValidator.validate(user, clause1)).thenReturn(getWarningMessageSet("Warning 1"));

        final MessageSet messageSet = validatorVisitor.visit(clause1);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet, "Warning 1");
    }

    @Test
    public void testVisitTerminalClauseHappyPathWithMultipleValidators() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);

        final ClauseValidator clauseValidator2 = Mockito.mock(ClauseValidator.class);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(asList(clauseValidator, clauseValidator2));
        when(clauseValidator.validate(user, clause1)).thenReturn(getErrorMessageSet("Error 1"));
        when(clauseValidator2.validate(user, clause1)).thenReturn(getWarningMessageSet("Warning 1"));

        final MessageSet messageSet = validatorVisitor.visit(clause1);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet, "Warning 1");
    }

    @Test
    public void testVisitTerminalClauseErrorInOperandHandler() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);

        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(singletonList(clauseValidator));
        jqlOperandResolver = Mockito.mock(JqlOperandResolver.class);
        when(jqlOperandResolver.validate(user, clause1.getOperand(), clause1)).thenReturn(getErrorMessageSet("Error 1"));

        validatorVisitor = new ValidatorVisitor(validatorRegistry, jqlOperandResolver, usageValidator, user, null);

        final MessageSet messageSet = validatorVisitor.visit(clause1);
        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);
    }

    @Test
    public void testVisitTerminalClauseSavedFilter()
    {
        final long filterId = 101188L;

        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        final SavedFilterClauseValidator savedFilterClauseValidator = Mockito.mock(SavedFilterClauseValidator.class);
        when(validatorRegistry.getClauseValidator(user, clause1)).thenReturn(Collections.<ClauseValidator>singletonList(savedFilterClauseValidator));
        when(savedFilterClauseValidator.validate(user, clause1, filterId)).thenReturn(getErrorMessageSet("Error 1"));

        validatorVisitor = new ValidatorVisitor(validatorRegistry, jqlOperandResolver, usageValidator, user, filterId);

        final MessageSet messageSet = validatorVisitor.visit(clause1);
        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);

        verify(savedFilterClauseValidator).validate(user, clause1, filterId);
    }

    @Test
    public void testVisitTerminalClauseNoValidator() throws Exception
    {
        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        final MessageSet messageSet = validatorVisitor.visit(clause1);
        assertMessageSetErrors(messageSet, NoopI18nHelper.makeTranslation("jira.jql.validation.no.such.field", clause1.getName()));
    }

    @Test
    public void testVisitTerminalClauseNoValidatorAnonymous() throws Exception
    {
        validatorVisitor = new MockValidatorVisitor(validatorRegistry, jqlOperandResolver, usageValidator, null, null);

        TerminalClause clause1 = new TerminalClauseImpl("clause1", Operator.EQUALS, 1);
        final MessageSet messageSet = validatorVisitor.visit(clause1);
        assertMessageSetErrors(messageSet, NoopI18nHelper.makeTranslation("jira.jql.validation.no.such.field.anonymous", clause1.getName()));
    }

    private static MessageSet getErrorMessageSet(String...args)
    {
        final MessageSetImpl set1 = new MessageSetImpl();
        for (String arg : args)
        {
            set1.addErrorMessage(arg);
        }
        return set1;
    }

    private static MessageSet getWarningMessageSet(String... args)
    {
        final MessageSetImpl set1 = new MessageSetImpl();
        for (String arg : args)
        {
            set1.addWarningMessage(arg);
        }
        return set1;
    }

    private void assertMessageSetErrors(MessageSet set, String...errors)
    {
        assertSet(set.getErrorMessages(), errors);
    }

    private void assertMessageSetWarnings(MessageSet set, String...warnings)
    {
        assertSet(set.getWarningMessages(), warnings);
    }

    private <T> void assertSet(Set<T> actualValues, T...expectedValues)
    {
        assertEquals(actualValues.size(), expectedValues.length);

        final Iterator<T> actualIter = actualValues.iterator();
        for (T expected : expectedValues)
        {
            assertEquals(expected, actualIter.next());
        }
    }

    private static class MockValidatorVisitor extends ValidatorVisitor
    {
        private MockValidatorVisitor(final ValidatorRegistry validatorRegistry, final JqlOperandResolver operandResolver, final OperatorUsageValidator operatorUsageValidator, final User searcher, final Long filterId)
        {
            super(validatorRegistry, operandResolver, operatorUsageValidator, searcher, filterId);
        }

        @Override
        NoopI18nHelper getI18n()
        {
            return new NoopI18nHelper();
        }
    }
}
