package com.atlassian.jira.plugin.rpc;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import org.dom4j.Element;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

/**
 * The RPC plugin allows end users to write RPC services for JIRA to expose.
 * @deprecated in JIRA 6.0, REST is the future :)
 */
@Deprecated
public class SoapModuleDescriptor extends RpcModuleDescriptor
{
    private Class<?> publishedInterface;
    private String interfaceClassName;

    public SoapModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern) {
        super.provideValidationRules(pattern);
        pattern.rule(test("published-interface").withError("The published-interface element is required for SOAP module descriptors"));
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        // Find the configured "published-interface". We cannot load the class yet because the OSGi bundle is not yet created.
        interfaceClassName = element.element("published-interface").getTextTrim();
    }

    @Override
    public void enabled()
    {
        try
        {
            publishedInterface = plugin.loadClass(interfaceClassName, getClass());
        }
        catch (final ClassNotFoundException ex)
        {
            throw new PluginException("Could not load published interface '" + interfaceClassName + "'.", ex);
        }
        super.enabled();
    }

    @Override
    public void disabled()
    {
        super.disabled();
        publishedInterface = null;
    }

    public Class<?> getPublishedInterface()
    {
        // Check if we are enabled.
        if (publishedInterface == null)
        {
            throw new IllegalStateException("The published interface '" + interfaceClassName +
                                "' is not currently loaded. This means the plugin has not been successfully enabled.");
        }
        return publishedInterface;
    }
}
