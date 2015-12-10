package com.atlassian.jira.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utility to parse locale Strings into Locale objects.
 * <p/>
 * This was previously provided by JiraLocaleUtils, but JiraLocaleUtils was deprecated.
 *
 * @since v4.2
 */
public class LocaleParser
{
    // Cache for default locale objects that are expensive to create an excessive number of times.
    // Note: we tried the google Computing Map but it was too sloww for this use case.  This method can be called hundreds of times per request.
    private static final Map<String, Locale> localeCache = new ConcurrentHashMap<String, Locale>(10, 0.75f, 1);

    /**
     * Creates a locale from the given string.
     *
     * @param localeString locale String
     *
     * @return new locale based on the parameter, or null is the string is null or blank.
     */
    public static Locale parseLocale(String localeString)
    {
        localeString = StringUtils.stripToNull(localeString);
        if (localeString == null)
        {
            return null;
        }

        Locale locale = localeCache.get(localeString);
        if (locale != null)
        {
            return locale;
        }

        // No locale in the map then we need to get the locale, store and return it.
        locale = computeLocale(localeString);
        localeCache.put(localeString, locale);
        return locale;
    }

    private static Locale computeLocale(String localeString)
    {
        int _pos = localeString.indexOf("_");
        Locale locale;
        if (_pos != -1)
        {
            locale = new Locale(localeString.substring(0, _pos), localeString.substring(_pos + 1));
        }
        else
        {
            locale = new Locale(localeString);
        }
        return locale;
    }

}
