package com.atlassian.jira.timezone;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.HttpRequestLocal;

import java.util.List;

/**
 * Caches user time zones on a per-request basis.
 *
 * @since v4.4
 */
public class TimeZoneServiceCachingDecorator implements TimeZoneService
{
    /**
     * Per-request user time zone cache.
     */
    private final HttpRequestLocal<TimeZoneInfo> userTimeZoneCache = new HttpRequestLocal<TimeZoneInfo>(TimeZoneServiceCachingDecorator.class.getName().replace('/', '.') + "#userTimeZone");

    /**
     * The actual time zone service.
     */
    private final TimeZoneService service;

    /**
     * Creates a new TimeZoneServiceCachingDecorator.
     *
     * @param timeZoneService a TimeZoneService
     */
    public TimeZoneServiceCachingDecorator(TimeZoneService timeZoneService)
    {
        this.service = Assertions.notNull(timeZoneService);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Mutators. Calling these methods clears the cache.
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void setUserDefaultTimeZone(String timeZoneId, JiraServiceContext serviceContext)
    {
        service.setUserDefaultTimeZone(timeZoneId, serviceContext);
        userTimeZoneCache.remove();
    }

    @Override
    public void clearUserDefaultTimeZone(JiraServiceContext serviceContext)
    {
        service.clearUserDefaultTimeZone(serviceContext);
        userTimeZoneCache.remove();
    }

    @Override
    public void setDefaultTimeZone(String timeZoneId, JiraServiceContext serviceContext)
    {
        service.setDefaultTimeZone(timeZoneId, serviceContext);
        userTimeZoneCache.remove();
    }

    @Override
    public void clearDefaultTimeZone(JiraServiceContext serviceContext)
    {
        service.clearDefaultTimeZone(serviceContext);
        userTimeZoneCache.remove();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Cached accessors. Cache misses populate the cache.
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public TimeZoneInfo getUserTimeZoneInfo(JiraServiceContext serviceContext)
    {
        TimeZoneInfo timeZoneInfo = userTimeZoneCache.get();
        if (timeZoneInfo != null)
        {
            return timeZoneInfo;
        }

        // hit the real service.
        TimeZoneInfo userTimeZoneInfo = service.getUserTimeZoneInfo(serviceContext);
        userTimeZoneCache.set(userTimeZoneInfo);

        return userTimeZoneInfo;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Non-cached accessors.
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public TimeZoneInfo getJVMTimeZoneInfo(JiraServiceContext serviceContext)
    {
        return service.getJVMTimeZoneInfo(serviceContext);
    }

    @Override
    public TimeZoneInfo getDefaultTimeZoneInfo(JiraServiceContext serviceContext)
    {
        return service.getDefaultTimeZoneInfo(serviceContext);
    }

    @Override
    public List<RegionInfo> getTimeZoneRegions(JiraServiceContext serviceContext)
    {
        return service.getTimeZoneRegions(serviceContext);
    }

    @Override
    public List<TimeZoneInfo> getTimeZoneInfos(JiraServiceContext serviceContext)
    {
        return service.getTimeZoneInfos(serviceContext);
    }

    @Override
    public String getDefaultTimeZoneRegionKey()
    {
        return service.getDefaultTimeZoneRegionKey();
    }

    @Override
    public boolean useSystemTimeZone()
    {
        return service.useSystemTimeZone();
    }

    @Override
    public boolean usesJiraTimeZone(JiraServiceContext serviceContext)
    {
        return service.usesJiraTimeZone(serviceContext);
    }
}
