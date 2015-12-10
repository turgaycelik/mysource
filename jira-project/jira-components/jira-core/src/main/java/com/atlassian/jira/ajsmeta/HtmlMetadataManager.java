package com.atlassian.jira.ajsmeta;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.webfragment.conditions.SmtpMailServerConfiguredCondition;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.util.ProductVersionDataBean;
import com.atlassian.jira.web.util.ProductVersionDataBeanProvider;

import com.opensymphony.util.TextUtils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The metadata that gets included by default on all JIRA pages. Accessible in the browser by using {@code
 * AJS.Meta.get(String)}.
 * <p/>
 * This is currently a hard-coded set of information but in the future we might think about making this a plugin point
 * so that plugins can use {@code AJS.Meta}.
 *
 * @since v5.2
 */
@Internal
public final class HtmlMetadataManager
{
    private final JiraWebResourceManager webResourceManager;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final KeyboardShortcutManager keyboardShortcutManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final FeatureManager featureManager;
    private final ProductVersionDataBean productVersionDataBean;
    private final GoogleSiteVerification googleSiteVerification;
    private final PermissionManager permissionManager;
    private final JiraProperties jiraSystemProperties;
    private final MailSettings mailSettings;

    public HtmlMetadataManager(
            JiraWebResourceManager webResourceManager, BuildUtilsInfo buildUtilsInfo, JiraAuthenticationContext authenticationContext,
            ApplicationProperties applicationProperties, KeyboardShortcutManager keyboardShortcutManager,
            VelocityRequestContextFactory velocityRequestContextFactory, FeatureManager featureManager,
            ProductVersionDataBeanProvider productVersionDataBeanProvider, GoogleSiteVerification googleSiteVerification,
            PermissionManager permissionManager, MailSettings mailSettings, final JiraProperties jiraSystemProperties)
    {
        this.webResourceManager = webResourceManager;
        this.buildUtilsInfo = buildUtilsInfo;
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.keyboardShortcutManager = keyboardShortcutManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.featureManager = featureManager;
        this.googleSiteVerification = googleSiteVerification;
        this.permissionManager = permissionManager;
        this.jiraSystemProperties = jiraSystemProperties;
        productVersionDataBean = productVersionDataBeanProvider.get();
        this.mailSettings = mailSettings;
    }

    /**
     * Includes the metadata in HTML &lt;meta&gt; elements in the page &lt;head&gt;.
     *
     * @param writer a Writer
     * @throws IOException if there is a problem writing to {@code writer}
     */
    public void includeMetadata(Writer writer) throws IOException
    {
        if (jiraSystemProperties.getBoolean("atlassian.disable.issue.collector"))
        {
            webResourceManager.putMetadata("disable-issue-collector", "true");
        }

        ApplicationUser user = authenticationContext.getUser();

        webResourceManager.putMetadata("dev-mode", Boolean.toString(jiraSystemProperties.isDevMode()));
        webResourceManager.putMetadata("context-path", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        webResourceManager.putMetadata("version-number", buildUtilsInfo.getVersion());
        webResourceManager.putMetadata("build-number", buildUtilsInfo.getCurrentBuildNumber());
        webResourceManager.putMetadata("is-beta", Boolean.toString(buildUtilsInfo.isBeta()));
        webResourceManager.putMetadata("is-rc", Boolean.toString(buildUtilsInfo.isRc()));
        webResourceManager.putMetadata("is-snapshot", Boolean.toString(buildUtilsInfo.isSnapshot()));
        webResourceManager.putMetadata("is-milestone", Boolean.toString(buildUtilsInfo.isMilestone()));
        webResourceManager.putMetadata("remote-user", user != null ? user.getName() : "");
        webResourceManager.putMetadata("remote-user-fullname", user != null ? user.getDisplayName() : "");
        webResourceManager.putMetadata("user-locale", authenticationContext.getLocale().toString());
        final NumberFormat numberFormat = NumberFormat.getInstance(authenticationContext.getLocale());
        String groupingSeparator = "";
        if (numberFormat.isGroupingUsed() && numberFormat instanceof DecimalFormat) {
            groupingSeparator = "" + ((DecimalFormat) numberFormat).getDecimalFormatSymbols().getGroupingSeparator();
        }
        webResourceManager.putMetadata("user-locale-group-separator", groupingSeparator);
        webResourceManager.putMetadata("app-title", getAppTitle());
        webResourceManager.putMetadata("keyboard-shortcuts-enabled", Boolean.toString(keyboardShortcutManager.isKeyboardShortcutsEnabled()));
        webResourceManager.putMetadata("keyboard-accesskey-modifier", BrowserUtils.getModifierKey());
        webResourceManager.putMetadata("enabled-dark-features", new DarkFeaturesMeta(featureManager).getContent());
        webResourceManager.putMetadata("date-relativize", applicationProperties.getDefaultBackedString("jira.lf.date.relativize"));
        webResourceManager.putMetadata("date-time", applicationProperties.getDefaultBackedString("jira.lf.date.time"));
        webResourceManager.putMetadata("date-day", applicationProperties.getDefaultBackedString("jira.lf.date.day"));
        webResourceManager.putMetadata("date-dmy", applicationProperties.getDefaultBackedString("jira.lf.date.dmy"));
        webResourceManager.putMetadata("date-complete", applicationProperties.getDefaultBackedString("jira.lf.date.complete"));
        webResourceManager.putMetadata("in-admin-mode", Boolean.toString(ExecutingHttpRequest.get().getAttribute("jira.admin.mode") != null));
        webResourceManager.putMetadata("is-sysadmin", Boolean.toString(user != null ? permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user) : false));
        webResourceManager.putMetadata("is-admin", Boolean.toString(user != null ? permissionManager.hasPermission(Permissions.ADMINISTER, user) : false));
        webResourceManager.putMetadata("outgoing-mail-enabled", Boolean.toString(SmtpMailServerConfiguredCondition.isOutgoingMailEnabled(mailSettings)));

        writeTo(writer);

        // quick fix to allow easy verification of OnDemand instances in Google Webmaster Tools.
        String googleSiteKey = googleSiteVerification.getKey();
        if (isNotBlank(googleSiteKey))
        {
            writeTo(writer, googleSiteVerification.getMetaName(), googleSiteKey);
        }
    }

    private void writeTo(Writer writer) throws IOException
    {
        final Map<String, String> metadata = webResourceManager.getMetadata();
        for (Map.Entry<String, String> metaDataEntry : metadata.entrySet())
        {
            writeTo(writer, "ajs-" + metaDataEntry.getKey(), metaDataEntry.getValue());
        }
    }

    private void writeTo(final Writer writer, final String key, final String value) throws IOException
    {
        writer.write("<meta name=\"");
        writer.write(TextUtils.htmlEncode(key));
        writer.write("\" content=\"");
        writer.write(TextUtils.htmlEncode(value));
        writer.write("\">\n");
    }

    private String getAppTitle()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE);
    }

    public ProductVersionDataBean getVersionBean()
    {
        return productVersionDataBean;
    }
}
