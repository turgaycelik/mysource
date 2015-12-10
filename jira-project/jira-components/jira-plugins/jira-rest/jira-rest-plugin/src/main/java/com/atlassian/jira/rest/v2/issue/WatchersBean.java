package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * This bean describes the watcher list. Apart from an expandable list of watchers, this bean also holds a link to the
 * watchers sub-resource, and a boolean indicating whether the current user is watching the issue.
 *
 * @since v4.2
 */
@SuppressWarnings ({ "FieldCanBeLocal", "UnusedDeclaration" })
@XmlRootElement (name = "watchers")
public class WatchersBean
{
    /**
     * Example WatchersBean instance used for generated documentation.
     */
    static final WatchersBean DOC_EXAMPLE = new WatchersBean(Examples.restURI("issue/EX-1/watchers").toString(), false, Arrays.asList(UserBean.SHORT_DOC_EXAMPLE), 1);

    @XmlElement
    private String self;

    @XmlElement (name = "isWatching")
    private Boolean isWatching;

    @XmlElement (name = "watchCount")
    private int watchCount;

    /**
     * This field is set by Atlassian REST.
     */
    @XmlElement
    private List<UserBean> watchers;

    /**
     * Creates a new SimpleListWrapper backed by the given list and returns at most maxResults items to the client.
     *
     * @param selfUri the URI
     * @param watching a Boolean indicating whether the user is watching the issue
     * @param userBeans a Collection
     * @param count the number of watcher (if the user does not have View Watchers permission then userBeans will be empty but count can be non-zero, so we can't just use userBeans.size())
     */
    public WatchersBean(String selfUri, Boolean watching, List<UserBean> userBeans, int count)
    {
        this.self = selfUri;
        this.isWatching = watching;
        this.watchCount = count;
        this.watchers = userBeans;
    }

    public WatchersBean(String selfUri, Boolean watching, int count)
    {
        this.self = selfUri;
        this.isWatching = watching;
        this.watchCount = count;
    }

    public static class Builder
    {
        private String self;
        private Boolean isWatching;
        private Integer watchCount;
        private List<UserBean> watchers;

        private Builder() {}

        public static Builder create()
        {
            return new Builder();
        }

        public Builder self(final String self)
        {
            this.self = self;
            return this;
        }

        public Builder isWatching(final boolean isWatching)
        {
            this.isWatching = isWatching;
            return this;
        }

        public Builder watchCount(final int count)
        {
            this.watchCount = count;
            return this;
        }

        public Builder watchers(final List<UserBean> users)
        {
            this.watchers = users;
            return this;
        }

        public WatchersBean build()
        {
            if (self == null) { throw new IllegalArgumentException("self URI must be set"); }
            if (isWatching == null) { throw new IllegalArgumentException("isWatching must be set"); }
            if (watchCount == null) { throw new IllegalArgumentException("watchCount must be set"); }

            if (watchers != null)
            {
                return new WatchersBean(self, isWatching, watchers, watchCount);
            }
            else
            {
                return new WatchersBean(self, isWatching, watchCount);
            }
        }



    }
}
