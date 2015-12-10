package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestAbstractUserValidator extends MockControllerTestCase
{
    @Test
    public void testRawValuesDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final DataValuesExistValidator dataValuesExistValidator = mockController.getMock(DataValuesExistValidator.class);
        mockController.replay();

        final MockControl mockSupportedOperatorsValidatorControl = MockClassControl.createControl(SupportedOperatorsValidator.class);
        final SupportedOperatorsValidator mockSupportedOperatorsValidator = (SupportedOperatorsValidator) mockSupportedOperatorsValidatorControl.getMock();
        mockSupportedOperatorsValidator.validate(null, clause);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");
        mockSupportedOperatorsValidatorControl.setReturnValue(messageSet);
        mockSupportedOperatorsValidatorControl.replay();


        final AbstractUserValidator userValidator = new MockUserValidator(null, jqlOperandResolver)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            DataValuesExistValidator getDataValuesValidator(final UserResolver userResolver, final JqlOperandResolver operandSupport, final I18nHelper.BeanFactory beanFactory)
            {
                return dataValuesExistValidator;
            }
        };

        userValidator.validate(null, clause);

        mockSupportedOperatorsValidatorControl.verify();
        mockController.verify();
    }

    @Test
    public void testRawValuesDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final DataValuesExistValidator dataValuesExistValidator = mockController.getMock(DataValuesExistValidator.class);
        dataValuesExistValidator.validate(null, clause);
        mockController.setReturnValue(new MessageSetImpl());
        mockController.replay();

        final MockControl mockSupportedOperatorsValidatorControl = MockClassControl.createControl(SupportedOperatorsValidator.class);
        final SupportedOperatorsValidator mockSupportedOperatorsValidator = (SupportedOperatorsValidator) mockSupportedOperatorsValidatorControl.getMock();
        mockSupportedOperatorsValidator.validate(null, clause);
        mockSupportedOperatorsValidatorControl.setReturnValue(new MessageSetImpl());
        mockSupportedOperatorsValidatorControl.replay();

        final AbstractUserValidator userValidator = new MockUserValidator(null, jqlOperandResolver)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            DataValuesExistValidator getDataValuesValidator(final UserResolver userResolver, final JqlOperandResolver operandSupport, final I18nHelper.BeanFactory beanFactory)
            {
                return dataValuesExistValidator;
            }
        };

        userValidator.validate(null, clause);

        mockSupportedOperatorsValidatorControl.verify();
        mockController.verify();
    }

    class MockUserValidator extends AbstractUserValidator
    {
        public MockUserValidator(UserResolver userResolver, JqlOperandResolver operandResolver)
        {
            super(userResolver, operandResolver, null);
        }
    }
}
