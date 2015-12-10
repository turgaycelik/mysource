package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.plugin.webresource.cdn.CDNStrategy;
import org.apache.commons.lang.StringUtils;

/**
 * CDN Strategy for JIRA that takes a static prefix. This will transform cdn-able resources to be served from
 * a single host, with the app's hostname as the first section of the url. For example, {@code /my/resource.js} will be
 * transformed to be served from {@code //my.cdnhost.com/my.jirahost.com/my/resource.js}
 *
 * <ul>
 *     <li>
 *         Set the dark feature {@code jira.fixed.cdn.enabled} to true
 *     </li>
 *     <li>
 *         Set the system propery {@code jira.fixed.cdn.prefix} to the cdn prefix, eg
 *         {@code //my.cdnhost.com/my.jira.com}. This must not end with a trailing slash.
 *     </li>
 * </ul>
 *
 * @since v7.0
 */
public class JiraPrefixCDNStrategy implements CDNStrategy
{
    static final String TOGGLE_FEATURE_KEY = "jira.fixed.cdn.enabled";
    static final String PREFIX_SYSTEM_PROPERTY = "jira.fixed.cdn.prefix";

    private final JiraProperties jiraSystemProperties = JiraSystemProperties.getInstance();

    @Override
    public boolean supportsCdn()
    {
        return !StringUtils.isBlank(getPrefix());
    }

    @Override
    public String transformRelativeUrl(String s)
    {
        return getPrefix() + s;
    }

    private String getPrefix()
    {
        return jiraSystemProperties.getProperty(PREFIX_SYSTEM_PROPERTY);
    }
}
