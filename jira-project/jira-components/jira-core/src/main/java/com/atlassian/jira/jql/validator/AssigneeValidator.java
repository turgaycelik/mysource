package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.I18nHelper;

/**
 * A Validator for the Assignee field clauses
 *
 * @since v4.0
 */
public class AssigneeValidator extends AbstractUserValidator implements ClauseValidator
{
    public AssigneeValidator(UserResolver userResolver, JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        super(userResolver, operandResolver, beanFactory);
    }
}