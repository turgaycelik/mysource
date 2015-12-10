package com.atlassian.jira.dev.backdoor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class FilterInfoBean
{
    @XmlElement
    public long id;
    @XmlElement
    public String name;
    @XmlElement
    public String description;
    @XmlElement
    public String owner;
    @XmlElement
    public Boolean favourite;
    @XmlElement
    public Long favouriteCount;
    @XmlElement
    public List<SharePermissionInfoBean> permissions = new ArrayList<SharePermissionInfoBean>();

    public FilterInfoBean() {}

    public FilterInfoBean(long id, String name, String description, String owner, Boolean favourite, Long favouriteCount, List<SharePermissionInfoBean> permissions)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.favourite = favourite;
        this.favouriteCount = favouriteCount;
        this.permissions = permissions;
    }
}
