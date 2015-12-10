package com.atlassian.jira.config.properties;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.ui.header.CurrentHeader;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;

import org.apache.commons.lang.StringUtils;

/**
 * Bean mainly used to maintain a version number for all the menu colours.  This allows us
 * to cache this information until it is updated (i.e.:version number is incremented).
 */
public class LookAndFeelBean
{
    public static class LookAndFeelChangedEvent
    {
    }

    private final ApplicationProperties applicationProperties;

    /**
     * @deprecated in JIRA 6.0 -- the common header is always enabled.
     * @return true
     */
    @Deprecated
    public boolean isCommonHeader()
    {
        return true;
    }

    protected CurrentHeader getCurrentHeader()
    {
        return ComponentAccessor.getComponent(CurrentHeader.class);
    }
    /**
     * These are the "intended" colours for the JIRA Header.
     *
     * @since v4.0
     */
    public static final class DefaultColours
    {
        public static final String TOP_HIGHLIGHTCOLOUR = "#296ca3";
        public static final String TOP_TEXTHIGHLIGHTCOLOUR = "#f0f0f0";
        public static final String TOP_SEPARATOR_BGCOLOUR = "#2e3d54";

        public static final String TOP_BGCOLOUR = "#205081";
        public static final String TOP_TEXTCOLOUR = "#ffffff";

        public static final String MENU_BGCOLOUR = "#3b73af";
        public static final String MENU_TEXTCOLOUR = "#ffffff";
        public static final String MENU_SEPARATOR = "#f0f0f0";

        public static final String TEXT_LINKCOLOR = "#3b73af";
        public static final String TEXT_ACTIVELINKCOLOR = "#3b73af";
        public static final String TEXT_HEADINGCOLOR = "#333333";

        private DefaultColours() {}
    }

    /**
     * These are the intended colours for the new common Header.
     *
     * @since v5.2
     */
    public static final class DefaultCommonColours
    {
        /** @deprecated since JIRA 6.0. use {@link DefaultColours.TOP_HIGHLIGHTCOLOUR} directly. */
        public static final String TOP_HIGHLIGHTCOLOUR = DefaultColours.TOP_HIGHLIGHTCOLOUR;
        /** @deprecated since JIRA 6.0. use {@link DefaultColours.TOP_SEPARATOR_BGCOLOUR} directly. */
        public static final String TOP_SEPARATOR_BGCOLOUR = DefaultColours.TOP_SEPARATOR_BGCOLOUR;
        /** @deprecated since JIRA 6.0. use {@link DefaultColours.TOP_BGCOLOUR} directly. */
        public static final String TOP_BGCOLOUR = DefaultColours.TOP_BGCOLOUR;

        public static final String HERO_BUTTON_TXTCOLOUR = "#ffffff";
        public static final String HERO_BUTTON_BASEBGCOLOUR = "#3b7fc4";

        private DefaultCommonColours() {}
    }

    public static class DefaultFaviconDimensions
    {
        public static final String FAVICON_DIMENSION = "16";
        public static final String FAVICON_HIRES_DIMENSION = "32";

        private DefaultFaviconDimensions() {}
    }

