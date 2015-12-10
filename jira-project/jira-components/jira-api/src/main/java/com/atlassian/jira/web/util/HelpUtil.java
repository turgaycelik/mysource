package com.atlassian.jira.web.util;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.HelpUrlsParser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Utility bean class for building links to JIRA help pages. This bean is now a fly-weight that delegates
 * most of its functionality off to {@link com.atlassian.jira.help.HelpUrls}.
 *
 * @deprecated since v6.2.4: The use of this class is discouraged. It now suggested to:
 * <ul>
 *  <li>
 *     Use an injected {@code HelpUrls} when you would have called {@link HelpUtil#getInstance()} or
 *     {@link HelpUtil#HelpUtil()}. It contains all of JIRA's help URLs and will adapt itself internally for the current
 *     user.
 *  </li>
 *  <li>
 *     Use an injected {@link com.atlassian.jira.help.HelpUrlsParser} when you would have called
 *     {@link HelpUtil#HelpUtil(java.util.Properties)}. {@link com.atlassian.jira.help.HelpUrlsParser#parse(java.util.Properties)}
 *     can be used to generate to generate a {@code HelpUrls} from a plugin provided {@code Properties} instance.
 *  </li>
 * </ul>
 */
@Deprecated
public class HelpUtil
{
    private static final Logger LOG = LoggerFactory.getLogger(HelpUtil.class);

    /**
     * Returns a {@code HelpUtil} for the current user.
     *
     * @return a {@code HelpUtil} instance for the current user.
     * @deprecated since v6.2.4: Inject an instance of {@link com.atlassian.jira.help.HelpUrls} to access JIRA's
     * help.
     */
    @Deprecated
    public static HelpUtil getInstance()
    {
        return new HelpUtil();
    }

    private final HelpUrls urls;

    /**
     * Returns a {@code HelpUtil} for JIRA's help URLs.
     *
     * @deprecated since v6.2.4: Inject an instance of {@link com.atlassian.jira.help.HelpUrls} to access JIRA's
     * help.
     */
    @Deprecated
    public HelpUtil()
    {
        urls = ComponentAccessor.getComponent(HelpUrls.class);
    }

    /**
     * Return a {@code HelpUtil} representation of the passed properties. See {@link com.atlassian.jira.help.HelpUrlsParser}
     * for information of the format of the passed properties.
     *
     * @param properties properties.
     * @deprecated since v6.2.4: Inject a {@link com.atlassian.jira.help.HelpUrlsParser} and use its
     * {@link com.atlassian.jira.help.HelpUrlsParser#parse(java.util.Properties)} method.
     */
    @Deprecated
    public HelpUtil(Properties properties)
    {
        urls = parseExternalProperties(properties);
    }

    /**
     * This method is really only useful internally to JIRA.
     *
     * @deprecated since v6.2.4: Inject a {@link com.atlassian.jira.help.HelpUrlsParser} and use its
     * {@link com.atlassian.jira.help.HelpUrlsParser#parse(java.util.Properties, java.util.Properties)} method.
     */
    @Deprecated
    public HelpUtil(String external, String internal)
    {
        urls = parseProperties(external, internal);
    }

    /**
     * Returns the {@link HelpUtil.HelpPath} object for a given key.
     *
     * @param helpPathKey the key in play
     * @return the {@link HelpUtil.HelpPath} for that key or defaultHelpPath if it cant be
     *         found.
     */
    public HelpPath getHelpPath(String helpPathKey)
    {
        final HelpUrl url = urls.getUrl(helpPathKey);

        // return a new HelpPath (this guy is mutable...)
        return new HelpPath(url.getUrl(), url.getAlt(), url.getTitle(), url.getKey(), url.isLocal());
    }

    /**
     * Returns the {@link HelpUtil.HelpPath} object for a given key.
     *
     * @param helpPathKey the key in play
     * @param ignored now ignored.
     *
     * @since 5.2
     * @deprecated since v6.2.4: Call {@link #getHelpPath(String)} instead.
     */
    @Deprecated
    public HelpPath getHelpPath(String helpPathKey, boolean ignored)
    {
        return getHelpPath(helpPathKey);
    }

    /**
     * @return a set of all the keys that have {@link HelpUtil.HelpPath} values
     */
    public Set<String> keySet()
    {
        return urls.getUrlKeys();
    }

    private static HelpUrls parseExternalProperties(final Properties properties)
    {
        HelpUrlsParser parser = ComponentAccessor.getComponent(HelpUrlsParser.class);
        return parser.parse(properties == null ? new Properties() : properties);
    }

    private static HelpUrls parseProperties(final String external, final String internal)
    {
        HelpUrlsParser parser = ComponentAccessor.getComponent(HelpUrlsParser.class);
        return parser.parse(loadProperties(external), loadProperties(internal));
    }

    private static Properties loadProperties(String propertiesFileLocation)
    {
        Properties properties = new Properties();
        if (propertiesFileLocation != null)
        {
            InputStream is = ClassLoaderUtils.getResourceAsStream(propertiesFileLocation, HelpUtil.class);
            if (is != null)
            {
                try
                {
                    properties.load(is);
                }
                catch (IOException e)
                {
                    LOG.debug("Error loading helpfile " + propertiesFileLocation, e);
                }
                finally
                {
                    IOUtils.closeQuietly(is);
                }
            }
        }

        return properties;
    }

    public class HelpPath implements Cloneable, Comparable<HelpPath>
    {
        String url;
        String alt;
        String title;
        String key;
        Boolean local;

        public HelpPath()
        {
        }

        HelpPath(String url, String alt, String title, String key, Boolean local)
        {
            this.url = url;
            this.alt = alt;
            this.title = title;
            this.key = key;
            this.local = local;
        }

        public String getUrl()
        {
            return url;
        }

        /**
         * This now an alias to {@link #getUrl()}.
         *
         * @return {@link #getUrl()}
         * @deprecated since 6.2.4 use {@link #getUrl()} instead.
         */
        @Deprecated
        public String getSimpleUrl()
        {
            return url;
        }

        public String getAlt()
        {
            return alt;
        }

        public String getTitle()
        {
            return title;
        }

        public String getKey()
        {
            return key;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public void setAlt(String alt)
        {
            this.alt = alt;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public Boolean isLocal()
        {
            return local;
        }

        /* Velocity doesn't understand $helpPath.local unless we have this. */
        public Boolean getLocal()
        {
            return local;
        }

        public void setLocal(Boolean local)
        {
            this.local = local;
        }

        @Override
        public int compareTo(HelpPath helpPath)
        {
            return key.compareTo(helpPath.key);
        }

        @Override
        @SuppressWarnings ("CloneDoesntDeclareCloneNotSupportedException")
        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                throw new UnsupportedOperationException(e);
            }
        }

        public String toString()
        {
            return new ToStringBuilder(this).append("url", url).append("title", title).append("key", key).append("alt", alt).toString();
        }

        public String toJson()
        {
            JSONObject object = new JSONObject();

            try
            {
                object.put("alt", getAlt());
                object.put("key", getKey());
                object.put("local", getLocal());
                object.put("title", getTitle());
                object.put("url", getUrl());
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }

            return object.toString();
        }
    }
}
