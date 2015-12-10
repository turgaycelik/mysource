package com.atlassian.jira.rest.v2.issue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

/**
 * A suggestion object for group pickers containing not only matched groups,
 * but also the header text e.g. Found X of Y groups..
 *
 * @since v4.4
 */
@XmlRootElement (name = "groupsuggestions")
public class GroupSuggestionsBean
{
    @XmlElement
    private String header;

    @XmlElement
    private Integer total;

    @XmlElement
    private List<GroupSuggestionBean> groups;

    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final GroupSuggestionsBean DOC_EXAMPLE;
    static
    {
        GroupSuggestionsBean bean = new GroupSuggestionsBean();
        bean.header = "Showing 20 of 25 matching groups";
        bean.total = 25;
        bean.groups = GroupSuggestionBean.DOC_EXAMPLE_LIST;
        DOC_EXAMPLE = bean;
    }

    public GroupSuggestionsBean()
    {
    }

    public GroupSuggestionsBean(final Integer total, final String header, final List<GroupSuggestionBean> groups)
    {
        this.total = total;
        this.header = header;
        this.groups = groups;
    }

    public int getTotal()
    {
        return total;
    }

    public String getHeader()
    {
        return header;
    }

    public Collection<GroupSuggestionBean> getGroups()
    {
        return groups;
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
