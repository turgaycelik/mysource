package com.atlassian.jira.lookandfeel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.lookandfeel.upload.UploadService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import org.apache.commons.io.FileUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;

public class LookAndFeelPropertiesImpl implements LookAndFeelProperties
{
    private static final String CUSTOMIZE_COLORS = "com.atlassian.jira.lookandfeel.customizeColors";
    private static final String LOGO_CHOICE = "com.atlassian.jira.lookandfeel.logoChoice";
    private static final String FAVICON_CHOICE = "com.atlassian.jira.lookandfeel.faviconChoice";
    private static final String DEFAULT_LOGO_URL = "com.atlassian.jira.lookandfeel.logo.default.url";
    private static final String DEFAULT_FAVICON_URL = "com.atlassian.jira.lookandfeel.favicon.default.url";
    private static final boolean DEFAULT_WHITE_ARROW = false;

    private final PluginSettings globalSettings;
    private final LookAndFeelBean lookAndFeelBean;
    private final UploadService uploadService;
    private final ApplicationProperties applicationProperties;

    public LookAndFeelPropertiesImpl(final PluginSettingsFactory pluginSettingsFactory,
            final ApplicationProperties applicationProperties, final UploadService uploadService)
    {
        this.applicationProperties = applicationProperties;
        this.globalSettings = pluginSettingsFactory.createGlobalSettings();
        this.lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        this.uploadService = uploadService;
    }

    @Override
    public void setCustomizeColors(final boolean value)
    {
        globalSettings.put(CUSTOMIZE_COLORS, String.valueOf(value));
    }

    @Override
    public boolean getCustomizeColors()
    {
        final String customizeColors = (String) globalSettings.get(CUSTOMIZE_COLORS);
        if (customizeColors != null)
        {
            return Boolean.parseBoolean(customizeColors);
        }
        else
        {
            return false;
        }
    }

    @Override
    public void setLogoChoice(final LogoChoice choice)
    {
        notNull("Choice cannot be null", choice);
        globalSettings.put(LOGO_CHOICE, choice.toString());
    }

    @Override
    public LogoChoice getLogoChoice()
    {
        LogoChoice logoOptionChoice = null;

        final String logoChoice = (String) globalSettings.get(LOGO_CHOICE);
        if (logoChoice != null)
        {
            logoOptionChoice = LogoChoice.safeValueOf(logoChoice);
        }

        return logoOptionChoice != null ? logoOptionChoice : LogoChoice.JIRA;
    }

    @Override
    public void setFaviconChoice(final LogoChoice choice)
    {
        notNull("Choice cannot be null", choice);
        globalSettings.put(FAVICON_CHOICE, choice.toString());
    }

    @Override
    public LogoChoice getFaviconChoice()
    {
        LogoChoice faviconOptionChoice = null;
        final String faviconChoice = (String) globalSettings.get(FAVICON_CHOICE);
        if (faviconChoice != null)
        {
            faviconOptionChoice = LogoChoice.safeValueOf(faviconChoice);
        }

        return faviconOptionChoice != null ? faviconOptionChoice : LogoChoice.JIRA;
    }

    @Override
    public void reset()
    {
        setCustomizeColors(false);
        setLogoChoice(LogoChoice.JIRA);
        setFaviconChoice(LogoChoice.JIRA);
        resetDefaultFaviconUrl();
        resetDefaultLogoUrl();
        resetApplicationTitleShownOnLogo();
    }

    private void resetApplicationTitleShownOnLogo()
    {
        applicationProperties.setOption("jira.lf.logo.show.application.title", false);
    }

    @Override
    public void uploadDefaultLogo(final BufferedImage image)
    {
        notNull("image cannot be null", image);
        final Map<String, String> imageInfo = uploadService.uploadDefaultLogo(image);
        if (imageInfo != null)
        {
            setLogoDefaults(imageInfo.get("path"), imageInfo.get("width"), imageInfo.get("height"));
        }
    }

