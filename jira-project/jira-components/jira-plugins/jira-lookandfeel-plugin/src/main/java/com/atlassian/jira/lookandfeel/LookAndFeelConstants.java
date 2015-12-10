package com.atlassian.jira.lookandfeel;

/**
 * 
 *
 * @since v5.0
 */
public class LookAndFeelConstants
{
    public static final String JIRA_LOGO_FILENAME = "jira-logo-original.png";
    public static final String JIRA_FAVICON_FILENAME = "jira-favicon-original.png";

    public static final String JIRA_FAVICON_HIRES_FILENAME = "jira-favicon-hires.png";
    public static final String JIRA_FAVICON_IEFORMAT_FILENAME = "jira-favicon-scaled.ico";

    public static final String JIRA_SCALED_LOGO_FILENAME = "jira-logo-scaled.png";
    public static final String JIRA_SCALED_FAVICON_FILENAME = "jira-favicon-scaled.png";

    public static final String JIRA_DEFAULT_LOGO_FILENAME = "jira-logo-default.png";
    public static final String JIRA_DEFAULT_FAVICON_FILENAME = "jira-favicon-default.png";

    public static final String JIRA_SCALED_DEFAULT_LOGO_FILENAME = "jira-logo-default-scaled.png";
    public static final String JIRA_SCALED_DEFAULT_FAVICON_FILENAME = "jira-favicon-default-scaled.png";
    public static final String USING_CUSTOM_DEFAULT_LOGO = "com.atlassian.jira.lookandfeel:isDefaultLogo";
    public static final String USING_CUSTOM_DEFAULT_FAVICON = "com.atlassian.jira.lookandfeel:isDefaultFavicon";

    public static final int LOGO_MAX_HEIGHT = 30; // TODO: This should come from rotp common header.
    public static final String DEFAULT_LOGO_WIDTH = "com.atlassian.jira.lookandfeel:logoWidth";
    public static final String DEFAULT_LOGO_HEIGHT = "com.atlassian.jira.lookandfeel:logoHeight";
    public static final String CUSTOM_DEFAULT_LOGO_URL = "com.atlassian.jira.lookandfeel:DefaultlogoURL";

    public static final String DEFAULT_FAVICON_WIDTH = "com.atlassian.jira.lookandfeel:faviconWidth";
    public static final String DEFAULT_FAVICON_HEIGHT = "com.atlassian.jira.lookandfeel:faviconHeight";
    public static final String CUSTOM_DEFAULT_FAVICON_URL = "com.atlassian.jira.lookandfeel:customDefaultFaviconURL";
    public static final String CUSTOM_DEFAULT_FAVICON_HIRES_URL = "com.atlassian.jira.lookandfeel:customDefaultFaviconHiresURL";


    // this is just to show in the L&F configuration section
    public static final String CSS_DEFAULTLOGO_URL = "/download/resources/com.atlassian.jira.lookandfeel:edit-look-and-feel/images/charlie48.png";

    public static final String BUILTIN_DEFAULT_LOGO_URL = "/images/icon-jira-logo.png";
    public static final String BUILTIN_DEFAULT_FAVICON_URL = "/favicon.ico";
    public static final String BUILTIN_DEFAULT_FAVICON_HIRES_URL = "/images/64jira.png";

    public static final String BUILTIN_DEFAULT_LOGO_WIDTH = Integer.toString(111);
    public static final String BUILTIN_DEFAULT_LOGO_HEIGHT = Integer.toString(30);

    public static final String BUILTIN_DEFAULT_FAVICON_WIDTH = Integer.toString(64);
    public static final String BUILTIN_DEFAULT_FAVICON_HEIGHT = Integer.toString(64);

    public static final  String USING_CUSTOM_LOGO = "com.atlassian.jira.lookandfeel:usingCustomLogo";
    public static final  String USING_CUSTOM_FAVICON = "com.atlassian.jira.lookandfeel:usingCustomFavicon";

    public static final String USING_BLACK_ARROW = "com.atlassian.jira.lookandfeel:usingBlackArrow";
}
