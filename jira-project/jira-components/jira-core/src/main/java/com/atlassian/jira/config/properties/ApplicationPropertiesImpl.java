package com.atlassian.jira.config.properties;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.LocaleParser;

import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * A class to manage the interface with a single property set, used for application properties
 */
@EventComponent
public class ApplicationPropertiesImpl implements ApplicationProperties
{
    private static final Logger log = Logger.getLogger(ApplicationPropertiesImpl.class);

    @VisibleForTesting
    public static final String DEFAULT_ENCODING = "UTF-8";

    private final ApplicationPropertiesStore applicationPropertiesStore;
    private final Locale defaultLocale = Locale.getDefault();

    public ApplicationPropertiesImpl(ApplicationPropertiesStore applicationPropertiesStore)
    {
        this.applicationPropertiesStore = applicationPropertiesStore;
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public String getText(final String name)
    {
        return this.applicationPropertiesStore.getTextFromDb(name);
    }

    public void setText(final String name, final String value)
    {
        applicationPropertiesStore.setText(name, value);
    }

    public String getString(final String name)
    {
        return applicationPropertiesStore.getStringFromDb(name);
    }

    public Collection<String> getDefaultKeys()
    {
        return getDefaultProperties().keySet();
    }

    public String getDefaultBackedString(final String name)
    {
        return applicationPropertiesStore.getString(name);
    }

    public String getDefaultBackedText(final String name)
    {
        String value = null;
        try
        {
            value = getText(name);
        }
        catch (final Exception e)
        {
            log.warn("Exception getting property '" + name + "' from database. Using default");
        }
        if (value == null)
        {
            value = getDefaultString(name);
        }
        return value;
    }

    public String getDefaultString(final String name)
    {
        return getDefaultProperties().get(name);
    }

    public void setString(final String name, final String value)
    {
        applicationPropertiesStore.setString(name, value);
    }

    public boolean getOption(final String key)
    {
        return applicationPropertiesStore.getOption(key);
    }

    public Collection<String> getKeys()
    {
        return applicationPropertiesStore.getKeysStoredInDb();
    }

    public Map<String, Object> asMap()
    {
        return applicationPropertiesStore.getPropertiesAsMap();
    }

    public void setOption(final String key, final boolean value)
    {
        applicationPropertiesStore.setOption(key, value);
    }

    public String getEncoding()
    {
        String encoding = getString(APKeys.JIRA_WEBWORK_ENCODING);
        if (!TextUtils.stringSet(encoding))
        {
            encoding = DEFAULT_ENCODING;
            setString(APKeys.JIRA_WEBWORK_ENCODING, encoding);
        }
        return encoding;
    }

    public String getMailEncoding()
    {
        String encoding = getDefaultBackedString(APKeys.JIRA_MAIL_ENCODING);
        if (!TextUtils.stringSet(encoding))
        {
            encoding = getEncoding();

        }
        return encoding;
    }

    public String getContentType()
    {
        return "text/html; charset=" + getEncoding();
    }

    public void refresh()
    {
        applicationPropertiesStore.refreshDbProperties();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("propertiesManager", applicationPropertiesStore).toString();
    }

    private Map<String, String> getDefaultProperties()
    {
        return applicationPropertiesStore.getDefaultsWithOverlays();
    }

    public Locale getDefaultLocale()
    {
        // The default locale almost never changes, but we must handle it correctly when it does.
        // We store the Locale for the defaultLocale string, which is expensive to create, in a very small cache (map).
        final String localeString = getDefaultBackedString(APKeys.JIRA_I18N_DEFAULT_LOCALE);
        if (localeString != null)
        {
            return LocaleParser.parseLocale(localeString);
        }
        return defaultLocale;
    }

    public Collection<String> getStringsWithPrefix(final String prefix)
    {
        return applicationPropertiesStore.getStringsWithPrefixFromDb(prefix);
    }
}
