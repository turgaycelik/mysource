package com.atlassian.jira.web.util;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.collection.EasyList;
import org.apache.log4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * A simple utility class that lets you resolve external links that may need to change, because of partner sites and
 * such.
 */
@Immutable
public class ExternalLinkUtilImpl implements ExternalLinkUtil
{
    protected final Logger log = Logger.getLogger(ExternalLinkUtil.class);

    private static final String PROPERTIES_FILE_LOCATION = "external-links.properties";
    private static final ExternalLinkUtil instance = new ExternalLinkUtilImpl();
    private Properties props;

    public ExternalLinkUtilImpl()
    {
        init(PROPERTIES_FILE_LOCATION);
    }

    public String getPropertiesFilename()
    {
        return PROPERTIES_FILE_LOCATION;
    }

    public ExternalLinkUtilImpl(String propFileLocation)
    {
        init(propFileLocation);
    }

    public static ExternalLinkUtil getInstance()
    {
        return instance;
    }

    public String getProperty(String key)
    {
        if(props.containsKey(key))
        {
            return (String) props.get(key);
        }
        else
        {
            return key;
        }
    }

    public String getProperty(String key, String value1)
    {
        return getProperty(key, EasyList.build(value1));
    }

    public String getProperty(String key, String value1, String value2)
    {
        return getProperty(key, EasyList.build(value1, value2));
    }

    public String getProperty(String key, String value1, String value2, String value3) {
        return getProperty(key, EasyList.build(value1, value2, value3));
    }

    public String getProperty(String key, String value1, String value2, String value3, String value4)
    {
        return getProperty(key, EasyList.build(value1, value2, value3, value4));
    }

    public String getProperty(String string, Object parameters)
    {
        Object[] params;
        if (parameters instanceof List)
        {
            params = ((List) parameters).toArray();
        }
        else if (parameters instanceof Object[])
        {
            params = (Object[]) parameters;
        }
        else
        {
            params = new Object[]{parameters};
        }

        String message = getProperty(string);
        MessageFormat mf = new MessageFormat(message);
        return mf.format(params);
    }

    private void init(String propFileLocation)
    {
        // initialize the properties from file
        props = new Properties();
        try
        {
            props.load(ClassLoaderUtils.getResourceAsStream(propFileLocation, ExternalLinkUtilImpl.class));
        }
        catch (IOException e)
        {
            log.warn("Unable to load the " + propFileLocation + " file");
        }
    }
}