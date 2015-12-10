package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.I18nHelper;

/**
 * The Affected Version clause validator.
 *
 * @since v4.0
 */
public class UserCustomFieldValidator extends AbstractUserValidator
{
    ///CLOVER:OFF
    public UserCustomFieldValidator(final UserResolver userResolver, final JqlOperandResolver operandResolver, I18nHelper.BeanFactory beanFactory)
    {
        super(userResolver, operandResolver, beanFactory);
    }
    ///CLOVER:ON
}
