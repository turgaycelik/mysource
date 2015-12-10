package com.atlassian.jira.timezone;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * @since v4.4
 */
public class TimeZoneServiceImpl implements TimeZoneService, TimeZoneResolver
{
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final UserPreferencesManager userPreferencesManager;
    private final TimeZoneIdsProvider timeZoneIdsProvider;
    private static final String[] ALLOWED_REGIONS = { "Etc", "Pacific", "America", "Antarctica", "Atlantic", "Africa", "Europe", "Asia", "Indian", "Australia"};
    private static final String[] FILTERED_IDS = { "Etc/UCT", "Etc/UTC"};

    public TimeZoneServiceImpl(ApplicationProperties applicationProperties, PermissionManager permissionManager, UserPreferencesManager userPreferencesManager)
    {
        this(applicationProperties, permissionManager, userPreferencesManager, new TimeZoneIds());
    }

    public TimeZoneServiceImpl(ApplicationProperties applicationProperties, PermissionManager permissionManager, UserPreferencesManager userPreferencesManager, TimeZoneIdsProvider timeZoneIdsProvider)
    {
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.userPreferencesManager = userPreferencesManager;
        this.timeZoneIdsProvider = timeZoneIdsProvider;
    }


    @Override
    public TimeZoneInfo getDefaultTimeZoneInfo(JiraServiceContext serviceContext)
    {
        String defaultTimeZoneStr = getDefaultTimeZoneId();
        if (StringUtils.isEmpty(defaultTimeZoneStr))
        {
            TimeZone jvmTimeZone = getJVMTimeZoneInfo(serviceContext).toTimeZone();
            return getTimeZoneInfo(jvmTimeZone, serviceContext);
        }
        else
        {
            return getTimeZoneInfo(TimeZone.getTimeZone(defaultTimeZoneStr), serviceContext);
        }
    }