    protected LookAndFeelBean(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public static LookAndFeelBean getInstance(final ApplicationProperties ap)
    {
        return new LookAndFeelBean(ap);
    }

    private void incrementVersion()
    {
        long version = getVersion();
        version++;
        updateFlushCounter(version);
    }

    private void updateFlushCounter(final long version)
    {
        applicationProperties.setString(APKeys.WEB_RESOURCE_FLUSH_COUNTER, Long.toString(version));
        EventPublisher publisher = ComponentAccessor.getComponent(EventPublisher.class);
        if (publisher != null) {
            publisher.publish(new LookAndFeelChangedEvent());
        }
    }

    public String stripHash(String colour)
    {
        if (StringUtils.isNotBlank(colour) && colour.startsWith("#"))
        {
            return colour.substring(1, colour.length());
        }
        return colour;
    }

    private void setValue(final String key, final String value)
    {
        applicationProperties.setString(key, value);
        incrementVersion();
    }

    /**
     * Convenience method used by data import (see JRA-11680) to update version number to
     * the greater version number after the import (to make sure LF values wont be cached).
     *
     * @param oldVersion the previous version
     */
    public void updateVersion(long oldVersion)
    {
        long currentVersion = getVersion();

        if (oldVersion > currentVersion)
        {
            updateFlushCounter(++oldVersion);
        }
        else
        {
            updateFlushCounter(++currentVersion);
        }
    }

    public long getVersion()
    {
        final String editVersion = applicationProperties.getDefaultBackedString(APKeys.WEB_RESOURCE_FLUSH_COUNTER);
        return Long.parseLong(StringUtils.isNotEmpty(editVersion) ? editVersion : "1");
    }

    /*
     * ======== LOGO ==================
     */
    public String getLogoUrl()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL);
    }

    public String getAbsoluteLogoUrl()
    {
        String jiraLogo = getLogoUrl();
        if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = ComponentAccessor.getComponent(WebResourceUrlProvider.class).getStaticResourcePrefix(UrlMode.AUTO) + jiraLogo;
        }

        return jiraLogo;
    }

    public void setLogoUrl(final String logoUrl)
    {
        setValue(APKeys.JIRA_LF_LOGO_URL, logoUrl);
    }

    public String getLogoWidth()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH);
    }

    public String getLogoPixelWidth()
    {
        return getLogoWidth() + "px";
    }

    public void setLogoWidth(final String logoWidth)
    {
        setValue(APKeys.JIRA_LF_LOGO_WIDTH, logoWidth);
    }

    public String getLogoHeight()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT);
    }

    public String getLogoPixelHeight()
    {
        return getLogoHeight() + "px";
    }

    public void setLogoHeight(final String logoHeight)
    {
        setValue(APKeys.JIRA_LF_LOGO_HEIGHT, logoHeight);
    }

    /*
    * ======== FAVICON ==================
    */
    public String getFaviconUrl()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_FAVICON_URL);
    }

    public void setFaviconUrl(String faviconUrl)
    {
        setValue(APKeys.JIRA_LF_FAVICON_URL, faviconUrl);
    }

    public String getFaviconWidth()
    {
        return DefaultFaviconDimensions.FAVICON_DIMENSION;
    }

    public String getFaviconHeight()
    {
         return DefaultFaviconDimensions.FAVICON_DIMENSION;
    }

     public String getFaviconHiResUrl()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_FAVICON_HIRES_URL);
    }

    public void setFaviconHiResUrl(String faviconUrl)
    {
        setValue(APKeys.JIRA_LF_FAVICON_HIRES_URL, faviconUrl);
    }

    public String getFaviconHiResWidth()
    {
        return DefaultFaviconDimensions.FAVICON_HIRES_DIMENSION;
    }


    public String getFaviconHiResHeight()
    {
         return DefaultFaviconDimensions.FAVICON_HIRES_DIMENSION;
    }

    /*
    * ======== TOP ==================
    */
    public String getTopBackgroundColour()
    {
        String defaultColour = DefaultCommonColours.TOP_BGCOLOUR;
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_BGCOLOUR, defaultColour);
    }

    public void setTopBackgroundColour(final String topBackgroundColour)
    {
        setValue(APKeys.JIRA_LF_TOP_BGCOLOUR, topBackgroundColour);
    }

    public String getTopTxtColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_TEXTCOLOUR, DefaultColours.TOP_TEXTCOLOUR);
    }

    public void setTopTxtColour(final String topTxtColour)
    {
        setValue(APKeys.JIRA_LF_TOP_TEXTCOLOUR, topTxtColour);
    }

    public String getTopHighlightColor()
    {
        String defaultColour = DefaultCommonColours.TOP_HIGHLIGHTCOLOUR;
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, defaultColour);
    }

    public void setTopHighlightColor(final String newValue)
    {
        setValue(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, newValue);
    }

    public String getTopTextHighlightColor()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, DefaultColours.TOP_TEXTHIGHLIGHTCOLOUR);
    }

    public void setTopTextHighlightColor(final String newValue)
    {
        setValue(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, newValue);
    }

    public String getTopSeparatorBackgroundColor()
    {
        String defaultColour = DefaultCommonColours.TOP_SEPARATOR_BGCOLOUR;
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, defaultColour);
    }

    public void setTopSeparatorBackgroundColor(final String newValue)
    {
        setValue(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, newValue);
    }

    /*
     * ======== MENU NAVIGATION ==================
     */
    public String getMenuTxtColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_MENU_TEXTCOLOUR, DefaultColours.MENU_TEXTCOLOUR);
    }

    public void setMenuTxtColour(final String menuTxtColour)
    {
        setValue(APKeys.JIRA_LF_MENU_TEXTCOLOUR, menuTxtColour);
    }

    public String getMenuBackgroundColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_MENU_BGCOLOUR, DefaultColours.MENU_BGCOLOUR);
    }

    public void setMenuBackgroundColour(final String menuBackgroundColour)
    {
        setValue(APKeys.JIRA_LF_MENU_BGCOLOUR, menuBackgroundColour);
    }

    public String getMenuSeparatorColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_MENU_SEPARATOR, DefaultColours.MENU_SEPARATOR);
    }

    public void setMenuSeparatorColour(final String menuSeparatorColour)
    {
        setValue(APKeys.JIRA_LF_MENU_SEPARATOR, menuSeparatorColour);
    }

    /*
     * ======== JIRA TEXT AND LINKS ==================
     */
    public String getTextHeadingColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, DefaultColours.TEXT_HEADINGCOLOR);
    }

    public void setTextHeadingColour(final String textHeadingColour)
    {
        setValue(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, textHeadingColour);
    }

    public String getTextLinkColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, DefaultColours.TEXT_LINKCOLOR);
    }

    public void setTextLinkColour(final String textLinkColour)
    {
        setValue(APKeys.JIRA_LF_TEXT_LINKCOLOUR, textLinkColour);
    }

    public String getTextActiveLinkColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, DefaultColours.TEXT_ACTIVELINKCOLOR);
    }

    public void setTextActiveLinkColour(final String textActiveLinkColour)
    {
        setValue(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, textActiveLinkColour);
    }

    /*
     * ======== HERO BUTTON COLOURS ===================
     */
    public String getHeroButtonTextColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR, DefaultCommonColours.HERO_BUTTON_TXTCOLOUR);
    }

    public void setHeroButtonTextColour(final String heroButtonTextColour)
    {
        setValue(APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR,heroButtonTextColour);
    }

    public String getHeroButtonBaseBGColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR, DefaultCommonColours.HERO_BUTTON_BASEBGCOLOUR);
    }

    public void setHeroButtonBaseBGColour(final String heroButtonBaseBGColour)
    {
        setValue(APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR,heroButtonBaseBGColour);
    }

    /*
     * ======== GADGET CHROME COLORS ==================
     */
    public String getGadgetChromeColor(final String id)
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_GADGET_COLOR_PREFIX + id);
    }

    public void setGadgetChromeColor(final String id, final String gadgetChromeColor)
    {
        setValue(APKeys.JIRA_LF_GADGET_COLOR_PREFIX + id, gadgetChromeColor);
    }

    /*
     * ======== MISC ==================
     */
    public String getApplicationID()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_APPLICATION_ID);
    }

    /**
     * Performs a lookup on the application properties for the specified key. If the key returns a null value,
     * returns the default value specified. This would happen if no value exists in the database AND no value exists
     * in the jira-application.properties file.
     *
     * @param key          the Application Properties key to look up
     * @param defaultValue the value to return if the key yields null
     * @return the value of the key in the Application Properties, or the default value specified
     */
    public String getDefaultBackedString(final String key, final String defaultValue)
    {
        final String value = applicationProperties.getString(key);
        return value == null ? defaultValue : value;
    }

    public String getDefaultBackedString(final String key)
    {
        if (key == null)
        {
            return null;
        }
        if (applicationProperties.getString(key) != null)
        {
            return applicationProperties.getString(key);
        }

        // Rely on the real defaults
        return applicationProperties.getDefaultBackedString(key);
    }
}
