package com.atlassian.jira.issue.link;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

/**
 * @since v3.13
 */
public class MockIssueLinkType extends IssueLinkTypeImpl
{
    private Long id;
    private String name;
    private String outward;
    private String inward;
    private String style;

    public MockIssueLinkType()
    {
        super(new MockGenericValue("IssueLinkType"));
    }

    public MockIssueLinkType(long id, String name, String outward, String inward, String style)
    {
        super(new MockGenericValue("IssueLinkType"));
        this.id = new Long(id);
        this.name = name;
        this.outward = outward;
        this.inward = inward;
        this.style = style;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getOutward()
    {
        return outward;
    }

    public String getInward()
    {
        return inward;
    }

    public String getStyle()
    {
        return style;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setOutward(final String outward)
    {
        this.outward = outward;
    }

    public void setInward(final String inward)
    {
        this.inward = inward;
    }

    public void setStyle(final String style)
    {
        this.style = style;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final MockIssueLinkType that = (MockIssueLinkType) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
