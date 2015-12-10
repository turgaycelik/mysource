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
public class VersionUnresolvedIssueCountsBean
{
    /**
     * An unresolved issue count bean instance used for auto-generated documentation.
     */
    static final VersionUnresolvedIssueCountsBean DOC_EXAMPLE;
    static
    {

        VersionUnresolvedIssueCountsBean bean = new VersionUnresolvedIssueCountsBean();
        bean.self = Examples.restURI("version/10000");
        bean.issuesUnresolvedCount = 23;

        DOC_EXAMPLE = bean;
    }

    @XmlElement
    private URI self;

    @XmlElement
    private long issuesUnresolvedCount;

    public long getIssuesUnresolvedCount()
    {
        return issuesUnresolvedCount;
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
    public VersionUnresolvedIssueCountsBean() {}

    VersionUnresolvedIssueCountsBean(final long issuesUnresolvedCount, final URI self)
    {
        this.self = self;
        this.issuesUnresolvedCount = issuesUnresolvedCount;
    }


    public static class Builder
    {
        private URI self;
        private long issuesUnresolvedCount;

        public URI getSelf()
        {
            return self;
        }

        public Builder setSelf(URI self)
        {
            this.self = self;
            return this;
        }

        public Builder issuesUnresolvedCount(long issuesUnresolvedCount)
        {
            this.issuesUnresolvedCount = issuesUnresolvedCount;
            return this;
        }

        public VersionUnresolvedIssueCountsBean build()
        {
            return new VersionUnresolvedIssueCountsBean(issuesUnresolvedCount, self);
        }
    }

}
