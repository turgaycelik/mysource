package com.atlassian.jira.timezone;

/**
 * @since v4.4
 */
public class RegionInfoImpl implements RegionInfo
{
    private final  String key;
    private final  String displayName;

    public RegionInfoImpl(String key, String displayName)
    {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey()
    {
        return key;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public int compareTo(RegionInfo regionInfo)
    {
        return displayName.compareTo(regionInfo.getDisplayName());
    }
}
