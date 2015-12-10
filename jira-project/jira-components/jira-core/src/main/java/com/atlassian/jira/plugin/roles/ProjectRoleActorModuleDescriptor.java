package com.atlassian.jira.plugin.roles;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * Descriptor that defines a ProjectRoleActorModuleDescriptor.
 * <p>
 * Note the the module class MUST be an implementation of {@link com.atlassian.jira.security.roles.RoleActorFactory}
 */
public class ProjectRoleActorModuleDescriptor extends AbstractJiraModuleDescriptor<RoleActorFactory>
{
    // -------------------------------------------------------------------------------------------------- static members

    public static final String TEMPLATE_NAME_CONFIGURE = "configure";

    private static final String ROLEACTOR_TYPE = "roleactor.type";

    private static final String PRETTYNAME_KEY = "prettyname.key";

    // ----------------------------------------------------------------------------------------------------------- ctors

    public ProjectRoleActorModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    // --------------------------------------------------------------------------------------------------------- methods

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        assertConfigurationParameterPresent(PRETTYNAME_KEY);
        assertConfigurationParameterPresent(ROLEACTOR_TYPE);
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(RoleActorFactory.class);
    }

    public boolean isRoleActorConfigurationTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_CONFIGURE);
    }

    public String getType()
    {
        return (String) getParams().get(ROLEACTOR_TYPE);
    }

    public String getConfigurationUrl()
    {
        return (String) getParams().get("ConfigurationURL");
    }

    public String getPrettyName()
    {
        return getText((String) getParams().get(PRETTYNAME_KEY), null);
    }

    private void assertConfigurationParameterPresent(final String paramKey) throws PluginParseException
    {
        if (getParams().get(paramKey) == null)
        {
            throw new PluginParseException(this.getClass() + " plugins must be configured with a '" + paramKey + "' parameter");
        }
    }
}
