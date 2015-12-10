package com.atlassian.jira.lookandfeel;

import java.awt.image.BufferedImage;

/**
 *
 *
 * @since v4.4
 */
public interface LookAndFeelProperties
{
    void setCustomizeColors(boolean value);

    boolean getCustomizeColors();

    void setLogoChoice(LogoChoice choice);

    LogoChoice getLogoChoice();

    void setFaviconChoice(LogoChoice choice);

    LogoChoice getFaviconChoice();

    /**
     * As of future versions, JIRA will no longer support dynamic adding of default favicons and logos. Plugins that wish
     * to modify default logo should add the logo as a web resource and use
     * #{@link #setDefaultLogo(String, String, String)} instead. Note that JIRA already provides ability to
     * upload custom logos and favicons in its Administration section.
     *
     * @param image image to upload
     * @deprecated use {@link #setDefaultFavicon(String, String, String)} instead
     *
     */
    void uploadDefaultLogo(BufferedImage image);

    /**
     * Set 'custom' default logo for this JIRA instance. It will be used as the 'Default' option for
     * logo in the Look & Feel JIRA settings.
     *
     * @param url URL of the logo
     * @param width width of the logo
     * @param height height of the logo
     */
    void setDefaultLogo(String url, String width, String height);

    /**
     * Reset the default logo to the original logo shipped with JIRA.
     *
     */
    void resetDefaultLogo();

    /**
     * As of future versions, JIRA will no longer support dynamic adding of default favicons and logos. Plugins that wish
     * to modify default favicon should add the favicon as a web resource and use
     * #{@link #setDefaultFavicon(String, String, String)} instead. Note that JIRA already provides ability to
     * upload custom logos and favicons in its Administration section.
     *
     * @param image image to upload
     * @deprecated use {@link #setDefaultFavicon(String, String, String)} instead
     *
     */
    @Deprecated
    void uploadDefaultFavicon(BufferedImage image);

    /**
     * Set 'custom' default favicon for this JIRA instance. It will be used as the 'Default' option for
     * the favicon in the Look & Feel JIRA settings.
     *
     * @param url URL of the favicon
     * @param width width of the favicon
     * @param height height of the favicon
     */
    void setDefaultFavicon(String url, String width, String height);

    /**
     * Set 'custom' default favicon for this JIRA instance. It will be used as the 'Default' option for
     * the favicon in the Look & Feel JIRA settings.
     *
     * @param url URL of the favicon
     * @param hiresUrl URL of the favicon in high resolution for display in L&F admin
     * @param width width of the favicon
     * @param height height of the favicon
     */
    void setDefaultFavicon(String url, String hiresUrl, String width, String height);

    /**
     * Reset the default favicon to the original logo shipped with JIRA.
     *
     */
    void resetDefaultFavicon();

    /**
     * Reset all L&F properties to defaults.
     *
     */
    void reset();

    boolean isBlackArrow();

    void setBlackArrow(final boolean blackArrow);

    boolean isUsingCustomDefaultLogo();

    boolean isUsingCustomDefaultFavicon();

    /**
     * URL to the default logo for display in the L&F configuration section.
     *
     * @return current default logo URL
     */
    String getDefaultCssLogoUrl();

    /**
     * Get the default logo URL of this JIRA instance. This is not necessarily the logo currently served.
     *
     * @return default logo URL
     */
    String getDefaultLogoUrl();

    /**
     * Get the default favicon URL of this JIRA instance. This is not necessarily the favicon currently served.
     *
     * @return default favicon URL
     */
    String getDefaultFaviconUrl();

    /**
     * Get the default favicon URL of this JIRA instance. This is not necessarily the favicon currently served.
     * This is URL to the favicon in high resolution, for display in L&F administration.
     *
     * @return default favicon URL
     */
    String getDefaultFaviconHiresUrl();


    // TODO remove - probably superfluous
    /**
     * Internal method, don't use
     *
     * @deprecated not for external usage, subject to removing
     */
    @Deprecated
    void resetDefaultFaviconUrl();

    /**
     * Internal method, don't use
     *
     * @deprecated not for external usage, subject to removing
     */
    @Deprecated
    void resetDefaultLogoUrl();

    /**
     * Whether the site title should be shown next to the logo.
     *
     * @return true if the site title should be shown next the logo; otherwise, false.
     */
    boolean isApplicationTitleShownOnLogo();

    /**
     * Configures whether JIRA shows the application title next to the logo.
     *
     * @param shouldShow true if JIRA should show the application title next to the logo.
     */
    void setApplicationTitleShownOnLogoTo(boolean shouldShow);

    int getMaxLogoHeight();
}
