package com.atlassian.jira.functest.framework.backdoor;

public class SharePermissionInfo
{
    public Long id;
    public String type;
    public String param1;
    public String param2;

    public SharePermissionInfo() {}

    public SharePermissionInfo(Long id, String type, String param1, String param2)
    {
        this.id = id;
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }
}
