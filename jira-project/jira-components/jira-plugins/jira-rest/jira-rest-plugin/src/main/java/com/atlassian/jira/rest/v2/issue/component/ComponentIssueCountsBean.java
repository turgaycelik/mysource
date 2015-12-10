package com.atlassian.jira.rest.v2.issue.component;

import com.atlassian.jira.rest.v2.issue.Examples;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
* @since v4.4
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="component")
public class ComponentIssueCountsBean
{
    /**
     * A component bean instance used for auto-generated documentation.
     */
    static final ComponentIssueCountsBean DOC_EXAMPLE;
    static
    {

        ComponentIssueCountsBean bean = new ComponentIssueCountsBean();
        bean.self = Examples.restURI("component/10000");
        bean.issueCount = 23;

        DOC_EXAMPLE = bean;
    }

    @XmlElement
    private URI self;

    @XmlElement
    private long issueCount;

    public long getIssueCount()
    {
        return issueCount;
    }

    public URI getSelf()
    {
        return self;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //Needed so that JAXB works.
    public ComponentIssueCountsBean() {}

    ComponentIssueCountsBean(final long issueCount, final URI self)
    {
        this.self = self;
        this.issueCount = issueCount;
    }


    public static class Builder
    {
        private URI self;
        private long issuesAffectedCount;
        private long issueCount;

        public URI getSelf()
        {
            return self;
        }

        public Builder setSelf(URI self)
        {
            this.self = self;
            return this;
        }

        public Builder issueCount(long issueCount)
        {
            this.issueCount = issueCount;
            return this;
        }
        public Builder issuesAffectedCount(long issuesAffectedCount)
        {
            this.issuesAffectedCount = issuesAffectedCount;
            return this;
        }

        public ComponentIssueCountsBean build()
        {
            return new ComponentIssueCountsBean(issueCount, self);
        }
    }
}
