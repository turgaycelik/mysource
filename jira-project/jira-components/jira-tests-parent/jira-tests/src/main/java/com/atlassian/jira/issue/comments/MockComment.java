package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.JiraDateUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

public class MockComment implements MutableComment
{
    private final Long id;
    private ApplicationUser author;
    private ApplicationUser updateAuthor;
    private String body;
    private String groupLevel;
    private Long roleLevelId;
    private Date created;
    private Date updated;
    private final Issue issue;
    public static final String COMMENT_ROLE_NAME = "My Role";
    public static final String COMMENT_ROLE_DESCRIPTION = "My Test Role";

    public MockComment(final String author, final String body)
    {
        this(author, body, null, null, null);
    }

    public MockComment(final String author, final String body, final String groupLevel, final Long roleLevelId)
    {
        this(author, body, groupLevel, roleLevelId, null);
    }

    public MockComment(final String author, final String body, final String groupLevel, final Long roleLevelId, final Date created)
    {
        this(null, author, body, groupLevel, roleLevelId, created, null);
    }

    public MockComment(final Long id, final String author, final String body, final String groupLevel, final Long roleLevelId, final Date created, final Issue issue)
    {
        this(id, author, null, body, groupLevel, roleLevelId, created, null, issue);
    }

    public MockComment(final Long id, final String author, final String updateAuthor, final String body, final String groupLevel, final Long roleLevelId, final Date created, final Date updated, final Issue issue)
    {
        this.id = id;
        this.author = author == null? null : new MockApplicationUser(author);
        this.updateAuthor = updateAuthor == null ? null : new MockApplicationUser(updateAuthor);
        this.body = body == null ? "" : body;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        final Date createdDate = JiraDateUtils.copyOrCreateDateNullsafe(created);
        this.created = createdDate;
        this.updated = (updated == null) ? createdDate : updated;
        this.issue = issue;
    }

    @Override
    public String getUpdateAuthor()
    {
        return updateAuthor.getKey(); //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public User getUpdateAuthorUser()
    {
        return null;
    }

    @Override
    public ApplicationUser getUpdateAuthorApplicationUser()
    {
        return updateAuthor;
    }

    @Override
    public String getUpdateAuthorFullName()
    {
        return null;
    }

    @Override
    public Date getUpdated()
    {
        return updated; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final MockComment comment = (MockComment) o;

        if (author != null ? !author.equals(comment.author) : comment.author != null)
        {
            return false;
        }
        if (!body.equals(comment.body))
        {
            return false;
        }
        if (created != null ? !created.equals(comment.created) : comment.created != null)
        {
            return false;
        }
        if (groupLevel != null ? !groupLevel.equals(comment.groupLevel) : comment.groupLevel != null)
        {
            return false;
        }
        if (!id.equals(comment.id))
        {
            return false;
        }
        if (!issue.equals(comment.issue))
        {
            return false;
        }
        if (roleLevelId != null ? !roleLevelId.equals(comment.roleLevelId) : comment.roleLevelId != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("id", id).append("author", author).append("updateAuthor", updateAuthor).append("body", body).append(
            "groupLevel", groupLevel).append("roleLevelId", roleLevelId).append("created", created).append("updated", updated).append("issue", issue).toString();
    }

    @Override
    public String getAuthor()
    {
        return author.getKey();
    }

    @Override
    public String getAuthorKey()
    {
        return author.getKey();
    }

    @Override
    public User getAuthorUser()
    {
        return author.getDirectoryUser();
    }

    @Override
    public ApplicationUser getAuthorApplicationUser()
    {
        return author;
    }

    @Override
    public String getAuthorFullName()
    {
        return author.getDisplayName();
    }

    @Override
    public String getBody()
    {
        return body;
    }

    @Override
    public Date getCreated()
    {
        return created;
    }

    @Override
    public String getGroupLevel()
    {
        return groupLevel;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    @Override
    public ProjectRole getRoleLevel()
    {
        return roleLevelId == null ? null : new ProjectRoleImpl(roleLevelId, COMMENT_ROLE_NAME, COMMENT_ROLE_DESCRIPTION);
    }

    @Override
    public Issue getIssue()
    {
        return issue;
    }

    @Override
    public void setAuthor(ApplicationUser author)
    {
        this.author = author;
    }

    @Override
    public void setAuthor(final String author)
    {
        this.author = new MockApplicationUser(author);
    }

    @Override
    public void setBody(final String body)
    {
        this.body = body;
    }

    @Override
    public void setCreated(final Date created)
    {
        this.created = created;
    }

    @Override
    public void setGroupLevel(final String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    @Override
    public void setRoleLevelId(final Long roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    @Override
    public void setUpdateAuthor(ApplicationUser updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    @Override
    public void setUpdateAuthor(final String updateAuthor)
    {
        this.updateAuthor = new MockApplicationUser(updateAuthor);
    }

    @Override
    public void setUpdated(final Date updated)
    {
        this.updated = updated;
    }

}
