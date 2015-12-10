package com.atlassian.jira.web.action.setup;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.config.properties.APKeys;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * This is a simple utility class that will map a chosen locale to a specified indexing language for Lucene.
 */
public class IndexLanguageToLocaleMapperImpl implements IndexLanguageToLocaleMapper
{
    private static final Logger log = Logger.getLogger(IndexLanguageToLocaleMapperImpl.class);
    private final Properties mappings;

    public IndexLanguageToLocaleMapperImpl()
    {
        // initialize the properties from file
        mappings = new Properties();
        try
        {
            mappings.load(ClassLoaderUtils.getResourceAsStream("index-language-map.properties", IndexLanguageToLocaleMapperImpl.class));
        }
        catch (IOException e)
        {
            log.warn("Unable to load the index-language-map.properties file");
        }
    }

    public String getLanguageForLocale(String locale)
    {
        if(locale != null && mappings.containsKey(locale))
        {
            return mappings.getProperty(locale);
        }
        else
        {
            return APKeys.Languages.ENGLISH_MODERATE_STEMMING;
        }
    }
}
