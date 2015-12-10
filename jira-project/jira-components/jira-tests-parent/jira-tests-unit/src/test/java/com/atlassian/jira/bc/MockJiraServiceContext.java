package com.atlassian.jira.bc;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Assert;

/**
 * Mock JiraServiceContext
 *
 * @since v3.13
 */
public class MockJiraServiceContext implements JiraServiceContext
{
    private final ErrorCollection errorCollection;
    private final User user;
    private final I18nHelper i18nBean;

    public MockJiraServiceContext()
    {
        this("TestUser");
    }

    public MockJiraServiceContext(User user)
    {
        this(user, new SimpleErrorCollection());
    }

    public MockJiraServiceContext(final User user, final ErrorCollection errors)
    {
        this.user = user;
        this.errorCollection = errors;
        this.i18nBean = new MockI18nBean();
    }

    public MockJiraServiceContext(String username)
    {
        this(username, null);
    }

    public MockJiraServiceContext(String username, String fullName)
    {
        this(new MockUser(username, fullName, null));
    }

    public MockJiraServiceContext(User user, I18nHelper helper)
    {
        this.i18nBean = helper;
        this.user = user;
        this.errorCollection = new SimpleErrorCollection();
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    public User getLoggedInUser()
    {
        return user;
    }

    @Override
    public ApplicationUser getLoggedInApplicationUser()
    {
        return user == null ? null : new DelegatingApplicationUser(user.getName(), user);
    }

    public I18nHelper getI18nBean()
    {
        return i18nBean;
    }

    /**
     * This mehtod is used to assert that there are no errors of any kind in the underlying ErrorCollection.
     */
    public void assertNoErrors()
    {
        if (getErrorCollection().hasAnyErrors())
            Assert.fail("Errors were found in the ErrorCollection.");
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
