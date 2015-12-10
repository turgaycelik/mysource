package com.atlassian.jira.dev.functest.ao;

import java.util.Date;

import javax.annotation.Nonnull;

import net.java.ao.Entity;

/**
 * @since v4.4
 */
public interface CommentAO extends Entity
{
    @Nonnull
    public String getComment();
    public void setComment(String comment);

    @Nonnull
    public String getAuthor();
    public void setAuthor(String author);

    @Nonnull
    public Date getDate();
    public void setDate(Date postedDate);

    public BlogAO getBlog();
    public void setBlog(BlogAO blog);
}
