package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.common.IconBean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean to represent {@link com.atlassian.jira.issue.link.RemoteIssueLink remote issue links}.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
@XmlRootElement (name = "remotelink")
public class RemoteIssueLinkBean
{
    @XmlElement
    private Long id;

    @XmlElement
    private URI self;

    @XmlElement
    private String globalId;

    @XmlElement
    private Application application;

    @XmlElement
    private String relationship;

    @XmlElement
    private RemoteObject object;

    public RemoteIssueLinkBean(final Long id, final URI self, final String globalId, final String applicationType, final String applicationName, final String relationship, final String url, final String title, final String summary, final String iconUrl, final String iconTitle, final Boolean resolved, final String statusIconUrl, final String statusIconTitle, final String statusIconLink)
    {
        this.id = id;
        this.self = self;
        this.globalId = globalId;
        this.application = new Application(applicationType, applicationName);
        this.relationship = relationship;
        this.object = new RemoteObject(url, title, summary, iconUrl, iconTitle, resolved, statusIconUrl, statusIconTitle, statusIconLink);
    }

    private static class Application
    {
        @XmlElement
        private String type;

        @XmlElement
        private String name;

        private Application(final String type, final String name)
        {
            this.type = type;
            this.name = name;
        }
    }

    private static class RemoteObject
    {
        @XmlElement
        private String url;

        @XmlElement
        private String title;

        @XmlElement
        private String summary;

        @XmlElement
        private IconBean icon;

        @XmlElement
        private Status status;

        private RemoteObject(String url, String title, String summary, String iconUrl, String iconTitle, Boolean resolved, String statusIconUrl, String statusIconTitle, String statusIconLink)
        {
            this.url = url;
            this.title = title;
            this.summary = summary;
            this.icon = new IconBean(iconUrl, iconTitle, null);
            this.status = new Status(resolved, statusIconUrl, statusIconTitle, statusIconLink);
        }

        private static class Status
        {
            @XmlElement
            private Boolean resolved;

            @XmlElement
            private IconBean icon;

            private Status(Boolean resolved, String iconUrl, String iconTitle, String iconLink)
            {
                this.resolved = resolved;
                this.icon = new IconBean(iconUrl, iconTitle, iconLink);
            }
        }
    }
}
