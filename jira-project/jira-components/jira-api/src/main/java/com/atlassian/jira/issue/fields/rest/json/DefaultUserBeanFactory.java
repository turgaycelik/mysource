package com.atlassian.jira.issue.fields.rest.json;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.1
 */
public class DefaultUserBeanFactory implements UserBeanFactory
{
    private final JiraBaseUrls jiraBaseUrls;
    private final EmailFormatter emailFormatter;

    /**
     * @deprecated Use {@link #DefaultUserBeanFactory(com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls)}
     */
    @Deprecated
    public DefaultUserBeanFactory(JiraBaseUrls jiraBaseUrls)
    {
        this(jiraBaseUrls, ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public DefaultUserBeanFactory(JiraBaseUrls jiraBaseUrls, final EmailFormatter emailFormatter)
    {
        this.jiraBaseUrls = jiraBaseUrls;
        this.emailFormatter = emailFormatter;
    }

    /**
     * @deprecated Use {@link #createBean(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.user.ApplicationUser)}
     */
    @Override
    @Deprecated
    public UserJsonBean createBean(User createdUser)
    {
        return createBean(createdUser, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser());
    }

    @Override
    public UserJsonBean createBean(User createdUser, final ApplicationUser loggedInUser)
    {
        notNull("createdUser", createdUser);
        return UserJsonBean.shortBean(createdUser, jiraBaseUrls, loggedInUser, emailFormatter);
    }
}
