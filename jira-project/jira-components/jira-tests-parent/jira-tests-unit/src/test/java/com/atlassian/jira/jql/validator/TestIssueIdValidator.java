package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link IssueIdValidator}.
 *
 * @since v4.0
 */
public class TestIssueIdValidator extends MockControllerTestCase
{
    private User theUser = null;

    @Mock
    MovedIssueKeyStore movedIssueKeyStore;

    @Test
    public void testValidateUnsupportedOperator() throws Exception
    {
        final String expectedError = "error message";

        mockController.addObjectInstance(new MockSupportedOperatorsValidator(expectedError));

        final IssueIdValidator idValidator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet =
                idValidator.validate(theUser, new TerminalClauseImpl("issueKey", Operator.LIKE, "PKY-134"));
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains(expectedError));

        mockController.verify();
    }

    @Test
    public void testValidateNullLiterals() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final SingleValueOperand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);

        expect(resolver.getValues(theUser, operand, clause)).andReturn(null);
        IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateEmptyLiterals() throws Exception
    {
        final String fieldName = "key";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);

        expect(resolver.getValues(theUser, operand, clause)).andReturn(Collections.singletonList(new QueryLiteral()));
        expect(resolver.isFunctionOperand(operand)).andReturn(false);

        IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections
                .singleton(NoopI18nHelper.makeTranslation("jira.jql.clause.field.does.not.support.empty", fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateBadKey() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final SingleValueOperand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);

        expect(resolver.getValues(theUser, operand, clause))
                .andReturn(Collections.singletonList(createLiteral(issueKey)));
        expect(resolver.isFunctionOperand(operand)).andReturn(false);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.isValidIssueKey(issueKey)).andReturn(false);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).andReturn(Sets.newHashSet(issueKey));

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(
                NoopI18nHelper.makeTranslation("jira.jql.clause.issuekey.invalidissuekey", issueKey, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateBadKeyFunction() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final String funcName = "qwerty";
        final Operand operand = new FunctionOperand(funcName);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);

        expect(resolver.getValues(theUser, operand, clause))
                .andReturn(Collections.singletonList(new QueryLiteral(operand, issueKey)));
        expect(resolver.isFunctionOperand(operand)).andReturn(true);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.isValidIssueKey(issueKey)).andReturn(false);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).andReturn(Sets.newHashSet(issueKey));

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(NoopI18nHelper
                .makeTranslation("jira.jql.clause.issuekey.invalidissuekey.from.func", funcName, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateNoIssue() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";

        final Operand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause))
                .andReturn(Collections.singletonList(createLiteral(issueKey)));
        expect(resolver.isFunctionOperand(operand)).andReturn(false);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.isValidIssueKey(issueKey)).andReturn(true);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).andReturn(Sets.newHashSet(issueKey));

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections
                .singleton(NoopI18nHelper.makeTranslation("jira.jql.clause.issuekey.noissue", issueKey, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateNoIssueFunction() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final String funcName = "qwerty";

        final Operand operand = new FunctionOperand(funcName);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause))
                .andReturn(Collections.singletonList(new QueryLiteral(operand, issueKey)));
        expect(resolver.isFunctionOperand(operand)).andReturn(true);

        final JqlIssueKeySupport keySupport = mockController.getMock(JqlIssueKeySupport.class);
        expect(keySupport.isValidIssueKey(issueKey)).andReturn(true);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).andReturn(Sets.newHashSet(issueKey));

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(
                NoopI18nHelper.makeTranslation("jira.jql.clause.issuekey.noissue.from.func", funcName, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateHappyPathWithValues() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final MockIssue issue = new MockIssue(90l);
        final Operand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause))
                .andReturn(Collections.singletonList(createLiteral(issueKey)));

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).andReturn(new HashSet<String>());

        final MovedIssueValidator movedIssueValidator = mockController.getMock(MovedIssueValidator.class);
        expect(movedIssueValidator.validate(null, Sets.newHashSet(issueKey), clause)).andReturn(new MessageSetImpl());

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateHappyPathWithIdValues() throws Exception
    {
        final long id = 9292;
        final String fieldName = "key";
        final MockIssue issue = new MockIssue(id);
        final Operand operand = new SingleValueOperand(id);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause)).andReturn(Collections.singletonList(createLiteral(id)));

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIdsOfMissingIssues(Sets.newHashSet(id))).andReturn(new HashSet<Long>());

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateHappyPathWithoutValues() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final Operand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause)).andReturn(Collections.<QueryLiteral>emptyList());

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());

        mockController.verify();
    }

    @Test
    public void testValidateInvalidId() throws Exception
    {
        final long id = 1287;
        final String fieldName = "name";
        final Operand operand = new SingleValueOperand(id);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause)).andReturn(Collections.singletonList(createLiteral(id)));
        expect(resolver.isFunctionOperand(operand)).andReturn(false);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIdsOfMissingIssues(Sets.newHashSet(id))).andReturn(Sets.newHashSet(id));

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.singleton("jira.jql.clause.no.value.for.id{[name, 1287]}"),
                messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateInvalidIdFromFunc() throws Exception
    {
        final long id = 1288;
        final String fieldName = "qwerty";
        final Operand operand = new FunctionOperand("funcName");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        mockController.addObjectInstance(new MockSupportedOperatorsValidator());
        mockController.addObjectInstance(new NoopI18nFactory());

        final JqlOperandResolver resolver = mockController.getMock(JqlOperandResolver.class);
        expect(resolver.getValues(theUser, operand, clause))
                .andReturn(Collections.singletonList(new QueryLiteral(operand, id)));
        expect(resolver.isFunctionOperand(operand)).andReturn(true);

        final JqlIssueSupport issueSupport = mockController.getMock(JqlIssueSupport.class);
        expect(issueSupport.getIdsOfMissingIssues(Sets.newHashSet(id))).andReturn(Sets.newHashSet(id));

        final IssueIdValidator validator = mockController.instantiate(IssueIdValidator.class);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.singleton("jira.jql.clause.no.value.for.name.from.function{[funcName, qwerty]}"),
                messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }


    private static class MockSupportedOperatorsValidator extends SupportedOperatorsValidator
    {
        private final String message;

        public MockSupportedOperatorsValidator()
        {
            this(null);
        }

        public MockSupportedOperatorsValidator(final String message)
        {
            this.message = message;
        }

        @Override
        public MessageSet validate(final User searcher, final TerminalClause terminalClause)
        {
            MessageSet set = new MessageSetImpl();
            if (message != null)
            {
                set.addErrorMessage(message);
            }
            return set;
        }
    }
}
