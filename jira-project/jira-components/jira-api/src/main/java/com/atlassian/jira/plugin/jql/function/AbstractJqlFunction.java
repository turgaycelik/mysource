package com.atlassian.jira.plugin.jql.function;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.operand.FunctionOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A useful base implementation of the {@link com.atlassian.jira.plugin.jql.function.JqlFunction} interface, that
 * provides sensible default behaviour for the {@link #init(JqlFunctionModuleDescriptor)}, {@link #getFunctionName()}
 * and {@link #isList()} methods. You should not need to override these methods in your implementation.
 *
 * @see com.atlassian.jira.plugin.jql.function.JqlFunction
 * @since 4.0
 */
@PublicSpi
public abstract class AbstractJqlFunction implements JqlFunction
{
    private volatile JqlFunctionModuleDescriptor moduleDescriptor;

    public void init(final JqlFunctionModuleDescriptor moduleDescriptor)
    {
        this.moduleDescriptor = notNull("moduleDescriptor", moduleDescriptor);
    }

    public JqlFunctionModuleDescriptor getModuleDescriptor()
    {
        return moduleDescriptor;
    }

    protected MessageSet validateNumberOfArgs(FunctionOperand operand, int expected)
    {
        return new NumberOfArgumentsValidator(expected, getI18n()).validate(operand);
    }

    public String getFunctionName()
    {
        return moduleDescriptor.getFunctionName();
    }

    public boolean isList()
    {
        return moduleDescriptor.isList();
    }

    ///CLOVER:OFF
    protected I18nHelper getI18n()
    {
        return moduleDescriptor.getI18nBean();
    }
    ///CLOVER:ON
}
