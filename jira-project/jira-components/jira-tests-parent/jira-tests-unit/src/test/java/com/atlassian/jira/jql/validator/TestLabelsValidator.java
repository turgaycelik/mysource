package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestLabelsValidator
{

    @Test
    public void testValidate()
    {
        final User user = new MockUser("admin");

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl("labels", Operator.EQUALS, "goodLabel");

        final JqlOperandResolver mockResolver = createMock(JqlOperandResolver.class);
        expect(mockResolver.getValues(user, terminalClause.getOperand(), terminalClause)).
                andReturn(CollectionBuilder.newBuilder(new QueryLiteral(new SingleValueOperand(""), "goodLabel")).asList());

        replay(mockResolver);

        LabelsValidator validator = new LabelsValidator(mockResolver) {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = validator.validate(user, terminalClause);

        assertTrue(messageSet.getErrorMessages().isEmpty());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        verify(mockResolver);
    }
    
    @Test
    public void testValidateLabelTooLong()
    {
        User user = new MockUser("admin");

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl("labels", Operator.EQUALS, "reallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelr");

        final JqlOperandResolver mockResolver = createMock(JqlOperandResolver.class);
        expect(mockResolver.getValues(user, terminalClause.getOperand(), terminalClause)).
                andReturn(CollectionBuilder.newBuilder(new QueryLiteral(new SingleValueOperand(""), "reallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelr")).asList());

        replay(mockResolver);

        LabelsValidator validator = new LabelsValidator(mockResolver) {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = validator.validate(user, terminalClause);

        assertFalse(messageSet.getErrorMessages().isEmpty());        
        assertTrue(messageSet.getWarningMessages().isEmpty());
        verify(mockResolver);
    }

    @Test
    public void testValidateLabelInvalidChars()
    {
        User user = new MockUser("admin");

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl("labels", Operator.EQUALS, "B AD");

        final JqlOperandResolver mockResolver = createMock(JqlOperandResolver.class);
        expect(mockResolver.getValues(user, terminalClause.getOperand(), terminalClause)).
                andReturn(CollectionBuilder.newBuilder(new QueryLiteral(new SingleValueOperand(""), "B AD")).asList());

        replay(mockResolver);

        LabelsValidator validator = new LabelsValidator(mockResolver) {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = validator.validate(user, terminalClause);

        assertFalse(messageSet.getErrorMessages().isEmpty());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        verify(mockResolver);
    }
    
    @Test
    public void testValidateInvalidOperator()
    {
        User user = new MockUser("admin");

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl("labels", Operator.GREATER_THAN, "goodlabel");

        final JqlOperandResolver mockResolver = createMock(JqlOperandResolver.class);

        replay(mockResolver);

        LabelsValidator validator = new LabelsValidator(mockResolver) {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = validator.validate(user, terminalClause);

        assertFalse(messageSet.getErrorMessages().isEmpty());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        verify(mockResolver);
    }
}
