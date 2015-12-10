package com.atlassian.jira.mock.plugin.jql.operand;

import java.util.Locale;

import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * For unit testing.
 * 
 * @since v4.0
 */
public class MockJqlFunctionModuleDescriptor extends JqlFunctionModuleDescriptorImpl
{
    private String functionName;
    private boolean isList;
    private I18nHelper helper;
    private JqlFunction module;
    private final Plugin plugin;

    private MockJqlFunctionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final String functionName, final boolean isList, final I18nHelper helper, JqlFunction module, Plugin plugin)
    {
        super(authenticationContext, ModuleFactory.LEGACY_MODULE_FACTORY);
        this.functionName = functionName;
        this.isList = isList;
        this.helper = helper;
        this.module = module;
        this.plugin = plugin;
    }

    @Override
    public JqlFunction getModule()
    {
        if (module != null)
        {
            return module;
        }
        else
        {
            return super.getModule();
        }
    }

    @Override
    public Plugin getPlugin()
    {
        if(plugin == null)
        {
            return super.getPlugin();

        }
        else
        {
            return plugin;
        }
    }

    @Override
    public String getPluginKey()
    {
        return super.getPluginKey();
    }

    @Override
    public String getFunctionName()
    {
        return functionName;
    }

    @Override
    public boolean isList()
    {
        return isList;
    }

    @Override
    public I18nHelper getI18nBean()
    {
        return helper;
    }

    /**
     * Creates a {@link MockJqlFunctionModuleDescriptor} with a blank
     * {@link com.atlassian.jira.security.JiraAuthenticationContext} and a simple
     * {@link com.atlassian.jira.jql.validator.MockJqlFunctionHandlerRegistry}.
     *
     * @param functionName the name of the function described.
     * @param isList if this function returns list values.
     * @return the new descriptor.
     */
    public static MockJqlFunctionModuleDescriptor create(String functionName, boolean isList)
    {
        return create(functionName, isList, new MockI18nBean());
    }

    public static MockJqlFunctionModuleDescriptor create(String functionName, boolean isList, I18nHelper helper)
    {
        return create(functionName, isList, helper, null, null);
    }

    public static MockJqlFunctionModuleDescriptor create(final String functionName, final boolean isList,
            final I18nHelper helper, final JqlFunction module, final Plugin plugin)
    {
        return new MockJqlFunctionModuleDescriptor(new MockSimpleAuthenticationContext(null, Locale.getDefault(), helper),
                functionName, isList, helper, module, plugin);
    }
}
