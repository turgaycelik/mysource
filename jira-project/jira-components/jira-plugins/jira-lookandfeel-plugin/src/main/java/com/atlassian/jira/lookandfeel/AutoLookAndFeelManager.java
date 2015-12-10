package com.atlassian.jira.lookandfeel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.lookandfeel.upload.UploadService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class AutoLookAndFeelManager
{
    private static final HSBColor LIGHT_TEXT = new HSBColor(Color.white);
    private static final HSBColor DARK_TEXT = new HSBColor(new Color(41, 41, 41));

    private static final String JUST_UPDATED_KEY = "lookandfeel.auto.just.updated";

    private static final String[] COLOR_KEYS = {
            // Background colours
            APKeys.JIRA_LF_TOP_BGCOLOUR,
            APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR,
            APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR,
            APKeys.JIRA_LF_MENU_BGCOLOUR,
            APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR,
            // Text colours
            APKeys.JIRA_LF_TOP_TEXTCOLOUR,
            APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR,
            APKeys.JIRA_LF_MENU_TEXTCOLOUR,
            APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR
    };

    private final ApplicationProperties applicationProperties;
    private final ApplicationPropertiesService applicationPropertiesService;
    private final UserPropertyManager userPropertyManager;
    private final UploadService uploadService;
    private final PluginSettings globalSettings;

    public AutoLookAndFeelManager(final ApplicationProperties applicationProperties,
            final ApplicationPropertiesService applicationPropertiesService,
            final UserPropertyManager userPropertyManager, final UploadService uploadService,
            final PluginSettingsFactory pluginSettingsFactory)
    {
        this.applicationProperties = applicationProperties;
        this.applicationPropertiesService = applicationPropertiesService;
        this.userPropertyManager = userPropertyManager;
        this.uploadService = uploadService;
        this.globalSettings = pluginSettingsFactory.createGlobalSettings();
    }

    public void generateFromLogo(final User user)
    {
        final ImageInfo logoInfo = getLogoInfo();
        if (logoInfo.isTransparentBackground())
        {
            // If the logo is mostly white, use the default colour scheme
            if (logoInfo.isMostlyWhite())
            {
                applyDefaultColors();
            }
            // If the logo is light and it has a predominant colour, use a darker version of that colour as the background
            else if (logoInfo.isTooBrightForWhiteBackground() && logoInfo.getPredominantColor() != null)
            {
                setBaseColor(logoInfo.getPredominantColor().saturateByAmount(30).darkenByAmount(30));
            }
            // Otherwise, use a white background
            else
            {
                setBaseColors(new HSBColor(Color.white), logoInfo.getPredominantColor());
            }
        }
        else
        {
            // If the background is monochrome or not very saturated, find a more interesting colour for the hero button
            final HSBColor base = logoInfo.getBackgroundColor();
            if (ImageInfo.isMonochrome(base) || base.getSaturation() < 30)
            {
                setBaseColors(base, logoInfo.getPredominantColor());
            }
            else
            {
                setBaseColor(base);
            }
        }

        setJustUpdated(user, true);
    }

    public boolean isJustUpdated(final User user)
    {
        final boolean justUpdated = userPropertyManager.getPropertySet(user).getBoolean(JUST_UPDATED_KEY);
        if (justUpdated)
        {
            // Reset the flag now that we have read it
            setJustUpdated(user, false);
        }

        return justUpdated;
    }

    public void setJustUpdated(final User user, final boolean justUpdated)
    {
        userPropertyManager.getPropertySet(user).setBoolean(JUST_UPDATED_KEY, justUpdated);
    }

    public boolean isDefaultColorScheme()
    {
        for (final String key : COLOR_KEYS)
        {
            if (!isDefaultColor(key))
            {
                return false;
            }
        }

        return true;
    }

    public boolean isDefaultColor(final String key)
    {
        final ApplicationProperty property = applicationPropertiesService.getApplicationProperty(key);
        return property.getMetadata().getDefaultValue().equals(property.getCurrentValue());
    }

    public void applyDefaultColors()
    {
        for (final String key : COLOR_KEYS)
        {
            applyDefaultColor(key);
        }
    }

    public void applyDefaultColor(final String key)
    {
        final ApplicationProperty property = applicationPropertiesService.getApplicationProperty(key);
        if (!property.getMetadata().getDefaultValue().equals(property.getCurrentValue()))
        {
            applicationProperties.setString(key, property.getMetadata().getDefaultValue());
        }
    }

    public void backupColorScheme()
    {
        for (final String key : COLOR_KEYS)
        {
            backupColor(key);
        }
    }

    public void backupColor(final String key)
    {
        applicationProperties.setString(key + ".backup", applicationProperties.getString(key));
    }

    public void restoreBackupColorScheme()
    {
        for (final String key : COLOR_KEYS)
        {
            restoreBackupColor(key);
        }
    }

    public void restoreBackupColor(final String key)
    {
        final String backupValue = applicationProperties.getString(key + ".backup");
        if (backupValue != null)
        {
            applicationProperties.setString(key, backupValue);
        }
        else
        {
            applyDefaultColor(key);
        }
    }

    public void setBaseColor(final HSBColor base)
    {
        setBaseColors(base, getHeroButtonBackground(base));
    }

    public void setBaseColors(final HSBColor base, HSBColor heroButtonBackground)
    {
        if (heroButtonBackground == null)
        {
            heroButtonBackground = getHeroButtonBackground(base);
        }

        final HSBColor baseHighlight = getBaseHighlight(heroButtonBackground);
        final HSBColor separator = getSeparator(base, heroButtonBackground);

        applicationProperties.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, base.getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, baseHighlight.getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, separator.getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_MENU_BGCOLOUR, baseHighlight.getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR, heroButtonBackground.getHexString());

        // Figure out appropriate text colours
        applicationProperties.setString(APKeys.JIRA_LF_TOP_TEXTCOLOUR, getTextColor(base).getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, getTextColor(baseHighlight).getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_MENU_TEXTCOLOUR, getTextColor(baseHighlight).getHexString());
        applicationProperties.setString(APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR, getTextColor(heroButtonBackground).getHexString());
    }

    private HSBColor getBaseHighlight(final HSBColor heroButtonBackground)
    {
        return heroButtonBackground.darkenByAmount(13);
    }

    private HSBColor getSeparator(final HSBColor base, final HSBColor heroButtonBackground)
    {
        if (ImageInfo.isMonochrome(base) && !ImageInfo.isMonochrome(heroButtonBackground))
        {
            return heroButtonBackground.darkenByAmount(45).desaturateByAmount(25);
        }

        return base.darkenByAmount(18).desaturateByAmount(30);
    }

    private HSBColor getHeroButtonBackground(final HSBColor base)
    {
        HSBColor result = (base.getBrightness() <= 73) ? base.lightenByAmount(27) : base.darkenByAmount(27);
        return result.desaturateByAmount(5);
    }

    private HSBColor getTextColor(final HSBColor background)
    {
        // Check the difference in perceived brightness to determine which text colour to use
        final float lightTextDiff = Math.abs(background.getPerceivedBrightness() - LIGHT_TEXT.getPerceivedBrightness());
        final float darkTextDiff = Math.abs(background.getPerceivedBrightness() - DARK_TEXT.getPerceivedBrightness());

        return (lightTextDiff > darkTextDiff) ? LIGHT_TEXT : DARK_TEXT;
    }

    private ImageInfo getLogoInfo()
    {
        try
        {
            final BufferedImage image = ImageIO.read(getLogoInputStream());
            return new ImageInfo(image);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private InputStream getLogoInputStream() throws IOException
    {
        if ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_LOGO)))
        {
            final File logoDirectory = uploadService.getLogoDirectory();
            return new FileInputStream(new File(logoDirectory, LookAndFeelConstants.JIRA_SCALED_LOGO_FILENAME));
        }

        return ServletContextProvider.getServletContext().getResourceAsStream(LookAndFeelConstants.BUILTIN_DEFAULT_LOGO_URL);
    }
}
