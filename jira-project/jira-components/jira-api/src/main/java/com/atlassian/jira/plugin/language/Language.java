package com.atlassian.jira.plugin.language;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.plugin.elements.ResourceDescriptor;

import java.util.List;
import java.util.Locale;

/**
 * User: kalamon Date: 12.01.11 Time: 12:40
 * @since 4.3 and better
 */
@PublicSpi
public class Language
{
    private String encoding;
    private Locale locale;
    private String flagUrl;

    public Language(Locale locale)
    {
        this.locale = locale;
    }

    public Language(LanguageModuleDescriptor moduleDescriptor)
    {
        setEncoding(moduleDescriptor.getEncoding());

        String language = moduleDescriptor.getLanguage();
        String country = moduleDescriptor.getCountry();
        String variant = moduleDescriptor.getVariant();

        if (language == null)
            throw new NullPointerException("The language attribute of Language cannot be null");

        if (country == null)
            country = "";

        if (variant == null)
            variant = "";

        // Create a new Locale object
        locale = new Locale(language, country, variant);

        List resources = moduleDescriptor.getResourceDescriptors("download");
        if (resources.size() > 0)
        {
            // Grab the first resource (there should only be one)
            ResourceDescriptor descriptor = (ResourceDescriptor) resources.get(0);

            setFlagUrl("/download/resources/" + moduleDescriptor.getCompleteKey() + "/" + descriptor.getName());
        }
    }

    /**
     * Returns the name of the Language
     *
     * @return The output of locale.toString()
     * @see Locale#toString()
     */
    public String getName()
    {
        return getLocale().toString();
    }

    /**
     * Returns the name of the Language in its locale form
     *
     * @return The output of locale.getDisplayLanguage(locale)
     * @see Locale#getDisplayLanguage()
     */
    public String getDisplayLanguage()
    {
        return getLocale().getDisplayLanguage(getLocale());
    }

    /**
     * Returns the name for the Locale that is appropriate to display to the user
     *
     * @return The output of locale.getDisplayName(locale)
     * @see Locale#getDisplayName()
     */
    public String getDisplayName()
    {
        return getLocale().getDisplayName(getLocale());
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public void setLanguage(String language)
    {
        // Grab the old locale
        Locale oldLocale = getLocale();

        // Create a new one
        Locale newLocale = new Locale(language, oldLocale.getCountry(), oldLocale.getVariant());
        setLocale(newLocale);
    }

    public String getLanguage()
    {
        return locale.getLanguage();
    }

    public void setCountry(String country)
    {
        // Grab the old locale
        Locale oldLocale = getLocale();

        // Create a new one
        Locale newLocale = new Locale(oldLocale.getLanguage(), country, oldLocale.getVariant());
        setLocale(newLocale);
    }

    public String getCountry()
    {
        return locale.getCountry();
    }

    public void setVariant(String variant)
    {
        // Grab the old locale
        Locale oldLocale = getLocale();

        // Create a new one
        Locale newLocale = new Locale(oldLocale.getLanguage(), oldLocale.getCountry(), variant);
        setLocale(newLocale);
    }

    public String setVariant()
    {
        return locale.getVariant();
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Returns the URL of the flag for this language
     *
     * Note: This URL does not contain the context path or base URL of the Confluence installation
     *
     * @return String representing the URL
     */
    public String getFlagUrl()
    {
        return flagUrl;
    }

    /**
     * Sets the URL of the flag for this language
     *
     * Note: This URL should not contain the context path or base URL of the Confluence installation
     *
     * @param flagUrl
     */
    public void setFlagUrl(String flagUrl)
    {
        this.flagUrl = flagUrl;
    }
}
