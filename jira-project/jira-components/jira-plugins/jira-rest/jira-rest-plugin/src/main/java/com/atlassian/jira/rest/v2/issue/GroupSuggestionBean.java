package com.atlassian.jira.rest.v2.issue;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

/**
 * A suggestion for a group picker. Returned by {@link GroupPickerResource}
 *
 * @since v4.4
 */
@XmlRootElement (name = "group")
public class GroupSuggestionBean
{
    @XmlElement
    private String name;

    @XmlElement
    private String html;

    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final GroupSuggestionBean DOC_EXAMPLE;
    static final GroupSuggestionBean DOC_EXAMPLE_2;
    static final List<GroupSuggestionBean> DOC_EXAMPLE_LIST;
    static
    {
        GroupSuggestionBean bean = new GroupSuggestionBean();
        bean.name = "jdog-developers";
        bean.html = "<b>j</b>dog-developers";
        DOC_EXAMPLE = bean;
        bean = new GroupSuggestionBean();
        bean.name = "juvenal-bot";
        bean.html = "<b>j</b>uvenal-bot";
        DOC_EXAMPLE_2 = bean;
        DOC_EXAMPLE_LIST = ImmutableList.of(DOC_EXAMPLE, DOC_EXAMPLE_2);
    }

    public GroupSuggestionBean()
    {
    }

    public GroupSuggestionBean(final String name, final String html)
    {
        this.name = name;
        this.html = html;
    }

    public String getName()
    {
        return name;
    }

    public String getHtml()
    {
        return html;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
