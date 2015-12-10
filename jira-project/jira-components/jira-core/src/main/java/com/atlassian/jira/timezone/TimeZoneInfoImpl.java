package com.atlassian.jira.timezone;

import com.atlassian.jira.util.I18nHelper;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @since v4.4
 */
public class TimeZoneInfoImpl implements TimeZoneInfo
{
    private final String timeZoneId;
    private final String displayName;
    private final TimeZone timeZone;
    private final I18nHelper i18nHelper;
    private final String regionKey;


    public TimeZoneInfoImpl(String timeZoneId, String displayName, TimeZone timeZone, I18nHelper i18nHelper, String regionKey)
    {
        this.timeZoneId = timeZoneId;
        this.displayName = displayName;
        this.timeZone = timeZone;
        this.i18nHelper = i18nHelper;
        this.regionKey = regionKey;
    }

    @Override
    public String getTimeZoneId()
    {
        return timeZoneId;
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public String getGMTOffset()
    {
        return formatGMTOffset(timeZone);
    }

    private String formatGMTOffset(TimeZone timeZone)
    {
        int rawOffset = timeZone.getRawOffset();
        String prefix = "+";
        if (rawOffset < 0)
        {
            prefix = "-";
        }
        rawOffset = (rawOffset < 0) ? rawOffset * -1: rawOffset;
        long hours = TimeUnit.HOURS.convert(rawOffset, TimeUnit.MILLISECONDS);
        long remainingOffset = rawOffset - TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
        long minutes = TimeUnit.MINUTES.convert(remainingOffset, TimeUnit.MILLISECONDS);

        return String.format("(GMT%s%02d:%02d)", prefix, hours, minutes);
    }

    @Override
    public String getCity()
    {
        String zoneLabel = timeZoneId;
        // Use the real time zone ID from Joda where possible in case the JDK converts an unrecognised ID to "GMT".
        // This only affects tests where we read all Joda zones, while in production we only use Joda zones that are
        // also recognised by the JDK.
        if (TimeZoneService.SYSTEM.equals(zoneLabel) || TimeZoneService.JIRA.equals(zoneLabel))
        {
            zoneLabel = timeZone.getID();
        }
        zoneLabel = zoneLabel.replaceAll("/", ".");
        return i18nHelper.getText("timezone.zone." + zoneLabel.toLowerCase());
    }

    @Override
    public String getRegionKey()
    {
        return regionKey;
    }

    @Override
    public TimeZone toTimeZone()
    {
        return timeZone;
    }

    @Override
    public int compareTo(TimeZoneInfo timeZoneInfo)
    {
        int rawOffset = timeZoneInfo.toTimeZone().getRawOffset();
        if (timeZone.getRawOffset() < rawOffset)
        {
            return -1;
        }
        else if(timeZone.getRawOffset() > rawOffset)
        {
            return 1;
        }
        return getCity().compareTo(timeZoneInfo.getCity());
    }

    @Override
    public String toString()
    {
        return "TimeZoneInfoImpl{" + timeZoneId + '}';
    }
}
