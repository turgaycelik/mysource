package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * A JSON-convertable representation of a Priority
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class PriorityJsonBean
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

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(String statusColor)
    {
        this.statusColor = statusColor;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     *
     * @return null if the input is null
     */
    public static PriorityJsonBean shortBean(final Priority priority, final JiraBaseUrls urls)
    {
        if (priority == null)
        {
            return null;
        }

        String iconAbsoluteUrl;
        try
        {
            iconAbsoluteUrl = new URL(priority.getIconUrl()).toString();
        }
        catch (MalformedURLException e)
        {
            iconAbsoluteUrl = urls.baseUrl() + priority.getIconUrl();
        }

        final PriorityJsonBean bean = new PriorityJsonBean();
        bean.self = urls.restApi2BaseUrl() + "priority/" + JiraUrlCodec.encode(priority.getId());
        bean.name = priority.getNameTranslation();
        bean.id = priority.getId();
        bean.iconUrl = iconAbsoluteUrl;

        return bean;
    }

    /**
     *
     * @return null if the input is null
     */
    public static PriorityJsonBean fullBean(final Priority priority, final JiraBaseUrls urls)
    {
        if (priority == null)
        {
            return null;
        }

        final PriorityJsonBean bean = shortBean(priority, urls);
        bean.statusColor = priority.getStatusColor();
        bean.description = priority.getDescTranslation();

        return bean;
    }

    public static Collection<PriorityJsonBean> shortBeans(final Collection<Priority> allowedValues, final JiraBaseUrls baseUrls)
    {
        Collection<PriorityJsonBean> result = Lists.newArrayListWithCapacity(allowedValues.size());
        for (Priority from : allowedValues)
        {
            result.add(PriorityJsonBean.shortBean(from, baseUrls));
        }

        return result;

    }
}
