package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestProjectValidator extends MockControllerTestCase
{
    @Test
    public void testRawValuesDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final MockControl mockSupportedOperatorsValidatorControl = MockClassControl.createControl(SupportedOperatorsValidator.class);
        final SupportedOperatorsValidator mockSupportedOperatorsValidator = (SupportedOperatorsValidator) mockSupportedOperatorsValidatorControl.getMock();
        mockSupportedOperatorsValidator.validate(null, clause);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");
        mockSupportedOperatorsValidatorControl.setReturnValue(messageSet);
        mockSupportedOperatorsValidatorControl.replay();

        final MockControl mockRawValuesExistValidatorControl = MockClassControl.createControl(RawValuesExistValidator.class);
        final RawValuesExistValidator mockRawValuesExistValidator = (RawValuesExistValidator) mockRawValuesExistValidatorControl.getMock();
        mockRawValuesExistValidatorControl.replay();

        final ProjectValidator projectValidator = new ProjectValidator(null, null, null, null, null)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            ValuesExistValidator getValuesValidator(final ProjectResolver projectResolver, final JqlOperandResolver operandSupport, final PermissionManager permissionManager, final ProjectManager projectManager)
            {
                return mockRawValuesExistValidator;
            }
        };

        projectValidator.validate(null, clause);

        mockSupportedOperatorsValidatorControl.verify();
        mockRawValuesExistValidatorControl.verify();
    }

    @Test
    public void testRawValuesDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final MockControl mockSupportedOperatorsValidatorControl = MockClassControl.createControl(SupportedOperatorsValidator.class);
        final SupportedOperatorsValidator mockSupportedOperatorsValidator = (SupportedOperatorsValidator) mockSupportedOperatorsValidatorControl.getMock();
        mockSupportedOperatorsValidator.validate(null, clause);
        mockSupportedOperatorsValidatorControl.setReturnValue(new MessageSetImpl());
        mockSupportedOperatorsValidatorControl.replay();

        final MockControl mockRawValuesExistValidatorControl = MockClassControl.createControl(RawValuesExistValidator.class);
        final RawValuesExistValidator mockRawValuesExistValidator = (RawValuesExistValidator) mockRawValuesExistValidatorControl.getMock();
        mockRawValuesExistValidator.validate(null, clause);
        mockRawValuesExistValidatorControl.setReturnValue(new MessageSetImpl());
        mockRawValuesExistValidatorControl.replay();

        final ProjectValidator projectValidator = new ProjectValidator(null, null, null, null, null)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mockSupportedOperatorsValidator;
            }

            @Override
            ValuesExistValidator getValuesValidator(final ProjectResolver projectResolver, final JqlOperandResolver operandSupport, final PermissionManager permissionManager, final ProjectManager projectManager)
            {
                return mockRawValuesExistValidator;
            }
        };

        projectValidator.validate(null, clause);

        mockSupportedOperatorsValidatorControl.verify();
        mockRawValuesExistValidatorControl.verify();
    }

}
