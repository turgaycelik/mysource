package com.atlassian.jira.plugin.headernav.legacy;

/**
* Copied from the jira-ondemand-theme-plugin, used only for migration
*/
@Deprecated
public class StudioTab
{
    private StudioTabType type;
    private String name;
    private String url;
    private boolean displayed;
    private String id;

    public StudioTab()
    {
    }

    public StudioTab(final StudioTabType type, final String name, final String url, final boolean displayed)
    {
        this.type = type;
        this.name = name;
        this.url = url;
        this.displayed = displayed;
    }

    public StudioTab(final String id, final StudioTabType type, final String name, final String url,
            final boolean displayed)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.url = url;
        this.displayed = displayed;
    }

    public static StudioTab displayedTabFromType(StudioTabType type)
    {
        return new StudioTab(type, null, null, true);
    }

    public static StudioTab fromString(final String value)
    {
        final String[] split = value.split("\n");

        if (split.length != 4)
        {
            return null;
        }

        final StudioTabType type = StudioTabType.valueOf(split[0]);
        final boolean displayed = Boolean.parseBoolean(split[3]);

        return new StudioTab(type, split[1], split[2], displayed);
    }

    public static String toString(final StudioTab studioTab)
    {
        String name;
        if (studioTab.getName() != null)
        {
            name = studioTab.getName();
        }
        else
        {
            name = "";
        }
        String url;
        if (studioTab.getUrl() != null)
        {
            url = studioTab.getUrl();
        }
        else
        {
            url = "";
        }
        return studioTab.getType() + "\n" + name + "\n" + url + "\n" + studioTab.isDisplayed();
    }

    /**
     * This implementation uses just the type if it's anything but custom, as only one of each type should exist, except
     * for custom.  It's used to help ensure that one of each custom one exists in the list of tabs configured.
     */
    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof StudioTab)) return false;

        final StudioTab studioTab = (StudioTab) o;
        if (type != studioTab.type) return false;
        if (type == StudioTab.StudioTabType.CUSTOM)
        {
            if (name != null ? !name.equals(studioTab.name) : studioTab.name != null) return false;
            if (url != null ? !url.equals(studioTab.url) : studioTab.url != null) return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = type.hashCode();
        if (type == StudioTab.StudioTabType.CUSTOM)
        {
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
        }
        return result;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public StudioTab.StudioTabType getType()
    {
        return type;
    }

    public void setType(final StudioTab.StudioTabType type)
    {
        this.type = type;
    }

    public boolean isDisplayed()
    {
        return displayed;
    }

    public void setDisplayed(final boolean displayed)
    {
        this.displayed = displayed;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * The type of the tab
     */
    enum StudioTabType
    {
        ISSUES("studio.tab.issues"), WIKI("studio.tab.wiki"), SOURCE("studio.tab.source"), REVIEWS(
            "studio.tab.reviews"), BUILDS("studio.tab.builds"), HOME("studio.tab.home"), CUSTOM("");
        private final String nameKey;

        StudioTabType(String nameKey)
        {
            this.nameKey = nameKey;
        }

        public String getNameKey()
        {
            return nameKey;
        }
    }
}
