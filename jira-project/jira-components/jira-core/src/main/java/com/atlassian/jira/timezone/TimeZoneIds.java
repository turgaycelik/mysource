package com.atlassian.jira.timezone;

import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.cluster.ClusterSafe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

/**
 * This class is used to generate canonical timezone IDs as described here
 * http://joda-time.sourceforge.net/timezones.html
 *
 * @since v4.4
 */
class TimeZoneIds implements TimeZoneIdsProvider
{
    static final long cNow = System.currentTimeMillis();

    private static final Logger log = Logger.getLogger(TimeZoneIds.class);

    @ClusterSafe
    static final Cache<TimeZone, TimeZone> canonicalTimeZoneMap = CacheBuilder.newBuilder()
       .build(
               new CacheLoader<TimeZone, TimeZone>()
               {
                   @Override
                   public TimeZone load(TimeZone timeZone)
                   {
                       try
                       {
                           DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
                           return dateTimeZone.toTimeZone();
                       }
                       catch (Exception e)
                       {
                           return timeZone;
                       }
                   }
               });

    public Set<String> getCanonicalIds()
    {
        // we only return ids that are supported by both JodaTime and the running JDK
        Set<String> jdkIds = Sets.newHashSet(TimeZone.getAvailableIDs());
        Set<String> jodaIds = DateTimeZone.getAvailableIDs();

        // time zone ids that exist in both JodaTime and the JDK
        Set<String> commonIds = Sets.intersection(jodaIds, jdkIds);

        Set<ZoneData> zones = Sets.newTreeSet();
        for (String id : commonIds)
        {
            zones.add(new ZoneData(id, DateTimeZone.forID(id)));
        }

        Set<String> canonicalIds = Sets.newHashSetWithExpectedSize(zones.size());
        for (ZoneData zone : zones)
        {
            if (zone.isCanonical())
            {
                canonicalIds.add(zone.getCanonicalID());
            }
        }

        return canonicalIds;
    }

    public TimeZone canonicalise(TimeZone timeZone)
    {
        try
    {
            return canonicalTimeZoneMap.get(timeZone);
        }
        catch (ExecutionException ex)
        {
            log.debug("Time zone with id '" + timeZone.getID() + "' not recognised.");
            return timeZone;
        }
    }

    private static class ZoneData implements Comparable
    {
        private final String iID;
        private final DateTimeZone iZone;

        ZoneData(String id, DateTimeZone zone)
        {
            iID = id;
            iZone = zone;
        }

        public String getID()
        {
            return iID;
        }

        public String getCanonicalID()
        {
            return iZone.getID();
        }

        public boolean isCanonical()
        {
            return getID().equals(getCanonicalID());
        }

        public int compareTo(Object obj)
        {
            ZoneData other = (ZoneData) obj;

            int offsetA = iZone.getStandardOffset(cNow);
            int offsetB = other.iZone.getStandardOffset(cNow);

            if (offsetA < offsetB)
            {
                return -1;
            }
            if (offsetA > offsetB)
            {
                return 1;
            }

            int result = getCanonicalID().compareTo(other.getCanonicalID());

            if (result != 0)
            {
                return result;
            }

            if (isCanonical())
            {
                if (!other.isCanonical())
                {
                    return -1;
                }
            }
            else if (other.isCanonical())
            {
                return 1;
            }

            return getID().compareTo(other.getID());
        }
    }

}
