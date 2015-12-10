package com.atlassian.jira.dev.functest.ao;

import javax.annotation.Nonnull;

import net.java.ao.OneToMany;
import net.java.ao.RawEntity;
import net.java.ao.schema.AutoIncrement;
import net.java.ao.schema.PrimaryKey;

/**
 * @since v4.4
 */
public interface BlogAO extends RawEntity<Long>
{
    @AutoIncrement
    @Nonnull
    @PrimaryKey ("ID")
    public long getID();

    @Nonnull
    String getAuthor();
    void setAuthor(String author);

    @Nonnull
    String getText();
    void setText(String text);

    @OneToMany
    CommentAO[] getComments();
}
