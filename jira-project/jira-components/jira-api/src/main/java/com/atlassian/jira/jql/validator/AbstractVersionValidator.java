package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * Abstract version clause validator that pretty much does all the work for version validation.
 *
 * @since v4.0
 */
abstract class AbstractVersionValidator implements ClauseValidator
{
    ///CLOVER:OFF

    private final ValuesExistValidator versionValuesExistValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    AbstractVersionValidator(final VersionResolver versionResolver, final JqlOperandResolver operandResolver, PermissionManager permissionManager, VersionManager versionManager, I18nHelper.BeanFactory beanFactory)
    {
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.versionValuesExistValidator = new VersionValuesExistValidator(operandResolver, new VersionIndexInfoResolver(versionResolver), permissionManager, versionManager, beanFactory);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            errors = versionValuesExistValidator.validate(searcher, terminalClause);
        }
        return errors;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }

    ///CLOVER:ON
}
