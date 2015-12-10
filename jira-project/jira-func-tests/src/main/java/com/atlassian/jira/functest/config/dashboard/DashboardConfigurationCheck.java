package com.atlassian.jira.functest.config.dashboard;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Configuration check that ensures dashboard configuration is clean and consistent.
 *
 * @since v4.2
 */
public class DashboardConfigurationCheck implements ConfigurationCheck
{
    /**
     * Check to see if there are non-system dashboards.
     */
    public static final String CHECKID_DASHBOARDS = "dashboards";

    /**
     * Check to see if there are gadgets on the dashboards.
     */
    public static final String CHECKID_GADGETS = "gadgets";

    /**
     * Check to see if there are any external gadgets defined.
     */
    public static final String CHECKID_EXTERNAL = "externalgadgets";

    /**
     * Check to see if there are any gadets with absolute URLs are defined.
     */
    public static final String CHECKID_ABSOLUTE = "absolutegadgets";

    private static final Pattern ABSOLUTE_REGEX = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);

    public Result checkConfiguration(final JiraConfig config, final CheckOptions options)
    {
        final CheckResultBuilder builder = new CheckResultBuilder();
        final List<ConfigDashboard> dashboards = config.getDashboards();
        final Set<String> externalUrls = getExternalGadgetUrls(config);
        for (final ConfigDashboard dashboard : dashboards)
        {
            if (options.checkEnabled(CHECKID_DASHBOARDS) && !dashboard.isSystem())
            {
                //If there exists a dashboard and it is not the system dashboard then we may have an error.
                builder.error(String.format("Non-system dashboard '%s' (%d) exists.", getName(dashboard), dashboard.getId()),
                        CHECKID_DASHBOARDS);
            }
            else if (!dashboard.getGadgets().isEmpty())
            {
                if (options.checkEnabled(CHECKID_GADGETS))
                {
                    builder.error(String.format("Gadget on dashboard '%s' (%d).",
                            getName(dashboard), dashboard.getId()), CHECKID_GADGETS);
                }
                else if (options.checkEnabled(CHECKID_ABSOLUTE))
                {
                    for (ConfigGadget gadget : dashboard.getGadgets())
                    {
                        if (!externalUrls.contains(toLowerCase(gadget.getGadgetXml()))
                                && isAbsoluteUrl(gadget))
                        {
                            builder.error(String.format("Gadget URL '%s' is absolute on dashboard '%s' (%d).",
                                    gadget.getGadgetXml(), getName(dashboard), dashboard.getId()), CHECKID_ABSOLUTE);
                        }
                    }
                }
            }
        }

        if (options.checkEnabled(CHECKID_EXTERNAL))
        {
            for (String externalUrl : externalUrls)
            {
                builder.error(String.format("External gadget '%s' configured.", externalUrl), CHECKID_EXTERNAL);
            }
        }

        return builder.buildResult();
    }

    public void fixConfiguration(final JiraConfig config, final CheckOptions options)
    {
        final List<ConfigDashboard> dashboards = config.getDashboards();
        for (Iterator<ConfigDashboard> dashboardIterator = dashboards.iterator(); dashboardIterator.hasNext();)
        {
            ConfigDashboard dashboard = dashboardIterator.next();
            if (options.checkEnabled(CHECKID_DASHBOARDS) && !dashboard.isSystem())
            {
                dashboardIterator.remove();
            }
            else if (!dashboard.getGadgets().isEmpty())
            {
                checkGadgets(config, options, dashboard);
            }
        }

        if (options.checkEnabled(CHECKID_EXTERNAL))
        {
            config.getExternalGadgets().clear();
        }
    }

    private void checkGadgets(final JiraConfig config, final CheckOptions options, final ConfigDashboard dashboard)
    {
        if (options.checkEnabled(CHECKID_GADGETS))
        {
            dashboard.getGadgets().clear();
        }
        else if (options.checkEnabled(CHECKID_ABSOLUTE) || options.checkEnabled(CHECKID_EXTERNAL))
        {
            final Set<String> externalUrls = getExternalGadgetUrls(config);
            boolean changed = false;
            for (final Iterator<ConfigGadget> remove = dashboard.getGadgets().iterator(); remove.hasNext();)
            {
                final ConfigGadget gadget = remove.next();
                if (externalUrls.contains(toLowerCase(gadget.getGadgetXml())))
                {
                    if (options.checkEnabled(CHECKID_EXTERNAL))
                    {
                        changed = true;
                        remove.remove();
                    }
                }
                else if (options.checkEnabled(CHECKID_ABSOLUTE) && isAbsoluteUrl(gadget))
                {
                    changed = true;
                    remove.remove();
                }
            }
            if (changed)
            {
                dashboard.reorderGadgets();
            }
        }
    }

    private static boolean isAbsoluteUrl(final ConfigGadget gadget)
    {
        return gadget.getGadgetXml() != null && ABSOLUTE_REGEX.matcher(gadget.getGadgetXml()).find();
    }

    private static String toLowerCase(String str)
    {
        return str == null ? null : str.toLowerCase(Locale.ENGLISH);
    }

    private Set<String> getExternalGadgetUrls(JiraConfig config)
    {
        final Set<String> urls = new HashSet<String>();
        final List<ConfigExternalGadget> gadgets = config.getExternalGadgets();
        for (ConfigExternalGadget gadget : gadgets)
        {
            final String xml = gadget.getGadgetXml();
            if (StringUtils.isNotBlank(xml))
            {
                urls.add(xml.toLowerCase(Locale.ENGLISH));
            }
        }
        return urls;
    }

    private static String getName(ConfigDashboard dashboard)
    {
        final String name = dashboard.getName();
        return StringUtils.isBlank(name) ? "<unknown>" : name;
    }
}
