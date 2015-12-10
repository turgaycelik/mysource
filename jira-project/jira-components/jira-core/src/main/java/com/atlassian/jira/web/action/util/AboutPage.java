package com.atlassian.jira.web.action.util;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.plugin.aboutpagepanel.AboutPagePanelModuleDescriptor;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.Lists;

/**
 * Displays the About page with licensing information.
 *
 * @since v6.0
 */
@SuppressWarnings("RedundantStringConstructorCall")
public class AboutPage extends JiraWebActionSupport
{
    public final static String KEY_COPYRIGHT_SINCE = new String("copyrightSince");
    public final static String KEY_COPYRIGHT_UNTIL = new String("copyrightUntil");
    public final static String KEY_REQUEST_CONTEXT = new String("requestContext");
    public final static String KEY_BUILD_VERSION = new String("buildVersion");

    public final static String KEY_PLUGIN_SECTIONS = new String("pluginModules");
    public final static String KEY_PLUGIN_SECTIONS_EXIST = new String("pluginModulesExist");
    public final static String KEY_LICENSE_SECTION_EXIST = new String("showLicenses");

    public final static String LGPL_TEMPLATE = new String("templates/jira/about/lgpl-libs.soy");
    private final static String FIRST_JIRA_RELEASE_YEAR = "2002";

    private final String copyrightSinceYear;
    private final String copyrightUntilYear;
    private final String buildVersion;
    private final PluginAccessor pluginAccessor;

    public AboutPage(final BuildUtilsInfo buildUtilsInfo, final PluginAccessor pluginAccessor)
    {
        copyrightSinceYear = FIRST_JIRA_RELEASE_YEAR;
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(buildUtilsInfo.getCurrentBuildDate());
        copyrightUntilYear = Integer.toString(calendar.get(GregorianCalendar.YEAR));
        buildVersion = buildUtilsInfo.getVersion();
        this.pluginAccessor = pluginAccessor;
    }

    @ActionViewData ("success")
    public Map<String, Object> getData()
    {
        List<String> pluginSections = asHtml();
        boolean showLicenses = getClass().getClassLoader().getResource(LGPL_TEMPLATE) != null;
        return MapBuilder.<String, Object>newBuilder()
                .add(KEY_COPYRIGHT_SINCE, copyrightSinceYear)
                .add(KEY_COPYRIGHT_UNTIL, copyrightUntilYear)
                .add(KEY_REQUEST_CONTEXT, getHttpRequest().getContextPath())
                .add(KEY_BUILD_VERSION, buildVersion)
                .add(KEY_PLUGIN_SECTIONS, pluginSections)
                .add(KEY_LICENSE_SECTION_EXIST, showLicenses)
                .add(KEY_PLUGIN_SECTIONS_EXIST, !pluginSections.isEmpty())
                .toMap();
    }

    public List<String> asHtml()
    {
        List<String> result = Lists.newArrayList();

        List<AboutPagePanelModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(AboutPagePanelModuleDescriptor.class);
        for (AboutPagePanelModuleDescriptor descriptor : descriptors)
        {
            result.add(descriptor.getPluginSectionHtml());
        }

        return result;
    }
}

