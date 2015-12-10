package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCommentValidator
{
    @Test
    public void testCommentDoesNotSupportEmpty() throws Exception
    {
        final EmptyOperand emptyOperand = new EmptyOperand();

        CommentValidator commentValidator = new CommentValidator(MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = commentValidator.validate(null, new TerminalClauseImpl("comment", Operator.IS, emptyOperand));
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("The field 'comment' does not support searching for EMPTY values.", messageSet.getErrorMessages().iterator().next());
    }
}
