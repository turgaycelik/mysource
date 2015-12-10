package com.atlassian.jira.plugin.link.applinks.rest.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * AppLinkInfoBean
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
@XmlRootElement
public class AppLinkInfoBean
{
    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private boolean primary;

    @XmlElement
    private String authUrl;

    @XmlElement
    private String url;

    @XmlElement
    private boolean requireCredentials;

    public AppLinkInfoBean(String id, String url, String name, boolean primary, String authUrl, boolean requireCredentials)
    {
        super();
        this.id = id;
        this.url = url;
        this.name = name;
        this.primary = primary;
        this.authUrl = authUrl;
        this.requireCredentials = requireCredentials;
    }
}
