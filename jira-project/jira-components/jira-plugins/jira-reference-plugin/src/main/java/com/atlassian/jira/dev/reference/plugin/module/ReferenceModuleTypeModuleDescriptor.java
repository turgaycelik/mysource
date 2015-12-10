package com.atlassian.jira.dev.reference.plugin.module;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Reference module descriptor: each instance will return a string consisting of the key, 'name' and 'description' elements
 * of given module declaration (i18ned if keys provided).
 *
 * @since v4.3
 */
public class ReferenceModuleTypeModuleDescriptor extends AbstractModuleDescriptor<String>
{
    private final JiraAuthenticationContext context;

    public ReferenceModuleTypeModuleDescriptor(JiraAuthenticationContext context, ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        this.context = context;
    }

    @Override
    public String getModule()
    {
        return getCompleteKey() + ": " + name() + ": " + desc();
    }

    private String getI18n(String key)
    {
        return context.getI18nHelper().getText(key);
    }

    private String name()
    {
        if (getI18nNameKey() != null)
        {
            String i18ned = getI18n(getI18nNameKey());
            if (i18ned != null && !i18ned.equals(getI18nNameKey()))
            {
                return i18ned;
            }
        }
        return getName();
    }

    private String desc()
    {
        if (getDescriptionKey() != null)
        {
            String i18ned = getI18n(getDescriptionKey());
            if (i18ned != null && !i18ned.equals(getDescriptionKey()))
            {
                return i18ned;
            }
        }
        return getDescription();
    }

}
