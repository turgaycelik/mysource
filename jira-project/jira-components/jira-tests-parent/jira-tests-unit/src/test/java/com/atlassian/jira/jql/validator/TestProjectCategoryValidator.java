package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableMap;

import org.easymock.classextension.EasyMock;
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
public class TestProjectCategoryValidator extends MockControllerTestCase
{
    private User theUser;
    private JqlOperandResolver jqlOperandResolver;
    private ProjectCategoryResolver projectCategoryResolver;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");

        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        projectCategoryResolver = mockController.getMock(ProjectCategoryResolver.class);
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
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IS, operand);

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = projectCategoryValidator.validate(theUser, clause);
        assertFalse(messageSet.hasAnyMessages());

        verify();
    }

    @Test
    public void testValidateHappyPathMultipleCategories() throws Exception
    {
        final SingleValueOperand cat1Operand = new SingleValueOperand("cat1");
        final SingleValueOperand cat2Operand = new SingleValueOperand("cat2");
        final SingleValueOperand cat3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = createLiteral("cat1");
        final QueryLiteral queryLiteral2 = createLiteral("cat2");
        final QueryLiteral queryLiteral3 = createLiteral(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(cat1Operand, cat2Operand, cat3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral1))
                .andReturn(createMockProjectCategory(1L, "cat1"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral2))
                .andReturn(createMockProjectCategory(2L, "cat2"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral3))
                .andReturn(createMockProjectCategory(3L, "cat3"));

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = projectCategoryValidator.validate(theUser, clause);
        assertFalse(messageSet.hasAnyMessages());
        verify();
    }

    @Test
    public void testErrorFindingCategoryByName() throws Exception
    {
        final SingleValueOperand cat1Operand = new SingleValueOperand("cat1");
        final SingleValueOperand cat2Operand = new SingleValueOperand("cat2");
        final SingleValueOperand cat3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = createLiteral("cat1");
        final QueryLiteral queryLiteral2 = createLiteral("cat2");
        final QueryLiteral queryLiteral3 = createLiteral(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(cat1Operand, cat2Operand, cat3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral1))
                .andReturn(createMockProjectCategory(1L, "cat1"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral2))
                .andReturn(null);
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral3))
                .andReturn(createMockProjectCategory(3L, "cat3"));

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = projectCategoryValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The value 'cat2' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        verify();
    }

    @Test
    public void testErrorFindingCategoryByNameFromFunction() throws Exception
    {
        final SingleValueOperand cat1Operand = new SingleValueOperand("cat1");
        final SingleValueOperand cat2Operand = new SingleValueOperand("cat2");
        final SingleValueOperand cat3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = new QueryLiteral(cat1Operand, "cat1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(cat2Operand, "cat2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(cat3Operand, 123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(cat1Operand, cat2Operand, cat3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(cat2Operand);
        mockController.setReturnValue(true);

        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral1))
                .andReturn(createMockProjectCategory(1L, "cat1"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral2))
                .andReturn(null);
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral3))
                .andReturn(createMockProjectCategory(3L, "cat3"));

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = projectCategoryValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        verify();
    }

    @Test
    public void testErrorFindingCategoryById() throws Exception
    {
        final SingleValueOperand cat1Operand = new SingleValueOperand("cat1");
        final SingleValueOperand cat2Operand = new SingleValueOperand("cat2");
        final SingleValueOperand cat3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = createLiteral("cat1");
        final QueryLiteral queryLiteral2 = createLiteral("cat2");
        final QueryLiteral queryLiteral3 = createLiteral(123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(cat1Operand, cat2Operand, cat3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral1))
                .andReturn(createMockProjectCategory(1L, "cat1"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral2))
                .andReturn(createMockProjectCategory(2L, "cat2"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral3))
                .andReturn(null);

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = projectCategoryValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value with ID '123' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        verify();
    }

    @Test
    public void testErrorFindingCategoryByIdFromFunction() throws Exception
    {
        final SingleValueOperand cat1Operand = new SingleValueOperand("cat1");
        final SingleValueOperand cat2Operand = new SingleValueOperand("cat2");
        final SingleValueOperand cat3Operand = new SingleValueOperand(123L);

        final QueryLiteral queryLiteral1 = new QueryLiteral(cat1Operand, "cat1");
        final QueryLiteral queryLiteral2 = new QueryLiteral(cat2Operand, "cat2");
        final QueryLiteral queryLiteral3 = new QueryLiteral(cat3Operand, 123L);

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(cat1Operand, cat2Operand, cat3Operand).asList());
        final TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(queryLiteral1, queryLiteral2, queryLiteral3).asList());
        jqlOperandResolver.isFunctionOperand(cat3Operand);
        mockController.setReturnValue(true);

        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral1))
                .andReturn(createMockProjectCategory(1L, "cat1"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral2))
                .andReturn(createMockProjectCategory(2L, "cat2"));
        EasyMock.expect(projectCategoryResolver.getProjectCategory(queryLiteral3))
                .andReturn(null);

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        final MessageSet messageSet = projectCategoryValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());
        verify();
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

        replay();

        final ProjectCategoryValidator projectCategoryValidator = new ProjectCategoryValidator(projectCategoryResolver, jqlOperandResolver)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet foundSet = projectCategoryValidator.validate(theUser, clause);
        assertTrue(foundSet.hasAnyMessages());

        mockController.verify();
    }

    private MockGenericValue createMockProjectCategory(final Long id, final String name)
    {
        return new MockGenericValue("ProjectCategory", ImmutableMap.of("id", id, "name", name));
    }
}
