package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

public final class Permission
{
    public String id;
    public String key;

    public Permission id(int id)
    {
        this.id = String.valueOf(id);
        return this;
    }

    public Permission key(String key)
    {
        this.key = key;
        return this;
    }
}
