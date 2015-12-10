package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.List;

/**
 * A JSON-convertable representation of a CustomFieldOption
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class CustomFieldOptionJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String value;

    @JsonProperty
    private String id;

    @JsonProperty
    private CustomFieldOptionJsonBean child;

    @JsonProperty
    private Collection<CustomFieldOptionJsonBean> children;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(Long id)
    {
        setId(String.valueOf(id));
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public CustomFieldOptionJsonBean getChild()
    {
        return child;
    }

    public void setChild(CustomFieldOptionJsonBean child)
    {
        this.child = child;
    }

    public Collection<CustomFieldOptionJsonBean> getChildren()
    {
        return children;
    }

    public void setChildren(Collection<CustomFieldOptionJsonBean> children)
    {
        this.children = children;
    }

    /**
     *
     * @return null if the input is null
     */
    public static CustomFieldOptionJsonBean shortBean(final Option option, final JiraBaseUrls urls)
    {
        if (option == null)
        {
            return null;
        }

        final CustomFieldOptionJsonBean bean = new CustomFieldOptionJsonBean();
        bean.self = urls.restApi2BaseUrl() + "customFieldOption/" + JiraUrlCodec.encode(option.getOptionId().toString());
        bean.value = option.getValue();
        bean.id = String.valueOf(option.getOptionId());
        List<Option> children = option.getChildOptions();
        if (children != null && !children.isEmpty())
        {
            bean.children = shortBeans(children, urls);
        }
        return bean;
    }

    /**
     * Method for building a cascading option with a specific child.
     * @return null if the input is null
     */
    public static CustomFieldOptionJsonBean shortBean(final Option parent, final Option child, final JiraBaseUrls urls)
    {
        if (parent == null)
        {
            return null;
        }

        final CustomFieldOptionJsonBean bean = new CustomFieldOptionJsonBean();
        bean.self = urls.restApi2BaseUrl() + "customFieldOption/" + JiraUrlCodec.encode(parent.getOptionId().toString());
        bean.value = parent.getValue();
        bean.id = String.valueOf(parent.getOptionId());
        bean.child = shortBean(child, urls);
        return bean;
    }

    public static Collection<CustomFieldOptionJsonBean> shortBeans(final Collection<Option> allowedValues, final JiraBaseUrls baseUrls)
    {
        Collection<CustomFieldOptionJsonBean> result = Lists.newArrayListWithCapacity(allowedValues.size());
        for (Option from : allowedValues)
        {
            result.add(CustomFieldOptionJsonBean.shortBean(from, baseUrls));
        }

        return result;

    }
}
