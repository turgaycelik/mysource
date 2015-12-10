package com.atlassian.jira.lookandfeel;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.admin.IntroductionProperty;
import com.atlassian.jira.avatar.GravatarSettings;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.event.web.action.admin.LookAndFeelUpdatedEvent;
import com.atlassian.jira.event.web.action.admin.LookAndFeelUpdatedEvent.Type;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.lookandfeel.image.ImageDescriptor;
import com.atlassian.jira.lookandfeel.image.MultiPartImageDescriptor;
import com.atlassian.jira.lookandfeel.image.URLImageDescriptor;
import com.atlassian.jira.lookandfeel.upload.UploadService;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatTypes;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.setting.GzipCompression;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.action.admin.ViewApplicationProperties;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;


@WebSudoRequired
public class EditLookAndFeel extends ViewApplicationProperties
{
    private static final Logger log = LoggerFactory.getLogger(EditLookAndFeel.class);
    public static final String USER_FORMAT_PREFIX = "user_format_for_";

    private final LookAndFeelProperties lookAndFeelProperties;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;
    private final FeatureManager featureManager;
    private final I18nHelper i18nHelper;
    private final UploadService uploadService;
    private final PluginSettings globalSettings;
    private final EventPublisher eventPublisher;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final AutoLookAndFeelManager autoLookAndFeelManager;
    private final UserFormatTypes userFormatTypes;
    private final UserFormatModuleDescriptors userFormatModuleDescriptors;
    private final UserFormatTypeConfiguration userFormatTypeConfiguration;

    private String logoUrl;
    private String faviconUrl;
    private boolean showSiteTitleOnLogo;


    public EditLookAndFeel(final UserPickerSearchService searchService,
            final BeanFactory i18nBeanFactory, final LocaleManager localeManager,
            final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext,
            final TimeZoneService timeZoneService, final RendererManager rendererManager,
            final LookAndFeelProperties lookAndFeelProperties, final PluginSettingsFactory pluginSettingsFactory,
            final VelocityRequestContextFactory requestContextFactory, final UploadService uploadService,
            final FeatureManager featureManager, final PluginAccessor pluginAccessor,
            final IntroductionProperty introduction,
            final EventPublisher eventPublisher, SoyTemplateRenderer soyTemplateRenderer, WebResourceUrlProvider webResourceUrlProvider,
            final AutoLookAndFeelManager autoLookAndFeelManager, final JiraLicenseService jiraLicenseService, final GravatarSettings gravatarSettings)
    {
        super(searchService, localeManager, timeZoneService, rendererManager, pluginAccessor, ComponentAccessor.getComponent(GzipCompression.class), featureManager, introduction, jiraLicenseService, gravatarSettings);

        this.soyTemplateRenderer = soyTemplateRenderer;
        this.eventPublisher = eventPublisher;
        this.uploadService = uploadService;
        this.requestContextFactory = requestContextFactory;
        this.lookAndFeelProperties = lookAndFeelProperties;
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.autoLookAndFeelManager = autoLookAndFeelManager;
        this.showSiteTitleOnLogo = false;
        this.globalSettings = pluginSettingsFactory.createGlobalSettings();
        this.i18nHelper = i18nBeanFactory.getInstance(authenticationContext.getLoggedInUser());
        this.userFormatTypes = ComponentAccessor.getComponent(UserFormatTypes.class);
        this.userFormatModuleDescriptors = ComponentAccessor.getComponent(UserFormatModuleDescriptors.class);
        this.userFormatTypeConfiguration = ComponentAccessor.getComponent(UserFormatTypeConfiguration.class);
    }

    private LookAndFeelBean init()
    {
        return LookAndFeelBean.getInstance(applicationProperties);
    }

