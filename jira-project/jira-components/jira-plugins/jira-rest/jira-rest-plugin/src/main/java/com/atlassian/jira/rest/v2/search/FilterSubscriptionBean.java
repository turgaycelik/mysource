package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.rest.v2.issue.UserBean;

import javax.xml.bind.annotation.XmlElement;

public class FilterSubscriptionBean
{
    @XmlElement
    private Long id;

    @XmlElement
    private UserBean user;

    @XmlElement
    private GroupJsonBean group;

    public FilterSubscriptionBean() { }

    public FilterSubscriptionBean(Long id, UserBean user, GroupJsonBean group)
    {
        this.id = id;
        this.user = user;
        this.group = group;
    }

    public Long getId()
    {
        return id;
    }

    public UserBean getUser()
    {
        return user;
    }

    public GroupJsonBean getGroup()
    {
        return group;
    }
}
