/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.core.util.LocaleUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import org.apache.commons.jelly.util.NestedRuntimeException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @deprecated Should use the {@link com.atlassian.jira.config.LocaleManager} interface or {@link
 *             com.atlassian.jira.util.LocaleParser} instead.
 */
public class JiraLocaleUtils extends LocaleUtils
{
    public static final String DEFAULT_LOCALE_ID = "-1";
    public static final String DEFAULT_LOCALE_I18N_KEY = "admin.common.phrases.default.locale";

    @SuppressWarnings ( { "deprecation" })
    private static final Logger log = Logger.getLogger(JiraLocaleUtils.class);
    private static final BuildUtilsInfo BUILD_UTILS_INFO = new BuildUtilsInfoImpl();

    private List<Locale> availableLocales = null;

    public JiraLocaleUtils()
    {
        // initialisation of availableLocales is now performed lazily. this alone speeds up unit test exec by 50%
    }

    /**
     * Returns a lazily-instantiated, unmodifiable List containing the installed locales.
     *
     * @return an unmodifiable List of Locale
     * @deprecated Use {@link com.atlassian.jira.config.LocaleManager#getInstalledLocales()} ) instead. Since v4.2
     */
    @ClusterSafe("Local. Locales are essentially program artefacts.")
    public synchronized List<Locale> getInstalledLocales()
    {
        if (availableLocales == null)
        {
            availableLocales = calculateAvailableLocales(Locale.getDefault());
        }
        return availableLocales;
    }


    @ClusterSafe("Local. Locales are essentially program artefacts.")
    public synchronized void resetInstalledLocales()
    {
        availableLocales = null;
    }

    /**
     * Retrieve the installed locales for the UI. Add 'Default' so that the JVM default can be used.
     *
     * The display name for each locale is written in that same locale to
     * ensure that users are able to identify their own language.
     *
     * @param defaultLocale default locale
     * @param helper i18n helper
     * @return a map of locale (as String) to locale display name
     * @deprecated Use {@link com.atlassian.jira.config.LocaleManager#getInstalledLocalesWithDefault(java.util.Locale,
     *             com.atlassian.jira.util.I18nHelper)} ) instead. Since v4.2
     */
    public Map<String, String> getInstalledLocalesWithDefault(Locale defaultLocale, I18nHelper helper)
    {

        Locale usersLocale = helper.getLocale();
        Map<String, String> locales = new LinkedHashMap<String, String>();
        // Add none to the map
        final String label = helper.getText(DEFAULT_LOCALE_I18N_KEY, defaultLocale.getDisplayName(defaultLocale));
        locales.put(DEFAULT_LOCALE_ID, label);

        final List<Locale> installedLocales = calculateAvailableLocales(usersLocale);
        for (Locale installedLocale : installedLocales)
        {
            if (!installedLocale.equals(defaultLocale))
            {
                // Get the locale's display name in that same locale. This
                // ensures that users will be able to identify their language.
                locales.put(installedLocale.toString(), installedLocale.getDisplayName(installedLocale));
            }
        }

        return ImmutableMap.copyOf(locales);
    }

    /**
     * Creates a locale from the given string.  Similar to LocaleUtils, but this one is static
     *
     * @param locale locale
     * @return new locale based on the parameter, or null if parameter not set
     * @deprecated Use {@link com.atlassian.jira.util.LocaleParser#parseLocale) instead. Since v4.2
     */
    public static Locale parseLocale(String locale)
    {
        return LocaleParser.parseLocale(locale);
    }

    protected List<Locale> calculateAvailableLocales(Locale userLocale)
    {
        Map<String, Locale> locales = new HashMap<String, Locale>();
        List<Locale> localeList;
        try
        {
            //noinspection unchecked
            localeList = new ArrayList<Locale>(super.getInstalledLocales());
            for (Locale locale : localeList)
            {
                locales.put(locale.toString(), locale);
            }
        }
        catch (IOException e)
        {
            throw new NestedRuntimeException(e);
        }
        Collection<Locale> unavailableLocales = BUILD_UTILS_INFO.getUnavailableLocales();

        List<LanguageModuleDescriptor> descriptors = ComponentAccessor.getPluginAccessor()
                .getEnabledModuleDescriptorsByClass(LanguageModuleDescriptor.class);

        for (LanguageModuleDescriptor descriptor : descriptors)
        {
            Locale loc = descriptor.getModule().getLocale();
            locales.put(loc.toString(), loc);
        }

        localeList = new ArrayList<Locale>(locales.values());

        for (Locale unavailableLocale : unavailableLocales)
        {
            if (localeList.contains(unavailableLocale))
            {
                log.warn("The '" + unavailableLocale.getDisplayName()
                        + "' locale is not permitted to run in this build of JIRA. Please contact Atlassian for further details.");
                localeList.remove(unavailableLocale);
            }
        }

        Collections.sort(localeList, new LocaleComparator(userLocale));
        return ImmutableList.copyOf(localeList);
    }

    private static class LocaleComparator implements Comparator<Locale>
    {
        private final Locale usersLocale;
        private Collator collator;

        public LocaleComparator(Locale usersLocale)
        {
            this.usersLocale = usersLocale;
            collator = Collator.getInstance(usersLocale);
        }

        @Override
        public int compare(Locale l1, Locale l2)
        {
            final String displayName1 = l1.getDisplayName(usersLocale);
            final String displayName2 = l2.getDisplayName(usersLocale);

            if (displayName1 == null)
            {
                return -1;
            }
            else if (displayName2 == null)
            {
                return 1;
            }
            return collator.compare(displayName1,displayName2);
        }
    }


}
