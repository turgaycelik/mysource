package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ComponentIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * An Validator for the component field clauses
 *
 * @since v4.0
 */
public class ComponentValidator implements ClauseValidator
{
    private final ValuesExistValidator componentValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final I18nHelper.BeanFactory beanFactory;

    public ComponentValidator(ComponentResolver componentResolver, JqlOperandResolver operandResolver, PermissionManager permissionManager,
            ProjectComponentManager projectComponentManager, ProjectManager projectManager, I18nHelper.BeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
        this.componentValuesExistValidator = getValuesValidator(componentResolver, operandResolver, permissionManager, projectComponentManager, projectManager);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = componentValuesExistValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    ValuesExistValidator getValuesValidator(final ComponentResolver componentResolver, final JqlOperandResolver operandResolver, final PermissionManager permissionManager,
            final ProjectComponentManager projectComponentManager, final ProjectManager projectManager)
    {
        return new ComponentValuesExistValidator(operandResolver, new ComponentIndexInfoResolver(componentResolver), permissionManager, projectComponentManager, projectManager, beanFactory);
    }
}
