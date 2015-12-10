package com.atlassian.jira.dev.backdoor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class SharePermissionInfoBean
{
    @XmlElement
    public Long id;
    @XmlElement
    public String type;
    @XmlElement
    public String param1;
    @XmlElement
    public String param2;

    public SharePermissionInfoBean() {};

    public SharePermissionInfoBean(Long id, String type, String param1, String param2)
    {
        this.id = id;
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }
}
