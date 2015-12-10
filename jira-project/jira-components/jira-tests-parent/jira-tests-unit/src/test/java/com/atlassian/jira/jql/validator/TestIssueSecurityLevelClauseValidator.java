package com.atlassian.jira.jql.validator;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IssueSecurityLevelResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueSecurityLevelClauseValidator extends MockControllerTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");

    }

    @After
    public void tearDown() throws Exception
    {
        theUser = null;

    }

    @Test
    public void testValidateHappyPathEmptyLiteral() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;

        final QueryLiteral emptyLiteral = new QueryLiteral();

        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(emptyLiteral).asList());

        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);

        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = levelClauseValidator.validate(theUser, clause);
        assertFalse(messageSet.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testValidateHappyPathMultipleLevels() throws Exception
    {
        final SingleValueOperand level1Operand = new SingleValueOperand("level1");
        final SingleValueOperand level2Operand = new SingleValueOperand("level2");
        final SingleValueOperand level3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = createLiteral("level1");
        final QueryLiteral queryLiteral2 = createLiteral("level2");
        final QueryLiteral queryLiteral3 = createLiteral(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(level1Operand, level2Operand, level3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());

        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral1);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(1L, "level1")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral2);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(2L, "level2")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral3);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(3L, "level3")));

        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = levelClauseValidator.validate(theUser, clause);
        assertFalse(messageSet.hasAnyMessages());
        mockController.verify();
    }

    @Test
    public void testErrorFindingLevelByName() throws Exception
    {
        final SingleValueOperand level1Operand = new SingleValueOperand("level1");
        final SingleValueOperand level2Operand = new SingleValueOperand("level2");
        final SingleValueOperand level3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = new QueryLiteral(level1Operand, "level1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(level2Operand, "level2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(level3Operand, 123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(level1Operand, level2Operand, level3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(level2Operand);
        mockController.setReturnValue(false);

        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral1);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(1L, "level1")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral2);
        mockController.setReturnValue(Collections.emptyList());
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral3);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(3L, "level3")));

        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = levelClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The value 'level2' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testErrorFindingLevelByNameFromFunction() throws Exception
    {
        final SingleValueOperand level1Operand = new SingleValueOperand("level1");
        final SingleValueOperand level2Operand = new SingleValueOperand("level2");
        final SingleValueOperand level3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = new QueryLiteral(level1Operand, "level1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(level2Operand, "level2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(level3Operand, 123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(level1Operand, level2Operand, level3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(level2Operand);
        mockController.setReturnValue(true);

        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral1);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(1L, "level1")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral2);
        mockController.setReturnValue(Collections.emptyList());
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral3);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(3L, "level3")));

        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = levelClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testErrorFindingLevelById() throws Exception
    {
        final SingleValueOperand level1Operand = new SingleValueOperand("level1");
        final SingleValueOperand level2Operand = new SingleValueOperand("level2");
        final SingleValueOperand level3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = new QueryLiteral(level1Operand, "level1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(level2Operand, "level2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(level3Operand, 123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(level1Operand, level2Operand, level3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(level3Operand);
        mockController.setReturnValue(false);

        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral1);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(1L, "level1")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral2);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(2L, "level2")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral3);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = levelClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value with ID '123' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();

    }

    @Test
    public void testErrorFindingLevelByIdFromFunction() throws Exception
    {
        final SingleValueOperand level1Operand = new SingleValueOperand("level1");
        final SingleValueOperand level2Operand = new SingleValueOperand("level2");
        final SingleValueOperand level3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = new QueryLiteral(level1Operand, "level1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(level2Operand, "level2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(level3Operand, 123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(level1Operand, level2Operand, level3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(level3Operand);
        mockController.setReturnValue(true);

        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral1);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(1L, "level1")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral2);
        mockController.setReturnValue(Collections.singletonList(createMockSecurityLevel(2L, "level2")));
        levelResolver.getIssueSecurityLevels(theUser, queryLiteral3);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = levelClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testInvalidOperator() throws Exception
    {
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("dude");

        final TerminalClause clause = new TerminalClauseImpl("test", Operator.EQUALS, "a");

        final SupportedOperatorsValidator operatorsValidator = mockController.getMock(SupportedOperatorsValidator.class);
        operatorsValidator.validate(theUser, clause);
        mockController.setReturnValue(messageSet);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final IssueSecurityLevelResolver levelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        mockController.replay();

        final IssueSecurityLevelClauseValidator levelClauseValidator = new IssueSecurityLevelClauseValidator(levelResolver, jqlOperandResolver)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet foundSet = levelClauseValidator.validate(theUser, clause);
        assertTrue(foundSet.hasAnyMessages());

        mockController.verify();
    }

    private MockGenericValue createMockSecurityLevel(final Long id, final String name)
    {
        return new MockGenericValue("IssueSecurityLevel", MapBuilder.newBuilder().add("id", id).add("name", name).toMap());
    }
}
