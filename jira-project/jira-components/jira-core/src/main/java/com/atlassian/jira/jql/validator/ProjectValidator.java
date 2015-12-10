package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * Validates input for project system fields.
 *
 * @since v4.0
 */
public class ProjectValidator implements ClauseValidator
{
    private final ValuesExistValidator projectValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final I18nHelper.BeanFactory beanFactory;

    public ProjectValidator(ProjectResolver projectResolver, JqlOperandResolver operandResolver, PermissionManager permissionManager, ProjectManager projectManager, I18nHelper.BeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
        this.projectValuesExistValidator = getValuesValidator(projectResolver, operandResolver, permissionManager, projectManager);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!messageSet.hasAnyErrors())
        {
            messageSet.addMessageSet(projectValuesExistValidator.validate(searcher, terminalClause));
        }
        return  messageSet;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    ValuesExistValidator getValuesValidator(final ProjectResolver projectResolver, final JqlOperandResolver operandResolver, final PermissionManager permissionManager, final ProjectManager projectManager)
    {
        return new ProjectValuesExistValidator(operandResolver, new ProjectIndexInfoResolver(projectResolver), permissionManager, projectManager, beanFactory);
    }

}
