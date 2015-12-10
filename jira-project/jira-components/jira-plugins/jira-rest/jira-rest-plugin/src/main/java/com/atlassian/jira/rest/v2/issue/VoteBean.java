package com.atlassian.jira.rest.v2.issue;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @since v4.2
 */
@XmlRootElement
@XmlSeeAlso ({ ArrayList.class })
public class VoteBean
{
    @XmlElement
    private URI self;

    @XmlElement
    private long votes;

    @XmlElement (name = "hasVoted")
    private boolean hasVoted;

    // This will either be a Collection<UserBean> or an ErrorCollection explaining that you don't have permission
    // to view the voters for this issue.
    @XmlElement
    private Object voters;

    private VoteBean()
    {
        // used by tooling
    }

    public VoteBean(final URI self, final boolean hasVoted, final long votes)
    {
        this.hasVoted = hasVoted;
        this.self = self;
        this.votes = votes;
    }

    public VoteBean(final URI self, final boolean hasVoted, final long votes, Object voters)
    {
        this(self, hasVoted, votes);
        this.voters = voters;
    }


    public static VoteBean DOC_EXAMPLE;

    static
    {
        Collection<UserBean> users = Lists.newArrayList(UserBean.SHORT_DOC_EXAMPLE);
        try
        {
            DOC_EXAMPLE = new VoteBean(new URI("http://www.example.com/jira/rest/api/issue/MKY-1/votes"), true, 24, users);
        }
        catch (URISyntaxException ignored)
        {
            // not possible;
        }
    }
}
