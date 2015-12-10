package com.atlassian.jira.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.plugin.navigation.FooterModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.InternalServerErrorDataSource;
import com.atlassian.jira.web.util.MetalResourcesManager;
import com.atlassian.jira.web.util.PrettyObjectPrinter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalServerErrorServlet extends HttpServlet
{

    private static final Logger generalLog = LoggerFactory.getLogger(InternalServerErrorServlet.class);

    private void doRequest(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {

        try
        {
            //generate an unique id
            final String id = UUID.randomUUID().toString();
            final Logger log = LoggerFactory.getLogger("500ErrorPage." + id);

            //set response parameters
            resp.setContentType("text/html");


            //create template data map
            ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
            map.put("footer", getFooterContent(req));
            map.put("helpsteps", prepareHelpSteps(req));
            map.put("resourcesContent", MetalResourcesManager.getMetalResources(req.getContextPath()));
            if (!isOnDemand() || isLoggedInAdmin())
            {
                map.put("errorId", id);
            }

            //prepare tech data map
            InternalServerErrorDataSource ds = initializeInternalServerErrorDS(req, log);
            final ImmutableMap.Builder<String, Object> technicalDetails = ImmutableMap.builder();
            final boolean shouldShowFullInfo = isLoggedInAdmin() || JiraSystemProperties.isDevMode();
            if (shouldShowFullInfo && !Boolean.parseBoolean(req.getParameter("short")))
            {
                ds.appendFullMessageData(technicalDetails, isLoggedInSysAdmin() || JiraSystemProperties.isDevMode());
                map.put("fullInfo", true);
            }
            else
            {
                ds.appendSimpleMessageData(technicalDetails);
                map.put("fullInfo", false);
                //mark that we dont need soy modifications
                ds.notForSoy();
            }

            //dump soy data
            final ImmutableMap.Builder<String, Object> rawMapBuilder = ImmutableMap.builder();
            //logs are supposed to be read by sysadmin, so we dump data with his permissions
            final Map<String, Object> soyData = ds.appendFullMessageData(rawMapBuilder, true).build();
            final PrettyObjectPrinter pop = new PrettyObjectPrinter(soyData);
            //SW-637: When in dev mode log problems at error level so that it's easier for developers to debug problems.
            if (JiraSystemProperties.isDevMode())
            {
                log.error(pop.toString());
            }
            else
            {
                log.debug(pop.toString());
            }
            map.put("technicalDetails", technicalDetails.build());

            //render template
            ImmutableMap<String, Object> build = map.build();
            SoyTemplateRenderer renderer = ComponentAccessor.getOSGiComponentInstanceOfType(SoyTemplateRenderer.class);
            renderer.render(resp.getWriter(), "jira.webresources:jira-errors", "JIRA.Templates.errors.InternalError.page", build);


        }
        catch (SoyException e)
        {
            generalLog.error("Cannot render soy template", e);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        doRequest(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        doRequest(req, resp);
    }

    private boolean isOnDemand()
    {
        FeatureManager featureManager = ComponentAccessor.getComponent(FeatureManager.class);

        //default answer is yes (safer)
        if (featureManager == null)
        { return true; }
        return featureManager.isOnDemand();
    }

    private String getContactAdministratorLink(final HttpServletRequest req)
    {
        JiraContactHelper jiraContactHelper = ComponentAccessor.getComponent(JiraContactHelper.class);
        if (jiraContactHelper == null)
        { return null; }
        if (!jiraContactHelper.isAdministratorContactFormEnabled())
        { return null; }
        return jiraContactHelper.getAdministratorContactLink(req.getContextPath());
    }


    private InternalServerErrorDataSource initializeInternalServerErrorDS(final HttpServletRequest req, final Logger log)
    {
        final I18nHelper i18nBean = getI18nHelper();
        ExtendedSystemInfoUtilsImpl extendedSystemInfoUtils = null;
        try
        {
            extendedSystemInfoUtils = new ExtendedSystemInfoUtilsImpl(i18nBean);
        }
        catch (Exception e)
        {
            log.warn("Cannot initialize ExtendedSystemInfoUtilsImpl", e);
        }

        return new InternalServerErrorDataSource(i18nBean, extendedSystemInfoUtils, getServletContext(), ComponentAccessor.getLocaleManager(), req);
    }

    private I18nHelper getI18nHelper()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class).getI18nHelper();
    }

    private boolean isLoggedInAdmin()
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
        if (user == null)
        {
            return false;
        }

        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(Permissions.ADMINISTER, user)
                || permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private boolean isLoggedInSysAdmin()
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
        if (user == null)
        {
            return false;
        }

        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }


    private String getFooterContent(HttpServletRequest httpRequest)
    {
        PluginAccessor pluginAccessor = ComponentAccessor.getPluginAccessor();
        if (pluginAccessor == null)
        {
            return "";
        }
        FooterModuleDescriptor footer = (FooterModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.footer:standard-footer");
        if (footer == null)
        {
            return "";
        }
        return footer.getModule().getFullFooterHtml(httpRequest);

    }

    private List<String> prepareHelpSteps(HttpServletRequest req)
    {

        I18nHelper i18n = getI18nHelper();
        if (isOnDemand())
        {
            if (isLoggedInAdmin())
            {
                final ExternalLinkUtilImpl externalLinkUtil = new ExternalLinkUtilImpl();
                final String sac = externalLinkUtil.getProperty("external.link.jira.support.site");
                return ImmutableList.of(
                        i18n.getText("500.raise.an.issue.on.sac", "<a href=\"" + sac + "\">", "</a>")
                );
            }
            else
            {
                final String contactAdministratorsLink = getContactAdministratorLink(req);
                return ImmutableList.of(
                        contactAdministratorsLink != null
                                ? i18n.getText("500.send.to.your.jira.admin.contact.form", "<a target=\"_blank\" href=\"" + contactAdministratorsLink + "\">", "</a>")
                                : i18n.getText("500.send.to.your.jira.admin")
                );
            }
        }
        else
        {
            if (isLoggedInAdmin())
            {
                final ExternalLinkUtilImpl externalLinkUtil = new ExternalLinkUtilImpl();
                final String sac = externalLinkUtil.getProperty("external.link.jira.support.site");
                final String stp = req.getContextPath() + "/plugins/servlet/stp/view/#support-zip";
                return ImmutableList.of(
                        i18n.getText("500.collect.when.problem.occurred"),
                        i18n.getText("500.collect.server.log"),
                        i18n.getText("500.create.support.zip", "<a target=\"_blank\" href=\"" + stp + "\">", "</a>"),
                        i18n.getText("500.raise.an.issue.with.all.info", "<a href=\"" + sac + "\">", "</a>")
                );
            }
            else
            {
                final String contactAdministratorsLink = getContactAdministratorLink(req);
                return ImmutableList.of(
                        contactAdministratorsLink != null
                                ? i18n.getText("500.send.with.ref.to.your.jira.admin.contact.form", "<a target=\"_blank\" href=\"" + contactAdministratorsLink + "\">", "</a>")
                                : i18n.getText("500.send.with.ref.to.your.jira.admin")
                );
            }
        }
    }


}
