package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.I18nHelper;

/**
 * A Validator for the Creator field clauses
 *
 * @since v6.2
 */
public class CreatorValidator extends AbstractUserValidator implements ClauseValidator
{
    public CreatorValidator(UserResolver userResolver, JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        super(userResolver, operandResolver, beanFactory);
    }
}