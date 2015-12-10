package com.atlassian.jira.timezone;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;

import java.util.List;

/**
 * The TimeZoneService manages the JIRA wide default timezone.
 * This default time zone is either the JVM default time zone or can be configured by the administrator.
 *
 * The TimeZoneService is also used to retrieve a list of time zone and regions for the administration UI.
 *
 * @since v4.4
 */
@PublicApi
public interface TimeZoneService
{
    public static final String SYSTEM = "System";
    public static final String JIRA = "JIRA";

    /**
     * Returns the time zone of the JVM
     *
     * @param serviceContext  JIRA Service Context containing the user that is retrieving the time zone information.
     *
     * @return the time zone of the JVM
     */
    TimeZoneInfo getJVMTimeZoneInfo(JiraServiceContext serviceContext);


    /**
     * Returns the default time zone which is globally configured.
     * This can either be the JVM time zone or a time zone configured by the administrator.
     *
     * @param serviceContext JIRA Service Context containing the user that is retrieving the time zone information.
     * @return Returns the default time zone
     */
    TimeZoneInfo getDefaultTimeZoneInfo(JiraServiceContext serviceContext);

    /**
     * Returns all time zone regions. Timezones are grouped by region, so
     * it is more convenient for the user to find the correct time zone.
     * Regions have a key and an i18n display name.
     *
     * @param serviceContext JIRA Service Context containing the user that is retrieving the time zone information.
     * @return  Returns all time zone regions.
     */
    List<RegionInfo> getTimeZoneRegions(JiraServiceContext serviceContext);


    /**
     * Retrieves all time zones.
     * Returns only a subset of the time zones which are provided by the JVM.
     * This cannonical list of time zones can be found here:
     * See http://joda-time.sourceforge.net/time zones.html
     *
     * @param serviceContext  JIRA Service Context containing the user that is retrieving the time zone information.
     * @return  all supported time zones
     */
    List<TimeZoneInfo> getTimeZoneInfos(JiraServiceContext serviceContext);

    /**
     * Returns true if the JVM time zone is used, otherwise false.
     *
     * @return true if the JVM time zone is used.
     */
    boolean useSystemTimeZone();

    /**
     * Allows to set the default time zone.
     * The user requires the JIRA administrator permission to perform this operation.
     * Only time zones with IDs returned by getTimeZoneInfos() are supported.
     *
     * @param timeZoneId the id of the time zone.
     * @param serviceContext  JIRA Service Context containing the user that changing the default time zone.
     */
    void setDefaultTimeZone(String timeZoneId, JiraServiceContext serviceContext);

    /**
     * Reset the default time zone to the JVM time zone.
     *
     * @param serviceContext JIRA Service Context containing the user that changing the default time zone.
     */
    void clearDefaultTimeZone(JiraServiceContext serviceContext);

    /**
     * Return the region key of the current default time zone.
     * If the region key is SYSTEM, it indicates it is using the JVM time zone.
     *
     * @return region key.
     */
    String getDefaultTimeZoneRegionKey();

    /**
     * Resets the time zone for this user to the JIRA default time zone.
     *
     * @param serviceContext JIRA Service Context containing the user.
     */
    void clearUserDefaultTimeZone(JiraServiceContext serviceContext);

    /**
     * Return the time zone for this user. This can either be a time zone which the user has defined in its preferences or
     * the JIRA default time zone.
     * NB: If the user is null JIRA's default time zone is returned.
     *
     * @param serviceContext JIRA Service Context containing the user.
     * @return  the TimeZone for this user.
     */
    TimeZoneInfo getUserTimeZoneInfo(JiraServiceContext serviceContext);

    /**
     * Returns true if this user is using the JIRA default time zone or false if the user has is using a custom time zone.
     *
     * @param serviceContext
     * @return true if this user is using the JIRA default time zone or false if the user has is using a custom time zone.
     */
    boolean usesJiraTimeZone(JiraServiceContext serviceContext);

    /**
     * Sets the default time zone for this user.
     *
     * @param timeZoneId the time zone id
     * @param serviceContext  JIRA Service Context containing the user.
     */
    void setUserDefaultTimeZone(String timeZoneId, JiraServiceContext serviceContext);

}
