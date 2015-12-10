package com.atlassian.jira.plugin.rpc;

import com.atlassian.jira.plugin.AbstractConfigurableModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import org.dom4j.Element;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

/**
 * @deprecated in JIRA 6.0, REST is the future :)
 */
@Deprecated
public abstract class RpcModuleDescriptor extends AbstractConfigurableModuleDescriptor<Void>
{
    private String servicePath;

    public RpcModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern) {
        super.provideValidationRules(pattern);
        pattern.rule(test("@class").withError("The 'class' attribute is required for RPC module descriptors"));
        pattern.rule(test("service-path").withError("The service-path is required for RPC module descriptors"));
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        servicePath = element.element("service-path").getTextTrim();
    }

    public String getServicePath()
    {
        return servicePath;
    }
}
