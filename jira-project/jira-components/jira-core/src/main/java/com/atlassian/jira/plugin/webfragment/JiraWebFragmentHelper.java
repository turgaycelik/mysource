package com.atlassian.jira.plugin.webfragment;

import com.atlassian.jira.plugin.PluginInjector;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.fragment;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class JiraWebFragmentHelper implements WebFragmentHelper
{
    private static final Logger log = Logger.getLogger(JiraWebFragmentHelper.class);

    private final VelocityTemplatingEngine templatingEngine;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraWebFragmentHelper(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext authenticationContext,
            final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.templatingEngine = templatingEngine;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    public Condition loadCondition(final String className, final Plugin plugin) throws ConditionLoadingException
    {
        try
        {
            final Class<Condition> conditionClass = plugin.loadClass(className, getClass());
            return PluginInjector.newInstance(conditionClass, plugin);
        }
        catch (Exception e)
        {
            throw new ConditionLoadingException("Could not load '" + className + "' in plugin " + plugin, e);
        }
    }

    public ContextProvider loadContextProvider(final String className, Plugin plugin) throws ConditionLoadingException
    {
        try
        {
            final Class<ContextProvider> providerClass = plugin.loadClass(className, getClass());
            if (plugin instanceof AbstractDelegatingPlugin)
            {
                plugin = ((AbstractDelegatingPlugin) plugin).getDelegate();
            }
            final ContextProvider contextProvider = PluginInjector.newInstance(providerClass, plugin);

            // If we are cacheable, wrap with the caching decorator
            if (contextProvider instanceof CacheableContextProvider)
            {
                return new CacheableContextProviderDecorator((CacheableContextProvider) contextProvider);
            }
            else
            {
                return contextProvider;
            }
        }
        catch (Exception e)
        {
            throw new ConditionLoadingException("Could not load '" + className + "' in plugin " + plugin, e);
        }
    }

    public String getI18nValue(final String key, final List arguments, final Map context)
    {
        final Object i18nObject = context.get(JiraWebInterfaceManager.CONTEXT_KEY_I18N);
        if (i18nObject != null)
        {
            try
            {
                return ((I18nHelper) i18nObject).getText(key, arguments);
            }
            catch (ClassCastException e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Expected an instance of I18nHelper in the context under key: '"
                            + JiraWebInterfaceManager.CONTEXT_KEY_I18N + "' but it was " + i18nObject.getClass(), e);
                }
            }
        }
        return authenticationContext.getI18nHelper().getText(key, arguments);
    }

    public String renderVelocityFragment(final String fragment, final Map<String, Object> context)
    {
        if (!needToRender(fragment))
        {
            return fragment;
        }

        return getPlainText(fragment, getDefaultParams(context));
    }

    String getPlainText(final String fragment, final Map<String, Object> startingParams)
    {
        try
        {
            if (isNotBlank(fragment))
            {
                return templatingEngine.render(fragment(fragment)).applying(startingParams).asPlainText();
            }
        }
        catch (VelocityException e)
        {
            log.error("Error while rendering velocity fragment: '" + fragment + "'.", e);
        }

        return "";
    }

    private Map<String, Object> getDefaultParams(final Map<String, Object> startingParams)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
    }

    private boolean needToRender(final String velocity)
    {
        return (isNotBlank(velocity) && (velocity.contains("$") || velocity.contains("#")));
    }

    /**
     * Returns the base URL from VelocityRequestContext
     *
     * @return the base URL
     * @see com.atlassian.jira.util.velocity.VelocityRequestContext
     */
    private String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

}
