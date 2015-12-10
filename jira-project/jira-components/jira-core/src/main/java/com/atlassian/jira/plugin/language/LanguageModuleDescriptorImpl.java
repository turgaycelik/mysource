package com.atlassian.jira.plugin.language;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.util.TextUtils;
import org.dom4j.Element;

/**
 * Module descriptor of the language pack plugin point.
 *
 * @since 4.3 and better
 */
public class LanguageModuleDescriptorImpl extends AbstractJiraModuleDescriptor<Language> implements LanguageModuleDescriptor
{
    private String encoding;
    private String language;
    private String country;
    private String variant;


    public LanguageModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    /**
     * Initialises the LanguageModuleDescriptor
     *
     * @param plugin The Plugin the ModuleDescriptor belongs to
     * @param element The XML Document to be parsed
     * @throws com.atlassian.plugin.PluginParseException if language is null
     */
    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        // Language is necessary
        if (element.attribute("language") == null || !TextUtils.stringSet(element.attribute("language").getValue()))
        {
            throw new PluginParseException("Module " + getCompleteKey() + " must define a \"language\" attribute");
        }

        // Grab the language
        if (element.attribute("language") != null)
        {
            setLanguage(element.attribute("language").getText());
        }

        // Grab the country
        if (element.attribute("country") != null)
        {
            setCountry(element.attribute("country").getText());
        }

        // Grab the variant
        if (element.attribute("variant") != null)
        {
            setVariant(element.attribute("variant").getText());
        }

        // Grab the encoding
        if (element.attribute("encoding") != null)
        {
            setEncoding(element.attribute("encoding").getText());
        }

    }

    @Override
    protected Language createModule()
    {
        return new Language(LanguageModuleDescriptorImpl.this);
    }

    public String getResourceBundleName()
    {
        return DefaultResourceBundle.DEFAULT_RESOURCE_BUNDLE_NAME;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getVariant()
    {
        return variant;
    }

    public void setVariant(String variant)
    {
        this.variant = variant;
    }
}
