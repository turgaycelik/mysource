package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.Date;

/**
* @since v5.0
*/
public class VersionJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String description;

    @JsonProperty
    private String name;

    @JsonProperty
    private Boolean archived;

    @JsonProperty
    private Boolean released;

    @JsonProperty
    @XmlJavaTypeAdapter (Dates.DateAdapter.class)
    private Date releaseDate;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public Boolean isReleased()
    {
        return released;
    }

    public Boolean isArchived()
    {
        return archived;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public void setId(Long id)
    {
        this.id = id == null ? null : id.toString();
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setArchived(Boolean archived)
    {
        this.archived = archived;
    }

    public void setReleased(Boolean released)
    {
        this.released = released;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public static Collection<VersionJsonBean> shortBeans(final Collection<Version> versions, final JiraBaseUrls urls) {
        Collection<VersionJsonBean> result = Lists.newArrayListWithCapacity(versions.size());
        for (Version from : versions)
        {
            result.add(shortBean(from, urls));
        }

        return result;
    }

    /**
     *
     * @return null if the input is null
     */
    public static VersionJsonBean shortBean(final Version version, final JiraBaseUrls urls)
    {
        if (version == null)
        {
            return null;
        }
        final VersionJsonBean bean = new VersionJsonBean();
        bean.self = urls.restApi2BaseUrl() + "version/" + JiraUrlCodec.encode(version.getId().toString());
        bean.id = version.getId().toString();
        bean.description = version.getDescription();
        bean.name = version.getName();
        bean.released = version.isReleased();
        bean.releaseDate = version.getReleaseDate();
        bean.archived = version.isArchived();

        return bean;
    }
}
