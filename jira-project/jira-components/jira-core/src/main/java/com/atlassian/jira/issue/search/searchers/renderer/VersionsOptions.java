package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.fields.option.GroupTextOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.google.common.collect.Lists;

import java.util.List;

class VersionsOptions extends Options
{
    private Option allReleased;
    private Option allUnreleased;
    private GroupTextOption released;
    private GroupTextOption unreleased;
    private GroupTextOption versions;

    public VersionsOptions()
    {
    }

    public void allReleased(Option option)
    {
        this.allReleased = option;
    }

    public void allUnreleased(Option option)
    {
        this.allUnreleased = option;
    }

    public void released(GroupTextOption options)
    {
        this.released = options;
    }

    public void unreleased(GroupTextOption options)
    {
        this.unreleased = options;
    }

    public void versions(GroupTextOption options)
    {
        this.versions = options;
    }

    public Option getAllReleased()
    {
        return allReleased;
    }

    public Option getAllUnreleased()
    {
        return allUnreleased;
    }

    public GroupTextOption getReleased()
    {
        return released;
    }

    public GroupTextOption getUnreleased()
    {
        return unreleased;
    }

    public GroupTextOption getVersions()
    {
        return versions;
    }

    public List<Option> all()
    {
        List<Option> all = Lists.newArrayList();
        if (null != no)
        {
            all.add(no);
        }
        if (null != versions)
        {
            all.add(versions);
        }
        if (null != allUnreleased)
        {
            all.add(allUnreleased);
        }
        if (null != allReleased)
        {
            all.add(allReleased);
        }
        if (null != released)
        {
            all.addAll(released.getChildOptions());
        }
        if (null != unreleased)
        {
            all.addAll(unreleased.getChildOptions());
        }
        return all;
    }
}
