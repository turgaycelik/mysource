package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.rest.api.expand.PagedListWrapper;
import com.atlassian.plugins.rest.common.expand.Expandable;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAttribute;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * A representation of Group for the GroupResource (contains additional expand users parameter)
 *
 * @since v4.2
 */
public class GroupBean extends GroupJsonBean
{
    @Expandable
    @JsonProperty
    private PagedListWrapper<UserJsonBean, User> users;

    @XmlAttribute(name = "expand") // EntityCrawler looks for XmlAttribute
    @JsonProperty
    private String expand;

    /**
     * Creates a new GroupBean for the group with the given name, self URI and users list.
     *
     * @param name a String containing the group name
     * @param self an REST Resource URI for that group
     * @param users a list of active users that're in this group. This is optional parameter and there can be null
     * passed (to avoid recursion when getting an user with associated groups).
     */
    public GroupBean(final String name, final URI self, final PagedListWrapper<UserJsonBean, User> users)
    {
        super(name, self);
        this.users = users;
    }

    public PagedListWrapper<UserJsonBean, User> getUsers()
    {
        return users;
    }

    // Docummentation
    public static final GroupBean DOC_EXAMPLE_WITH_USERS = BuildDocExample("jira-administrators", Collections.singletonList(UserJsonBean.USER_SHORT_DOC_EXAMPLE));

    /**
     * This constructor is only for documentation purpose.
     */
    private GroupBean(final String name, final URI self, final PagedListWrapper<UserJsonBean, User> users, final String expand)
    {
        super(name, self);
        this.users = users;
        this.expand = expand;
    }

    static GroupBean BuildDocExample(String name, List<UserJsonBean> users)
    {
        final PagedListWrapper.PagedListWrapperDocExample<UserJsonBean, User> pagedUserBeans = new PagedListWrapper.PagedListWrapperDocExample<UserJsonBean, User>(
                GroupResource.MAX_EXPANDED_USERS_COUNT, 0, users.size() - 1, users
        );
        return new GroupBean(name, Examples.restURI("group?groupname=" + name), pagedUserBeans, "users");
    }
}
