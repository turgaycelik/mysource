package com.atlassian.jira.web.action.browser;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.web.action.browser.ReportConfiguredEvent;
import com.atlassian.jira.event.web.action.browser.ReportViewedEvent;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ConfigureReport extends ProjectActionSupport
{
    private static final String EXCEL_VIEW = "excel";

    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;

    private String reportKey;
    private ReportModuleDescriptor descriptor;
    private ObjectConfiguration oc;
    private String generatedReport;
    private Report report;

    /**
     * Deprecated. Do not use.
     *
     * @param projectManager
     * @param permissionManager
     * @param pluginAccessor
     *
     * @deprecated Use {@link #ConfigureReport(com.atlassian.jira.project.ProjectManager, com.atlassian.jira.security.PermissionManager, com.atlassian.plugin.PluginAccessor, com.atlassian.event.api.EventPublisher)} instead. Since v5.2.
     */
    @Deprecated
    public ConfigureReport(ProjectManager projectManager, PermissionManager permissionManager, PluginAccessor pluginAccessor)
    {
        this(projectManager, permissionManager, pluginAccessor, ComponentAccessor.getComponent(EventPublisher.class));
    }

    /**
     * @param projectManager
     * @param permissionManager
     * @param pluginAccessor
     * @param eventPublisher
     * @since 5.2
     */
    public ConfigureReport(ProjectManager projectManager, PermissionManager permissionManager, PluginAccessor pluginAccessor, EventPublisher eventPublisher)
    {
        super(projectManager, permissionManager);
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
    }

    public String getParamValue(final String key)
    {
        final Map<String, Object> inputParams = makeReportParams();
        String value = (String) inputParams.get(key);
        if (value == null)
        {
            try
            {
                value = getObjectConfiguration().getFieldDefault(key);
            }
            catch (ObjectConfigurationException objectConfigurationException)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(
                            format("The configuration property with the key: %s could not be found for the "
                                    + "report module descriptor with the key: %s", key, reportKey),
                            objectConfigurationException
                    );
                }
            }
        }
        return value;
    }

    public List getParamValues(String key)
    {
        final Map<String, Object> inputParams = makeReportParams();
        Object values = inputParams.get(key);
        if (values == null)
        {
            try
            {
                values = getObjectConfiguration().getFieldDefault(key);
            }
            catch (ObjectConfigurationException objectConfigurationException)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(
                            format("The configuration property with the key: %s could not be found for the "
                                    + "report module descriptor with the key: %s", key, reportKey),
                            objectConfigurationException
                    );
                }
                return Collections.emptyList();
            }
        }
        else if (values instanceof String[])
        {
            return Arrays.asList((String[])values);
        }

        return Arrays.asList(values.toString());
    }

    public String doDefault() throws Exception
    {
        //JRA-13939: Need to be null safe here, as some crawlers may be too stupid to submit a &amp; in a URL and will
        //submit an invalid reportKey param
        if (!validReportKey())
        {
            return "noreporterror";
        }
        eventPublisher.publish(new ReportConfiguredEvent(getReportKey()));
        return super.doDefault();
    }

    protected String doExecute() throws Exception
    {
        //JRA-13939: Need to be null safe here, as some crawlers may be too stupid to submit a &amp; in a URL and will
        //submit an invalid reportKey param
        if (!validReportKey())
        {
            return "noreporterror";
        }
        getReportModule().validate(this, makeReportParams());
        if (getReasons().contains(Reason.NOT_LOGGED_IN))
        {
            return forceRedirect(RedirectUtils.getLoginUrl(request));
        }
        if (invalidInput())
        {
            return INPUT;
        }
        generatedReport = getReportModule().generateReportHtml(this, makeReportParams());
        eventPublisher.publish(new ReportViewedEvent(getReportKey()));
        return SUCCESS;
    }

    //JRA-13939: Need to be null safe here, as some crawlers may be too stupid to submit a &amp; in a URL and will
    //submit an invalid reportKey param
    private boolean validReportKey()
    {
        if (StringUtils.isEmpty(getReportKey()))
        {
            addErrorMessage(getText("report.configure.error.no.report.key"));
            return false;
        }
        return true;
    }

    public String doExcelView() throws Exception
    {
        generatedReport = getReportModule().generateReportExcel(this, makeReportParams());
        return EXCEL_VIEW;
    }

    /**
     * Makes report params from action params.
     *
     * @return a map of report parameters
     */
    private Map<String, Object> makeReportParams()
    {
        @SuppressWarnings ({ "unchecked" })
        Map<String, String[]> params = ActionContext.getParameters();
        Map<String, Object> reportParams = new LinkedHashMap<String, Object>(params.size());

        for (final Map.Entry entry : params.entrySet())
        {
            final String key = (String) entry.getKey();
            if (((String[]) entry.getValue()).length == 1)
            {
                reportParams.put(key, ((String[]) entry.getValue())[0]);
            }
            else
            {
                reportParams.put(key, entry.getValue());
            }
        }
        return reportParams;
    }

    public String getQueryString()
    {
        final Map<String, Object> params = makeReportParams();
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirstKey = true;

        for (final String key : params.keySet())
        {
            Object value = params.get(key);
            if (value instanceof String)
            {
                isFirstKey = appendUrlParameter(isFirstKey, key, (String) value, stringBuilder);
            }
            else if (value instanceof String[])
            {
                for (int i = 0; i < ((String[]) value).length; i++)
                {
                    String s = ((String[]) value)[i];
                    isFirstKey = appendUrlParameter(isFirstKey, key, s, stringBuilder);
                }
            }
        }

        return stringBuilder.toString();
    }

    private boolean appendUrlParameter(final boolean firstKey, String key, String value, StringBuilder stringBuilder)
    {
        if (firstKey)
        {
            stringBuilder.append(encode(key)).append('=').append(encode(value));
        }
        else
        {
            stringBuilder.append('&').append(encode(key)).append('=').append(encode(value));
        }
        return false;
    }

    private String encode(final String key)
    {
        return JiraUrlCodec.encode(key);
    }

    private Report getReportModule()
    {
        if (report == null)
        {
            report = getReport().getModule();
        }

        return report;
    }

    public String getGeneratedReport()
    {
        return generatedReport;
    }

    public String getReportKey()
    {
        return reportKey;
    }

    public void setReportKey(String reportKey)
    {
        this.reportKey = reportKey;
    }

    public ReportModuleDescriptor getReport()
    {
        if (descriptor == null)
        {
            descriptor = (ReportModuleDescriptor) pluginAccessor.getEnabledPluginModule(reportKey);
        }

        return descriptor;
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        if (oc == null)
        {
            final Map objectConfigurationParameters =
                    MapBuilder.build
                            (
                                    "project", getSelectedProject() == null ? null : getSelectedProject().getGenericValue(),
                                    "User", getLoggedInUser()
                            );
            oc = getReport().getObjectConfiguration(objectConfigurationParameters);
        }

        return oc;
    }

    public boolean getLoginAdvisable()
    {
        return (this.getLoggedInUser() == null && this.hasAnyErrors());
    }

    public String getLoginAdviceMessage()
    {
        StringBuilder loginLink = new StringBuilder("<a rel=\"nofollow\" href=\"");
        loginLink.append(RedirectUtils.getLinkLoginURL(request));
        loginLink.append("\">");
        return this.getText("report.loginadvised", loginLink.toString(),"</a>");
    }

    public String getFilterProjectName(String key)
    {
        // This is currently hard coded to pull from the URL parameters. Previously getFilterProjectName didn't exist.
        return this.getParamValue("projectOrFilterName");
    }
}
