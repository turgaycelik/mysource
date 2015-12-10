package com.atlassian.jira.plugin.headernav.navlinks.spi;


import com.atlassian.plugins.navlink.spi.Project;

public class NavlinkProject implements Project
{
    private final String key;

    public NavlinkProject(String key) {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return key;
    }
}
