package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * @deprecated you shouldn't be using this class directly, use {@link com.atlassian.jira.util.BuildUtilsInfo} instead.
 */
///CLOVER:OFF
public final class BuildUtils
{
    @SuppressWarnings ({ "deprecation" })
    private static final Logger log = Logger.getLogger(BuildUtils.class);

    private static final String BUILD_PARTNER_NAME = null;
    private static final Collection<Locale> UNAVAILABLE_LOCALES = Collections.emptyList();
    private static final String UNPARSED_DATE = "03-02-2015";
    private static final String BUILD_NUMBER = "6346";
    private static final String VERSION = "6.3.15";
    private static final String COMMIT_ID = "dbc023dd75cecacf443c4b235f66124b15f5c5fe";
    private static final String MINIMUM_UPGRADABLE_VERSION = "4.0";
    private static final String MINIMUM_UPGRADABLE_BUILD_NUMBER = "466";

    private static final Date PARSED_DATE;

    static
    {
        Date parsedDate = null;
        try
        {
            parsedDate = new SimpleDateFormat("dd-MM-yyy", Locale.US).parse(UNPARSED_DATE);
        }
        catch (ParseException e)
        {
            log.fatal("Cannot Parse date: " + UNPARSED_DATE + ".  Returning null for date");
        }
        PARSED_DATE = parsedDate;
    }

    public static String getVersion()
    {
        return VERSION;
    }

    public static String getCommitId()
    {
        return COMMIT_ID;
    }

    public static String getCurrentBuildNumber()
    {
        return BUILD_NUMBER;
    }

    public static Date getCurrentBuildDate()
    {
        return PARSED_DATE;
    }

    public static String getBuildPartnerName()
    {
        return BUILD_PARTNER_NAME;
    }

    public static Collection<Locale> getUnavailableLocales()
    {
        return UNAVAILABLE_LOCALES;
    }

    public static String getMinimumUpgradableVersion()
    {
        return MINIMUM_UPGRADABLE_VERSION;
    }

    public static String getMinimumUpgradableBuildNumber()
    {
        return MINIMUM_UPGRADABLE_BUILD_NUMBER;
    }
}
