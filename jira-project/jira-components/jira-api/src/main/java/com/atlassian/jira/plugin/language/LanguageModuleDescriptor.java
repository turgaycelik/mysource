package com.atlassian.jira.plugin.language;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

/**
 * Module descriptor of the language pack plugin point.
 *
 * @since 4.3 and better
 */
public interface LanguageModuleDescriptor extends JiraResourcedModuleDescriptor<Language>
{
    public String getResourceBundleName();

    public String getEncoding();

    public void setEncoding(String encoding);

    public String getLanguage();

    public void setLanguage(String language);

    public String getCountry();

    public void setCountry(String country);

    public String getVariant();

    public void setVariant(String variant);
}
