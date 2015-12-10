package com.atlassian.jira.web.action.util;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Default implementation that reads mappings from calendar-language-util.properties file.
 */
public final class CalendarLanguageUtilImpl implements CalendarLanguageUtil
{
    private static final Logger log = Logger.getLogger(CalendarLanguageUtilImpl.class);
    private static final String PROPS_FILENAME = "calendar-language-util.properties";

    private static final CalendarLanguageUtil instance = new CalendarLanguageUtilImpl();

    public static CalendarLanguageUtil getInstance()
    {
        return instance;
    }

    private final Map<String, String> mappings;

    public CalendarLanguageUtilImpl()
    {
        mappings = load(PROPS_FILENAME);
    }

    /**
     * Determines if a translation for a language exists
     *
     * @param language language
     * @return true if exists, false otherwise or if given language was null
     */
    public boolean hasTranslationForLanguage(final String language)
    {
        return (language != null) && mappings.containsKey(language);
    }

    /**
     * Returns a filename of the calendar file for a given language.
     *
     * @param language language
     * @return a filename if a translation file exists, null otherwise or if given language was null
     */
    public String getCalendarFilenameForLanguage(final String language)
    {
        return (language == null) || !mappings.containsKey(language) ? null : mappings.get(language);
    }

    static Map<String, String> load(final String name)
    {
        // initialize the properties from file
        final Properties mappings = new Properties();
        InputStream is = null;
        try
        {
            is = ClassLoaderUtils.getResourceAsStream(name, CalendarLanguageUtilImpl.class);
            mappings.load(is);
        }
        catch (final IOException e)
        {
            log.warn("Unable to load the calendar-language-util.properties file");
        }
        finally
        {
            closeQuietly(is);
        }

        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        // Java6:
        // for (final String key : mappings.stringPropertyNames())
        // Java5:
        for (final String key : CollectionUtil.transform(mappings.keySet(), Functions.<Object, String> downcast(String.class)))
        {
            builder.put(key, mappings.getProperty(key));
        }
        return builder.build();
    }
}