    @Override
    public void setDefaultLogo(final String url, final String width, final String height)
    {
        notNull("URL cannot be null", url);
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO, toStringTrueFalse(true));
        setLogoDefaults(url, width, height);
    }

    @Override
    public void resetDefaultLogo()
    {
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO, toStringTrueFalse(false));
        deleteUploadedCustomDefaultLogo();
        setLogoDefaults(applicationProperties.getDefaultString(APKeys.JIRA_LF_LOGO_URL), applicationProperties.getDefaultString(APKeys.JIRA_LF_LOGO_WIDTH),
                applicationProperties.getDefaultString(APKeys.JIRA_LF_LOGO_HEIGHT));
    }

    private void deleteUploadedCustomDefaultLogo()
    {
        final File file = new File(uploadService.getLogoDirectory(), LookAndFeelConstants.JIRA_SCALED_DEFAULT_LOGO_FILENAME);
        FileUtils.deleteQuietly(file);
    }

    @Override
    public void uploadDefaultFavicon(final BufferedImage image)
    {
        notNull("image cannot be null", image);
        final Map<String, String> imageInfo = uploadService.uploadDefaultFavicon(image);
        if (imageInfo != null)
        {
            setFaviconDefaults(imageInfo.get("path"), imageInfo.get("path"), imageInfo.get("width"), imageInfo.get("height"));
        }
    }

    @Override
    public void setDefaultFavicon(final String url, final String width, final String height)
    {
        notNull("url cannot be null", url);
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON, toStringTrueFalse(true));
        setFaviconDefaults(url, url, width, height);
    }

    @Override
    public void setDefaultFavicon(final String url, final String hiresUrl, final String width, final String height)
    {
        notNull("url cannot be null", url);
        notNull("hiresUrl cannot be null", hiresUrl);
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON, toStringTrueFalse(true));
        setFaviconDefaults(url, hiresUrl, width, height);
    }

    @Override
    public void resetDefaultFavicon()
    {
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON, toStringTrueFalse(false));
        deleteUploadedCustomDefaultFavicon();
        setFaviconDefaults(LookAndFeelConstants.BUILTIN_DEFAULT_FAVICON_URL,
                LookAndFeelConstants.BUILTIN_DEFAULT_FAVICON_HIRES_URL,
                LookAndFeelConstants.BUILTIN_DEFAULT_FAVICON_WIDTH,
                LookAndFeelConstants.BUILTIN_DEFAULT_FAVICON_HEIGHT);
    }

    private void deleteUploadedCustomDefaultFavicon()
    {
        final File file = new File(uploadService.getLogoDirectory(), LookAndFeelConstants.JIRA_SCALED_DEFAULT_FAVICON_FILENAME);
        FileUtils.deleteQuietly(file);
    }

    @Override
    public boolean isUsingCustomDefaultLogo()
    {
        return ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)));
    }

    @Override
    public boolean isUsingCustomDefaultFavicon()
    {
        return ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON)));
    }

    @Override
    public String getDefaultCssLogoUrl()
    {
        if (isUsingCustomDefaultLogo())
        {
            return (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
        }
        else
        {
            return LookAndFeelConstants.CSS_DEFAULTLOGO_URL;
        }
    }

    @Override
    public String getDefaultLogoUrl()
    {
        if (isUsingCustomDefaultLogo())
        {
            return (String) globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
        }
        else
        {
            return LookAndFeelConstants.BUILTIN_DEFAULT_LOGO_URL;
        }
    }

    @Override
    public String getDefaultFaviconUrl()
    {
        if (isUsingCustomDefaultFavicon())
        {
            return (String) globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
        }
        else
        {
            return LookAndFeelConstants.BUILTIN_DEFAULT_FAVICON_URL;
        }
    }

    @Override
    public String getDefaultFaviconHiresUrl()
    {
        if (isUsingCustomDefaultFavicon())
        {
            final String hires = (String) globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_HIRES_URL);
            return hires != null ? hires : (String) globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
        }
        else
        {
            return LookAndFeelConstants.BUILTIN_DEFAULT_FAVICON_HIRES_URL;
        }
    }

    private void setFaviconDefaults(final String path, final String hiresPath, final String width, final String height)
    {
        // out of sync sometimes for some reason...
        if (getLogoChoice() == LogoChoice.JIRA)
        {
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_FAVICON, toStringTrueFalse(false));
        }
        globalSettings.put(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL, path);
        globalSettings.put(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_HIRES_URL, hiresPath);
        globalSettings.put(LookAndFeelConstants.DEFAULT_FAVICON_WIDTH, width);
        globalSettings.put(LookAndFeelConstants.DEFAULT_FAVICON_HEIGHT, height);
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_FAVICON)))
        {
            lookAndFeelBean.setFaviconUrl(path);
            lookAndFeelBean.setFaviconHiResUrl(hiresPath);
        }
    }

    private void setLogoDefaults(final String path, final String width, final String height)
    {
        // out of sync sometimes for some reason...
        if (getLogoChoice() == LogoChoice.JIRA)
        {
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_LOGO, toStringTrueFalse(false));
        }
        globalSettings.put(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL, path);
        globalSettings.put(LookAndFeelConstants.DEFAULT_LOGO_WIDTH, width);
        globalSettings.put(LookAndFeelConstants.DEFAULT_LOGO_HEIGHT, height);
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_LOGO)))
        {
            lookAndFeelBean.setLogoUrl(path);
            lookAndFeelBean.setLogoHeight(height);
            lookAndFeelBean.setLogoWidth(width);
        }
    }

    @Override
    public void setBlackArrow(final boolean blackArrow)
    {
        globalSettings.put(LookAndFeelConstants.USING_BLACK_ARROW, String.valueOf(blackArrow));
    }

    @Override
    public boolean isBlackArrow()
    {
        final Object shouldUseBlackArrowAsObject = globalSettings.get(LookAndFeelConstants.USING_BLACK_ARROW);
        if (shouldUseBlackArrowAsObject == null)
        {
            return DEFAULT_WHITE_ARROW;
        }
        return Boolean.parseBoolean(shouldUseBlackArrowAsObject.toString());
    }

    @Override
    public void resetDefaultFaviconUrl()
    {
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON)))
        {
            globalSettings.remove(DEFAULT_FAVICON_URL);
        }
        else
        {
            globalSettings.put(DEFAULT_FAVICON_URL, LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
        }
    }

    @Override
    public void resetDefaultLogoUrl()
    {
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
        {
            globalSettings.remove(DEFAULT_LOGO_URL);
        }
        else
        {
            globalSettings.put(DEFAULT_LOGO_URL, LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
        }
    }

    @Override
    public boolean isApplicationTitleShownOnLogo()
    {
        return applicationProperties.getOption("jira.lf.logo.show.application.title");
    }

    @Override
    public void setApplicationTitleShownOnLogoTo(final boolean shouldShow)
    {
        applicationProperties.setOption("jira.lf.logo.show.application.title", shouldShow);
    }

    @Override
    public int getMaxLogoHeight()
    {
        return LookAndFeelConstants.LOGO_MAX_HEIGHT;
    }
}
