package com.atlassian.jira.issue.search.searchers.transformer;

public class VersionSearchInput extends SearchInput
{
    private static class VersionInputType extends InputType
    {
        final static VersionInputType NO_VERSION = new VersionInputType("NO_VERSION");
        final static VersionInputType ALL_UNRELEASED = new VersionInputType("ALL_UNRELEASED");
        final static VersionInputType ALL_RELEASED = new VersionInputType("ALL_RELEASED");
        final static VersionInputType VERSION = new VersionInputType("VERSION");

        private VersionInputType(String name)
        {
            super(name);
        }
    }

    private VersionSearchInput(VersionInputType type, String value)
    {
        super(type, value);
    }

    public static VersionSearchInput noVersions()
    {
        return new VersionSearchInput(VersionInputType.NO_VERSION, null);
    }

    public static VersionSearchInput allUnreleased()
    {
        return new VersionSearchInput(VersionInputType.ALL_UNRELEASED, null);
    }

    public static VersionSearchInput allReleased()
    {
        return new VersionSearchInput(VersionInputType.ALL_RELEASED, null);
    }

    public static VersionSearchInput version(String value)
    {
        return new VersionSearchInput(VersionInputType.VERSION, value);
    }

    public boolean isNoVersion()
    {
        return VersionInputType.NO_VERSION.equals(type);
    }

    public boolean isAllUnreleased()
    {
        return VersionInputType.ALL_UNRELEASED.equals(type);
    }

    public boolean isAllReleased()
    {
        return VersionInputType.ALL_RELEASED.equals(type);
    }

    public boolean isVersion()
    {
        return VersionInputType.VERSION.equals(type);
    }
}
