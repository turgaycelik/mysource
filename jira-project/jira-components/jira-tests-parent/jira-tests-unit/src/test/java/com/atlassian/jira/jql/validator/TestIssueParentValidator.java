package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUsers;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.jql.validator.IssueParentValidator}.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIssueParentValidator
{
    @Mock
    private SubTaskManager subTaskManager;
    private User theUser = null;
    @Mock
    private JqlOperandResolver operandResolver;
    @Mock
    private JqlIssueKeySupport jqlIssueKeySupport;
    @Mock
    private JqlIssueSupport jqlIssueSupport;
    @Mock
    private SupportedOperatorsValidator mockSupportedOperatorsValidator;
    @Mock
    private MovedIssueValidator movedIssueValidator;

    private IssueParentValidator validator;


    @Before
    public void setUp() throws Exception
    {
        final NoopI18nFactory beanFactory = new NoopI18nFactory();
        final IssueIdValidator issueIdValidator =
                new IssueIdValidator(operandResolver, jqlIssueKeySupport, jqlIssueSupport, beanFactory,
                        mockSupportedOperatorsValidator, movedIssueValidator);
        validator = new IssueParentValidator(issueIdValidator, subTaskManager, beanFactory);

        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(mockSupportedOperatorsValidator.validate(any(User.class), any(TerminalClause.class)))
                .thenReturn(new MessageSetImpl());
    }

    @Test
    public void testValidateSubTasksDisabled() throws Exception
    {
        final String fieldName = "parent";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(subTaskManager.isSubTasksEnabled()).thenReturn(false);

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections
                .singleton(NoopI18nHelper.makeTranslation("jira.jql.clause.issue.parent.subtasks.disabled", fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateUnsupportedOperator() throws Exception
    {
        final String expectedError = "error message";

        when(mockSupportedOperatorsValidator.validate(eq(theUser), any(TerminalClause.class)))
                .thenReturn(messageSet(expectedError));

        final MessageSet messageSet =
                validator.validate(theUser, new TerminalClauseImpl("issueKey", Operator.LIKE, "PKY-134"));
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getErrorMessages().contains(expectedError));
    }

    @Test
    public void testValidateEmptyOperand() throws Exception
    {
        final String fieldName = "key";
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral()));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(false);

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections
                .singleton(NoopI18nHelper.makeTranslation("jira.jql.clause.field.does.not.support.empty", fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateEmptyOperandFromFunc() throws Exception
    {
        final String fieldName = "key";
        final Operand operand = new FunctionOperand("generateEmpty");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral(operand)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(true);

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(NoopI18nHelper
                .makeTranslation("jira.jql.clause.field.does.not.support.empty.from.func", fieldName, "generateEmpty"));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateNullLiterals() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final SingleValueOperand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        final JqlOperandResolver resolver = Mockito.mock(JqlOperandResolver.class);

        when(resolver.getValues(theUser, operand, clause)).thenReturn(null);
        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateBadKey() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final SingleValueOperand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(createLiteral(issueKey)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(false);
        when(jqlIssueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).thenReturn(Sets.newHashSet(issueKey));

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(
                NoopI18nHelper.makeTranslation("jira.jql.clause.issuekey.invalidissuekey", issueKey, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateBadKeyFunction() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final String funcName = "qwerty";
        final Operand operand = new FunctionOperand(funcName);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral(operand, issueKey)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(true);
        when(jqlIssueKeySupport.isValidIssueKey(issueKey)).thenReturn(false);
        when(jqlIssueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).thenReturn(Sets.newHashSet(issueKey));

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(NoopI18nHelper
                .makeTranslation("jira.jql.clause.issuekey.invalidissuekey.from.func", funcName, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateNoIssue() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";

        final Operand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral(operand, issueKey)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(false);
        when(jqlIssueKeySupport.isValidIssueKey(issueKey)).thenReturn(true);
        when(jqlIssueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).thenReturn(Sets.newHashSet(issueKey));

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections
                .singleton(NoopI18nHelper.makeTranslation("jira.jql.clause.issuekey.noissue", issueKey, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateNoIssueFunction() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final String funcName = "qwerty";

        final Operand operand = new FunctionOperand(funcName);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral(operand, issueKey)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(true);
        when(jqlIssueKeySupport.isValidIssueKey(issueKey)).thenReturn(true);
        when(jqlIssueSupport.getKeysOfMissingIssues(Sets.newHashSet(issueKey))).thenReturn(Sets.newHashSet(issueKey));

        final MessageSet messageSet = validator.validate(theUser, clause);

        final Set<String> expectedErrors = Collections.singleton(
                NoopI18nHelper.makeTranslation("jira.jql.clause.issuekey.noissue.from.func", funcName, fieldName));

        assertNotNull(messageSet);
        assertEquals(expectedErrors, messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateHappyPathWithValues() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final MockIssue issue = new MockIssue(90l);
        final Operand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral(operand, issueKey)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(false);
        when(jqlIssueSupport.getIssue(issueKey, ApplicationUsers.from(theUser))).thenReturn(issue);

        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateHappyPathWithIdValues() throws Exception
    {
        final long id = 9292;
        final String fieldName = "key";
        final MockIssue issue = new MockIssue(id);
        final Operand operand = new SingleValueOperand(id);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(createLiteral(id)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(false);
        when(jqlIssueSupport.getIssue(id, theUser)).thenReturn(issue);

        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateHappyPathWithoutValues() throws Exception
    {
        final String issueKey = "PK-1287";
        final String fieldName = "key";
        final Operand operand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause)).thenReturn(Collections.<QueryLiteral>emptyList());

        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.<String>emptySet(), messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    @Test
    public void testValidateInvalidId() throws Exception
    {
        final long id = 1287;
        final String fieldName = "name";
        final Operand operand = new SingleValueOperand(id);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(createLiteral(id)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(false);
        when(jqlIssueSupport.getIdsOfMissingIssues(Sets.newHashSet(id))).thenReturn(Sets.newHashSet(id));

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

        when(operandResolver.getValues(theUser, operand, clause))
                .thenReturn(Collections.singletonList(new QueryLiteral(operand, id)));
        when(operandResolver.isFunctionOperand(operand)).thenReturn(true);
        when(jqlIssueSupport.getIdsOfMissingIssues(Sets.newHashSet(id))).thenReturn(Sets.newHashSet(id));

        final MessageSet messageSet = validator.validate(theUser, clause);

        assertNotNull(messageSet);
        assertEquals(Collections.singleton("jira.jql.clause.no.value.for.name.from.function{[funcName, qwerty]}"),
                messageSet.getErrorMessages());
        assertEquals(Collections.<String>emptySet(), messageSet.getWarningMessages());
    }

    private MessageSetImpl messageSet(final String expectedError)
    {
        final MessageSetImpl messageSet1 = new MessageSetImpl();
        messageSet1.addErrorMessage(expectedError);
        return messageSet1;
    }
}