    public String getLogoFormAsHtml()
    {

        LookAndFeelBean lookandfeelBean = init();
        Map<String, Object> soyRenderData = new HashMap<String, Object>();
        soyRenderData.put("uploadAction", "LookAndFeel!uploadLogo.jspa");
        soyRenderData.put("resetAction","LookAndFeel!resetLogo.jspa?atl_token="+getXsrfToken());
        soyRenderData.put("imageWithContext",addContextToUrl(lookandfeelBean.getLogoUrl()));
        soyRenderData.put("imageType","logo");
        soyRenderData.put("imageDescription", i18nHelper.getText("admin.globalsettings.lookandfeel.edit.look.and.feel.recommended.logo.dimensions"));
        soyRenderData.put("formTitle",getText("admin.globalsettings.lookandfeel.logo"));
        if (globalSettings.get(LookAndFeelConstants.USING_CUSTOM_LOGO) == null)
        {
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_LOGO,Boolean.toString(false));
        }
        soyRenderData.put("isNotDefault",globalSettings.get(LookAndFeelConstants.USING_CUSTOM_LOGO).toString());
        soyRenderData.put("token",getXsrfToken());
        try
        {
            return soyTemplateRenderer.render("com.atlassian.jira.lookandfeel:logoFormSoy", "JIRA.Templates.LookandFeelLogo.logoForm", soyRenderData);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for logo form");
            log.debug("Exception: ", e);
        }
        return null;
    }

    public String getSiteTitleFormAsHtml()
    {
        if (!isCommonHeaderEnabled())
        {
            return null;
        }

        Map<String, Object> soyRenderData = new HashMap<String, Object>();
        soyRenderData.put("action", "LookAndFeel!updateSiteTitle.jspa");
        soyRenderData.put("token", getXsrfToken());
        if (lookAndFeelProperties.isApplicationTitleShownOnLogo())
        {
            soyRenderData.put("isChecked",Boolean.toString(lookAndFeelProperties.isApplicationTitleShownOnLogo()));
        }

        try
        {
            return soyTemplateRenderer.render("com.atlassian.jira.lookandfeel:logoFormSoy", "JIRA.Templates.LookandFeelLogo.showSiteTitleForm", soyRenderData);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for site title form");
            log.debug("Exception: ", e);
        }

        return null;
    }

    public String getFavIconFormAsHtml()
    {

        LookAndFeelBean lookandfeelBean = init();
        Map<String, Object> soyRenderData = new HashMap<String, Object>();
        soyRenderData.put("uploadAction", "LookAndFeel!uploadFavicon.jspa");
        soyRenderData.put("resetAction","LookAndFeel!resetFavicon.jspa?atl_token="+getXsrfToken());
        soyRenderData.put("imageWithContext", addContextToUrl(lookandfeelBean.getFaviconHiResUrl()));
        soyRenderData.put("imageType","favicon");
        soyRenderData.put("formTitle",getText("admin.globalsettings.lookandfeel.favicon"));
        if (globalSettings.get(LookAndFeelConstants.USING_CUSTOM_FAVICON) == null)
        {
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_FAVICON,Boolean.toString(false));
        }
        soyRenderData.put("isNotDefault",globalSettings.get(LookAndFeelConstants.USING_CUSTOM_FAVICON).toString());
        soyRenderData.put("token",getXsrfToken());
        try
        {
            return soyTemplateRenderer.render("com.atlassian.jira.lookandfeel:logoFormSoy", "JIRA.Templates.LookandFeelLogo.logoForm", soyRenderData);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for fav icon form");
            log.debug("Exception: ", e);
        }
        return null;
    }

    public String getUserFormatFormAsHtml()
    {
        Map<String, Object> soyRenderData = new HashMap<String, Object>();
        soyRenderData.put("submitAction", "LookAndFeel!userFormatUpdate.jspa");
        soyRenderData.put("formTitle",getText("admin.globalsettings.lookandfeel.user.formats"));
        soyRenderData.put("token",getXsrfToken());
        List<Object> userFormatTypes = new ArrayList<Object>();
        for (String formatType : getUserFormatTypes())
        {
            if (getUserFormatModuleDescriptorsForType(formatType).size() > 1)
            {
                Map<String, Object> formatTypeData = new HashMap<String, Object>();
                formatTypeData.put("label",getUserFormatTypeName(formatType));
                formatTypeData.put("name","user_format_for_"+formatType);
                List<Map<String, String>> options = new ArrayList<Map<String, String>>();
                for (UserFormatModuleDescriptor ufmd : getUserFormatModuleDescriptorsForType(formatType))
                {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("text",ufmd.getName());
                    option.put("value",ufmd.getCompleteKey());
                    if (getUserFormatKey(formatType).equalsIgnoreCase(ufmd.getCompleteKey()))
                    {
                        option.put("selected","true");
                    }
                    options.add(option);
                }
                formatTypeData.put("options",options);
                userFormatTypes.add(formatTypeData);
            }
            soyRenderData.put("userTypes",userFormatTypes);
        }
        try
        {
            return soyTemplateRenderer.render("com.atlassian.jira.lookandfeel:userFormatFormSoy","JIRA.Templates.LookandFeelUserFormat.userformatForm",soyRenderData);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for user format types");
            log.debug("Exception: ", e);
        }
        return null;
    }

    private String addContextToUrl(String url)
    {
        if (url != null &&  !url.startsWith("http://") && !url.startsWith("https://"))
        {
            url = webResourceUrlProvider.getStaticResourcePrefix(UrlMode.AUTO)+url;
        }
        return url;
    }

    public boolean currentSiteTitleOnLogoSetting()
    {
        return lookAndFeelProperties.isApplicationTitleShownOnLogo();
    }

    public boolean isShowSiteTitleOnLogo()
    {
        return showSiteTitleOnLogo;
    }

    public void setShowSiteTitleOnLogo(final boolean showSiteTitleOnLogo)
    {
        this.showSiteTitleOnLogo = showSiteTitleOnLogo;
    }

    private void setDefaultFavicon(final LookAndFeelBean lookAndFeelBean)
    {
        lookAndFeelBean.setFaviconUrl(lookAndFeelProperties.getDefaultFaviconUrl());
        lookAndFeelBean.setFaviconHiResUrl(lookAndFeelProperties.getDefaultFaviconHiresUrl());
        lookAndFeelProperties.resetDefaultFaviconUrl();
    }

    private String ensureUrlCorrect(String url)
    {
        // url must start with 'http://', 'http://', or else add the leading '/'
        if (StringUtils.isNotBlank(url) && !url.startsWith("http") && !url.startsWith("/"))
        {
            url = "/" + url;
        }
        return url;
    }

    @RequiresXsrfCheck
    public String doUploadLogo()
    {
        final String parameterName =  "logoFile";
        LookAndFeelBean lookAndFeelBean = init();
        final String serverPath = ServletContextProvider.getServletContext().getRealPath("/");
        final MultiPartRequestWrapper multiPartRequest = ServletActionContext.getMultiPartRequest();
        if (multiPartRequest == null)
        {
            return ERROR;
        }
        ImageDescriptor imageDescriptor = null;
        try
        {

            if (StringUtils.isNotBlank(multiPartRequest.getFilesystemName(parameterName)))
            {
                imageDescriptor = new MultiPartImageDescriptor(parameterName, multiPartRequest, i18nHelper);
            }
            if (imageDescriptor == null)
            {
                final String url = getLogoUrl();
                if (StringUtils.isNotBlank(url) && !url.equals(lookAndFeelBean.getLogoUrl()))
                {
                    imageDescriptor = new URLImageDescriptor(serverPath, getLogoUrl(), i18nHelper);
                }
                else
                {
                    setLogoUrl(lookAndFeelBean.getLogoUrl());
                }
            }
        }
        catch (IOException e)
        {
            addErrorMessage(getText("jira.lookandfeel.upload.error", "",  e.getMessage()));
            return ERROR;
        }

        try

        {
            if (imageDescriptor != null)
            {
                addErrorMessages(uploadService.uploadLogo(imageDescriptor, lookAndFeelBean));
                if (hasAnyErrors())
                {
                    return ERROR;
                }
            }
            setLogoUrl(lookAndFeelBean.getLogoUrl());

            // Automatically update the colour scheme to match the new logo
            autoLookAndFeelManager.backupColorScheme();
            autoLookAndFeelManager.generateFromLogo(getLoggedInUser());

            eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.UPLOAD_LOGO));

            return getRedirect("LookAndFeel!default.jspa");
        }
        finally
        {
            if (imageDescriptor != null)
            {
                imageDescriptor.closeImageStreamQuietly();
            }
        }
    }

    @RequiresXsrfCheck
    public String doAutoUpdateColors()
    {
        autoLookAndFeelManager.backupColorScheme();
        autoLookAndFeelManager.generateFromLogo(getLoggedInUser());
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.AUTO_COLOR_SCHEME));
        return getRedirect("LookAndFeel!default.jspa");
    }

    @RequiresXsrfCheck
    public String doRestoreColorScheme()
    {
        autoLookAndFeelManager.restoreBackupColorScheme();
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.UNDO_AUTO_COLOR_SCHEME));
        refreshResources();
        return getRedirect("LookAndFeel!default.jspa");
    }

    @RequiresXsrfCheck
    public String doUpdateSiteTitle()
    {
        if (isCommonHeaderEnabled())
        {
            lookAndFeelProperties.setApplicationTitleShownOnLogoTo(showSiteTitleOnLogo);
        }
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.SITE_TITLE));
        return getRedirect("LookAndFeel!default.jspa");
    }

    @RequiresXsrfCheck
    public String doUploadFavicon()
    {
        final String parameterName = "faviconFile";
        LookAndFeelBean lookAndFeelBean = init();
        final String serverPath = ServletContextProvider.getServletContext().getRealPath("/");

        final MultiPartRequestWrapper multiPartRequest = ServletActionContext.getMultiPartRequest();
        if (multiPartRequest == null)
        {
            return ERROR;
        }
        ImageDescriptor imageDescriptor = null;
        try
        {
            if (StringUtils.isNotBlank(multiPartRequest.getFilesystemName(parameterName)))
            {
                imageDescriptor = new MultiPartImageDescriptor(parameterName, multiPartRequest, i18nHelper);
            }

            if (imageDescriptor == null)
            {
                String url = getFaviconUrl();
                if (StringUtils.isNotBlank(url) && !url.equals(lookAndFeelBean.getFaviconUrl()))
                {
                    imageDescriptor = new URLImageDescriptor(serverPath, url, i18nHelper);
                }
            }
        }
        catch (IOException e)
        {
            addErrorMessage(getText("jira.lookandfeel.upload.error", "", e.getMessage()));
            return ERROR;
        }
        try
        {

            if (imageDescriptor != null)
            {
                addErrorMessages(uploadService.uploadFavicon(lookAndFeelBean, imageDescriptor));
                lookAndFeelProperties.setFaviconChoice(LogoChoice.UPLOAD);
            }
        }
        finally
        {
            if (imageDescriptor != null)
            {
                imageDescriptor.closeImageStreamQuietly();
            }
        }
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.UPLOAD_FAVICON));
        return getRedirect("LookAndFeel!default.jspa");
    }

    @RequiresXsrfCheck
    public String doResetLogo()
    {
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_LOGO, BooleanUtils.toStringTrueFalse(false));
        lookAndFeelProperties.resetDefaultLogo();
        autoLookAndFeelManager.backupColorScheme();
        autoLookAndFeelManager.generateFromLogo(getLoggedInUser());
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.RESET_LOGO));
        return getRedirect("LookAndFeel!default.jspa");
    }

    @RequiresXsrfCheck
    public String doResetFavicon()
    {

        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_FAVICON, BooleanUtils.toStringTrueFalse(false));
        lookAndFeelProperties.resetDefaultFavicon();
        lookAndFeelProperties.setFaviconChoice(LogoChoice.JIRA);
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.RESET_FAVICON));
        return getRedirect("LookAndFeel!default.jspa");
    }

    @RequiresXsrfCheck
    public String doUserFormatUpdate()
    {
        final Set set = ActionContext.getParameters().entrySet();
        for (final Object aSet : set)
        {
            final Map.Entry entry = (Map.Entry) aSet;
            final String key = (String) entry.getKey();
            if (key.startsWith(USER_FORMAT_PREFIX))
            {
                String type = key.substring(USER_FORMAT_PREFIX.length());
                try
                {
                    userFormatTypeConfiguration.setUserFormatKeyForType(type, ((String[]) entry.getValue())[0]);
                }
                catch (IllegalArgumentException e)
                {
                    addError(USER_FORMAT_PREFIX + type, getText("admin.globalsettings.lookandfeel.error.invalid.user.format"));
                }
            }
        }

        if (hasAnyErrors())
        {
            return ERROR;
        }
        return getRedirect("LookAndFeel!default.jspa");
    }


    public String getContextPath()
    {
        return ActionContext.getRequest().getContextPath();
    }

    @Override
    @HtmlSafe
    public String getText(final String aTextName)
    {
        return i18nHelper.getText(aTextName);
    }

    public String getFaviconUrl()
    {
        return faviconUrl != null ? faviconUrl : "";
    }

    public void setFaviconUrl(String faviconUrl)
    {

        this.faviconUrl = ensureUrlCorrect(faviconUrl);
    }

    public String getDbBackedDefaultLogoUrl()
    {
        final String defaultUrl;
        if ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
        {
            defaultUrl =  ensureURLContext((String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL));
        }
        else
        {
            defaultUrl =  null;
        }
        return defaultUrl;
    }

    private String ensureURLContext(final String defaultUrl)
    {
        if (defaultUrl.startsWith("http"))
        {
            return defaultUrl;
        }
        else
        {
            return getBaseUrl() + defaultUrl;
        }
    }

    public void setLogoUrl(String logoUrl)
    {
        if (StringUtils.isBlank(logoUrl))
        {
            if("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
            {
                logoUrl = (String) globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
            }
            else
            {
                final LookAndFeelBean lookAndFeelBean = init();
                logoUrl=lookAndFeelBean.getLogoUrl();
            }
        }
        //logoUrl must start with 'http', 'file:', or else add the leading '/'
        if (isNotBlank(logoUrl) &&
                !logoUrl.startsWith("http") &&
                !logoUrl.startsWith("file:") &&
                !logoUrl.startsWith("/"))
        {
            logoUrl = "/" + logoUrl;
        }

        this.logoUrl = logoUrl;
    }


    private String getBaseUrl()
    {
        return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

    public int getMaxLogoInHeaderSize()
    {
        return lookAndFeelProperties.getMaxLogoHeight();
    }

    public boolean isStudioEnabled()
    {
        return featureManager.isOnDemand();
    }

    @RequiresXsrfCheck
    public String doRefreshResources() throws Exception
    {
        refreshResources();
        return getRedirect("LookAndFeel.jspa?refreshResourcesPerformed=true");
    }

    private void refreshResources()
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        // this causes the underlying counter to be bumped
        lookAndFeelBean.updateVersion(0);
        eventPublisher.publish(new LookAndFeelUpdatedEvent(getLoggedInUser(), Type.REFRESH_RESOURCES));
    }

    private boolean hasChanged(final String value1, final String value2)
    {
        return value1 == null ? value2 != null : !value1.equals(value2);
    }

    public boolean isCommonHeaderEnabled()
    {
        return true;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public String getLookAndFeelVersionNumber()
    {
        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        return lookAndFeelBean.getVersion() + "";
    }

    private String getUserFormatTypeName(String type)
    {
        final UserFormatModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            final String typeI18nKey = descriptor.getTypeI18nKey();
            if(StringUtils.isNotEmpty(typeI18nKey))
            {
                return getText(typeI18nKey);
            }
        }
        return type;
    }

    private Set<String> getUserFormatTypes()
    {
        return ImmutableSet.copyOf(userFormatTypes.get());
    }

    private Collection<UserFormatModuleDescriptor> getUserFormatModuleDescriptorsForType(String type)
    {
        return ImmutableList.copyOf(userFormatModuleDescriptors.forType(type));
    }

    private String getUserFormatKey(String type)
    {
        final UserFormatModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            return descriptor.getCompleteKey();
        }
        return type;
    }

    public boolean hasUserFormatsToEdit()
    {
        Set types = getUserFormatTypes();
        for (Object type1 : types)
        {
            String type = (String) type1;
            if (getUserFormatModuleDescriptorsForType(type).size() > 1)
            {
                return true;
            }
        }
        return false;
    }

    public String getAutoUpdateColorsLinkAsHtml()
    {
        return getText("jira.lookandfeel.updatecolors.link", "<a href='" + getContextPath() + "/secure/admin/LookAndFeel!autoUpdateColors.jspa?atl_token=" + getXsrfToken() + "'>", "</a>");
    }
}
