package com.atlassian.jira.issue.fields.rest.json.beans;

import java.util.Collection;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.util.JiraUrlCodec;

import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A JSON-convertable representation of a StatusCategory
 *
 * @since v6.1
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class StatusCategoryJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private Long id;

    @JsonProperty
    private String key;

    @JsonProperty
    private String colorName;

    @JsonProperty
    private String name;

    public StatusCategoryJsonBean()
    {
    }

    public StatusCategoryJsonBean(String self, Long id, String key, String colorName)
    {
        this(self, id, key, colorName, null);
    }

    public StatusCategoryJsonBean(String self, Long id, String key, String colorName, final String name)
    {
        this.self = self;
        this.id = id;
        this.key = key;
        this.colorName = colorName;
        this.name = name;
    }

    public String self()
    {
        return self;
    }

    public StatusCategoryJsonBean self(String self)
    {
        return new StatusCategoryJsonBean(self, id, key, colorName, name);
    }

    public Long id()
    {
        return id;
    }

    public StatusCategoryJsonBean id(Long id)
    {
        return new StatusCategoryJsonBean(self, id, key, colorName, name);
    }

    public String key()
    {
        return key;
    }

    public StatusCategoryJsonBean key(String key)
    {
        return new StatusCategoryJsonBean(self, id, key, colorName, name);
    }

    public String colorName()
    {
        return colorName;
    }

    public StatusCategoryJsonBean colorName(String colorName)
    {
        return new StatusCategoryJsonBean(self, id, key, colorName, name);
    }

    public String name()
    {
        return name;
    }

    public StatusCategoryJsonBean name(String name)
    {
        return new StatusCategoryJsonBean(self, id, key, colorName, name);
    }

    public static StatusCategoryJsonBean bean(final StatusCategory statusCategory, final JiraBaseUrls baseUrls)
    {
        if (null == statusCategory)
        {
            return null;
        }

        return bean(
                baseUrls.restApi2BaseUrl() + "statuscategory/" + JiraUrlCodec.encode(String.valueOf(statusCategory.getId())),
                statusCategory.getId(),
                statusCategory.getKey(),
                statusCategory.getColorName(),
                statusCategory.getTranslatedName()
        );
    }

    public static StatusCategoryJsonBean bean(String self, Long id, String key, String colorName)
    {
        return bean(self, id, key, colorName, null);
    }

    public static StatusCategoryJsonBean bean(String self, Long id, String key, String colorName, String name)
    {
        return new StatusCategoryJsonBean()
                .self(self)
                .id(id)
                .key(key)
                .colorName(colorName)
                .name(name);
    }

    public static Collection<StatusCategoryJsonBean> beans(final Collection<StatusCategory> statusCategories, final JiraBaseUrls baseUrls)
    {
        Collection<StatusCategoryJsonBean> result = Lists.newArrayListWithCapacity(statusCategories.size());

        for (StatusCategory statusCategory : statusCategories)
        {
            result.add(StatusCategoryJsonBean.bean(statusCategory, baseUrls));
        }

        return result;
    }

}
