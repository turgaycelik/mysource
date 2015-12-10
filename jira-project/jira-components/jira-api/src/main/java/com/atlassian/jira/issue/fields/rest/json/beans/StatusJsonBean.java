package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * A JSON-convertable representation of a Status
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class StatusJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String statusColor;

    @JsonProperty
    private String description;

    @JsonProperty
    private String iconUrl;

    @JsonProperty
    private String name;

    @JsonProperty
    private String id;

    @JsonProperty
    private StatusCategoryJsonBean statusCategory;

    public StatusJsonBean()
    {
    }

    public StatusJsonBean(String self, String statusColor, String description, String iconUrl, String name, String id)
    {
        this.self = self;
        this.statusColor = statusColor;
        this.description = description;
        this.iconUrl = iconUrl;
        this.name = name;
        this.id = id;
    }

    public String self()
    {
        return self;
    }

    public StatusJsonBean self(String self)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String statusColor()
    {
        return statusColor;
    }

    public StatusJsonBean statusColor(String statusColor)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String description()
    {
        return description;
    }

    public StatusJsonBean description(String description)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String iconUrl()
    {
        return iconUrl;
    }

    public StatusJsonBean iconUrl(String iconUrl)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String name()
    {
        return name;
    }

    public StatusJsonBean name(String name)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String id()
    {
        return id;
    }

    public StatusJsonBean id(String id)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public StatusCategoryJsonBean statusCategory()
    {
        return statusCategory;
    }

    /**
     * @return null if the input is null
     */
    public static StatusJsonBean bean(final Status status, final JiraBaseUrls urls)
    {
        if (status == null)
        {
            return null;
        }

        StatusCategoryJsonBean statusCategoryJsonBean = StatusCategoryJsonBean.bean(status.getStatusCategory(), urls);
        String absoluteIconUrl;

        try
        {
            absoluteIconUrl = new URL(status.getIconUrl()).toString();
        }
        catch (MalformedURLException e)
        {
            absoluteIconUrl = urls.baseUrl() + status.getIconUrl();
        }

        return new Builder()
                .self(urls.restApi2BaseUrl() + "status/" + JiraUrlCodec.encode(status.getId()))
                .name(status.getNameTranslation())
                .id(status.getId())
                .iconUrl(absoluteIconUrl)
                .description(status.getDescTranslation())
                .statusCategory(statusCategoryJsonBean)
                .build();
    }

    public static Collection<StatusJsonBean> beans(final Collection<Status> allowedValues, final JiraBaseUrls baseUrls)
    {
        Collection<StatusJsonBean> result = Lists.newArrayListWithCapacity(allowedValues.size());
        for (Status from : allowedValues)
        {
            result.add(StatusJsonBean.bean(from, baseUrls));
        }

        return result;

    }

    public static StatusJsonBean bean(String id, String name, String self, String iconUrl, String description)
    {
        return new Builder()
                .self(self)
                .description(description)
                .iconUrl(iconUrl)
                .name(name)
                .id(id)
                .build();
    }

    public static StatusJsonBean bean(String id, String name, String self, String iconUrl, String description, StatusCategoryJsonBean statusCategoryJsonBean)
    {
        return new Builder()
                .self(self)
                .description(description)
                .iconUrl(iconUrl)
                .name(name)
                .id(id)
                .statusCategory(statusCategoryJsonBean)
                .build();
    }

    public static class Builder
    {
        private String self;
        private String description;
        private String iconUrl;
        private String name;
        private String id;
        private StatusCategoryJsonBean statusCategory;

        public Builder()
        {

        }

        public Builder self(String self)
        {
            this.self = self;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder iconUrl(String iconUrl)
        {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder statusCategory(StatusCategoryJsonBean statusCategory)
        {
            this.statusCategory = statusCategory;
            return this;
        }

        public StatusJsonBean build()
        {
            StatusJsonBean statusJsonBean = new StatusJsonBean()
                    .self(self)
                    .description(description)
                    .iconUrl(iconUrl)
                    .name(name)
                    .id(id);
            statusJsonBean.statusCategory = statusCategory;

            return statusJsonBean;
        }
    }
}
