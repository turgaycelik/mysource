package com.atlassian.jira.plugin.permission;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

import org.dom4j.Attribute;
import org.dom4j.Element;

import java.text.MessageFormat;

import javax.annotation.Nonnull;

public class GlobalPermissionModuleDescriptorImpl extends AbstractJiraModuleDescriptor<Void> implements GlobalPermissionModuleDescriptor
{
    private static final String GLOBAL_PERMISSION_ROOT = "global-permission";
    private static final String GLOBAL_PERMISSION_I18N_DESCRIPTION = "i18n-description-key";
    private static final String GLOBAL_PERMISSION_ANONYMOUS_ALLOWED = "anonymous-allowed";

    private final GlobalPermissionTypesManager globalPermissionTypesManager;

    private String descriptionI18nKey;
    private boolean anonymousAllowed;

    public GlobalPermissionModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, ModuleFactory moduleFactory, GlobalPermissionTypesManager globalPermissionTypesManager)
    {
        super(authenticationContext, moduleFactory);
        this.globalPermissionTypesManager = globalPermissionTypesManager;
    }

    @Override
    public void init(@Nonnull final Plugin plugin, @Nonnull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        try
        {
            if (!GLOBAL_PERMISSION_ROOT.equals(element.getName()))
            {
                throw new GlobalPermissionParseException(MessageFormat.format("Root element for configuration should be {0}", GLOBAL_PERMISSION_ROOT));
            }
            descriptionI18nKey = getRequiredAttribute(element, GLOBAL_PERMISSION_ROOT, GLOBAL_PERMISSION_I18N_DESCRIPTION);
            anonymousAllowed = getOptionalBooleanAttribute(element, GLOBAL_PERMISSION_ANONYMOUS_ALLOWED, true);
        }
        catch (GlobalPermissionParseException e)
        {
            throw new PluginParseException("Cannot parse global permission plugin descriptor", e);
        }
    }

    private String getRequiredAttribute(final Element element, final String elementName, final String attributeName) throws GlobalPermissionParseException
    {
        final Attribute entityKeyAttribute = element.attribute(attributeName);
        if (entityKeyAttribute == null)
        {
            throw new GlobalPermissionParseException(MessageFormat.format("Element {0} must have attribute {1} defined", elementName, attributeName));
        }
        return entityKeyAttribute.getValue();
    }

    private boolean getOptionalBooleanAttribute(final Element element, final String attributeName, final boolean defaultValue) throws GlobalPermissionParseException
    {
        final Attribute entityKeyAttribute = element.attribute(attributeName);
        if (entityKeyAttribute != null)
        {
            final String value = entityKeyAttribute.getValue();
            if("true".equals(value) || "false".equals(value))
            {
                return Boolean.valueOf(value);
            }
        }
        return defaultValue;
    }

    @Override
    public String getDescriptionI18nKey() {
        return descriptionI18nKey;
    }

    @Override
    public boolean isAnonymousAllowed() {
        return anonymousAllowed;
    }
}
