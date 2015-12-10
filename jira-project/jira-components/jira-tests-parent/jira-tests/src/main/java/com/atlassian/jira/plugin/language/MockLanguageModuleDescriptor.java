package com.atlassian.jira.plugin.language;

import com.atlassian.jira.plugin.MockJiraResourcedModuleDescriptor;
import com.atlassian.plugin.Plugin;

/**
 * @since v6.2.3
 */
public class MockLanguageModuleDescriptor
        extends MockJiraResourcedModuleDescriptor<Language>
        implements LanguageModuleDescriptor
{
    private String encoding;
    private String language;
    private String country;
    private String variant;
    private String bundleName;

    public MockLanguageModuleDescriptor()
    {
        super(Language.class);
    }

    public MockLanguageModuleDescriptor(Plugin plugin, String key)
    {
        super(Language.class, plugin, key);
    }

    @Override
    public String getResourceBundleName()
    {
        return bundleName;
    }

    public void setBundleName(final String bundleName)
    {
        this.bundleName = bundleName;
    }

    @Override
    public String getEncoding()
    {
        return encoding;
    }

    @Override
    public void setEncoding(final String encoding)
    {
        this.encoding = encoding;
    }

    @Override
    public String getLanguage()
    {
        return language;
    }

    @Override
    public void setLanguage(final String language)
    {
        this.language = language;
    }

    @Override
    public String getCountry()
    {
        return country;
    }

    @Override
    public void setCountry(final String country)
    {
        this.country = country;
    }

    @Override
    public String getVariant()
    {
        return variant;
    }

    @Override
    public void setVariant(final String variant)
    {
        this.variant = variant;
    }

    @Override
    public Language getModule()
    {
        return new Language(this);
    }
}
