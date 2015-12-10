package com.atlassian.jira.rest.api.issue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Request to create/update a remote issue link.
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class RemoteIssueLinkCreateOrUpdateRequest
{
    @JsonProperty
    private String globalId;

    @JsonProperty
    private Application application = new Application();

    @JsonProperty
    private String relationship;

    @JsonProperty
    private RemoteObject object = new RemoteObject();

    public String globalId()
    {
        return globalId;
    }

    public RemoteIssueLinkCreateOrUpdateRequest globalId(String globalId)
    {
        this.globalId = globalId;
        return this;
    }

    public String applicationType()
    {
        return application.type;
    }

    public RemoteIssueLinkCreateOrUpdateRequest applicationType(String applicationType)
    {
        this.application.type = applicationType;
        return this;
    }

    public String applicationName()
    {
        return application.name;
    }

    public RemoteIssueLinkCreateOrUpdateRequest applicationName(String applicationName)
    {
        this.application.name = applicationName;
        return this;
    }

    public String relationship()
    {
        return relationship;
    }

    public RemoteIssueLinkCreateOrUpdateRequest relationship(String relationship)
    {
        this.relationship = relationship;
        return this;
    }

    public String url()
    {
        return object.url;
    }

    public RemoteIssueLinkCreateOrUpdateRequest url(String url)
    {
        this.object.url = url;
        return this;
    }

    public String title()
    {
        return object.title;
    }

    public RemoteIssueLinkCreateOrUpdateRequest title(String title)
    {
        this.object.title = title;
        return this;
    }

    public String summary()
    {
        return object.summary;
    }

    public RemoteIssueLinkCreateOrUpdateRequest summary(String summary)
    {
        this.object.summary = summary;
        return this;
    }

    public String iconUrl()
    {
        return object.icon.url16x16;
    }

    public RemoteIssueLinkCreateOrUpdateRequest iconUrl(String iconUrl)
    {
        this.object.icon.url16x16 = iconUrl;
        return this;
    }

    public String iconTitle()
    {
        return object.icon.title;
    }

    public RemoteIssueLinkCreateOrUpdateRequest iconTitle(String iconTitle)
    {
        this.object.icon.title = iconTitle;
        return this;
    }

    public Boolean resolved()
    {
        return object.status.resolved;
    }

    public RemoteIssueLinkCreateOrUpdateRequest resolved(Boolean resolved)
    {
        this.object.status.resolved = resolved;
        return this;
    }

    public String statusIconUrl()
    {
        return object.status.icon.url16x16;
    }

    public RemoteIssueLinkCreateOrUpdateRequest statusIconUrl(String statusIconUrl)
    {
        this.object.status.icon.url16x16 = statusIconUrl;
        return this;
    }

    public String statusIconTitle()
    {
        return object.status.icon.title;
    }

    public RemoteIssueLinkCreateOrUpdateRequest statusIconTitle(String statusIconTitle)
    {
        this.object.status.icon.title = statusIconTitle;
        return this;
    }

    public String statusIconLink()
    {
        return object.status.icon.link;
    }

    public RemoteIssueLinkCreateOrUpdateRequest statusIconLink(String statusIconLink)
    {
        this.object.status.icon.link = statusIconLink;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    private static class Application
    {
        @JsonProperty
        private String type;

        @JsonProperty
        private String name;
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    private static class RemoteObject
    {
        @JsonProperty
        private String url;

        @JsonProperty
        private String title;

        @JsonProperty
        private String summary;

        @JsonProperty
        private Icon icon = new Icon();

        @JsonProperty
        private Status status = new Status();

        @JsonIgnoreProperties (ignoreUnknown = true)
        private static class Icon
        {
            @JsonProperty
            private String url16x16;

            @JsonProperty
            private String title;

            @JsonProperty
            private String link;
        }

        @JsonIgnoreProperties (ignoreUnknown = true)
        private static class Status
        {
            @JsonProperty
            private Boolean resolved;

            @JsonProperty
            private Icon icon = new Icon();
        }
    }
}