    private String getDefaultTimeZoneId()
    {
        return applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE);
    }

    @Override
    public boolean useSystemTimeZone()
    {
        return StringUtils.isEmpty(getDefaultTimeZoneId());
    }

    @Override
    public List<RegionInfo> getTimeZoneRegions(JiraServiceContext serviceContext)
    {
        final List<RegionInfo> regions = new ArrayList<RegionInfo>(ALLOWED_REGIONS.length);
        for (String regionId : ALLOWED_REGIONS)
        {
            regions.add(new RegionInfoImpl(regionId, serviceContext.getI18nBean().getText("timezone.region." + regionId.toLowerCase())));
        }
        // sort the Regions by I18n-ed display name
        Collections.sort(regions);

        return regions;
    }

    private String getRegionFromTimeZoneId(String timeZoneId)
    {
        final int firstSlashIndex = timeZoneId.indexOf("/");
        if (firstSlashIndex != -1)
        {
            final String region = timeZoneId.substring(0, firstSlashIndex);
            return region;
        }
        else
        {
            return timeZoneId;
        }
    }

    @Override
    public List<TimeZoneInfo> getTimeZoneInfos(JiraServiceContext serviceContext)
    {
        final Iterable<String> timeZoneIds = getAllowedCannonicalIds();

        final List<TimeZoneInfo> timeZoneInfos = new ArrayList<TimeZoneInfo>();
        for (String timeZoneId : timeZoneIds)
        {
            final String region = getRegionFromTimeZoneId(timeZoneId);
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);

            TimeZoneInfoImpl timeZoneInfo = new TimeZoneInfoImpl(timeZoneId, timeZone.getDisplayName(serviceContext.getI18nBean().getLocale()), timeZone, serviceContext.getI18nBean(), region);
            timeZoneInfos.add(timeZoneInfo);
        }
        //Sort the timezones by GMT offset and then by city name.
        Collections.sort(timeZoneInfos);
        return timeZoneInfos;
    }

    private Iterable<String> getAllowedCannonicalIds()
    {
        return Iterables.filter(getCanonicalIds(), new Predicate<String>()
        {
            @Override
            public boolean apply(@Nullable String timeZoneId)
            {
                for (String filteredId : FILTERED_IDS)
                {
                    if (filteredId.equals(timeZoneId))
                    {
                        return false;
                    }
                }
                for (String allowedRegion : ALLOWED_REGIONS)
                {
                    if (timeZoneId != null && timeZoneId.startsWith(allowedRegion))
                    {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    protected Set<String> getCanonicalIds()
    {
        return timeZoneIdsProvider.getCanonicalIds();
    }

    private TimeZoneInfo getTimeZoneInfo(TimeZone timeZone, JiraServiceContext serviceContext)
    {
        return new TimeZoneInfoImpl(timeZone.getID(), timeZone.getDisplayName(serviceContext.getI18nBean().getLocale()), timeZone, serviceContext.getI18nBean(), getRegionFromTimeZoneId(timeZone.getID()));
    }

    @Override
    public TimeZoneInfo getJVMTimeZoneInfo(JiraServiceContext serviceContext)
    {
        TimeZone canonicalJVMTimeZone = timeZoneIdsProvider.canonicalise(getJVMTimeZone());
        return getTimeZoneInfo(canonicalJVMTimeZone, serviceContext);
    }

    protected TimeZone getJVMTimeZone()
    {
        return TimeZone.getDefault();
    }

    @Override
    public void setDefaultTimeZone(String timeZoneId, JiraServiceContext serviceContext)
    {
        TimeZone timeZone = validateTimeZoneId(timeZoneId);
        checkAdministratorPermission(serviceContext);
        applicationProperties.setString(APKeys.JIRA_DEFAULT_TIMEZONE, timeZone.getID());
    }

    private TimeZone validateTimeZoneId(String timeZoneId)
    {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        Set<String> allowedIds = new HashSet<String>();
        Iterables.addAll(allowedIds, getAllowedCannonicalIds());
        if (!allowedIds.contains(timeZoneId))
        {
            throw new IllegalArgumentException("Timezone with id '" + timeZoneId + "' not supported. Only these IDs are supported: '" + allowedIds.toString() + "'");
        }
        if (!timeZone.getID().equals(timeZoneId))
        {
            throw new IllegalArgumentException("No timezone found with id '" + timeZoneId + "'");
        }
        return timeZone;
    }

    @Override
    public void clearDefaultTimeZone(JiraServiceContext serviceContext)
    {
        checkAdministratorPermission(serviceContext);
        applicationProperties.setString(APKeys.JIRA_DEFAULT_TIMEZONE, null);
    }

    private void checkAdministratorPermission(JiraServiceContext serviceContext)
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, serviceContext.getLoggedInUser()))
        {
            throw new RuntimeException("This user does not have the JIRA Administrator permission. This permission is required to change the default timezone.");
        }
    }

    @Override
    public String getDefaultTimeZoneRegionKey()
    {
        if (useSystemTimeZone())
        {
            return SYSTEM;
        }
        return getRegionFromTimeZoneId(getDefaultTimeZoneId());
    }

    @Override
    public void clearUserDefaultTimeZone(JiraServiceContext serviceContext)
    {
        Preferences preferences = userPreferencesManager.getPreferences(serviceContext.getLoggedInUser());
        try
        {
            preferences.setString(PreferenceKeys.USER_TIMEZONE, null);
        }
        catch (AtlassianCoreException e)
        {
            throw new RuntimeException("Failed to set timezone for user '" + serviceContext.getLoggedInUser().getName() + "' to JIRA's default timezone", e);
        }
    }

    @Override
    public TimeZoneInfo getUserTimeZoneInfo(JiraServiceContext serviceContext)
    {
        Preferences preferences = userPreferencesManager.getPreferences(serviceContext.getLoggedInUser());
        String timezoneId = (preferences != null) ? preferences.getString(PreferenceKeys.USER_TIMEZONE) : null;
        if (StringUtils.isEmpty(timezoneId))
        {
            return getDefaultTimeZoneInfo(serviceContext);
        }
        TimeZone timeZone = TimeZone.getTimeZone(timezoneId);
        return getTimeZoneInfo(timeZone, serviceContext);
    }

    @Override
    public boolean usesJiraTimeZone(JiraServiceContext serviceContext)
    {
        Preferences preferences = userPreferencesManager.getPreferences(serviceContext.getLoggedInUser());
        return StringUtils.isEmpty(preferences.getString(PreferenceKeys.USER_TIMEZONE));
    }

    @Override
    public void setUserDefaultTimeZone(String timeZoneId, JiraServiceContext serviceContext)
    {
        Preferences preferences = userPreferencesManager.getPreferences(serviceContext.getLoggedInUser());
        validateTimeZoneId(timeZoneId);
        try
        {
            preferences.setString(PreferenceKeys.USER_TIMEZONE, timeZoneId);
        }
        catch (AtlassianCoreException e)
        {
            throw new RuntimeException("Failed to set the timezone with id '" + timeZoneId + "' for user '" + serviceContext.getLoggedInUser() + "'", e);
        }
    }

    @Override
    public TimeZone getDefaultTimeZone(JiraServiceContext serviceContext)
    {
        return getDefaultTimeZoneInfo(serviceContext).toTimeZone();
    }

    @Override
    public TimeZone getUserTimeZone(JiraServiceContext serviceContext)
    {
        return getUserTimeZoneInfo(serviceContext).toTimeZone();
    }
}
