package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a saved filter.
 *
 * @since v5.0
 */
@XmlRootElement (name = "filter")
public class FilterBean
{
    public static final int MAX_USER_LIMIT = 1000;

    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private UserBean owner;

    @XmlElement
    private String jql;

    @XmlElement
    private URI viewUrl;

    @XmlElement
    private URI searchUrl;

    @XmlElement
    private boolean favourite;

    @XmlElement
    private Collection<FilterPermissionBean> sharePermissions;

    @Expandable("sharedUsers")
    @JsonProperty("sharedUsers")
    private UserBeanListWrapper sharedUserWrapper;

    @Expandable("subscriptions")
    @XmlElement(name = "subscriptions")
    private FilterSubscriptionBeanListWrapper subscriptionsWrapper;

    public FilterBean() { }

    public FilterBean(URI self, String id, String name, String description, UserBean owner, String jql, URI viewUrl, URI searchUrl, boolean isFavourite, Collection<FilterPermissionBean> sharePermissions, FilterSubscriptionBeanListWrapper subscriptionsWrapper)
    {
        this(self, id, name, description, owner, jql, viewUrl, searchUrl, isFavourite, sharePermissions, subscriptionsWrapper, null);
    }

    public FilterBean(URI self, String id, String name, String description, UserBean owner, String jql, URI viewUrl, URI searchUrl, boolean isFavourite, Collection<FilterPermissionBean> sharePermissions, FilterSubscriptionBeanListWrapper subscriptionsWrapper, UserBeanListWrapper sharedUserWrapper)
    {
        this.self = self;
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.jql = jql;
        this.viewUrl = viewUrl;
        this.searchUrl = searchUrl;
        this.favourite = isFavourite;
        this.sharePermissions = sharePermissions;
        this.subscriptionsWrapper = subscriptionsWrapper;
        this.sharedUserWrapper = sharedUserWrapper;
    }


    public URI getSelf()
    {
        return self;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public UserBean getOwner()
    {
        return owner;
    }

    public String getJql()
    {
        return jql;
    }

    public URI getViewUrl()
    {
        return viewUrl;
    }

    public URI getSearchUrl()
    {
        return searchUrl;
    }

    public boolean isFavourite()
    {
        return favourite;
    }

    public Collection<FilterPermissionBean> getSharePermissions()
    {
        return sharePermissions;
    }

    // Documentation
    public static final FilterBean DOC_EXAMPLE_1 = new FilterBean(
            Examples.restURI("filter/10000"),
            "10000",
            "All Open Bugs",
            "Lists all open bugs",
            UserBean.SHORT_DOC_EXAMPLE,
            "type = Bug and resolution is empty",
            Examples.jiraURI("secure/IssueNavigator.jspa?mode=hide&requestId=10000"),
            Examples.restURI("search?jql=type%20%3D%20Bug%20and%20resolutino%20is%20empty"),
            true,
            Collections.<FilterPermissionBean>emptyList(),
            FilterSubscriptionBeanListWrapper.empty());

    public static final FilterBean DOC_EXAMPLE_2 = new FilterBean(
            Examples.restURI("filter/10010"),
            "10010",
            "My issues",
            "Issues assigned to me",
            UserBean.SHORT_DOC_EXAMPLE,
            "assignee = currentUser() and resolution is empty",
            Examples.jiraURI("secure/IssueNavigator.jspa?mode=hide&requestId=10010"),
            Examples.restURI("search?jql=assignee+%3D+currentUser%28%29+and+resolution+is+empty"),
            true,
            CollectionBuilder.list(FilterPermissionBean.DOC_EXAMPLE_1, FilterPermissionBean.DOC_EXAMPLE_2),
            FilterSubscriptionBeanListWrapper.empty());

    public static final FilterBean DOC_EXAMPLE_REQUEST = new FilterBean(
            null, null,
            DOC_EXAMPLE_1.getName(),
            DOC_EXAMPLE_1.getDescription(),
            null,
            DOC_EXAMPLE_1.getJql(),
            null, null,
            DOC_EXAMPLE_1.isFavourite(),
            null, null);

    public static final List<FilterBean> DOC_FILTER_LIST_EXAMPLE = Lists.newArrayList(DOC_EXAMPLE_1, DOC_EXAMPLE_2);
}
