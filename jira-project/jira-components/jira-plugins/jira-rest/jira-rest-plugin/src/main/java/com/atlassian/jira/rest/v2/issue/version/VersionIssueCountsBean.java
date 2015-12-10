package com.atlassian.jira.rest.v2.issue.version;

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
@XmlRootElement (name="version")
public class VersionIssueCountsBean
{
    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final VersionIssueCountsBean DOC_EXAMPLE;
    static
    {

        VersionIssueCountsBean bean = new VersionIssueCountsBean();
        bean.self = Examples.restURI("version/10000");
        bean.issuesFixedCount = 23;
        bean.issuesAffectedCount = 101;

        DOC_EXAMPLE = bean;
    }

    @XmlElement
    private URI self;

    @XmlElement
    private long issuesFixedCount;

    @XmlElement
    private long issuesAffectedCount;

    public long getIssuesFixedCount()
    {
        return issuesFixedCount;
    }

    public long getIssuesAffectedCount()
    {
        return issuesAffectedCount;
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
    public VersionIssueCountsBean() {}

    VersionIssueCountsBean(final long issuesFixedCount, final long issuesAffectedCount, final URI self)
    {
        this.self = self;
        this.issuesFixedCount = issuesFixedCount;
        this.issuesAffectedCount = issuesAffectedCount;
    }


    public static class Builder
    {
        private URI self;
        private long issuesAffectedCount;
        private long issuesFixedCount;

        public URI getSelf()
        {
            return self;
        }

        public Builder setSelf(URI self)
        {
            this.self = self;
            return this;
        }

        public Builder issuesFixedCount(long issuesFixedCount)
        {
            this.issuesFixedCount = issuesFixedCount;
            return this;
        }
        public Builder issuesAffectedCount(long issuesAffectedCount)
        {
            this.issuesAffectedCount = issuesAffectedCount;
            return this;
        }

        public VersionIssueCountsBean build()
        {
            return new VersionIssueCountsBean(issuesFixedCount, issuesAffectedCount, self);
        }
    }
}
