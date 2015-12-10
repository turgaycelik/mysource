package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.admin.IntroductionProperty;
import com.atlassian.jira.avatar.GravatarSettings;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.setting.GzipCompression;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class ViewApplicationProperties extends ProjectActionSupport
{
    protected final UserPickerSearchService searchService;
    protected final LocaleManager localeManager;
    protected final TimeZoneService timeZoneService;
    protected final RendererManager rendererManager;
    private final PluginAccessor pluginAccessor;
    private final GzipCompression gZipCompression;
    private final IntroductionProperty introductionProperty;
    private final FeatureManager featureManager;
    private final JiraLicenseService jiraLicenseService;
    protected final GravatarSettings gravatarSettings;

    private boolean useGravatar;
    private String gravatarApiAddress;
    protected boolean disableInlineEdit;
    protected boolean criteriaAutoUpdate;
    private Boolean productRecommendations;
    private boolean projectDescriptionHtmlEnabled;


    public ViewApplicationProperties(UserPickerSearchService searchService, LocaleManager localeManager, TimeZoneService timeZoneService,
            RendererManager rendererManager, PluginAccessor pluginAccessor, GzipCompression gZipCompression,
            FeatureManager featureManager, IntroductionProperty introductionProperty, JiraLicenseService jiraLicenseService,
            GravatarSettings gravatarSettings)
    {
        this.searchService = searchService;
        this.localeManager = localeManager;
        this.timeZoneService = timeZoneService;
        this.rendererManager = rendererManager;
        this.pluginAccessor = pluginAccessor;
        this.gZipCompression = gZipCompression;
        this.featureManager = featureManager;
        this.introductionProperty = introductionProperty;
        this.jiraLicenseService = jiraLicenseService;
        this.gravatarSettings = gravatarSettings;

        this.useGravatar = gravatarSettings.isAllowGravatars();
        this.gravatarApiAddress = gravatarSettings.getCustomApiAddress();
        this.disableInlineEdit = getApplicationProperties().getOption(APKeys.JIRA_OPTION_DISABLE_INLINE_EDIT);
        this.criteriaAutoUpdate = getApplicationProperties().getOption(APKeys.JIRA_ISSUENAV_CRITERIA_AUTOUPDATE);
        this.projectDescriptionHtmlEnabled = getApplicationProperties().getOption(APKeys.JIRA_OPTION_PROJECT_DESCRIPTION_HTML_ENABLED);
    }

    public LocaleManager getLocaleManager()
    {
        return localeManager;
    }

    public String getJiraMode()
    {
        StringBuilder i18nString = new StringBuilder("admin.jira.mode.").append(getApplicationProperties().getString(APKeys.JIRA_MODE));
        return getText(i18nString.toString());
    }

    public String getDisplayNameOfLocale(Locale locale)
    {
        return locale.getDisplayName(getLocale());
    }

    public boolean useSystemTimeZone()
    {
        return timeZoneService.useSystemTimeZone();
    }

    public TimeZoneInfo getDefaultTimeZoneInfo()
    {
        return timeZoneService.getDefaultTimeZoneInfo(getJiraServiceContext());
    }

    public boolean isUseGravatar() { return useGravatar; }
    public void setUseGravatar(boolean useGravatar) { this.useGravatar = useGravatar; }

    public String getGravatarApiAddress() { return gravatarApiAddress; }
    public void setGravatarApiAddress(String gravatarApiAddress) { this.gravatarApiAddress = gravatarApiAddress; }

    public String getContactAdministratorsMessage()
    {
        String message = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        return rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(message, null);
    }

    public boolean getShowPluginHints()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_SHOW_MARKETING_LINKS);
    }

    public String getTacUrl()
    {
        return HelpUtil.getInstance().getHelpPath("application.properties.server.language.from.tac").getUrl();
    }

    public boolean isDisableInlineEdit()
    {
        return disableInlineEdit;
    }

    public boolean isShowDisableInlineEdit()
    {
        return pluginAccessor.isPluginEnabled("com.atlassian.jira.jira-issue-nav-plugin");
    }

    public boolean isShowDisableCriteriaAutoUpdate()
    {
        return pluginAccessor.isPluginEnabled("com.atlassian.jira.jira-issue-nav-plugin");
    }

    public boolean isCriteriaAutoUpdate()
    {
        return criteriaAutoUpdate;
    }

    /**
     * @return a boolean indicating if the "Show Product Recommendations" option is available
     */
    public final boolean isProductRecommendationsAvailable()
    {
        return featureManager.isOnDemand();
    }

    /**
     * @return a boolean indicating if the "Show Product Recommendations" option can be edited
     */
    public final boolean isProductRecommendationsEditable()
    {
        return isEvaluationLicense();
    }

    /**
     * @return a boolean indicating whether "Show Product Recommendations" is enabled
     */
    public final boolean isProductRecommendations()
    {
        if (productRecommendations == null)
        {
            productRecommendations = featureManager.isOnDemand()
                    && isEvaluationLicense()
                    && !getApplicationProperties().getOption(APKeys.JIRA_OPTION_ADS_DISABLED);
        }

        return productRecommendations;
    }

    /**
     * @param productRecommendations whether "Show Product Recommendations" is enabled
     */
    public void setProductRecommendations(boolean productRecommendations)
    {
        this.productRecommendations = productRecommendations;
    }

    /**
     * @return a boolean indicating whether the Project Description Mode option should be visible
     */
    public final boolean isShowProjectDescriptionHtmlEnabled()
    {
        return !this.featureManager.isOnDemand();
    }

    public boolean isProjectDescriptionHtmlEnabled()
    {
        return projectDescriptionHtmlEnabled;
    }

    public void setProjectDescriptionHtmlEnabled(boolean projectDescriptionHtmlEnabled)
    {
        this.projectDescriptionHtmlEnabled = projectDescriptionHtmlEnabled;
    }

    public GzipCompression getGzipCompression()
    {
        return gZipCompression;
    }

    public IntroductionProperty getIntroductionProperty()
    {
        return introductionProperty;
    }

    public boolean isBackgroundIndexingAvailable()
    {
        return true;
    }

    private boolean isEvaluationLicense()
    {
        return jiraLicenseService.getLicense().isEvaluation();
    }

    public Map<String, String> getAllowedLanguages()
    {
        Map<String, String> allowedLanguages = new LinkedHashMap<String, String>();

        allowedLanguages.put(APKeys.Languages.ARMENIAN, getText("admin.jira.allowed.language.armenian"));
        allowedLanguages.put(APKeys.Languages.BASQUE, getText("admin.jira.allowed.language.basque"));
        allowedLanguages.put(APKeys.Languages.BRAZILIAN, getText("admin.jira.allowed.language.brazilian"));
        allowedLanguages.put(APKeys.Languages.BULGARIAN, getText("admin.jira.allowed.language.bulgarian"));
        allowedLanguages.put(APKeys.Languages.CATALAN, getText("admin.jira.allowed.language.catalan"));
        allowedLanguages.put(APKeys.Languages.CHINESE, getText("admin.jira.allowed.language.chinese"));
        allowedLanguages.put(APKeys.Languages.CJK, getText("admin.jira.allowed.language.cjk"));
        allowedLanguages.put(APKeys.Languages.CZECH, getText("admin.jira.allowed.language.czech"));
        allowedLanguages.put(APKeys.Languages.DANISH, getText("admin.jira.allowed.language.danish"));
        allowedLanguages.put(APKeys.Languages.DUTCH, getText("admin.jira.allowed.language.dutch"));
        allowedLanguages.put(APKeys.Languages.ENGLISH, getText("admin.jira.allowed.language.english.aggressive.stemming"));
        allowedLanguages.put(APKeys.Languages.ENGLISH_MODERATE_STEMMING, getText("admin.jira.allowed.language.english.moderate.stemming"));
        allowedLanguages.put(APKeys.Languages.ENGLISH_MINIMAL_STEMMING, getText("admin.jira.allowed.language.english.minimal.stemming"));
        allowedLanguages.put(APKeys.Languages.FINNISH, getText("admin.jira.allowed.language.finnish"));
        allowedLanguages.put(APKeys.Languages.FRENCH, getText("admin.jira.allowed.language.french"));
        allowedLanguages.put(APKeys.Languages.GERMAN, getText("admin.jira.allowed.language.german"));
        allowedLanguages.put(APKeys.Languages.GREEK, getText("admin.jira.allowed.language.greek"));
        allowedLanguages.put(APKeys.Languages.HUNGARIAN, getText("admin.jira.allowed.language.hungarian"));
        allowedLanguages.put(APKeys.Languages.ITALIAN, getText("admin.jira.allowed.language.italian"));
        allowedLanguages.put(APKeys.Languages.NORWEGIAN, getText("admin.jira.allowed.language.norwegian"));
        allowedLanguages.put(APKeys.Languages.PORTUGUESE, getText("admin.jira.allowed.language.portuguese"));
        allowedLanguages.put(APKeys.Languages.ROMANIAN, getText("admin.jira.allowed.language.romanian"));
        allowedLanguages.put(APKeys.Languages.RUSSIAN, getText("admin.jira.allowed.language.russian"));
        allowedLanguages.put(APKeys.Languages.SPANISH, getText("admin.jira.allowed.language.spanish"));
        allowedLanguages.put(APKeys.Languages.SWEDISH, getText("admin.jira.allowed.language.swedish"));
        allowedLanguages.put(APKeys.Languages.THAI, getText("admin.jira.allowed.language.thai"));
        allowedLanguages.put(APKeys.Languages.OTHER, getText("admin.jira.allowed.language.other"));

        return allowedLanguages;
    }

    public String getCurrentIndexingLanguageDescription()
    {
        return getAllowedLanguages().get(getApplicationProperties().getString(APKeys.JIRA_I18N_LANGUAGE_INPUT));
    }

    public boolean isUnifiedUserManagementEnabled()
    {
        return featureManager.isEnabled("unified.usermanagement");
    }
}
