package com.atlassian.jira.rest.v2.issue;

import com.atlassian.plugins.rest.common.expand.Expandable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bean to represent projects in a createmeta issue request.
 *
 * @since v5.0
 */
public class CreateMetaProjectBean
{
    @XmlAttribute
    private String expand;

    @XmlElement
    private String self;

    @XmlElement
    private String id;

    @XmlElement
    private String key;

    @XmlElement
    private String name;

    @XmlElement
    private Map<String, String> avatarUrls;

    @XmlElement
    @Expandable
    private List<CreateMetaIssueTypeBean> issuetypes;

    public CreateMetaProjectBean(final String self, final String id, final String key, final String name, final Map<String, String> avatarUrls, final List<CreateMetaIssueTypeBean> issuetypes)
    {
        this.self = self;
        this.id = id;
        this.key = key;
        this.name = name;
        this.avatarUrls = avatarUrls;
        this.issuetypes = (issuetypes != null) ? issuetypes : Collections.<CreateMetaIssueTypeBean>emptyList();
    }
}
